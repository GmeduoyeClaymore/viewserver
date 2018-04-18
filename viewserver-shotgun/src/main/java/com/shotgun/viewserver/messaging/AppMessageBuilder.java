package com.shotgun.viewserver.messaging;

import java.util.HashMap;

/**
 * Created by Gbemiga on 17/01/18.
 */
public class AppMessageBuilder {

    AppMessage message;

    public AppMessageBuilder() {
        message = new AppMessage();
    }

    public AppMessageBuilder withFromTo(String from,String to){
        message.setToUserId(to);
        message.setFromUserId(from);
        return this;
    }

    public AppMessageBuilder withDefaults(){
        message.setSound("default");
        message.setPriority("high");
        return this;
    }

    public AppMessageBuilder to(String token){
        message.setTo(token);
        return this;
    }

    public AppMessageBuilder withAction(String action){
        message.setAction(action);
        return this;
    }

    public AppMessageBuilder message(String title,String body){
        message.setTitle(title);
        message.setBody(body);
        return this;
    }

    public AppMessage build(){
        return message;
    }
}
