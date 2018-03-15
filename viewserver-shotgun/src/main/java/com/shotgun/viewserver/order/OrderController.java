package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.TableUpdater;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.Delivery;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Controller(name = "orderController")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    DeliveryAddressController deliveryAddressController;
    DeliveryController deliveryController;
    OrderItemController orderItemController;
    private PricingStrategyResolver pricingStrategyResolver;
    private KeyedTable productTable;
    private TableUpdater tableUpdater;

    public OrderController(TableUpdater tableUpdater,
                           DeliveryAddressController deliveryAddressController,
                           DeliveryController deliveryController,
                           OrderItemController orderItemController,
                           PricingStrategyResolver pricingStrategyResolver) {
        this.tableUpdater = tableUpdater;
        this.deliveryAddressController = deliveryAddressController;
        this.deliveryController = deliveryController;
        this.orderItemController = orderItemController;
        this.pricingStrategyResolver = pricingStrategyResolver;
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

        String customerId = (String) ControllerContext.get("userId");
        if(customerId == null){
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
        String deliveryId = deliveryController.addOrUpdateDelivery(customerId, delivery);
        delivery.setDeliveryId(deliveryId);
        Date now = new Date();
        String orderId = ControllerUtils.generateGuid();

        IRecord orderRecord = new Record()
        .addValue("orderId", orderId)
        .addValue("totalPrice", calculateTotalPrice(delivery, orderItems))
        .addValue("created", now)
        .addValue("lastModified", now)
        .addValue("status", OrderStatuses.PLACED.name())
        .addValue("userId", customerId)
        .addValue("paymentId", paymentId)
        .addValue("deliveryId", deliveryId);

        tableUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

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

    @ControllerAction(path = "addCustomerRating", isSynchronous = true)
    public void addCustomerRating(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "rating") int rating) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        Record ratingRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("userId", orderUserId)
                .addValue("rating", rating);

        tableUpdater.addOrUpdateRow(TableNames.RATING_TABLE_NAME, "rating", ratingRecord);
    }

    @ControllerAction(path = "addDriverRating", isSynchronous = true)
    public void addDriverRating(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "rating") int rating) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        String deliveryId = (String)ControllerUtils.getColumnValue(orderTable, "deliveryId", orderId);
        String driverId = (String)ControllerUtils.getColumnValue(deliveryTable, "driverId", deliveryId);

        Record ratingRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("userId", driverId)
                .addValue("rating", rating);

        tableUpdater.addOrUpdateRow(TableNames.RATING_TABLE_NAME, "rating", ratingRecord);
    }

    private Double calculatePrice(OrderItem orderItem, Delivery delivery) {
        PriceStrategy strategy = getPriceStrategy(orderItem);
        Product product = getProduct(orderItem.getProductId());
        switch (strategy){//TODO this will need some refining
            case JOURNEY_TIME:
                return (getQuantity(orderItem) * calculateDistance(delivery)) + product.getPrice() + calculateOffload(delivery);
            case FIXED:
                return getQuantity(orderItem) * product.getPrice();
            case DURATION:
                return getQuantity(orderItem) * product.getPrice() * calculateDays(delivery);
            default:
                throw new RuntimeException(String.format("Couldn't find a pricing strategy for product \"%s\"",orderItem.getProductId()));
        }
    }

    private int calculateOffload(Delivery delivery){
        return (delivery.getNumRequiredForOffload() - 1 * 12);
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
        //Currently set to £1 per km
        return (delivery.getDistance() / 1000) * 100;
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
