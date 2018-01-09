package com.shotgun.viewserver.delivery;

/**
 * Created by Gbemiga on 09/01/18.
 */
public class VehicleDetailsApiKey {
    private String key;
    private boolean mock;

    public VehicleDetailsApiKey(String key, boolean mock) {
        this.key = key;
        this.mock = mock;
    }

    public String getKey() {
        return key;
    }

    public boolean isMock() {
        return mock;
    }
}
