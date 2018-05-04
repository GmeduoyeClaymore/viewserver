package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.setup.datasource.PaymentDataSource;
import com.shotgun.viewserver.user.SavedBankAccount;
import com.shotgun.viewserver.user.SavedPaymentCard;
import com.shotgun.viewserver.user.User;
import com.stripe.model.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.network.IChannel;
import io.viewserver.network.IPeerSession;
import io.viewserver.network.netty.NettyChannel;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public abstract class BasePaymentController implements IPaymentController{
    private static final Logger logger = LoggerFactory.getLogger(BasePaymentController.class);
    private final IDatabaseUpdater iDatabaseUpdater;

    public BasePaymentController(IDatabaseUpdater iDatabaseUpdater) {
        this.iDatabaseUpdater = iDatabaseUpdater;
    }

    public HashMap<String, Object> createPaymentCustomer(String emailAddress, PaymentCard paymentCard) {
        String cardToken = createCardToken(paymentCard);
        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("email", emailAddress);
        customerParams.put("source", cardToken);

        try {
            Customer customer = createCustomer(customerParams);
            logger.debug("Added stripe payment customer with id {}", customer.getId());

            Card stripeSavedCard = (Card) customer.getSources().retrieve(customer.getDefaultSource());
            HashMap<String, Object> result = new HashMap<>();
            result.put("stripeCustomerId", customer.getId());
            result.put("savedPaymentCard", mapStripeCardToSavedPaymentCard(stripeSavedCard));
            return result;
        } catch (Exception e) {
            logger.error("There was a problem adding the payment customer", e);
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Object> createPaymentAccount(User user, DeliveryAddress address, PaymentBankAccount paymentBankAccount) {
        Map<String, Object> accountParams = null;

        try {
            //https://stripe.com/docs/api#update_account
            Map<String, Object> payoutSchedule = new HashMap<>();
            payoutSchedule.put("delay_days", 7);
            payoutSchedule.put("interval", "weekly");
            payoutSchedule.put("weekly_anchor", "monday");

            Map<String, Object> tosAcceptance = new HashMap<>();
            tosAcceptance.put("date", System.currentTimeMillis() / 1000L);
            IPeerSession session = ControllerContext.Current().getPeerSession();
            IChannel channel = session.getChannel();
            String ip = "0.0.0.0";
            if (channel instanceof NioSocketChannel) {
                ip = ((NioSocketChannel) ((NettyChannel) channel).getChannel()).remoteAddress().getAddress().toString().substring(1);
            }
            tosAcceptance.put("ip", ip);

            Map<String, Object> dob = new HashMap<>();
            Calendar c = Calendar.getInstance();
            if (user.getDob() == null) {
                throw new RuntimeException("No DOB specified");
            }
            c.setTime(user.getDob());
            dob.put("day", c.get(Calendar.DAY_OF_MONTH));
            dob.put("month", c.get(Calendar.MONTH));
            dob.put("year", c.get(Calendar.YEAR));

            Map<String, Object> entityAddress = new HashMap<>();
            entityAddress.put("line1", address.getLine1());
            entityAddress.put("city", address.getCity());
            entityAddress.put("postal_code", address.getPostCode());

            Map<String, Object> legalEntity = new HashMap<>();
            legalEntity.put("address", entityAddress);
            legalEntity.put("dob", dob);
            legalEntity.put("first_name", user.getFirstName());
            legalEntity.put("last_name", user.getLastName());
            legalEntity.put("type", "individual");

            Map<String, Object> externalAccount = new HashMap<>();
            externalAccount.put("object", "bank_account");
            externalAccount.put("account_number", paymentBankAccount.getAccountNumber().replaceAll("[^\\d]", ""));
            externalAccount.put("routing_number", paymentBankAccount.getSortCode().replaceAll("[^\\d]", ""));
            externalAccount.put("account_holder_type", "individual");
            externalAccount.put("country", "GB");
            externalAccount.put("currency", "gbp");

            accountParams = new HashMap<>();
            accountParams.put("type", "custom");
            accountParams.put("country", "GB");
            accountParams.put("default_currency", "gbp");
            accountParams.put("email", user.getEmail());
            accountParams.put("external_account", externalAccount);
            accountParams.put("payout_schedule", payoutSchedule);
            accountParams.put("tos_acceptance", tosAcceptance);
            accountParams.put("legal_entity", legalEntity);

            BankAccount stripeBankAccount = createAccount(accountParams);
            logger.debug("Added stripe account id {} params are {}", stripeBankAccount.getAccount(), ControllerUtils.toString(accountParams));

            HashMap<String, Object> result = new HashMap<>();
            result.put("stripeAccountId", stripeBankAccount.getAccount());
            result.put("savedBankAccount", mapStripeBankAccountToSavedBankAccount(stripeBankAccount));
            return result;

        } catch (Exception e) {
            logger.error(String.format("There was a problem creating the payment account \"%s\"", ControllerUtils.toString(accountParams)), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public SavedBankAccount setBankAccount(PaymentBankAccount paymentBankAccount) {
        try {
            String stripeAccountId =  getStripeAccountId();

            //stripe account exists update the bank account
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
            account = account.update(accountParams);

            logger.debug("Set bank account for stripe account {}", stripeAccountId);
            return this.mapStripeBankAccountToSavedBankAccount(getBankAccountFromAccount(account));
        } catch (Exception e) {
            logger.error("There was a problem setting the bank account", e);
            throw new RuntimeException(e);
        }
    }

    public SavedPaymentCard addPaymentCard(PaymentCard paymentCard) {
        try {
            String customerToken = getStripeCustomerToken();
            String cardToken = createCardToken(paymentCard);
            Customer customer = Customer.retrieve(customerToken);
            Map<String, Object> params = new HashMap<>();
            params.put("source", cardToken);
            Card stripeCard = (Card)customer.getSources().create(params);
            logger.debug("Added stripe payment card with token {}", cardToken);
            return mapStripeCardToSavedPaymentCard(stripeCard);
        } catch (Exception e) {
            logger.error("There was a problem adding the payment card", e);
            throw new RuntimeException(e);
        }
    }

    public void deletePaymentCard(String cardId) {
        try {
            String customerToken = getStripeCustomerToken();
            Customer customer = Customer.retrieve(customerToken);
            customer.getSources().retrieve(cardId).delete();
        } catch (Exception e) {
            logger.error("There was a problem deleting the payment card", e);
            throw new RuntimeException(e);
        }
    }

    public String createCharge(int totalPrice,
                               String paymentMethodId,
                               String fromCustomerUserId,
                               String toPartnerUserId,
                               String description) {


        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);

        String stripeCustomerId = (String) ControllerUtils.getColumnValue(userTable, "stripeCustomerId", fromCustomerUserId);
        String toAccountId = (String) ControllerUtils.getColumnValue(userTable, "stripeAccountId", toPartnerUserId);
        int chargePercentage = (int) ControllerUtils.getColumnValue(userTable, "chargePercentage", toPartnerUserId);


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
            Charge charge = createStripeCharge(params);

            String paymentid = ControllerUtils.generateGuid();
            IRecord paymentRecord = new Record().
                    addValue("totalPrice", totalPrice).
                    addValue("chargeId", charge.getId()).
                    addValue("chargePercentage", chargePercentage).
                    addValue("paymentId", paymentid).
                    addValue("paymentMethodId", paymentMethodId).
                    addValue("paidFromUserId", fromCustomerUserId).
                    addValue("paidToUserId", toPartnerUserId).
                    addValue("accountId", toAccountId).
                    addValue("description", description);

            iDatabaseUpdater.addOrUpdateRow(TableNames.PAYMENT_TABLE_NAME, PaymentDataSource.getDataSource().getSchema(), paymentRecord);

            logger.debug("Created stripe charge {} with amount {} with {} sent to driver", charge.getId(), totalPrice, destinationAmount);

            return paymentid;
        } catch (Exception e) {
            logger.error("There was a problem creating the charge", e);
            throw new RuntimeException(e);
        }
    }

    protected BankAccount getBankAccountFromAccount(Account account){
        return (BankAccount)account.getExternalAccounts().getData().get(0);
    }

    protected SavedPaymentCard mapStripeCardToSavedPaymentCard(Card stripeCard) {
        SavedPaymentCard savedPaymentCard = JSONBackedObjectFactory.create(SavedPaymentCard.class);
        savedPaymentCard.set("cardId", stripeCard.getId());
        savedPaymentCard.set("last4", stripeCard.getLast4());
        savedPaymentCard.set("brand", stripeCard.getBrand());
        savedPaymentCard.set("expMonth", stripeCard.getExpMonth());
        savedPaymentCard.set("expYear", stripeCard.getExpYear());
        savedPaymentCard.set("isDefault", true);

        return savedPaymentCard;
    }

    protected SavedBankAccount mapStripeBankAccountToSavedBankAccount(BankAccount stripeBankAccount){
        SavedBankAccount savedBankAccount = JSONBackedObjectFactory.create(SavedBankAccount.class);
        savedBankAccount.set("id", stripeBankAccount.getId());
        savedBankAccount.set("bankName", stripeBankAccount.getBankName());
        savedBankAccount.set("sortCode", stripeBankAccount.getRoutingNumber());
        savedBankAccount.set("last4", stripeBankAccount.getLast4());
        savedBankAccount.set("country", stripeBankAccount.getCountry());

        return savedBankAccount;
    }


    protected abstract Customer createCustomer(Map<String, Object> params);
    protected abstract BankAccount createAccount(Map<String, Object> params);
    protected abstract Charge createStripeCharge(Map<String, Object> params);
    protected abstract String getStripeCustomerToken();
    protected abstract String getStripeAccountId();
    protected abstract String createCardToken(PaymentCard paymentCard);

}
