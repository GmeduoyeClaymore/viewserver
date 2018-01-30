package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.PhoneNumberStatuses;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;


@Controller(name = "phoneCallController")
public class PhoneCallController {
    private static final Logger log = LoggerFactory.getLogger(PhoneCallController.class);

    public PhoneCallController() {
    }

    @ControllerAction(path = "getCustomerVirtualNumber", isSynchronous = true)
    public String getCustomerVirtualNumber(String orderId) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        String driverId = (String) ControllerContext.get("userId");
        String customerId = (String) ControllerUtils.getColumnValue(orderTable, "userId", orderId);

        ArrayList<String> availablePhoneNumbers = assignPhoneNumbers(orderId, customerId, driverId);
        return availablePhoneNumbers.get(0);
    }

    @ControllerAction(path = "getDriverVirtualNumber", isSynchronous = true)
    public String getDriverVirtualNumber(String orderId) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        String customerId = (String) ControllerContext.get("userId");
        String deliveryId = (String) ControllerUtils.getColumnValue(orderTable, "deliveryId", orderId);
        String driverId = (String) ControllerUtils.getColumnValue(deliveryTable, "driverId", deliveryId);

        ArrayList<String> availablePhoneNumbers = assignPhoneNumbers(orderId, customerId, driverId);
        return availablePhoneNumbers.get(1);
    }

    private ArrayList<String> assignPhoneNumbers(String orderId, String customerId, String driverId) {
        KeyedTable phoneNumberTable = ControllerUtils.getKeyedTable(TableNames.PHONE_NUMBER_TABLE_NAME);
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);

        String customerNumber = (String) ControllerUtils.getColumnValue(userTable, "contactNo", customerId);
        String driverNumber = (String) ControllerUtils.getColumnValue(userTable, "contactNo", driverId);

        ArrayList<String> availablePhoneNumbers = getAvailablePhoneNumbers(phoneNumberTable, 2);

        Date now = new Date();

        phoneNumberTable.updateRow(new TableKey(availablePhoneNumbers.get(0)), row -> {
            row.setString("userPhoneNumber", customerNumber);
            row.setString("orderId", orderId);
            row.setString("status", PhoneNumberStatuses.ASSIGNED.name());
            row.setLong("assignedTime", now.getTime());
        });

        phoneNumberTable.updateRow(new TableKey(availablePhoneNumbers.get(1)), row -> {
            row.setString("userPhoneNumber", driverNumber);
            row.setString("orderId", orderId);
            row.setString("status", PhoneNumberStatuses.ASSIGNED.name());
            row.setLong("assignedTime", now.getTime());
        });

        //TODO - possibly validate the phone number with Nexmo

        return availablePhoneNumbers;
    }

    private ArrayList<String> getAvailablePhoneNumbers(ITable phoneNumberTable, int requiredNumbers) {
        IRowSequence rows = phoneNumberTable.getOutput().getAllRows();
        ArrayList availableNumbers = new ArrayList();

        while (rows.moveNext()) {
            String orderId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "orderId", rows.getRowId());
            String status = (String) ControllerUtils.getColumnValue(phoneNumberTable, "status", rows.getRowId());
            long assignedTime = (long) ControllerUtils.getColumnValue(phoneNumberTable, "assignedTime", rows.getRowId());
            long now = new Date().getTime();
            long assignedAgoMillis = now - assignedTime;

            //if the orderId is not set or the number is assigned but hasn't been updated in 30 seconds then we can use it.
            if (orderId == null || orderId == "" || (status == PhoneNumberStatuses.ASSIGNED.name() && assignedAgoMillis > (1000 * 30))) {
                String phoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "phoneNumber", rows.getRowId());
                availableNumbers.add(phoneNumber);
            }
        }

        if (availableNumbers.size() < 2) {
            String errorMsg = String.format("Unable to find %s available free numbers could not make call", requiredNumbers);
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        return availableNumbers;
    }
}
