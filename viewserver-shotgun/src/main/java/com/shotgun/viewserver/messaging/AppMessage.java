package com.shotgun.viewserver.messaging;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import javax.naming.event.ObjectChangeListener;
import java.util.HashMap;
import java.util.Map;

public interface AppMessage extends DynamicJsonBackedObject {
    public String getFromUserId();
    public String getToUserId();
    public String getTitle();
    public String getBody();
    public String getSound();
    public String getTo();
    public String getPriority();
    public String getAction();
    public String getPicture();

    default String toSimpleMessage() {
        HashMap<String, Object> body = new HashMap<String, Object>();
        HashMap<String, Object> notification = new HashMap<>();
        notification.put("title", getTitle());
        notification.put("body", getBody());

        HashMap<String, Object> customNotificationBody = new HashMap<>();
        customNotificationBody.put("title", getTitle());
        customNotificationBody.put("body", getBody());
        customNotificationBody.put("sound", getSound());
        customNotificationBody.put("picture", getPicture());
        customNotificationBody.put("image", getPicture());
        customNotificationBody.put("big_picture", getPicture());
        customNotificationBody.put("click_action", getAction());
        customNotificationBody.put("icon", "ic_notif");
        customNotificationBody.put("priority", getPriority());
        customNotificationBody.put("show_in_foreground", true);

       /* HashMap<String, Object> alert = new HashMap<>();
        alert.put("alert", notification);
        HashMap<String, Object> apns = new HashMap<>();
        apns.put("payload", alert);
        body.put("apns", apns);*/

        body.put("notification", notification); //this breaks Android messages //this is needed for IOS messages can you explain how it breaks android messages
        HashMap<String, Object> customNotification = new HashMap<>();
        customNotification.put("custom_notification", customNotificationBody);
        body.put("data", customNotification);
        body.put("to", getTo());
        return ControllerUtils.toString(body);
    }

}
