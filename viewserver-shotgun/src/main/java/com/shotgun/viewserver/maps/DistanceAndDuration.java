package com.shotgun.viewserver.maps;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;

public interface DistanceAndDuration extends DynamicJsonBackedObject{
    static DistanceAndDuration from(HashMap<String, Object> response) {
        List<HashMap> routes = (List<HashMap>) response.get("routes");
        if(routes== null|| routes.size() == 0){
            throw new RuntimeException("No routes found");
        }
        return from(parseLegValue(response, "distance"), parseLegValue(response, "duration"));
    }

    static DistanceAndDuration from(Integer distance, Integer duration) {
        DistanceAndDuration result = JSONBackedObjectFactory.create(DistanceAndDuration.class);
        result.set("distance",distance);
        result.set("duration",duration);
        return result;
    }

    public Integer getDistance();
    public Integer getDuration();

    static Integer parseLegValue(HashMap<String, Object> response, String variableName) {
        List<HashMap> routes = (List<HashMap>) response.get("routes");
        if(routes== null|| routes.size() == 0){
            throw new RuntimeException("No routes found");
        }
        int total = 0;
        for(HashMap route : routes){
            for(HashMap leg : (List<HashMap>)route.get("legs") ){
                total += (Integer)((HashMap)leg.get(variableName)).get("value");
            }
        }
        return total;
    }
}
