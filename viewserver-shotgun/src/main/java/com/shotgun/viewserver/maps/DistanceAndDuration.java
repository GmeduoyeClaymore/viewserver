package com.shotgun.viewserver.maps;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;

public class DistanceAndDuration {

    private final int distance;
    private final int duration;

    public DistanceAndDuration(HashMap<String, Object> response) {
        List<String> routes = (List<String>) response.get("routes");
        if(routes== null|| routes.size() == 0){
            throw new RuntimeException("No routes found");
        }
        distance = parseDistance(response);
        duration = parseDuration(response);
    }

    public DistanceAndDuration(int distance, int duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public int getDistance() {
        return distance;
    }

    public int getDuration() {
        return duration;
    }

    private int parseDuration(HashMap<String, Object> response) {
        throw new NotImplementedException();
    }

    private int parseDistance(HashMap<String, Object> response) {
        throw new NotImplementedException();
        //return 0;
    }
}
