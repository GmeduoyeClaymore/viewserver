import com.shotgun.viewserver.user.DriverController;
import io.viewserver.command.ControllerRegistration;
import org.junit.Before;
import org.junit.Test;

public class DriverControllerTest {

    private DriverController sut;


    @Before
    public void createSut(){
        sut = new DriverController(null,null,null,null,null,null,null, null, null);
    }

    @Test
    public  void canRegisterDriver() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        String params = "{\"user\":{\"firstName\":\"paul\",\"lastName\":\"graves\",\"email\":\"test@test.com\",\"contactNo\":\"07733362799\",\"type\":\"driver\",\"password\":\"password\",\"dob\":\"1982-02-03\",\"selectedContentTypes\":\"1\"},\"vehicle\":{\"dimensions\":{\"height\":2385,\"width\":1974,\"length\":2582,\"weight\":1073},\"vehicleId\":null,\"registrationNumber\":\"YA61AYB\",\"colour\":\"White\",\"make\":\"Ford\",\"model\":\"Transit 100 T280 Fwd\",\"vehicleTypeId\":\"LCV\",\"numAvailableForOffload\":null},\"bankAccount\":{\"accountNumber\":\"00012345\",\"sortCode\":\"108800\"},\"address\":{\"line1\":\"line1\",\"city\":\"London\",\"postCode\":\"SW178RS\"}}";
        TestControllerUtils.invoke(reg, "registerDriver", params);
    }
}
