package com.shotgun.viewserver.maps;

/**
 * Created by Gbemiga on 15/12/17.
 */


public class MapsControllerKey{
    private String key;
    private boolean supportsReverveGeocoding;

    public MapsControllerKey(String key, boolean supportsReverveGeocoding) {
        this.key = key;
        this.supportsReverveGeocoding = supportsReverveGeocoding;
    }

    public String getKey() {
        return key;
    }

    public boolean isSupportsReverseGeocoding() {
        return supportsReverveGeocoding;
    }
}
