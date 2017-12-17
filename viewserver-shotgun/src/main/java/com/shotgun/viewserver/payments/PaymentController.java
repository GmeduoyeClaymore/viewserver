package com.shotgun.viewserver.payments;

import com.stripe.Stripe;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccountCollection;
import com.stripe.model.Token;
import com.stripe.net.RequestOptions;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.core.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller(name = "paymentController")
public class PaymentController{


    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private StripeApiKey apiKey;

    public PaymentController(StripeApiKey apiKey) {
        this.apiKey = apiKey;
        Stripe.apiKey = apiKey.getPrivateKey();
    }

    @ControllerAction(path = "createPaymentCustomer", isSynchronous = true)
    public HashMap<String, Object> createPaymentCustomer(PaymentCustomer paymentCustomer){
        String cardToken = createCardToken(paymentCustomer.getPaymentCard());
        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("email", paymentCustomer.getEmail());
        customerParams.put("source", cardToken);

        try {
            Customer customer = Customer.create(customerParams);
            logger.debug("Added stripe payment customer with id {}", customer.getId());
            HashMap<String, Object> result  = new HashMap<>();
            result.put("customerId",customer.getId());
            result.put("paymentToken",cardToken);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "addPaymentCard", isSynchronous = true)
    public String addPaymentCard(PaymentCard paymentCard){
        try {
            String cardToken = createCardToken(paymentCard);
            Customer customer = Customer.retrieve(paymentCard.customerToken);
            Map<String, Object> params = new HashMap<>();
            params.put("source", cardToken);
            customer.getSources().create(params);
            logger.debug("Added stripe payment card with token {}", cardToken);
            return cardToken;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "getPaymentCards", isSynchronous = true)
    public List<Card> getPaymentCards(String customerToken){
        try {
            HashMap<String, Object> sourcesParams = new HashMap<>();
            sourcesParams.put("object", "card");
            ExternalAccountCollection cards = Customer.retrieve(customerToken).getSources().list(sourcesParams);
            logger.debug("Got {} stripe cards for customerToken {}", cards.getCount(), customerToken);
            return cards.getData().stream().map(a -> (Card)a).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createCardToken(PaymentCard paymentCard){
        RequestOptions requestOptions = RequestOptions.builder().setApiKey(this.apiKey.getPublicKey()).build();

        Map<String, Object> tokenParams = new HashMap<>();
        Map<String, Object> cardParams = new HashMap<>();
        cardParams.put("number", paymentCard.number);
        cardParams.put("exp_month", paymentCard.expMonth);
        cardParams.put("exp_year", paymentCard.expYear);
        cardParams.put("cvc", paymentCard.cvc);
        tokenParams.put("card", cardParams);

        try {
            Token token = Token.create(tokenParams, requestOptions);
            logger.debug("Created stripe payment with token {}", token.getId());
            return token.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
