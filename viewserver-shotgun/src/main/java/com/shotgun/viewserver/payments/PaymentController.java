package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.user.User;
import com.stripe.Stripe;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.catalog.ICatalog;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Controller(name = "paymentController")
public class PaymentController extends BasePaymentController implements IPaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private StripeApiKey apiKey;

    public PaymentController(StripeApiKey apiKey, IDatabaseUpdater iDatabaseUpdater, ICatalog catalog) {
        super(iDatabaseUpdater, catalog);
        this.apiKey = apiKey;
        Stripe.apiKey = apiKey.getPrivateKey();
    }

    protected BankAccount createAccount(Map<String, Object> params){
        try{
            Account account = Account.create(params);
            return this.getBankAccountFromAccount(account);
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

    protected Charge createStripeCharge(Map<String, Object> params) {
        try{
            return Charge.create(params);
        } catch (Exception e) {
            logger.error("There was a problem creating the stripe charge", e);
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

    protected String getStripeCustomerToken() {
        User user = getUser();
        String stripeCustomerToken = user.getStripeCustomerId();
        if (stripeCustomerToken == null) {
            throw new RuntimeException("No stripe customerid specified for current user");
        }
        return stripeCustomerToken;
    }

    protected String getStripeAccountId() {
        User user = getUser();
        return user.getStripeAccountId();
    }

    private User getUser() {
        User user = (User) ControllerContext.get("user");
        if (user == null) {
            throw new RuntimeException("User must be logged in to get current payment cards");
        }
        return user;
    }
}
