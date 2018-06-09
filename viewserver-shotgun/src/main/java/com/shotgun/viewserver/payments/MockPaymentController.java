package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.setup.datasource.PaymentDataSource;
import com.shotgun.viewserver.user.SavedBankAccount;
import com.shotgun.viewserver.user.SavedPaymentCard;
import com.shotgun.viewserver.user.User;
import com.stripe.model.*;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.controller.Controller;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.shotgun.viewserver.ControllerUtils.getUser;

@Controller(name = "paymentController")
public class MockPaymentController implements IPaymentController {

    private IDatabaseUpdater iDatabaseUpdater;
    private ICatalog catalog;

    public MockPaymentController(IDatabaseUpdater iDatabaseUpdater, ICatalog catalog){
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.catalog = catalog;
    }
    private static final Logger logger = LoggerFactory.getLogger(MockPaymentController.class);

    public SavedPaymentCard addPaymentCard(PaymentCard paymentCard) {
        try {
            String cardToken = createCardToken(paymentCard);
            Map<String, Object> params = new HashMap<>();
            params.put("source", cardToken);
            logger.debug("Added stripe payment card with token {}", cardToken);
            return mapStripeCardToSavedPaymentCard(paymentCard);
        } catch (Exception e) {
            logger.error("There was a problem adding the payment card", e);
            throw new RuntimeException(e);
        }
    }

    public void deletePaymentCard(String cardId) {
        try {
            String customerToken = getStripeCustomerToken();
        } catch (Exception e) {
            logger.error("There was a problem deleting the payment card", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public SavedBankAccount setBankAccount(PaymentBankAccount paymentBankAccount) {
        return mapStripeBankAccountToSavedBankAccount(paymentBankAccount);
    }


    private SavedBankAccount mapStripeBankAccountToSavedBankAccount(PaymentBankAccount paymentBankAccount) {
        try {
            SavedBankAccount bankAccount = JSONBackedObjectFactory.create(SavedBankAccount.class);
            bankAccount.set("id",ControllerUtils.generateGuid());
            bankAccount.set("bankName","STRIPE TEST BANK");
            bankAccount.set("sortCode","10-88-00");
            bankAccount.set("last4",paymentBankAccount.getAccountNumber().substring(paymentBankAccount.getAccountNumber().length() - 4));
            bankAccount.set("country", "GB");
            return bankAccount;
        } catch (Exception e) {
            logger.error("There was a problem setting the bank account", e);
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Object> createPaymentCustomer(String emailAddress, PaymentCard paymentCard) {
        String cardToken = createCardToken(paymentCard);
        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("email", emailAddress);
        customerParams.put("source", cardToken);
        String customerId = ControllerUtils.generateGuid();
        try {
            logger.debug("Added stripe payment customer with id {}", customerId);

            HashMap<String, Object> result = new HashMap<>();
            result.put("stripeCustomerId", customerId);
            result.put("savedPaymentCard", mapStripeCardToSavedPaymentCard(paymentCard));
            return result;
        } catch (Exception e) {
            logger.error("There was a problem adding the payment customer", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public HashMap<String, Object> createPaymentAccount(User user, DeliveryAddress address, PaymentBankAccount paymentBankAccount) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("stripeAccountId", paymentBankAccount.getAccountNumber());
        result.put("savedBankAccount", mapStripeBankAccountToSavedBankAccount(paymentBankAccount));
        return result;
    }

    public Observable<String> createCharge(int totalPrice,
                                           String paymentMethodId,
                                           String fromCustomerUserId,
                                           String toPartnerUserId,
                                           String description) {


        KeyedTable userTable = (KeyedTable) catalog.getOperatorByPath(TableNames.USER_TABLE_NAME);


        String stripeCustomerId = (String) ColumnHolderUtils.getColumnValue(userTable, "stripeCustomerId",fromCustomerUserId);
        String toAccountId = (String) ColumnHolderUtils.getColumnValue(userTable, "stripeAccountId",toPartnerUserId);
        int chargePercentage = (int) ColumnHolderUtils.getColumnValue(userTable, "chargePercentage",toPartnerUserId);


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

            logger.debug("Created stripe charge {} with amount {} with {} sent to driver", chargeId, totalPrice, destinationAmount);

            return iDatabaseUpdater.addOrUpdateRow(TableNames.PAYMENT_TABLE_NAME, PaymentDataSource.getDataSource().getSchema(), paymentRecord, IRecord.UPDATE_LATEST_VERSION).map(res -> paymentid);
        } catch (Exception e) {
            logger.error("There was a problem creating the charge", e);
            throw new RuntimeException(e);
        }
    }

    private SavedPaymentCard mapStripeCardToSavedPaymentCard(PaymentCard paymentCard) {
        SavedPaymentCard savedPaymentCard = JSONBackedObjectFactory.create(SavedPaymentCard.class);
        savedPaymentCard.set("cardId", ControllerUtils.generateGuid());
        String number = paymentCard.getNumber();
        savedPaymentCard.set("last4", number.substring(number.length() - 4));
        savedPaymentCard.set("brand", paymentCard.getBrand());
        savedPaymentCard.set("expMonth", Integer.parseInt(paymentCard.getExpMonth()));
        savedPaymentCard.set("expYear", 2000 + Integer.parseInt(paymentCard.getExpYear()));
        savedPaymentCard.set("isDefault", true);
        return savedPaymentCard;
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
