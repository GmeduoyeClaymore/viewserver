package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.order.controllers.contracts.OrderTransformationController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.PhoneNumberStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.PhoneNumberDataSource;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
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

import static com.google.common.base.Strings.isNullOrEmpty;


@Controller(name = "phoneCallController")
public class PhoneCallController implements OrderTransformationController{
    private static final Logger log = LoggerFactory.getLogger(PhoneCallController.class);
    private IDatabaseUpdater iDatabaseUpdater;
    private ICatalog systemCatlog;

    public PhoneCallController(IDatabaseUpdater iDatabaseUpdater, ICatalog systemCatlog) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.systemCatlog = systemCatlog;
    }

    @ControllerAction(path = "getVirtualNumber", isSynchronous = true)
    public String getVirtualNumber(@ActionParam(name = "userId")String toUserId) {
        String fromUserId = (String) ControllerContext.get("userId");
        ArrayList<String> availablePhoneNumbers = assignPhoneNumbers(fromUserId, toUserId);
        return availablePhoneNumbers.get(1);
    }

    private ArrayList<String> assignPhoneNumbers(String fromUserId, String toUserId) {
        KeyedTable phoneNumberTable = (KeyedTable) systemCatlog.getOperatorByPath(TableNames.PHONE_NUMBER_TABLE_NAME);
        KeyedTable userTable = (KeyedTable) systemCatlog.getOperatorByPath(TableNames.USER_TABLE_NAME);

        String customerNumber = (String) ControllerUtils.getColumnValue(userTable, "contactNo", fromUserId);
        String driverNumber = (String) ControllerUtils.getColumnValue(userTable, "contactNo", toUserId);

        ArrayList<String> availablePhoneNumbers = getAvailablePhoneNumbers(phoneNumberTable, 2);

        Date now = new Date();

        Record customerVirtualNumber = new Record()
                .addValue("phoneNumber", availablePhoneNumbers.get(0))
                .addValue("userPhoneNumber", customerNumber)
                .addValue("fromUserId", fromUserId)
                .addValue("toUserId", toUserId)
                .addValue("version", ControllerUtils.getColumnValue(userTable, "version", availablePhoneNumbers.get(0)))
                .addValue("phoneNumberStatus", PhoneNumberStatuses.ASSIGNED.name())
                .addValue("assignedTime", now);

        iDatabaseUpdater.addOrUpdateRow(TableNames.PHONE_NUMBER_TABLE_NAME, PhoneNumberDataSource.getDataSource().getSchema(), customerVirtualNumber).subscribe();

        Record driverVirtualNumber = new Record()
                .addValue("phoneNumber", availablePhoneNumbers.get(1))
                .addValue("userPhoneNumber", driverNumber)
                .addValue("fromUserId", fromUserId)
                .addValue("toUserId", toUserId)
                .addValue("version", ControllerUtils.getColumnValue(userTable, "version", availablePhoneNumbers.get(1)))
                .addValue("phoneNumberStatus", PhoneNumberStatuses.ASSIGNED.name())
                .addValue("assignedTime", now);

        iDatabaseUpdater.addOrUpdateRow(TableNames.PHONE_NUMBER_TABLE_NAME, PhoneNumberDataSource.getDataSource().getSchema(), driverVirtualNumber).subscribe();

        return availablePhoneNumbers;
    }

    private ArrayList<String> getAvailablePhoneNumbers(ITable phoneNumberTable, int requiredNumbers) {
        IRowSequence rows = phoneNumberTable.getOutput().getAllRows();
        ArrayList availableNumbers = new ArrayList();

        while (rows.moveNext()) {
            String fromUserId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "fromUserId", rows.getRowId());
            String toUserId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "toUserId", rows.getRowId());
            String status = (String) ControllerUtils.getColumnValue(phoneNumberTable, "phoneNumberStatus", rows.getRowId());
            long assignedTime = (long) ControllerUtils.getColumnValue(phoneNumberTable, "assignedTime", rows.getRowId());
            long now = new Date().getTime();
            long assignedAgoMillis = now - assignedTime;

            //if the orderId is not set or the number is assigned but hasn't been updated in 300 seconds then we can use it.
            if ((isNullOrEmpty(fromUserId) &&  isNullOrEmpty(toUserId)) ||(status == PhoneNumberStatuses.ASSIGNED.name() && assignedAgoMillis > (1000 * 300))) {
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

    @Override
    public ICatalog getSystemCatalog() {
        return systemCatlog;
    }

    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return iDatabaseUpdater;
    }
}
