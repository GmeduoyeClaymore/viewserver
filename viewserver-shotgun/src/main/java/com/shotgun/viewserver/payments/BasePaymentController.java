package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.user.SavedPaymentCard;
import com.shotgun.viewserver.user.User;
import com.stripe.model.Account;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.viewserver.controller.ControllerContext;
import io.viewserver.network.IChannel;
import io.viewserver.network.IPeerSession;
import io.viewserver.network.netty.NettyChannel;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public abstract class BasePaymentController implements IPaymentController{
    private static final Logger logger = LoggerFactory.getLogger(BasePaymentController.class);

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

            String accountId = createAccount(accountParams);
            logger.debug("Added stripe account id {} params are {}", accountId, ControllerUtils.toString(accountParams));

            HashMap<String, Object> result = new HashMap<>();
            result.put("stripeAccountId", accountId);
            result.put("savedBankAccount", "");
            return result;

        } catch (Exception e) {
            logger.error(String.format("There was a problem creating the payment account \"%s\"", ControllerUtils.toString(accountParams)), e);
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


    protected abstract Customer createCustomer(Map<String, Object> params);
    protected abstract String createAccount(Map<String, Object> params);
    protected abstract String getStripeCustomerToken();
    protected abstract String createCardToken(PaymentCard paymentCard);

}
