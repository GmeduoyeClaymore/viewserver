package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.setup.datasource.PaymentDataSource;
import com.shotgun.viewserver.user.SavedPaymentCard;
import com.shotgun.viewserver.user.User;
import com.stripe.model.*;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.table.KeyedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.shotgun.viewserver.ControllerUtils.getUser;

@Controller(name = "paymentController")
public class MockPaymentController extends BasePaymentController implements IPaymentController {
    private static final Logger logger = LoggerFactory.getLogger(MockPaymentController.class);
    private IDatabaseUpdater iDatabaseUpdater;
    public MockPaymentController(IDatabaseUpdater iDatabaseUpdater) {
        this.iDatabaseUpdater = iDatabaseUpdater;
    }

    protected Customer createCustomer(Map<String, Object> params){
        Customer customer = new Customer();
        UUID uuid = UUID.randomUUID();
        customer.setId(uuid.toString());
        customer.setEmail((String) params.get("email"));
        return customer;
    }

    protected String createAccount(Map<String, Object> params){
        return UUID.randomUUID().toString();
    }

    public String createCharge(int totalPrice,
                               String paymentMethodId,
                               String fromCustomerUserId,
                               String toPartnerUserId,
                               String description) {


        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);


        String stripeCustomerId = (String) ControllerUtils.getColumnValue(userTable, "stripeCustomerId",fromCustomerUserId);
        String toAccountId = (String) ControllerUtils.getColumnValue(userTable, "stripeAccountId",toPartnerUserId);
        int chargePercentage = (int) ControllerUtils.getColumnValue(userTable, "chargePercentage",toPartnerUserId);


        try {
            //TODO is there a better way to do this without using big decimals all over the place?
            BigDecimal chargeDecimal = BigDecimal.valueOf(chargePercentage).divide(BigDecimal.valueOf(100));
            BigDecimal destinationAmount = BigDecimal.valueOf(totalPrice).subtract(BigDecimal.valueOf(totalPrice).multiply(chargeDecimal).setScale(0, RoundingMode.DOWN));
            Map<String, Object> destinationParams = new HashMap<>();
            destinationParams.put("account", toAccountId);
            destinationParams.put("amount", destinationAmount);

            Map<String, Object> params = new HashMap<>();
            params.put("amount", totalPrice);
            params.put("currency", "gbp");
            params.put("customer", stripeCustomerId);
            params.put("source", paymentMethodId);
            params.put("description", description);
            params.put("destination", destinationParams);

            String paymentid = ControllerUtils.generateGuid();
            String chargeId = ControllerUtils.generateGuid();
            IRecord paymentRecord = new Record().
                    addValue("totalPrice", totalPrice).
                    addValue("chargeId", chargeId).
                    addValue("chargePercentage", chargePercentage).
                    addValue("paymentId", paymentid).
                    addValue("paymentMethodId", paymentMethodId).
                    addValue("paidFromUserId", fromCustomerUserId).
                    addValue("paidToUserId", toPartnerUserId).
                    addValue("accountId", toAccountId).
                    addValue("description", description);

            iDatabaseUpdater.addOrUpdateRow(TableNames.PAYMENT_TABLE_NAME, PaymentDataSource.getDataSource().getSchema(), paymentRecord);

            logger.debug("Created stripe charge {} with amount {} with {} sent to driver", chargeId, totalPrice, destinationAmount);

            return paymentid;
        } catch (Exception e) {
            logger.error("There was a problem creating the charge", e);
            throw new RuntimeException(e);
        }
    }

    private String getStripeAccountId() {
        User user = getUser();
        String stripeAccountId = user.getStripeAccountId();
        if (stripeAccountId == null) {
            throw new RuntimeException("No stripe account id specified for current user");
        }
        return stripeAccountId;
    }

    public void setBankAccount(PaymentBankAccount paymentBankAccount) {
        try {
            String stripeAccountId = getStripeAccountId();
            Map<String, Object> externalAccount = new HashMap<>();
            externalAccount.put("object", "bank_account");
            externalAccount.put("account_number", paymentBankAccount.getAccountNumber().replaceAll("[^\\d]", ""));
            externalAccount.put("routing_number", paymentBankAccount.getSortCode().replaceAll("[^\\d]", ""));
            externalAccount.put("account_holder_type", "individual");
            externalAccount.put("country", "GB");
            externalAccount.put("currency", "gbp");

            Account account = Account.retrieve(stripeAccountId, null);

            Map<String, Object> accountParams = new HashMap<>();
            accountParams.put("external_account", externalAccount);
            account.update(accountParams);
            logger.debug("Set bank account for stripe account {}", stripeAccountId);
        } catch (Exception e) {
            logger.error("There was a problem setting the bank account", e);
            throw new RuntimeException(e);
        }
    }

    protected String getStripeCustomerToken() {
        User user = getUser();
        String stripeCustomerToken = user.getStripeCustomerId();
        if (stripeCustomerToken == null) {
            throw new RuntimeException("No stripe customerid specified for current user");
        }
        return stripeCustomerToken;
    }

    protected String createCardToken(PaymentCard paymentCard) {

        Map<String, Object> tokenParams = new HashMap<>();
        Map<String, Object> cardParams = new HashMap<>();
        cardParams.put("number", paymentCard.getNumber());
        cardParams.put("exp_month", paymentCard.getExpMonth());
        cardParams.put("exp_year", paymentCard.getExpYear());
        cardParams.put("cvc", paymentCard.getCvc());
        tokenParams.put("card", cardParams);
        return tokenParams.toString();
    }
}
