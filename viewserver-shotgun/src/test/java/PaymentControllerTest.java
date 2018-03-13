import com.shotgun.viewserver.payments.*;
import com.shotgun.viewserver.user.User;
import com.stripe.model.Card;
import io.viewserver.controller.ControllerContext;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Gbemiga on 17/12/17.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PaymentControllerTest {

    private PaymentController sut;

    private static String customerId;
    private ControllerContext ctxt;

    @Before
    public void createSut(){
        sut = new PaymentController(new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4"));
        TestControllerUtils.getControllerContext("foo");
        User user = (User) ControllerContext.get("user");
        user.setStripeCustomerId(this.customerId);
    }

    @Test
    public void A_canCreateCustomer(){
        PaymentCard paymentCard = getPaymentCard("");
        HashMap<String,Object> result = sut.createPaymentCustomer("FOO@BAR.com", paymentCard);
        this.customerId = (String)result.get("customerId");
        assertNotNull(this.customerId);

        System.out.println(result);
    }


    @Test
    public void B_canAddPaymentCard(){
        assertNotNull(sut.addPaymentCard(getPaymentCard(this.customerId)));
    }

    @Test
    public void C_canGetPaymentCards(){
        List<Card> paymentCards = sut.getPaymentCards();
        assertTrue(paymentCards.size() == 2);
    }


    private PaymentCard getPaymentCard(String customerToken) {
        if(customerToken == null){
            throw new RuntimeException("specify customer id");
        }
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCustomerToken(customerToken);
        paymentCard.setCvc("123");
        paymentCard.setExpMonth("12");
        paymentCard.setExpYear("22");
        paymentCard.setNumber("4242424242424242");
        return paymentCard;
    }
}
