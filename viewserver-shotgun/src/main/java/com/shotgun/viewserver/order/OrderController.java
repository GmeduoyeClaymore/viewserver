package com.shotgun.viewserver.order;

import com.amazonaws.util.JodaTime;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.Delivery;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

@Controller(name = "orderController")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    DeliveryAddressController deliveryAddressController;
    DeliveryController deliveryController;
    OrderItemController orderItemController;
    private PricingStrategyResolver pricingStrategyResolver;
    private KeyedTable orderTable;
    private KeyedTable productTable;

    public OrderController(DeliveryAddressController deliveryAddressController,
                           DeliveryController deliveryController,
                           OrderItemController orderItemController,
                           PricingStrategyResolver pricingStrategyResolver) {
        this.deliveryAddressController = deliveryAddressController;
        this.deliveryController = deliveryController;
        this.orderItemController = orderItemController;
        this.pricingStrategyResolver = pricingStrategyResolver;
    }

    KeyedTable getOrderTable(){
        if(this.orderTable == null){
            this.orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        }
        return this.orderTable;
    }
    KeyedTable getProductTable(){
        if(this.productTable == null){
            this.productTable = ControllerUtils.getKeyedTable(TableNames.PRODUCT_TABLE_NAME);
        }
        return this.productTable;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentId")String paymentId,
                              @ActionParam(name = "delivery")Delivery delivery,
                              @ActionParam(name = "orderItems")OrderItem[] orderItems){

        String userId = (String) ControllerContext.get("userId");
        if(userId == null){
            throw new RuntimeException("User id must be set in the controller context before this method is called");
        }

        //add addresses
        String originDeliveryAddressId = deliveryAddressController.addOrUpdateDeliveryAddress(delivery.getOrigin());
        if(delivery.getDestination() != null && delivery.getDestination().getLine1() != null){
            String destinationDeliverAddressId  = deliveryAddressController.addOrUpdateDeliveryAddress(delivery.getDestination());
            delivery.getDestination().setDeliveryAddressId(destinationDeliverAddressId);
        }

        delivery.getOrigin().setDeliveryAddressId(originDeliveryAddressId);
        //add delivery
        String deliveryId = deliveryController.addOrUpdateDelivery(userId, delivery);
        delivery.setDeliveryId(deliveryId);
        Date now = new Date();
        String orderId = ControllerUtils.generateGuid();

       // IDataSource orderDataSource = IDataSourceRegistry.get

        //add order
        ITableRowUpdater tableUpdater = row -> {
            row.setString("orderId", orderId);
            row.setDouble("totalPrice", calculateTotalPrice(delivery, orderItems));
            row.setLong("created", now.getTime());
            row.setLong("lastModified", now.getTime());
            row.setString("status", OrderStatuses.PLACED.name());
            row.setString("userId", userId);
            row.setString("paymentId", paymentId);
            row.setString("deliveryId", deliveryId);
        };

        getOrderTable().addRow(new TableKey(orderId), tableUpdater);

        //add orderItems
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderId);
            orderItemController.addOrUpdateOrderItem(orderItem);
        }

        return orderId;
    }

    @ControllerAction(path = "calculateTotalPrice", isSynchronous = true)
    public Double calculateTotalPrice(@ActionParam(name = "delivery")Delivery delivery,@ActionParam(name = "orderItems")OrderItem[] orderItems){
        Double result = new Double(0);
        for(OrderItem orderItem : orderItems){
            result += calculatePrice(orderItem,delivery);
        }
        return result;
    }

    private Double calculatePrice(OrderItem orderItem, Delivery delivery) {
        PriceStrategy strategy = getPriceStrategy(orderItem);
        Product product = getProduct(orderItem.getProductId());
        switch (strategy){//TODO this will need some refining
            case JOURNEY_TIME:
                return getQuantity(orderItem) * calculateDistance(delivery) * calculateDuration(delivery) * product.getPrice();
            case FIXED:
                return getQuantity(orderItem) * product.getPrice();
            case DURATION:
                return getQuantity(orderItem) * product.getPrice() * calculateDays(delivery);
            default:
                throw new RuntimeException(String.format("Couldn't find a pricing strategy for product \"%s\"",orderItem.getProductId()));
        }
    }

    private int calculateDays(Delivery delivery) {
        if(delivery.getFrom() == null){
            throw new RuntimeException("From date must be specified");
        }
        if(delivery.getTill() == null){
            throw new RuntimeException("Till date must be specified");
        }
       return Days.daysBetween(new LocalDate(delivery.getFrom()), new LocalDate(delivery.getTill())).getDays();
    }

    private int getQuantity(OrderItem orderItem) {
        return orderItem.getQuantity() == 0 ? 1 : orderItem.getQuantity();
    }

    private Product getProduct(String productId) {
        int row = this.getProductTable().getRow(new TableKey(productId));
        if(row == -1){
            throw new RuntimeException(String.format("Unable to find product id \"%s\" in the product table",productId));
        }

        Product result = new Product();
        result.setCategoryId((String) ControllerUtils.getColumnValue(this.productTable, "categoryId", row));
        result.setDescription((String) ControllerUtils.getColumnValue(this.productTable, "description", row));
        result.setName((String) ControllerUtils.getColumnValue(this.productTable, "name", row));
        result.setPrice((Double) ControllerUtils.getColumnValue(this.productTable, "price", row));
        result.setProductId((String)ControllerUtils.getColumnValue(this.productTable,"productId",row));
        return result;
    }

    private int calculateDistance(Delivery delivery) {
        if(delivery.getDistance() == 0){
            throw new RuntimeException("Zero delivery distance found for delivery");
        }
        return delivery.getDistance();
    }

    private int calculateDuration(Delivery delivery) {
        if(delivery.getDuration() == 0){
            throw new RuntimeException("Zero duration found for delivery");
        }
        return delivery.getDuration();
    }

    private PriceStrategy getPriceStrategy(OrderItem orderItem) {
        return pricingStrategyResolver.resolve(orderItem.getProductId());
    }

}
