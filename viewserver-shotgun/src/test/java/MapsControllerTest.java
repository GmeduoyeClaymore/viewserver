import com.shotgun.viewserver.maps.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Gbemiga on 15/12/17.
 */
public class MapsControllerTest {

    private MapsController sut;


    @Before
    public void createSut(){
        sut = new MapsController(new MapsControllerKey("AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M",false));
    }

    @Test
    public void canRequestAutoComplete(){
       System.out.println(sut.makeAutoCompleteRequest(new MapRequest("kinnou", "en")));
    }

    @Test
    public void canRequestPlace(){
        System.out.println(sut.mapPlaceRequest(new PlaceRequest("ChIJrTLr-GyuEmsRBfy61i59si0", "en")));
    }

    @Test
    public void canRequestDirections(){
        System.out.println(sut.mapDirectionRequest(new DirectionRequest("W6 8NQ", "E5 9QR","driving")));
    }

    @Test
    public void canRequestNearby(){
        System.out.println(sut.requestNearbyPlaces(new NearbyPlaceRequest(2.17403, 41.40338, "distance")));
    }
}
