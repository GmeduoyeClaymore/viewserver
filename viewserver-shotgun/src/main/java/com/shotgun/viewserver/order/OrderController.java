package com.shotgun.viewserver.order;

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
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import java.util.Date;

@Controller(name = "orderController")
public class OrderController {

    DeliveryAddressController deliveryAddressController;
    DeliveryController deliveryController;
    OrderItemController orderItemController;
    private PricingStrategyResolver pricingStrategyResolver;
    private KeyedTable orderTable;
    private KeyedTable productTable;
    private KeyedTable productCategoryTable;

    public OrderController(DeliveryAddressController deliveryAddressController, DeliveryController deliveryController, OrderItemController orderItemController, PricingStrategyResolver pricingStrategyResolver) {
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

    KeyedTable getProductCategoryTable(){
        if(this.productCategoryTable == null){
            this.productCategoryTable = ControllerUtils.getKeyedTable(TableNames.PRODUCT_TABLE_NAME);
        }
        return this.productCategoryTable;
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

        //add order
        ITableRowUpdater tableUpdater = row -> {
            row.setString("orderId", orderId);
            row.setInt("totalPrice", calculateTotalPrice(delivery,orderItems));
            row.setLong("created", now.getTime());
            row.setLong("lastModified", now.getTime());
            row.setString("status", OrderStatuses.PLACED.name());
            row.setString("userId", userId);
            row.setString("paymentId", paymentId);
            row.setString("deliveryId", deliveryId);
        };

        orderTable.addRow(new TableKey(orderId), tableUpdater);

        //add orderItems
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderId);
            orderItemController.addOrUpdateOrderItem(userId, orderItem);
        }

        return orderId;
    }

    @ControllerAction(path = "calculateTotalPrice", isSynchronous = true)
    public Integer calculateTotalPrice(@ActionParam(name = "delivery")Delivery delivery,@ActionParam(name = "orderItems")OrderItem[] orderItems){
        int result = 0;
        for(OrderItem orderItem : orderItems){
            result += calculatePrice(orderItem,delivery);
        }
        return result;
    }

    private int calculatePrice(OrderItem orderItem, Delivery delivery) {
        PriceStrategy strategy = getPriceStrategy(orderItem);
        Product product = getProduct(orderItem);
        switch (strategy){//TODO this will need some refining
            case JOURNEY_TIME:
                return orderItem.getQuantity() * delivery.getNoRequiredForOffload() * calculateDistance(delivery) * calculateDuration(delivery) * product.getPrice();
            case FIXED:
                return orderItem.getQuantity() * product.getPrice();
            case DURATION:
                return orderItem.getQuantity() * product.getPrice() * calculateDistance(delivery);
            default:
                throw new RuntimeException(String.format("Couldn't find a pricing strategy for product \"%s\"",orderItem.getProductId()));
        }
    }

    private Product getProduct(OrderItem orderItem) {
        int row = this.getProductTable().getRow(new TableKey(orderItem.getProductId()));
        if(row == -1){
            throw new RuntimeException(String.format("Unable to find product id \"%s\" in the product table",orderItem.getProductId()));
        }

        Product result = new Product();
        result.setCategoryId((String) ControllerUtils.getColumnValue(this.productTable, "categoryId", row));
        result.setDescription((String) ControllerUtils.getColumnValue(this.productTable, "description", row));
        result.setName((String) ControllerUtils.getColumnValue(this.productTable, "name", row));
        result.setPrice((Integer) ControllerUtils.getColumnValue(this.productTable, "price", row));
        result.setProductId((String)ControllerUtils.getColumnValue(this.productTable,"productId",row));
        return result;
    }

    private int calculateDistance(Delivery delivery) {
        return delivery.getDistance();
    }

    private int calculateDuration(Delivery delivery) {
        return delivery.getDuration();
    }

    private PriceStrategy getPriceStrategy(OrderItem orderItem) {
        return pricingStrategyResolver.resolve(orderItem.getProductId());
    }

}
