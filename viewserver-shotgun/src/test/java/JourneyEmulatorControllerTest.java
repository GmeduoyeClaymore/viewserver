import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotgun.viewserver.delivery.Delivery;
import com.shotgun.viewserver.maps.MapsController;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.order.OrderController;
import com.shotgun.viewserver.user.JourneyEmulatorController;
import io.viewserver.command.ControllerContext;
import io.viewserver.command.ControllerRegistration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class JourneyEmulatorControllerTest {

    private JourneyEmulatorController sut;


    @Before
    public void createSut(){
        sut = new JourneyEmulatorController(new MapsController(new MapsControllerKey("AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M",false)));
    }

    @Test
    public  void canEmulatorJourney() throws NoSuchMethodException {
        ControllerContext.create(new MockSession());
        ControllerContext.set("userId","foo");
        ControllerRegistration reg = new ControllerRegistration(sut);

        String params = "{\"emulator\":\"emulator-5558\",\"directionsRequest\":{\"mode\":\"driving\",\"locations\":[{\"lat\":51.5033640,\"lng\":-0.1276250},{\"lat\":51.5526951,\"lng\":-0.1412256}]}}";
        reg.getActions().get("emulateJourney").invoke(params);
    }
}
