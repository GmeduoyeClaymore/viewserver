package com.shotgun.viewserver.delivery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface Vehicle extends DynamicJsonBackedObject{

    default Vehicle with(String make, String model, String bodyStyle,String colour, String reg, String[] selectedProductIds, double volume, long weight) {
        this.set("volume",volume);
        this.set("weight",weight);
        this.set("model",model);
        this.set("make",make);
        this.set("bodyStyle",bodyStyle);
        this.set("colour",colour);
        this.set("registrationNumber",reg);
        this.set("selectedProductIds",selectedProductIds);
        return this;
    }

    Double getVolume();
    Long getWeight();
    String[] getSelectedProductIds();
    String getBodyStyle();
    String getVehicleId();
    String getRegistrationNumber();
    String getColour();
    String getMake();
    String getModel();
    Dimensions getDimensions();
    Integer getNumAvailableForOffload();
}
