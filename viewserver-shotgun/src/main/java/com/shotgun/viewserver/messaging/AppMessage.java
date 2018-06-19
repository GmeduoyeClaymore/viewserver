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
    public String getIcon();
    public String getColor();
    public String getOperatingSystem();

    default String toSimpleMessage() {
        HashMap<String, Object> body = new HashMap<>();

        HashMap<String, Object> notification = new HashMap<>();
        notification.put("title", getTitle());
        notification.put("body", getBody());
        notification.put("sound", getSound());
        notification.put("picture", getPicture());
        notification.put("image", getPicture());
        notification.put("big_picture", getPicture());
        notification.put("click_action", getAction());
        notification.put("icon", getIcon());
        notification.put("color", getColor());
        notification.put("priority", getPriority());
        notification.put("show_in_foreground", true);

        if(getOperatingSystem().isEmpty() || !getOperatingSystem().equals("android")) {
            body.put("notification", notification); //this breaks Android messages so only send if OS is IOS
        }else{
            HashMap<String, Object> customNotification = new HashMap<>();
            customNotification.put("custom_notification", notification);
            body.put("data", customNotification);
        }

        body.put("to", getTo());
        return ControllerUtils.toString(body);
    }

}
