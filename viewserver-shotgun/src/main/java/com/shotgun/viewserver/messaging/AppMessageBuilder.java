package com.shotgun.viewserver.messaging;

import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.HashMap;

/**
 * Created by Gbemiga on 17/01/18.
 */
public class AppMessageBuilder {

    AppMessage message;

    public AppMessageBuilder() {
        message = JSONBackedObjectFactory.create(AppMessage.class);
    }

    public AppMessageBuilder withFromTo(String from,String to){
        message.set("toUserId",to);
        message.set("fromUserId",from);
        return this;
    }

    public AppMessageBuilder withDefaults(){
        message.set("sound","default");
        message.set("priority","high");
        //message.set("icon", "ic_notif");
        //message.set("show_in_foreground", true);
        return this;
    }

    public AppMessageBuilder to(String token){
        message.set("to",token);
        return this;
    }

    public AppMessageBuilder withAction(String action){
        message.set("action",action);
        return this;
    }

    public AppMessageBuilder message(String title,String body){
        message.set("title",title);
        message.set("body",body);
        return this;
    }

    public AppMessage build(){
        return message;
    }

    public AppMessageBuilder withPicture(String picture) {
        message.set("picture",picture);
        return this;
    }
}
