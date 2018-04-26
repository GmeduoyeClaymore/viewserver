import com.fasterxml.jackson.databind.Module;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.DeliveryOrder;
import com.shotgun.viewserver.servercomponents.OrderSerializationModule;
import io.viewserver.core.JacksonSerialiser;
import org.apache.commons.beanutils.ConvertUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class UtilsTests {
    @Test
    public void Can_deserialize_date_from_string(){
        System.out.println(JacksonSerialiser.getInstance().deserialise("\"2018-04-26T05:01:34.498+01:00\"", Date.class));
    }

    @Test
    public void Can_deserialize_delivery_order(){
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
        String json = "{\"origin\":{\"deliveryAddressId\":\"918ba7b9-730c-4d63-a884-3cc0b1ff869d\",\"isDefault\":false,\"line1\":\"12 Kinnoul Rd\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EikxMiBLaW5ub3VsIFJkLCBIYW1tZXJzbWl0aCwgTG9uZG9uIFc2LCBVSw\",\"latitude\":51.4857236,\"longitude\":-0.2123406,\"created\":\"2018-04-25T05:34:50.253+01:00\"},\"destination\":{\"deliveryAddressId\":\"c3728793-b47c-4b04-a99d-aa4c446c392e\",\"isDefault\":false,\"line1\":\"129 Drakefield Road\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL\",\"latitude\":51.4341614,\"longitude\":-0.1523323,\"created\":\"2018-04-25T05:34:50.252+01:00\"},\"customerId\":\"{param_customerId}\",\"estimatedCost\":0,\"noPeopleRequired\":0,\"orderProduct\":\"1SmallVan\",\"requiredDate\":\"2018-04-26T05:34:44.096+01:00\",\"actualDistance\":0,\"actualDuration\":0,\"status\":\"REQUESTED\"}";

        String json2 = "{\"origin\":{\"deliveryAddressId\":\"ffe53e71-7e3b-4532-8261-995de88cc744\",\"isDefault\":false,\"line1\":\"12 Kinnoul Rd\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EikxMiBLaW5ub3VsIFJkLCBIYW1tZXJzbWl0aCwgTG9uZG9uIFc2LCBVSw\",\"latitude\":51.4857236,\"longitude\":-0.2123406,\"created\":\"2018-04-25T05:46:15.672+01:00\"},\"destination\":{\"deliveryAddressId\":\"3e0cfaad-b239-47e1-9d7b-31af4653f6c4\",\"isDefault\":false,\"line1\":\"129 Drakefield Road\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL\",\"latitude\":51.4341614,\"longitude\":-0.1523323,\"created\":\"2018-04-25T05:46:15.617+01:00\"},\"customerId\":\"{param_customerId}\",\"estimatedCost\":0,\"noPeopleRequired\":0,\"orderProduct\":\"1SmallVan\",\"requiredDate\":\"2018-04-26T05:45:47.746+01:00\",\"actualDistance\":0,\"actualDuration\":0,\"status\":\"REQUESTED\"}";
        Assert.assertNotNull(JacksonSerialiser.getInstance().deserialise(json, DeliveryOrder.class));
        Assert.assertNotNull(ControllerUtils.mapDefault(json));

    }
}
