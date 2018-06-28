package com.shotgun.viewserver.user;

public class NexmoControllerKey{
    private String domain;
    private String key;
    private String secret;

    public NexmoControllerKey(String domain, String key, String secret) {
        this.domain = domain;
        this.key = key;
        this.secret = secret;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public String getDomain() {
        return domain;
    }
}
