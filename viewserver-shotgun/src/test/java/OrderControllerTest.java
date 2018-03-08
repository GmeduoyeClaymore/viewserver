/*
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotgun.viewserver.delivery.Delivery;
import com.shotgun.viewserver.order.OrderController;
import io.viewserver.controller.ControllerRegistration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class OrderControllerTest {

    private OrderController sut;


    @Before
    public void createSut(){
        sut = new OrderController(rowUpdater, null,null,null,null);
    }

    @Test
    public void jsonTest() throws IOException {
        String json = "{\"noRequiredForOffload\":0,\"vehicleTypeId\":\"12323232\",\"xxxvehicleTypeId\":\"12323232\"}";
        ObjectMapper om = new ObjectMapper();
        Delivery del = om.readValue(json, Delivery.class);
        System.out.println(del.toString());
    }

    @Test
    public  void canDeserializeParam() throws NoSuchMethodException {

        String param = "{\"userId\":\"2BBui\",\"totalPrice\":\"1000\",\"paymentId\":\"card_1BZjOtJf2h7PvwlulrG7Lrbx\",\"delivery\":{\"eta\":\"2018-01-19T03:54:00.000Z\",\"noRequiredForOffload\":0,\"origin\":{\"deliveryAddressId\":\"22002c3a-6920-47e9-b9e4-d5e44daf60ee\",\"userId\":\"2BBui\",\"created\":0,\"lastUsed\":0,\"line1\":\"12 Kinnoul Road\",\"city\":\"London\",\"postCode\":\"SE12 4RT\",\"latitude\":51.4857236,\"longitude\":-0.2123406,\"rank\":0,\"key\":0,\"rowId\":0},\"destination\":{\"deliveryAddressId\":\"2ec23526-e8f4-4566-bb55-25c1b2877d98\",\"userId\":\"2BBui\",\"created\":0,\"lastUsed\":0,\"line1\":\"129 Drakefield Road\",\"city\":\"London\",\"postCode\":\"SW17 8RS\",\"latitude\":51.4341614,\"longitude\":-0.1523323,\"rank\":1,\"key\":1,\"rowId\":1}},\"orderItems\":[{\"quantity\":1,\"productId\":\"PROD_Delivery\",\"notes\":\"ddddd\"}]}";
        ControllerRegistration reg = new ControllerRegistration(sut);
        System.out.println(TestControllerUtils.invoke(reg, "createOrder", param));
    }
}
*/
