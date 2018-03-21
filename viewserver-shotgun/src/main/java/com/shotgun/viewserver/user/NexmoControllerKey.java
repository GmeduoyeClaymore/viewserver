package com.shotgun.viewserver.user;

public class NexmoControllerKey{
    private String key;
    private String secret;

    public NexmoControllerKey(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }
}
