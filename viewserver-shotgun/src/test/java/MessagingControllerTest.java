import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.messaging.MessagingController;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Created by Gbemiga on 18/01/18.
 */
public class MessagingControllerTest {
    private MessagingController sut;

    @Before
    public void createSut(){
        sut = new MessagingController(new MessagingApiKey("AAAA43sqrgA:APA91bH1hL-tEDjcfzUNxkiyQyvMOToWaTzH7N1g4r6W9TkMSLsPX7TQV_JoIkXkWpWvthr7C57AS5nHXTLKH0Xbz9pZCQgvDM5LpWmJXGVj-3sa_mmoD407IS3NZJv8iTSxNtHQyxZA"));
    }

    @Test
    public void canSendMessage(){
        AppMessage message = new AppMessage();
        message.setTo("eBq4b7NZvP4:APA91bEZFoTmj2I555b9Q7bqjyOAH3ajpgnm87mR5TwIoNRwjReWWyYq87b54Q1zTcsh5rRapl5RuZxJ97eiS1sepuCG6SEYdjBrifj0LbFSkp1USGnI-S5yW21HnjSUTl1uHICE3OKl");
        message.setBody("Messaging controller message");
        message.setTitle("Messaging controller title");
        message.setSound("default");
        message.setPriority(10);
        sut.sendMessage(message);
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
