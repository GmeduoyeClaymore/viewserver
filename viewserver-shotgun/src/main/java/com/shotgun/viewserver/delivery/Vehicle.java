package com.shotgun.viewserver.delivery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

public  class Vehicle{
    private double volume;
    long weight;
    String vehicleId;
    String registrationNumber;
    String colour;
    String make;
    String model;
    String bodyStyle;
    String[] selectedProductIds;

    public Vehicle() {
    }

    public Vehicle(String make, String model, String bodyStyle,String colour, String reg, String[] selectedProductIds, double volume, long weight) {
        this.volume = volume;
        this.weight = weight;
        this.model = model;
        this.make = make;
        this.bodyStyle = bodyStyle;
        this.colour = colour;
        this.registrationNumber = reg;
        this.selectedProductIds = selectedProductIds;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public void setBodyStyle(String bodyStyle) {
        this.bodyStyle = bodyStyle;
    }

    public String[] getSelectedProductIds() {
        return selectedProductIds;
    }

    public void setSelectedProductIds(String[] selectedProductIds) {
        this.selectedProductIds = selectedProductIds;
    }

    public String getBodyStyle() {
        return bodyStyle;
    }

    public void getBodyStyle(String bodyStyle) {
        this.bodyStyle = bodyStyle;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                ", vehicleId='" + vehicleId + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", colour='" + colour + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", bodyStyle='" + bodyStyle + '\'' +
                '}';
    }
}
