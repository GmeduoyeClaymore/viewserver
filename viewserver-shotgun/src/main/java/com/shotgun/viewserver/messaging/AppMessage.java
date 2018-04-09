package com.shotgun.viewserver.messaging;

import com.shotgun.viewserver.ControllerUtils;

import javax.naming.event.ObjectChangeListener;
import java.util.HashMap;
import java.util.Map;

public class AppMessage {
    private String to;
    private String priority;
    private String title;
    private String body;
    private String sound;
    private String action;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public String toAndroidMessage() {
        HashMap<String, Object> body = new HashMap<String, Object>();
        HashMap<String, Object> customNotificationBody = new HashMap<>();
        customNotificationBody.put("title", getTitle());
        customNotificationBody.put("body", getBody());
        customNotificationBody.put("sound", getSound());
        customNotificationBody.put("click_action", getAction());
        customNotificationBody.put("priority", getPriority());
        customNotificationBody.put("show_in_foreground", true);

        HashMap<String, Object> customNotification = new HashMap<>();
        customNotification.put("custom_notification", customNotificationBody);

        body.put("data", customNotification);
        body.put("to", getTo());
        return ControllerUtils.toString(body);
    }

    public String toSimpleMessage() {
        HashMap<String, Object> body = new HashMap<String, Object>();
        HashMap<String, Object> customNotificationBody = new HashMap<>();
        customNotificationBody.put("title", getTitle());
        customNotificationBody.put("body", getBody());
        customNotificationBody.put("sound", getSound());
        customNotificationBody.put("click_action", getAction());
        customNotificationBody.put("priority", getPriority());
        customNotificationBody.put("show_in_foreground", true);
        body.put("notification", customNotificationBody);
        HashMap<String, Object> customNotification = new HashMap<>();
        customNotification.put("custom_notification", customNotificationBody);
        body.put("data", customNotification);
        body.put("to", getTo());
        return ControllerUtils.toString(body);
    }
    @Override
    public String toString() {
        return "AppMessage{" +
                "to='" + to + '\'' +
                ", priority=" + priority +
                ", title='" + title + '\'' +
                ", action='" + action + '\'' +
                ", body='" + body + '\'' +
                ", sound='" + sound + '\'' +
                '}';
    }
}
