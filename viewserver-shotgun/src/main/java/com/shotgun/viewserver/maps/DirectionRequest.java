package com.shotgun.viewserver.maps;

import java.net.URLEncoder;

public class DirectionRequest{
    private String origin;
    private String destination;
    private String mode;

    public DirectionRequest() {
    }

    public DirectionRequest(String origin, String destination, String mode) {
        this.origin = origin;
        this.destination = destination;
        this.mode = mode;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String toQueryString(String key){
        return String.format("key=%s&origin=%s&destination=%s&mode=%s",key, URLEncoder.encode(this.origin),URLEncoder.encode(this.destination),this.mode);
    }

}
