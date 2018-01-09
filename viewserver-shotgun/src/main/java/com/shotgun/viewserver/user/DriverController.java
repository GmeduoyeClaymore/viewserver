package com.shotgun.viewserver.user;

import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.table.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller(name = "driverController")
public class DriverController {

    private static final Logger log = LoggerFactory.getLogger(DriverController.class);
    private static String ORDER_TABLE_NAME = "/datasources/order/order";
    private static String DELIVERY_TABLE_NAME = "/datasources/delivery/delivery";

    @ControllerAction(path = "registerDriver", isSynchronous = true)
    public String registerDriver(@ActionParam(name = "user")User user, @ActionParam(name = "vehicle")Vehicle vehicle){
        log.debug("Registering driver: " + user.getEmail());
        UserController userController = new UserController();
        VehicleController vehicleController = new VehicleController();

        String userId = userController.addOrUpdateUser(user);
        ControllerContext.set("userId",userId);
        vehicleController.addOrUpdateVehicle(vehicle);
        log.debug("Registered driver: " + user.getEmail() + " with id " + userId);
        return userId;
    }

    @ControllerAction(path = "acceptOrder", isSynchronous = true)
    public String acceptOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "driverId")String driverId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(DELIVERY_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String currentStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String deliveryId = this.getDeliveryId(orderTable, currentRow);

        if(currentStatus != OrderStatuses.PLACED.name()){
            //TODO - handle this on the client side
            throw new RuntimeException("Order has already been assigned");
        }

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.ACCEPTED.name());
        });

        deliveryTable.updateRow(new TableKey(deliveryId), row -> {
            row.setString("driverId", driverId);
        });

        //TODO - need to send a notification to the customer
        return orderId;
    }

    @ControllerAction(path = "startOrder", isSynchronous = true)
    public String startOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "driverId")String driverId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(ORDER_TABLE_NAME);

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.PICKEDUP.name());
        });

        //TODO - need to send a notification to the customer
        return orderId;
    }

    @ControllerAction(path = "cancelOrder", isSynchronous = true)
    public String cancelOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "driverId")String driverId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(DELIVERY_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String deliveryId = this.getDeliveryId(orderTable, currentRow);

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.PLACED.name());
        });

        deliveryTable.updateRow(new TableKey(deliveryId), row -> {
            row.setString("driverId", null);
        });

        //TODO - need to send a notification to the customer
        return orderId;
    }

    private String getDeliveryId(KeyedTable orderTable, int currentRow){
        String deliveryId = ControllerUtils.getColumnValue(orderTable, "deliveryId", currentRow).toString();
        return deliveryId;
    }
}
