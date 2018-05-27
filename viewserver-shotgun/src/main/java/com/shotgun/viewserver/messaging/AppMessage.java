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

    default String toSimpleMessage() {
        HashMap<String, Object> body = new HashMap<String, Object>();
        HashMap<String, Object> customNotificationBody = new HashMap<>();
        customNotificationBody.put("title", getTitle());
        customNotificationBody.put("body", getBody());
        customNotificationBody.put("sound", getSound());
        customNotificationBody.put("click_action", getAction());
        customNotificationBody.put("icon", "ic_notif");
        customNotificationBody.put("priority", getPriority());
        customNotificationBody.put("show_in_foreground", true);
        body.put("notification", customNotificationBody); //this breaks Android messages //this is needed for IOS messages can you explain how it breaks android messages
        HashMap<String, Object> customNotification = new HashMap<>();
        customNotification.put("custom_notification", customNotificationBody);
        body.put("data", customNotification);
        body.put("to", getTo());
        return ControllerUtils.toString(body);
    }
}
