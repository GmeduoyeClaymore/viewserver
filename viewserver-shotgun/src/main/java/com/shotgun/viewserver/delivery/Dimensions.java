package com.shotgun.viewserver.delivery;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface Dimensions extends DynamicJsonBackedObject{
    Double getVolume();
    Long getWeight();
    default Dimensions with(double volume, long weight){
        this.set("volume",volume);
        this.set("weight",weight);
        return this;
    }
}
