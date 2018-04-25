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
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

@Controller(name = "deliveryOrderController")
public class DeliveryOrderController {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryOrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private IMapsController controller;
    private DeliveryAddressController deliveryAddressController;

    public DeliveryOrderController(IDatabaseUpdater iDatabaseUpdater,
                                   IMessagingController messagingController,
                                   IMapsController controller,
                                   DeliveryAddressController deliveryAddressController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.controller = controller;
        this.deliveryAddressController = deliveryAddressController;
    }


    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentId")String paymentId, @ActionParam(name = "delivery")DeliveryOrder order){

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
        .addValue("paymentId", paymentId)
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

        order.responses.add(new DeliveryOrder.DeliveryOrderFill(partnerId,estimatedDate));
        Date now = new Date();

        IRecord orderRecord = new Record().
                addValue("orderId", orderId).
                addValue("lastModified", now).
                addValue("orderDetails", order);


        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, OrderDataSource.getDataSource().getSchema(), orderRecord);

        notifyJobResponded(orderId, orderUserId);
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
