import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.messaging.MessagingController;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;

/**
 * Created by Gbemiga on 18/01/18.
 */
public class MessagingControllerTest {
    private MessagingController sut;

    @Before
    public void createSut(){
        sut = new MessagingController(new MessagingApiKey("AAAA43sqrgA:APA91bH1hL-tEDjcfzUNxkiyQyvMOToWaTzH7N1g4r6W9TkMSLsPX7TQV_JoIkXkWpWvthr7C57AS5nHXTLKH0Xbz9pZCQgvDM5LpWmJXGVj-3sa_mmoD407IS3NZJv8iTSxNtHQyxZA"), null);
    }

    @Test
    public void canSendMessage(){
        AppMessageBuilder message = new AppMessageBuilder();
        message.to("2bGY0:APA91bHC74kxY7Qyel_LDVd5URjbW9_Qq4FDEL2bjDQCivjRSAQD1NexVHD2PYPIi349TqNUIqF2wUGqRQd0Twrk854Nl_69gdeQH6GUYIm_4TjVl6FiIAw0EBwcAzPvkBUJYu7Q7gnr");
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
