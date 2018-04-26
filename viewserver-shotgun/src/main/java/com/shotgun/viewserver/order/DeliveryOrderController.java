package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryOrder;
import com.shotgun.viewserver.delivery.OrderContentType;
import com.shotgun.viewserver.maps.*;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.setup.datasource.DeliveryDataSource;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.user.User;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

@Controller(name = "deliveryOrderController")
public class DeliveryOrderController {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryOrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private PaymentController paymentController;
    private IMapsController controller;
    private DeliveryAddressController deliveryAddressController;

    public DeliveryOrderController(IDatabaseUpdater iDatabaseUpdater,
                                   IMessagingController messagingController,
                                   PaymentController paymentController,
                                   IMapsController controller,
                                   DeliveryAddressController deliveryAddressController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.paymentController = paymentController;
        this.controller = controller;
        this.deliveryAddressController = deliveryAddressController;
    }


    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentMethodId")String paymentMethodId, @ActionParam(name = "delivery")DeliveryOrder order){

        String customerId = (String) ControllerContext.get("userId");
        if(customerId == null){
            throw new RuntimeException("User id must be set in the controller context before this method is called");
        }
        if(order.assignedPartner != null) {
            order.status = DeliveryOrder.DeliveryOrderStatus.ASSIGNED;
        }else{
            order.status = DeliveryOrder.DeliveryOrderStatus.REQUESTED;
        }

        deliveryAddressController.addOrUpdateDeliveryAddress(order.destination);
        deliveryAddressController.addOrUpdateDeliveryAddress(order.origin);

        Date now = new Date();
        String orderId = ControllerUtils.generateGuid();

        order.orderId = orderId;

        IRecord orderRecord = new Record()
        .addValue("orderId", orderId)
        .addValue("created", now)
        .addValue("requiredDate", order.requiredDate)
        .addValue("status", order.status.getOrderStatus().name())
        .addValue("orderLocation", order.origin)
        .addValue("orderContentTypeId", OrderContentType.Delivery.getContentTypeId())
        .addValue("lastModified", now)
        .addValue("userId", customerId)
        .addValue("assignedPartnerUserId", order.assignedPartner != null ? order.assignedPartner.partnerId : null)
        .addValue("paymentMethodId", paymentMethodId)
        .addValue("orderDetails", order);

        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, OrderDataSource.getDataSource().getSchema(), orderRecord);


        if(order.assignedPartner != null){
            notifyJobAssigned(orderId,order.assignedPartner.partnerId);
        }

        return orderId;
    }

    @ControllerAction(path = "respondToOrder", isSynchronous = true)
    public void respondToOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "estimatedDate")Date estimatedDate){
        String partnerId = getUserId();
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));

        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        String orderDetailsString = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow).toString();
        String orderStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        if(!OrderStatus.valueOf(orderStatus).equals(OrderStatus.PLACED)){
            throw new RuntimeException("Can only respond to an order which is in placed status");
        }

        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailsString, DeliveryOrder.class);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }

        if(order.responses == null){
            order.responses = new ArrayList<>();
        }

        if(order.responses.stream().anyMatch(c->c.partnerId.equals(partnerId))){
            logger.info(partnerId + "Has already responded to this order aborting");
            return;
        }
        order.status = order.status.transitionTo(DeliveryOrder.DeliveryOrderStatus.RESPONDED);
        order.responses.add(new DeliveryOrder.DeliveryOrderFill(partnerId,estimatedDate, DeliveryOrder.DeliveryOrderFill.DeliveryOrderFillStatus.RESPONDED));

        updateOrderRecord(order);

        notifyJobResponded(orderId, orderUserId);
    }


    @ControllerAction(path = "cancelResponse", isSynchronous = true)
    public void cancelResponse(@ActionParam(name = "orderId")String orderId){
        String partnerId = getUserId();
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));

        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        String orderDetailsString = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();


        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailsString, DeliveryOrder.class);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }

        order.status = order.status.transitionTo(DeliveryOrder.DeliveryOrderStatus.RESPONDED);

        if(order.responses == null){
            order.responses = new ArrayList<>();
        }
        order.assignedPartner = null;
        for(DeliveryOrder.DeliveryOrderFill fill : order.responses){
            if(fill.partnerId.equals(partnerId)){
                fill.fillStatus = DeliveryOrder.DeliveryOrderFill.DeliveryOrderFillStatus.CANCELLED;
            }else{
                notifyJobBackOnTheMarket(orderId,fill.partnerId);
                fill.fillStatus = DeliveryOrder.DeliveryOrderFill.DeliveryOrderFillStatus.RESPONDED;
            }
        }
        if(!order.responses.stream().anyMatch(c->c.partnerId.equals(partnerId))){
            throw new RuntimeException("Cannot cancel find a response from partner " + partnerId + " to cancel ");
        }

        updateOrderRecord(order);

        notifyResponseCancelled(orderId, orderUserId);
    }



    @ControllerAction(path = "acceptResponse", isSynchronous = true)
    public void acceptResponseToOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "partnerId")String partnerId){

        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));

        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        String orderDetailsString = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow).toString();
        String orderStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        assertOrderMine(orderUserId);

        if(!OrderStatus.valueOf(orderStatus).equals(OrderStatus.PLACED)){
            throw new RuntimeException("Can only accept response for an order in status placed");
        }

        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailsString, DeliveryOrder.class);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }

        if(order.responses == null){
            throw new RuntimeException("Unable to find responses from " + partnerId + " to orderId " + partnerId);
        }

        Optional<DeliveryOrder.DeliveryOrderFill> first = order.responses.stream().filter(c -> c.partnerId.equals(partnerId)).findFirst();
        if(!first.isPresent()){
            throw new RuntimeException("Unable to find responses from " + partnerId + " to orderId " + partnerId);
        }

        order.assignedPartner = first.get();
        order.status = order.status.transitionTo(DeliveryOrder.DeliveryOrderStatus.ASSIGNED);
        for(DeliveryOrder.DeliveryOrderFill fill : order.responses){
            if(!fill.partnerId.equals(partnerId)){
                fill.fillStatus = DeliveryOrder.DeliveryOrderFill.DeliveryOrderFillStatus.DECLINED;
                notifyJobRejected(orderId,fill.partnerId);
            }else{
                fill.fillStatus = DeliveryOrder.DeliveryOrderFill.DeliveryOrderFillStatus.ACCEPTED;
                notifyJobAccepted(orderId,fill.partnerId);
            }
        }
        updateOrderRecord(order);
    }


    @ControllerAction(path = "startDelivery", isSynchronous = true)
    public void startDelivery(@ActionParam(name = "orderId")String orderId){

        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String partnerId = getUserId();
        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        String orderDetailsString = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow).toString();
        String orderStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        if(!OrderStatus.valueOf(orderStatus).equals(OrderStatus.ACCEPTED)){
            throw new RuntimeException("Can only start job that is in status accepted");
        }

        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailsString, DeliveryOrder.class);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }

        if(!partnerId.equals(order.assignedPartner.partnerId)){
            throw new RuntimeException("You can only start a delivery job once it has been assigned to you");
        }

        order.status = order.status.transitionTo(DeliveryOrder.DeliveryOrderStatus.ENROUTE);

        updateOrderRecord(order);

        notifyJobStarted(orderId,orderUserId);
    }


    @ControllerAction(path = "partnerComplete", isSynchronous = true)
    public void partnerComplete(@ActionParam(name = "orderId")String orderId){

        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String partnerId = getUserId();
        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        String orderDetailsString = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow).toString();
        String orderStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        if(!OrderStatus.valueOf(orderStatus).equals(OrderStatus.INPROGRESS)){
            throw new RuntimeException("Can only complete a job that is in progress");
        }

        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailsString, DeliveryOrder.class);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }

        if(!partnerId.equals(order.assignedPartner.partnerId)){
            throw new RuntimeException("You can only start a delivery job once it has been assigned to you");
        }

        order.status = order.status.transitionTo(DeliveryOrder.DeliveryOrderStatus.PARTNERCOMPLETE);

        updateOrderRecord(order);

        notifyJobComplete(orderId,orderUserId);
    }

    @ControllerAction(path = "customerCompleteAndPay", isSynchronous = true)
    public String customerCompleteAndPay(@ActionParam(name = "orderId")String orderId){

        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String customerId = getUserId();
        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        String orderDetailsString = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow).toString();
        String orderStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();
        String paymentMethodId = (String)ControllerUtils.getColumnValue(orderTable, "paymentMethodId", currentRow);

        assertOrderMine(orderUserId);

        if(!OrderStatus.valueOf(orderStatus).equals(OrderStatus.INPROGRESS)){
            throw new RuntimeException("Can only complete a job that is in progress");
        }

        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailsString, DeliveryOrder.class);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }

        order.status = order.status.transitionTo(DeliveryOrder.DeliveryOrderStatus.CUSTOMERCOMPLETE);
        order.payments = order.payments == null ? new ArrayList<>() : order.payments;
        String paymentId = paymentController.createCharge(order.estimatedCost, paymentMethodId, customerId, order.assignedPartner.partnerId, order.description);
        order.payments.add(new DeliveryOrder.OrderPaymentStage(DeliveryOrder.OrderPaymentStage.PaymentStageStatus.Complete,paymentId,100, new Date()));
        updateOrderRecord(order);
        notifyJobCompleted(orderId, order.assignedPartner.partnerId);
        return paymentId;
    }

    private void assertOrderMine(String orderUserId) {
        if(!getUserId().equals(orderUserId)){
            throw new RuntimeException("Can only perform this operation on an order that you own");
        }
    }


    private void updateOrderRecord(DeliveryOrder order) {
        Date now = new Date();

        IRecord orderRecord = new Record().
                addValue("orderId", order.orderId).
                addValue("status", order.status.getOrderStatus().name()).
                addValue("lastModified", now).
                addValue("orderDetails", order);


        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, OrderDataSource.getDataSource().getSchema(), orderRecord);
    }


    private void notifyJobBackOnTheMarket(String orderId, String partnerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job back on the market"),"Job has just come back onto the market" )
                    .withFromTo(user.getUserId(),partnerId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }

    private void notifyJobCompleted(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job completed and paid"), String.format("%s has  just marked your job complete and paid", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }

    private void notifyJobAccepted(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job assigned to you"), String.format("%s has  just accepted your offer", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }

    private void notifyJobRejected(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun offer rejected"), String.format("%s has  just rejected your offer", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }

    private void notifyJobComplete(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job started"), String.format("%s has  just marked your job as complete", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }



    private void notifyJobStarted(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job started"), String.format("%s has  just started your job", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }


    private void notifyJobResponded(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job assigned to you"), String.format("%s has  just responded to a job that you posted", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }

    private void notifyResponseCancelled(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun response cancelled"), String.format("%s has  just cancelled response to job", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }

    private void notifyJobAssigned(String orderId, String driverId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job assigned to you"), String.format("%s has  just assigned a job to you in shotgun", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),driverId)
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            logger.error("There was a problem sending the notification", ex);
        }
    }

    private String createActionUri(String orderId){
        return String.format("shotgun://DriverOrderDetail/%s", orderId);
    }

    @ControllerAction(path = "calculateEstimatedPrice", isSynchronous = true)
    public int calculateTotalPrice(@ActionParam(name = "delivery")DeliveryOrder order){

        List<LatLng> locations = new ArrayList<>();
        locations.add(new LatLng(order.origin.getLatitude(),order.origin.getLongitude()));
        locations.add(new LatLng(order.destination.getLatitude(),order.destination.getLongitude()));
        DistanceAndDuration duration  = controller.getDistanceAndDuration(new DirectionRequest(locations.toArray(new LatLng[locations.size()]),"driving"));
        return (duration.getDistance() / 1000) * 100;
    }

}
