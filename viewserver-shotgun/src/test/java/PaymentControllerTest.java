import com.shotgun.viewserver.payments.*;
import com.shotgun.viewserver.user.User;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import rx.Observable;

import static org.junit.Assert.*;

import java.util.HashMap;

/**
 * Created by Gbemiga on 17/12/17.
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PaymentControllerTest {

    private IPaymentController sut;

    private static String customerId;
    private ControllerContext ctxt;

    @Before
    public void createSut(){
        sut = new PaymentController(new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4"), new IDatabaseUpdater() {
            @Override
            public Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record) {
                return null;
            }
        }, null);
        TestControllerUtils.getControllerContext("foo");
        User user = (User) ControllerContext.get("user");
        user.set("stripeCustomerId",this.customerId);
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


    private PaymentCard getPaymentCard(String customerToken) {
        if(customerToken == null){
            throw new RuntimeException("specify customer id");
        }
        PaymentCard paymentCard = JSONBackedObjectFactory.create(PaymentCard.class);
        paymentCard.set("cvc","123");
        paymentCard.set("expMonth","12");
        paymentCard.set("expYear","22");
        paymentCard.set("number","4242424242424242");
        return paymentCard;
    }
}
