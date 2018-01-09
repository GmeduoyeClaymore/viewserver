package com.shotgun.viewserver.delivery;

/**
 * Created by Gbemiga on 09/01/18.
 */
public class VehicleDetails {
    private Dimensions dimensions;
    private String model;
    private String color;
    private String reg;

    public VehicleDetails() {
    }

    public VehicleDetails(Dimensions dimensions, String model, String color, String reg) {
        this.dimensions = dimensions;
        this.model = model;
        this.color = color;
        this.reg = reg;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getReg() {
        return reg;
    }

    public void setReg(String reg) {
        this.reg = reg;
    }

    @Override
    public String toString() {
        return "VehicleDetails{" +
                "dimensions=" + dimensions +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                ", reg='" + reg + '\'' +
                '}';
    }
}

