import com.shotgun.viewserver.user.NexmoController;
import com.shotgun.viewserver.user.NexmoControllerKey;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class NexmoControllerTest {

    private NexmoController sut;


    @Before
    public void createSut(){
        sut = new NexmoController("prod1.shotgun.ltd",9000, null, new NexmoControllerKey("localhost", "c03cd396", "33151c6772f2bd52"), null, null,null);
    }

    @Test
    public void canGetNumberInfo() {
        String reps = sut.getInternationalFormatNumber("07733362799");
        System.out.println(reps);
    }

    @Test
    public void canUpdateEndPoint() {
        sut.modifyNexmoEndpoints();
    }
}
