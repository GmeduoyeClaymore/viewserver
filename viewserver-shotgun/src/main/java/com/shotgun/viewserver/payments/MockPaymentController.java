package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.user.SavedPaymentCard;
import com.shotgun.viewserver.user.User;
import com.stripe.model.*;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.controller.Controller;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.shotgun.viewserver.ControllerUtils.getUser;

@Controller(name = "paymentController")
public class MockPaymentController extends BasePaymentController implements IPaymentController {

    public MockPaymentController(IDatabaseUpdater iDatabaseUpdater) {
        super (iDatabaseUpdater);
    }

    protected Customer createCustomer(Map<String, Object> params){
        SavedPaymentCard savedPaymentCard = JSONBackedObjectFactory.create((String)params.get("source"), SavedPaymentCard.class);

        ExternalAccount externalAccount = new ExternalAccount();
        externalAccount.setId(savedPaymentCard.getCardId());


    //    ExternalAccountCollection externalAccountCollection = new ExternalAccountCollection();
    //    externalAccountCollection.setData();

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
      //  customer.setDefaultSource(source);
      //  customer.setSources(externalAccountCollection);
        customer.setEmail((String) params.get("email"));
        return customer;
    }

    protected Charge createStripeCharge(Map<String, Object> params) {
        Charge charge = new Charge();
        charge.setId(UUID.randomUUID().toString());
        return charge;
    }

    protected BankAccount createAccount(Map<String, Object> params) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(UUID.randomUUID().toString());
        return bankAccount;
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
        String stripeAccountId = user.getStripeAccountId();
        if (stripeAccountId == null) {
            throw new RuntimeException("No stripe account id specified for current user");
        }
        return stripeAccountId;
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
