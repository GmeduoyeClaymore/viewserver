import com.shotgun.viewserver.user.UserController;
import io.viewserver.controller.ControllerRegistration;
import org.junit.Before;
import org.junit.Test;

public class UserControllerTest {

    private UserController sut;


    @Before
    public void createSut(){
        sut = new UserController(null,null,null,null,null);
    }

    @Test
    public  void canRegisterUser() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        String params = "{\"user\":{\"firstName\":\"Paul\",\"lastName\":\"Graves\",\"email\":\"test5@test.com\",\"contactNo\":\"077333627\",\"type\":\"customer\",\"password\":\"welcome\"},\"deliveryAddress\":{\"line1\":\"129 Drakefield Road\",\"line2\":\"Tooting\",\"city\":\"London\",\"country\":\"UK\",\"postCode\":\"SW17 8RS\",\"googlePlaceId\":\"123456\",\"latitude\":15.1,\"longitude\":16.3,\"isDefault\":true},\"paymentCard\":{\"number\":\"4242 4242 4242 4242\",\"expMonth\":\"12\",\"expYear\":\"22\",\"cvc\":\"123\"}}";
        System.out.println(TestControllerUtils.invoke(reg, "registerCustomer", params));
    }


}
