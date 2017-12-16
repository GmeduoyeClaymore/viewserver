package com.shotgun.viewserver.maps;

import java.net.URLEncoder;

/**
 * Created by Gbemiga on 15/12/17.
 */
public class MapRequest {
    private String input;
    private String language;

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

    public String toQueryString(String key){
        return "input="+ URLEncoder.encode(this.getInput())+"&key="+key+"&language="+this.getLanguage()+"&types%5B0%5D=address&components=country%3Auk";
    }
}


