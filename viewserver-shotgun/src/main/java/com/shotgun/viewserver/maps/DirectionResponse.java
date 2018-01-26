package com.shotgun.viewserver.maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionResponse{

    private HashMap<String,Object> parameterValues;
    public DirectionResponse(HashMap<String, Object> parameterValues) {
        this.parameterValues = parameterValues;
    }

    Integer getDistance(){
        Map<String, Object> firstLeg = getFirstLeg();
        Map<String,Object> distanceMap = (Map<String, Object>) firstLeg.get("distance");
        return (Integer) distanceMap.get("value");

    }

    Integer getDuration(){
        Map<String, Object> firstLeg = getFirstLeg();
        Map<String,Object> durationMap = (Map<String, Object>) firstLeg.get("duration");
        return (Integer) durationMap.get("value");
    }

    private Map<String, Object> getFirstLeg() {
        Map<String,Object> routes = (Map<String, Object>) parameterValues.get("routes");
        List legs = (List) routes.get("legs");
        if(legs == null || legs.size() == 0){
            throw new RuntimeException("Unable to find first leg in route");
        }
        return (Map<String, Object>) legs.get(0);
    }
}
