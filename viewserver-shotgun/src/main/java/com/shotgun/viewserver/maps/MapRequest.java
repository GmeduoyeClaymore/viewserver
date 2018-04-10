package com.shotgun.viewserver.maps;

import com.shotgun.viewserver.user.User;

import java.net.URLEncoder;

/**
 * Created by Gbemiga on 15/12/17.
 */
public class MapRequest {
    private String input;
    private String language;
    private Double lat;
    private Double lng;

    public MapRequest() {
    }

    public MapRequest(String input,String language) {
        this.input = input;
        this.language = language;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String toQueryString(String key){
        return "input="+ URLEncoder.encode(this.getInput())+"&types=address&key="+key+"&language="+this.getLanguage()+"&components=country%3Auk&" + LocationBiasParams.toQueryString(lat,lng);
    }
}


