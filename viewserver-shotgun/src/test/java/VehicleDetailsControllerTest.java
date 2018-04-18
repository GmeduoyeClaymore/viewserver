import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Gbemiga on 09/01/18.
 */
public class VehicleDetailsControllerTest {

    private VehicleDetailsController sut;

    public void createSut(){
        sut = new VehicleDetailsController(new VehicleDetailsApiKey("881fc904-6ddf-4a48-91ad-7248677ffd1c"), true);
    }

    @Test
    public void canRetrieveDetails(){
        createSut();
        System.out.println(sut.getDetails("YA61AYB"));
    }

    @Test
    public void canRetrieveMockDetails(){
        createSut();
        System.out.println(sut.getDetails("YA61AYB"));
    }
    @Test(expected = RuntimeException.class)
    public void EXCEPTION_invalidRegistrationNumber(){
        createSut();
        System.out.println(sut.getDetails("YA57MNX"));
    }

    @Test(expected = RuntimeException.class)
    public void EXCEPTION_invalidVehicleType(){
        createSut();
        System.out.println(sut.getDetails("AU06KUC"));
    }


}
