import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotgun.viewserver.delivery.Delivery;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.maps.*;
import com.shotgun.viewserver.order.OrderController;
import com.shotgun.viewserver.order.OrderItem;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Gbemiga on 15/12/17.
 */
public class OrderControllerTest {

    private OrderController sut;


    @Before
    public void createSut(){
        sut = new OrderController();
    }

    @Test
    public void jsonTest() throws IOException {
        String json = "{\"noRequiredForOffload\":0,\"vehicleTypeId\":\"12323232\"}";
        ObjectMapper om = new ObjectMapper();
        Delivery del = om.readValue(json, Delivery.class);
        System.out.println(del.toString());
    }

    @Test
    public void canCreateOrder(){

        Delivery delivery = new Delivery(){{
            setDeliveryId("DEL1234");
            setDriverId("DRIV567");
            setEta(new Date());
            setNoRequiredForOffload(0);
            setVehicleTypeId("VEH1234");
         /*   setOrigin(new DeliveryAddress() {{
                setLine1("ORIGINLINE1");
                setCity("City");
                setPostCode("PostCode");
                setLatitude(0.0);
                setLongitude(0.0);
                setDefault(false);
            }});
            setDestination(new DeliveryAddress() {{
                setLine1("DESTINATIONLINE1");
                setCity("City");
                setPostCode("PostCode");
                setLatitude(0.0);
                setLongitude(0.0);
                setDefault(false);
            }});*/
        }};

        OrderItem orderItem = new OrderItem(){{
            setProductId("PROD1234");
            setNotes("this is the notes");
        }};


     //  System.out.println( sut.createOrder("USER123", "PAYMENT456", delivery, Arrays.asList(orderItem)));
    }
}
