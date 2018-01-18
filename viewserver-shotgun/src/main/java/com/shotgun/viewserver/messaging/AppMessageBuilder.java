package com.shotgun.viewserver.messaging;

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

    public AppMessageBuilder message(String title,String body){
        message.setTitle(title);
        message.setBody(body);
        return this;
    }

    public AppMessage build(){
        return message;
    }
}
