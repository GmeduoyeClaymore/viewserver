import com.shotgun.viewserver.maps.*;
import com.shotgun.viewserver.payments.*;
import com.stripe.model.Card;
import org.apache.logging.log4j.core.util.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

    @Before
    public void createSut(){
        sut = new PaymentController(new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4"));
    }

    @Test
    public void A_canCreateCustomer(){
        PaymentCustomer paymentCustomer = new PaymentCustomer();
        paymentCustomer.setEmail("FOO@BAR.com");
        PaymentCard paymentCard = getPaymentCard("");
        paymentCustomer.setPaymentCard(paymentCard);
        HashMap<String,Object> result = sut.createPaymentCustomer(paymentCustomer);
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
        List<Card> paymentCards = sut.getPaymentCards(this.customerId);
        assertTrue(paymentCards.size() > 0);
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
