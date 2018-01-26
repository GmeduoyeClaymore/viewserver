import com.shotgun.viewserver.maps.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

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
        ArrayList<LatLng> locations = new ArrayList<>();
        locations.add(new LatLng(51.5526951, -0.1412256));
        locations.add(new LatLng(51.4857236, -0.2123406));

        System.out.println(sut.mapDirectionRequest(new DirectionRequest((LatLng[]) locations.toArray(new LatLng[0]), "driving")));
    }

    @Test
    public void canRequestNearby(){
        System.out.println(sut.requestNearbyPlaces(new NearbyPlaceRequest(2.17403, 41.40338, "distance")));
    }
}
