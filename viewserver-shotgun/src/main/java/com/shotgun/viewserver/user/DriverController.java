package com.shotgun.viewserver.user;

import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import com.shotgun.viewserver.payments.PaymentBankAccount;
import com.shotgun.viewserver.payments.PaymentController;
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
    private PaymentController paymentController;

    public DriverController(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    @ControllerAction(path = "registerDriver", isSynchronous = true)
    public String registerDriver(@ActionParam(name = "user")User user,
                                 @ActionParam(name = "vehicle")Vehicle vehicle,
                                 @ActionParam(name = "address")DeliveryAddress address,
                                 @ActionParam(name = "bankAccount")PaymentBankAccount bankAccount){
        log.debug("Registering driver: " + user.getEmail());
        UserController userController = new UserController();
        VehicleController vehicleController = new VehicleController();

        //We can change this later on or on a per user basis
        user.setChargePercentage(10);

        String paymentAccountId = paymentController.createPaymentAccount(user, address, bankAccount);
        user.setStripeDefaultSourceId(paymentAccountId);

        String userId = userController.addOrUpdateUser(user);
        ControllerContext.set("userId",userId);
        vehicleController.addOrUpdateVehicle(vehicle);
        log.debug("Registered driver: " + user.getEmail() + " with id " + userId);
        return userId;
    }

    @ControllerAction(path = "acceptOrder", isSynchronous = true)
    public String acceptOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "driverId")String driverId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String currentStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String deliveryId = ControllerUtils.getDeliveryId(orderTable, currentRow);

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
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.PICKEDUP.name());
        });

        //TODO - need to send a notification to the customer
        return orderId;
    }

    @ControllerAction(path = "completeOrder", isSynchronous = true)
    public String completeOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "driverId")String driverId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);

        int currentOrderRow = orderTable.getRow(new TableKey(orderId));
        int currentDriverRow = userTable.getRow(new TableKey(driverId));
        String customerId = (String)ControllerUtils.getColumnValue(orderTable, "userId", currentOrderRow);
        int currentCustomerRow = userTable.getRow(new TableKey(customerId));

        String paymentId = (String)ControllerUtils.getColumnValue(orderTable, "paymentId", currentOrderRow);
        String stripeCustomerId = (String)ControllerUtils.getColumnValue(userTable, "stripeCustomerId", currentCustomerRow);
        String accountId = (String)ControllerUtils.getColumnValue(userTable, "stripeAccountId", currentDriverRow);
        int chargePercentage = (int)ControllerUtils.getColumnValue(userTable, "chargePercentage", currentDriverRow);
        int totalPrice = (int)ControllerUtils.getColumnValue(orderTable, "totalPrice", currentOrderRow);

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.COMPLETED.name());
        });
        //TODO - need to send a notification to the customer

        paymentController.createCharge(totalPrice, chargePercentage, paymentId, stripeCustomerId, accountId);
        return orderId;
    }

    @ControllerAction(path = "cancelOrder", isSynchronous = true)
    public String cancelOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "driverId")String driverId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String deliveryId = ControllerUtils.getDeliveryId(orderTable, currentRow);

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.PLACED.name());
        });

        deliveryTable.updateRow(new TableKey(deliveryId), row -> {
            row.setString("driverId", null);
        });

        //TODO - need to send a notification to the customer
        return orderId;
    }
}
