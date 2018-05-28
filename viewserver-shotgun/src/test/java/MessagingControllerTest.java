import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.messaging.MessagingController;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Gbemiga on 18/01/18.
 */
public class MessagingControllerTest {
    private MessagingController sut;

    @Before
    public void createSut(){
        sut = new MessagingController(new MessagingApiKey("AAAA43sqrgA:APA91bH1hL-tEDjcfzUNxkiyQyvMOToWaTzH7N1g4r6W9TkMSLsPX7TQV_JoIkXkWpWvthr7C57AS5nHXTLKH0Xbz9pZCQgvDM5LpWmJXGVj-3sa_mmoD407IS3NZJv8iTSxNtHQyxZA"), null, new ICatalog() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public IOutput getOutput() {
                return null;
            }

            @Override
            public ICatalog getParent() {
                return null;
            }

            @Override
            public IExecutionContext getExecutionContext() {
                return null;
            }

            @Override
            public int registerOperator(IOperator operator) {
                return 0;
            }

            @Override
            public IOperator getOperator(String name) {
                return null;
            }

            @Override
            public IOperator getOperatorByPath(String name) {
                return null;
            }

            @Override
            public Observable<IOperator> waitForOperatorAtThisPath(String path) {
                return null;
            }

            @Override
            public void unregisterOperator(IOperator operator) {

            }

            @Override
            public ICatalog createDescendant(String path) {
                return null;
            }

            @Override
            public ICatalog getDescendant(String path) {
                return null;
            }

            @Override
            public void addChild(ICatalog childCatalog) {

            }

            @Override
            public void removeChild(ICatalog childCatalog) {

            }

            @Override
            public Observable<IOperator> waitForOperatorInThisCatalog(String name) {
                return null;
            }

            @Override
            public Observable<ICatalog> waitForChild(String name) {
                return null;
            }

            @Override
            public Collection<IOperator> getAllOperators() {
                return null;
            }

            @Override
            public ICatalog getChild(String name) {
                return null;
            }

            @Override
            public void tearDown() {

            }
        });
    }

    @Test
    public void canSendMessage(){
        AppMessageBuilder message = new AppMessageBuilder();
        message.to("eSyZ9aHA-dU:APA91bEDhkju-uP7mV0AhDX2JFg7lzeJz4gNojGnOy33-Mz3bob7v8W7DFEORYDtR4fmKmFoM9k0MbdVITbpvc2O8S3nzeurfca1MjmnjUJ9jTJoeGSTX4Z395i-5RlXkegcE3LaftKS");
        message.message("Messaging controller title", "Messaging controller message");
        message.withAction("shotgun://order/1234");
        message.withDefaults();
        sut.sendMessage(message.build());
    }
    @Test
    public void canSendCannedGenericMessage(){
        URL resource = getClass().getClassLoader().getResource("mock//generic_message.json");
        if(resource == null){
            throw new RuntimeException("Unable to find mock vehicle details");
        }
        String payload = ControllerUtils.urlToString(resource);
        sut.sendPayload(payload);
    }
    @Test
    public void canSendCannedAndroidMessage(){
        URL resource = getClass().getClassLoader().getResource("mock//android_message.json");
        if(resource == null){
            throw new RuntimeException("Unable to find mock vehicle details");
        }
        String payload = ControllerUtils.urlToString(resource);
        sut.sendPayload(payload);
    }

}
