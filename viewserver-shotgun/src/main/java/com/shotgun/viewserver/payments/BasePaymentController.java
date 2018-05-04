package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.user.SavedPaymentCard;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            SavedPaymentCard savedPaymentCard = JSONBackedObjectFactory.create(SavedPaymentCard.class);
            savedPaymentCard.set("cardId", stripeSavedCard.getId());
            savedPaymentCard.set("last4", stripeSavedCard.getLast4());
            savedPaymentCard.set("brand", stripeSavedCard.getBrand());
            savedPaymentCard.set("expMonth", stripeSavedCard.getExpMonth());
            savedPaymentCard.set("expYear", stripeSavedCard.getExpYear());

            HashMap<String, Object> result = new HashMap<>();
            result.put("stripeCustomerId", customer.getId());
            result.put("savedPaymentCard", savedPaymentCard);
            return result;
        } catch (Exception e) {
            logger.error("There was a problem adding the payment customer", e);
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
        savedPaymentCard.set("cardId", stripeCard.getId())
                .set("last4", stripeCard.getLast4())
                .set("brand", stripeCard.getBrand())
                .set("expMonth", stripeCard.getExpMonth())
                .set("expYear", stripeCard.getExpYear());

        return savedPaymentCard;
    }

    protected abstract Customer createCustomer(Map<String, Object> params);
    protected abstract String getStripeCustomerToken();
    protected abstract String createCardToken(PaymentCard paymentCard);

}
