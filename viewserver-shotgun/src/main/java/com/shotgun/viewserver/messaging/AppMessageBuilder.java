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

    public AppMessageBuilder withDefaults(){
        message.setSound("default");
        message.setPriority(10);
        return this;
    }

    public AppMessageBuilder to(String token){
        message.setTo(token);
        return this;
    }
    public AppMessageBuilder withData(String key,Object value){
        if(message.getData() == null){
           message.setData(new HashMap<>());
        }
        message.getData().put(key, value);
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
