import com.shotgun.viewserver.user.NexmoController;
import com.shotgun.viewserver.user.NexmoControllerKey;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class NexmoControllerTest {

    private NexmoController sut;


    @Before
    public void createSut(){
        sut = new NexmoController(9000, null, new NexmoControllerKey("c03cd396", "33151c6772f2bd52"), null);
    }

    @Test
    public void canGetNumberInfo() throws NoSuchMethodException {
        String reps = sut.getInternationalFormatNumber("07733362799");
        System.out.println(reps);
    }
}
