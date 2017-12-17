package com.shotgun.viewserver.maps;

public class PlaceRequest {
    private String placeid;
    private String language;

    public PlaceRequest() {
    }

    public PlaceRequest(String placeid, String language) {
        this.placeid = placeid;
        this.language = language;
    }

    public String getPlaceid() {
        return placeid;
    }

    public void setPlaceid(String placeid) {
        this.placeid = placeid;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String toQueryString(String key){
        return String.format("key=%s&placeid=%s&language=%s",key,this.placeid,this.language);
    }

}


