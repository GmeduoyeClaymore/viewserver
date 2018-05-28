package com.shotgun.viewserver.messaging;

/**
 * Created by Gbemiga on 17/01/18.
 */
public class MessagingApiKey {
    private String apiKey;
    private boolean blockRemoteSending;
    public MessagingApiKey(String apiKey, boolean blockRemoteSending) {
        this.apiKey = apiKey;
        this.blockRemoteSending = blockRemoteSending;
    }

    public String getApiKey() {
        return apiKey;
    }


    public boolean isBlockRemoteSending() {
        return blockRemoteSending;
    }
}
