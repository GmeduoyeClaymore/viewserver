package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.IDatabaseUpdater;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.Delivery;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.user.User;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
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
    private MessagingController messagingController;
    private boolean isTest;
    private KeyedTable productTable;
    private IDatabaseUpdater iDatabaseUpdater;

    public OrderController(IDatabaseUpdater iDatabaseUpdater,
                           DeliveryAddressController deliveryAddressController,
                           DeliveryController deliveryController,
                           OrderItemController orderItemController,
                           PricingStrategyResolver pricingStrategyResolver,
                           MessagingController messagingController,
                           boolean isTest) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.deliveryAddressController = deliveryAddressController;
        this.deliveryController = deliveryController;
        this.orderItemController = orderItemController;
        this.pricingStrategyResolver = pricingStrategyResolver;
        this.messagingController = messagingController;
        this.isTest = isTest;
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


        for(OrderItem item : orderItems){
            if(item.getContentTypeId() == null){
                throw new RuntimeException("All order items must have a content type");
            }
        }

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
        .addValue("status", delivery.getDriverId() == null ? OrderStatuses.PLACED.name() : OrderStatuses.ACCEPTED.name())
        .addValue("userId", customerId)
        .addValue("paymentId", paymentId)
        .addValue("deliveryId", deliveryId);

        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        //add orderItems
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderId);
            orderItemController.addOrUpdateOrderItem(orderItem);
        }

        if(delivery.getDriverId() != null){
            notifyJobAssigned(orderId,delivery.getDriverId());
        }

        return orderId;
    }

    private void notifyJobAssigned(String orderId, String driverId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job assigned to you"), String.format("%s has  just assigned a job to you in shotgun", user.getFirstName() + " " + user.getLastName()))
                    .build();
            messagingController.sendMessageToUser(driverId, builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }

    private String createActionUri(String orderId){
        return String.format("shotgun://DriverOrderDetail/%s", orderId);
    }

    @ControllerAction(path = "calculateTotalPrice", isSynchronous = true)
    public int calculateTotalPrice(@ActionParam(name = "delivery")Delivery delivery,@ActionParam(name = "orderItems")OrderItem[] orderItems){
      /*  if(this.isTest){
            return 30;
        }*/

        int result = 0;
        for(OrderItem orderItem : orderItems){
            result += calculatePrice(orderItem, delivery);
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

        iDatabaseUpdater.addOrUpdateRow(TableNames.RATING_TABLE_NAME, "rating", ratingRecord);
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

        iDatabaseUpdater.addOrUpdateRow(TableNames.RATING_TABLE_NAME, "rating", ratingRecord);
    }

    private int calculatePrice(OrderItem orderItem, Delivery delivery) {
        if(orderItem.getFixedPrice() != null){
            return orderItem.getFixedPrice();
        }

        PriceStrategy strategy = getPriceStrategy(orderItem);
        if(strategy == null){
            throw  new RuntimeException(String.format("Unable to get pricing strategy for order item %s. There is some convuluted method of doing this maybe it could be simplified",orderItem));
        }
        Product product = getProduct(orderItem.getProductId());
        switch (strategy){
            case JOURNEY_DISTANCE:
                //£1 per km + fixed price based on van size
                return (calculatePriceForDistance(delivery)) + product.getPrice();
            case FIXED:
                //quantity * product price
                return orderItem.getQuantity() * product.getPrice();
            case DURATION:
                //quantity * product price * number of days
                return orderItem.getQuantity() * product.getPrice() * calculateDays(orderItem);
            case JOB_DURATION:
                // product/tradesman day rate * number of days
                // OR
                // (product/trademans day rate / 9) * number of hours
                int days = calculateDays(orderItem);
                if(days >= 1){ //this job is at least on day
                    return product.getPrice() * days;
                }

                return (product.getPrice() / 9) * calculateHours(orderItem);
            default:
                throw new RuntimeException(String.format("Couldn't find a pricing strategy for product \"%s\"",orderItem.getProductId()));
        }
    }

    private int calculateDays(OrderItem orderItem) {
        if(orderItem.getStartTime() == null){
            throw new RuntimeException("Start time must be specified");
        }
        if(orderItem.getEndTime() == null){
            throw new RuntimeException("End time must be specified");
        }
       return Days.daysBetween(new LocalDate(orderItem.getStartTime()), new LocalDate(orderItem.getEndTime())).getDays();
    }


    private int calculateHours(OrderItem orderItem) {
        if (orderItem.getStartTime() == null) {
            throw new RuntimeException("Start time must be specified");
        }
        if (orderItem.getEndTime() == null) {
            throw new RuntimeException("End time must be specified");
        }
        return Hours.hoursBetween(new LocalDateTime(orderItem.getStartTime()), new LocalDateTime(orderItem.getEndTime())).getHours();
    }

    private int calculatePriceForDistance(Delivery delivery) {
        if(delivery.getDistance() == 0){
            throw new RuntimeException("Zero delivery distance found for delivery");
        }
        //Currently set to £1 per km
        return (delivery.getDistance() / 1000) * 100;
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
        result.setPrice((int) ControllerUtils.getColumnValue(this.productTable, "price", row));
        result.setProductId((String)ControllerUtils.getColumnValue(this.productTable,"productId",row));
        return result;
    }

    private PriceStrategy getPriceStrategy(OrderItem orderItem) {
        return pricingStrategyResolver.resolve(orderItem.getProductId());
    }
}
