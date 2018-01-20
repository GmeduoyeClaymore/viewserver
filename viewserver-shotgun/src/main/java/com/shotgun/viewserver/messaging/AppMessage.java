package com.shotgun.viewserver.messaging;

import com.shotgun.viewserver.ControllerUtils;

import javax.naming.event.ObjectChangeListener;
import java.util.HashMap;
import java.util.Map;

public class AppMessage {
    private String to;
    private Integer priority;
    private String title;
    private String body;
    private String sound;
    private HashMap<String,Object> data;

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String toSimpleMessage(){
        HashMap<String,Object> body = new HashMap<String,Object>();
        HashMap<String,Object> notification = new HashMap<String,Object>();
        notification.put("title",getTitle());
        notification.put("body",getBody());
        notification.put("sound",getSound());
        body.put("notification",notification);
        if(this.data != null){
            body.put("data",this.data);
        }
        body.put("to",getTo());
        body.put("priority",getPriority());
        return ControllerUtils.toString(body);
    }


    public String toAndroidMessage(){
        HashMap<String,Object> body = new HashMap<>();
        HashMap<String,Object> data = new HashMap<>();
        if(this.data != null){ //This doesn't  appear to do anything
            for(Map.Entry<String,Object> entry :  this.data.entrySet()){
                data.put(entry.getKey(),entry.getValue());
            }
        }
        HashMap<String,Object> notification = new HashMap<>();
        notification.put("title",getTitle());
        notification.put("body",getBody());
        notification.put("sound",getSound());
        notification.put("priority","high");
        notification.put("show_in_foreground",true);
        data.put("custom_notification",notification);
        body.put("data",data);
        body.put("to",getTo());
        body.put("priority",getPriority());
        return ControllerUtils.toString(body);
    }
}
