import com.fasterxml.jackson.databind.Module;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.domain.DeliveryOrder;
import com.shotgun.viewserver.order.domain.OrderPaymentStage;
import com.shotgun.viewserver.order.domain.SinglePaymentOrder;
import com.shotgun.viewserver.order.domain.StagedPaymentOrder;
import com.shotgun.viewserver.order.types.NegotiationResponse;
import com.shotgun.viewserver.servercomponents.OrderSerializationModule;
import com.shotgun.viewserver.user.User;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.server.steps.TestUtils;
import io.viewserver.util.dynamic.ClassInterpreter;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import io.viewserver.util.dynamic.MethodInfo;
import org.junit.Assert;
import org.junit.Test;
import rx.Emitter;
import rx.Observable;
import rx.Observer;
import rx.observables.AsyncOnSubscribe;
import rx.observables.SyncOnSubscribe;
import rx.subjects.PublishSubject;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UtilsTests {
    @Test
    public void Can_deserialize_date_from_string(){
        System.out.println(JacksonSerialiser.getInstance().deserialise("\"2018-04-26T05:01:34.498+01:00\"", Date.class));
    }


    interface TestInterface extends DynamicJsonBackedObject{
        DeliveryAddress getOrigin();
        DeliveryAddress getDestination();
    }


    @Test
    public void Can_timeout_observable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10000);

        PublishSubject subject = PublishSubject.create();

        new Thread(){
            @Override
            public void run() {
                while (true){
                    System.out.println("Nexting");
                    subject.onNext(null);
                    try {
                        System.out.println("Sleeping");
                        Thread.sleep(8000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Nexting");
                    subject.onNext(null);
                }
            }
        }.start();


        Observable result = Observable.concat(Observable.create(subscriber -> {
            int i=0;
            while(i++ < 10){
                subscriber.onNext(i);
            }
            subscriber.onCompleted();
        }),subject);

        Thread.sleep(4000);

        System.out.println("Now subscribing");
        result.timeout(5,TimeUnit.SECONDS).subscribe(
                ne -> {
                    System.out.println("Now nexting");
                    latch.countDown();
                },
                err -> System.err.println(err)
        );

        latch.await();

    }


    @Test
    public void Can_deserialize(){
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
        String json = "{\"origin\":{\"deliveryAddressId\":\"918ba7b9-730c-4d63-a884-3cc0b1ff869d\",\"isDefault\":false,\"line1\":\"12 Kinnoul Rd\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EikxMiBLaW5ub3VsIFJkLCBIYW1tZXJzbWl0aCwgTG9uZG9uIFc2LCBVSw\",\"latitude\":51.4857236,\"longitude\":-0.2123406,\"created\":\"2018-04-25T05:34:50.253+01:00\"},\"destination\":{\"deliveryAddressId\":\"c3728793-b47c-4b04-a99d-aa4c446c392e\",\"isDefault\":false,\"line1\":\"129 Drakefield Road\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL\",\"latitude\":51.4341614,\"longitude\":-0.1523323,\"created\":\"2018-04-25T05:34:50.252+01:00\"},\"customerId\":\"{param_customerId}\",\"estimatedCost\":0,\"noPeopleRequired\":0,\"orderProduct\":\"SmallVan\",\"requiredDate\":\"2018-04-26T05:34:44.096+01:00\",\"actualDistance\":0,\"actualDuration\":0,\"status\":\"REQUESTED\"}";

        String json2 = "{\"origin\":{\"deliveryAddressId\":\"ffe53e71-7e3b-4532-8261-995de88cc744\",\"isDefault\":false,\"line1\":\"12 Kinnoul Rd\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EikxMiBLaW5ub3VsIFJkLCBIYW1tZXJzbWl0aCwgTG9uZG9uIFc2LCBVSw\",\"latitude\":51.4857236,\"longitude\":-0.2123406,\"created\":\"2018-04-25T05:46:15.672+01:00\"},\"destination\":{\"deliveryAddressId\":\"3e0cfaad-b239-47e1-9d7b-31af4653f6c4\",\"isDefault\":false,\"line1\":\"129 Drakefield Road\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL\",\"latitude\":51.4341614,\"longitude\":-0.1523323,\"created\":\"2018-04-25T05:46:15.617+01:00\"},\"customerId\":\"{param_customerId}\",\"estimatedCost\":0,\"noPeopleRequired\":0,\"orderProduct\":\"SmallVan\",\"requiredDate\":\"2018-04-26T05:45:47.746+01:00\",\"actualDistance\":0,\"actualDuration\":0,\"status\":\"REQUESTED\"}";
        TestInterface object = JSONBackedObjectFactory.create(json, TestInterface.class);
        System.out.println(object.getOrigin());
        System.out.println(object.getDestination());
        Assert.assertNotNull(object);

        Assert.assertNotNull(ControllerUtils.mapDefault(json));
    }


    @Test
    public void Can_deserialize_delivery_order(){
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
        String json = TestUtils.getJsonStringFromFile("json/bugs/deliveryOrder_withLongDates.json");
        DeliveryOrder object = JSONBackedObjectFactory.create(json, DeliveryOrder.class);
        System.out.println(object.getOrigin().getCreated());
        System.out.println(object.getDestination().getCreated());
        Assert.assertNotNull(object);

        Assert.assertNotNull(ControllerUtils.mapDefault(json));
    }


    @Test
    public void Can_deserialize_date(){
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
        Assert.assertNotNull(JacksonSerialiser.getInstance().deserialise("2018-04-27T22:14:57.027+01:00",Date.class));
    }
    @Test
    public void Can_serialize(){
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
        String json = "{\"origin\":{\"deliveryAddressId\":\"918ba7b9-730c-4d63-a884-3cc0b1ff869d\",\"isDefault\":false,\"line1\":\"12 Kinnoul Rd\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EikxMiBLaW5ub3VsIFJkLCBIYW1tZXJzbWl0aCwgTG9uZG9uIFc2LCBVSw\",\"latitude\":51.4857236,\"longitude\":-0.2123406,\"created\":\"2018-04-25T05:34:50.253+01:00\"},\"destination\":{\"deliveryAddressId\":\"c3728793-b47c-4b04-a99d-aa4c446c392e\",\"isDefault\":false,\"line1\":\"129 Drakefield Road\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL\",\"latitude\":51.4341614,\"longitude\":-0.1523323,\"created\":\"2018-04-25T05:34:50.252+01:00\"},\"customerId\":\"{param_customerId}\",\"estimatedCost\":0,\"noPeopleRequired\":0,\"orderProduct\":\"SmallVan\",\"requiredDate\":\"2018-04-26T05:34:44.096+01:00\",\"actualDistance\":0,\"actualDuration\":0,\"status\":\"REQUESTED\"}";

        String json2 = "{\"origin\":{\"deliveryAddressId\":\"ffe53e71-7e3b-4532-8261-995de88cc744\",\"isDefault\":false,\"line1\":\"12 Kinnoul Rd\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EikxMiBLaW5ub3VsIFJkLCBIYW1tZXJzbWl0aCwgTG9uZG9uIFc2LCBVSw\",\"latitude\":51.4857236,\"longitude\":-0.2123406,\"created\":\"2018-04-25T05:46:15.672+01:00\"},\"destination\":{\"deliveryAddressId\":\"3e0cfaad-b239-47e1-9d7b-31af4653f6c4\",\"isDefault\":false,\"line1\":\"129 Drakefield Road\",\"city\":\"London\",\"postCode\":\"W6\",\"country\":\"United Kingdom\",\"googlePlaceId\":\"EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL\",\"latitude\":51.4341614,\"longitude\":-0.1523323,\"created\":\"2018-04-25T05:46:15.617+01:00\"},\"customerId\":\"{param_customerId}\",\"estimatedCost\":0,\"noPeopleRequired\":0,\"orderProduct\":\"SmallVan\",\"requiredDate\":\"2018-04-26T05:45:47.746+01:00\",\"actualDistance\":0,\"actualDuration\":0,\"status\":\"REQUESTED\"}";
        TestInterface object = JSONBackedObjectFactory.create(json, TestInterface.class);
        System.out.println(object.getOrigin());
        System.out.println(object.getDestination());

        Assert.assertNotNull(object.serialize("origin"));
        Assert.assertNotNull(ControllerUtils.mapDefault(json));
    }


    @Test
    public void desr_ser_test(){
        OrderSerializationModule orderSerializationModule = new OrderSerializationModule();
        orderSerializationModule.registerDynamicClass(NegotiationResponse.class);
        orderSerializationModule.registerDynamicClass(DynamicJsonBackedObject.class);
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        orderSerializationModule
                }
        );

        DeliveryOrder order = JSONBackedObjectFactory.create(DeliveryOrder.class);
        order.respond("foo", new Date(), 1);
        System.out.println(order.serialize());
        NegotiationResponse[] response  = order.getResponses();
        Assert.assertNotNull(response);
    }


    interface TestUnit extends DynamicJsonBackedObject{
        String getName();
    }
    interface TestUnitHolder extends DynamicJsonBackedObject{
        TestUnit[] getHolders();
    }
    @Test
    public void Can_de_serialize_generic_list(){
        OrderSerializationModule orderSerializationModule = new OrderSerializationModule();
        orderSerializationModule.registerDynamicClass(TestUnit.class);
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        orderSerializationModule
                }
        );
        String json = "{\"holders\":[{\"name\":\"foo\"},{\"name\":\"bar\"}]}";
        TestUnitHolder object = JSONBackedObjectFactory.create(json, TestUnitHolder.class);

        Assert.assertTrue(object.getHolders().length > 0);
        Assert.assertTrue("Holder type is " + object.getHolders()[0].getClass(),object.getHolders()[0] instanceof TestUnit);
    }


    @Test
    public void Can_add_payment_stage_to_order(){
        OrderSerializationModule orderSerializationModule = new OrderSerializationModule();
        orderSerializationModule.registerDynamicClass(TestUnit.class);
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        orderSerializationModule
                }
        );
        StagedPaymentOrder object = JSONBackedObjectFactory.create(StagedPaymentOrder.class);
        object.addPaymentStage(1,"Foo","Foo", OrderPaymentStage.PaymentStageType.Fixed, OrderPaymentStage.PaymentStageStatus.Started, true);
        Assert.assertNotNull(object.serialize());
    }


    @Test
    public void Can_create_response(){
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
        String json = "{\"date\":\"2018-04-27T13:56:59.808+01:00\",\"partnerId\":\"foo\",\"responseStatus\":\"RESPONDED\"}";
        System.out.println(JSONBackedObjectFactory.create(json, NegotiationResponse.class));
    }

    @Test
    public void Can_reserialized_user(){
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
        String jsonStringFromFile = TestUtils.getJsonStringFromFile("json/users/allRounderUser.json");
        User user = JSONBackedObjectFactory.create(jsonStringFromFile, User.class);
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getSelectedContentTypes());
        user = JSONBackedObjectFactory.create(user.serialize(), User.class);
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getSelectedContentTypes());
        System.out.println(user.getSelectedContentTypes());
    }

    @Test
    public void Can_get_default_method() {
        Set<Method> allMethods = ClassInterpreter.getAllMethods(SinglePaymentOrder.class);
        Method method  = allMethods.stream().filter(c-> c.getName().equals("getAmount")).findFirst().get();
        Assert.assertNotNull(method);
        Assert.assertTrue(MethodInfo.forMethod(method).isDefault());
    }



}


