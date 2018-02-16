import com.shotgun.viewserver.user.DriverController;
import com.shotgun.viewserver.user.NexmoController;
import io.viewserver.command.ControllerRegistration;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class NexmoControllerTest {

    private NexmoController sut;


    @Before
    public void createSut(){
        sut = new NexmoController(9000, null, "c03cd396", "33151c6772f2bd52", null);
    }

    @Test
    public void canGetNumberInfo() throws NoSuchMethodException {
        HashMap<String, Object> reps = sut.getPhoneNumberInfo("07733362799");
        System.out.println(reps);
    }
}
