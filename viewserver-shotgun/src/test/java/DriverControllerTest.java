import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.user.DriverController;
import io.viewserver.command.ControllerRegistration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class DriverControllerTest {

    private DriverController sut;


    @Before
    public void createSut(){
        sut = new DriverController(null,null,null,null,null,null,null, null, null, null, true);
    }

    @Test
    public  void canRegisterDriver() throws NoSuchMethodException, IOException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        String vehicle = "{\"dimensions\":{\"height\":2385,\"width\":1974,\"length\":2582,\"weight\":1073,\"volumeMetresCubed\":12},\"vehicleId\":null,\"registrationNumber\":\"YA61AYB\",\"colour\":\"White\",\"make\":\"Ford\",\"model\":\"Transit 100 T280 Fwd\",\"bodyStyle\":\"PANEL VAN\",\"selectedProductIds\":[\"1SmallVan\",\"2MediumVan \"]}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue(vehicle, Vehicle.class);
    }
}
