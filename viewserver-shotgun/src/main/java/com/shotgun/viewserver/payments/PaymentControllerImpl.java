package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.setup.datasource.PaymentDataSource;
import com.shotgun.viewserver.user.SavedPaymentCard;
import com.shotgun.viewserver.user.User;
import com.stripe.Stripe;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
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
import java.util.UUID;

@Controller(name = "paymentController")
public class PaymentControllerImpl extends BasePaymentController implements IPaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentControllerImpl.class);
    private StripeApiKey apiKey;
    private IDatabaseUpdater iDatabaseUpdater;

    public PaymentControllerImpl(StripeApiKey apiKey, IDatabaseUpdater iDatabaseUpdater) {
        this.apiKey = apiKey;
        this.iDatabaseUpdater = iDatabaseUpdater;
        Stripe.apiKey = apiKey.getPrivateKey();
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
            Charge charge = Charge.create(params);

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



    protected String createAccount(Map<String, Object> params){
        try{
            Account account = Account.create(params);
            return account.getId();
        } catch (Exception e) {
            logger.error("There was a problem creating the payment account", e);
            throw new RuntimeException(e);
        }
    }

    protected Customer createCustomer(Map<String, Object> params){
        try{
         return Customer.create(params);
        } catch (Exception e) {
            logger.error("There was a problem creating the payment customer", e);
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

    @Override
    public void setBankAccount(PaymentBankAccount paymentBankAccount) {
        try {
            String stripeAccountId = getUser().getStripeAccountId();

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
            account.update(accountParams);

            logger.debug("Set bank account for stripe account {}", stripeAccountId);
        } catch (Exception e) {
            logger.error("There was a problem setting the bank account", e);
            throw new RuntimeException(e);
        }
    }

    protected String createCardToken(PaymentCard paymentCard) {
        RequestOptions requestOptions = RequestOptions.builder().setApiKey(this.apiKey.getPublicKey()).build();

        Map<String, Object> tokenParams = new HashMap<>();
        Map<String, Object> cardParams = new HashMap<>();
        cardParams.put("number", paymentCard.getNumber());
        cardParams.put("exp_month", paymentCard.getExpMonth());
        cardParams.put("exp_year", paymentCard.getExpYear());
        cardParams.put("cvc", paymentCard.getCvc());
        tokenParams.put("card", cardParams);

        try {
            Token token = Token.create(tokenParams, requestOptions);
            logger.debug("Created stripe payment with token {}", token.getId());
            return token.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User getUser() {
        User user = (User) ControllerContext.get("user");
        if (user == null) {
            throw new RuntimeException("User must be logged in to get current payment cards");
        }
        return user;
    }
}
