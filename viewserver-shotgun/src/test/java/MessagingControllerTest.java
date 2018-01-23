import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
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
        AppMessageBuilder message = new AppMessageBuilder();
        message.to("fV3ttxSBvFo:APA91bEUBin7eMh-ijCB6WUp79a9kFabnium-CJnXdZKpev1VResdjavWnXfJKzgENXQilMzYlLaG68ebJjnbKQmhPXw9rc2Fd1h8DyWcToDrMjuRao1ixacBe0f6g33ysJAtqhOTFGp");
        message.message("Messaging controller title","Messaging controller message");
        message.withData("orderId","XXXXX");
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
