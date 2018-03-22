package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.IDatabaseUpdater;
import com.shotgun.viewserver.constants.PhoneNumberStatuses;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;


@Controller(name = "phoneCallController")
public class PhoneCallController {
    private static final Logger log = LoggerFactory.getLogger(PhoneCallController.class);
    private IDatabaseUpdater iDatabaseUpdater;

    public PhoneCallController(IDatabaseUpdater iDatabaseUpdater) {
        this.iDatabaseUpdater = iDatabaseUpdater;
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

    @ControllerAction(path = "getVirtualNumber", isSynchronous = true)
    public String getVirtualNumber(String userId) {
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        String customerId = (String) ControllerContext.get("userId");
        ArrayList<String> availablePhoneNumbers = assignPhoneNumbers(null, customerId, userId);
        return availablePhoneNumbers.get(1);
    }

    private ArrayList<String> assignPhoneNumbers(String orderId, String customerId, String driverId) {
        KeyedTable phoneNumberTable = ControllerUtils.getKeyedTable(TableNames.PHONE_NUMBER_TABLE_NAME);
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);

        String customerNumber = (String) ControllerUtils.getColumnValue(userTable, "contactNo", customerId);
        String driverNumber = (String) ControllerUtils.getColumnValue(userTable, "contactNo", driverId);

        ArrayList<String> availablePhoneNumbers = getAvailablePhoneNumbers(phoneNumberTable, 2);

        Date now = new Date();

        Record customerVirtualNumber = new Record()
                .addValue("phoneNumber", availablePhoneNumbers.get(0))
                .addValue("userPhoneNumber", customerNumber)
                .addValue("orderId", orderId)
                .addValue("status", PhoneNumberStatuses.ASSIGNED.name())
                .addValue("assignedTime", now);

        iDatabaseUpdater.addOrUpdateRow(TableNames.PHONE_NUMBER_TABLE_NAME, "phoneNumber", customerVirtualNumber);

        Record driverVirtualNumber = new Record()
                .addValue("phoneNumber", availablePhoneNumbers.get(1))
                .addValue("userPhoneNumber", driverNumber)
                .addValue("orderId", orderId)
                .addValue("status", PhoneNumberStatuses.ASSIGNED.name())
                .addValue("assignedTime", now);

        iDatabaseUpdater.addOrUpdateRow(TableNames.PHONE_NUMBER_TABLE_NAME, "phoneNumber", driverVirtualNumber);

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

            //if the orderId is not set or the number is assigned but hasn't been updated in 300 seconds then we can use it.
            if (orderId == null || orderId == "" ||(status == PhoneNumberStatuses.ASSIGNED.name() && assignedAgoMillis > (1000 * 300))) {
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
