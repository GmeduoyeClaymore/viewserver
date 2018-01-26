package com.shotgun.viewserver.maps;

import java.util.ArrayList;

public class DirectionRequest{
    private String mode;
    private LatLng[] locations;

    public DirectionRequest() {
    }

    public DirectionRequest(LatLng[] locations, String mode) {
        this.locations = locations;
        this.mode = mode;
    }

    public LatLng[] getLocations() {
        return locations;
    }

    public void setLocations(LatLng[] locations) {
        this.locations = locations;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String toQueryString(String key){
        LatLng origin = locations[0];
        LatLng destination = locations[locations.length-1];

        return String.format("key=%s&origin=%s,%s&destination=%s,%s&mode=%s%s", key, origin.getLatitude(), origin.getLongitude(), destination.getLatitude(), destination.getLongitude(), this.mode, getWayPoints());
    }

    private String getWayPoints(){
        StringBuilder sb = new StringBuilder();

        for(int i=1;i<locations.length-1; i++){
            sb.append(String.format("%s,%s|", locations[i].getLatitude(), locations[i].getLongitude()));
        }

        return sb.length() > 0 ? "&waypoints=via:"+sb.toString() : "";
    }

}
