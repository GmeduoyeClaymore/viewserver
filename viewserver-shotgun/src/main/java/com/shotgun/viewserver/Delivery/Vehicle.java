package com.shotgun.viewserver.delivery;

public  class Vehicle{
    private Dimensions dimensions;
    String vehicleId;
    String registrationNumber;
    String colour;
    String make;
    String model;
    String bodyStyle;
    Integer numAvailableForOffload;

    public Vehicle() {
    }

    public Vehicle(Dimensions dimensions,String make, String model, String bodyStyle,String colour, String reg) {
        this.dimensions = dimensions;
        this.model = model;
        this.make = make;
        this.bodyStyle = bodyStyle;
        this.colour = colour;
        this.registrationNumber = reg;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public Integer getNumAvailableForOffload() {
        return numAvailableForOffload;
    }

    public void setNumAvailableForOffload(Integer numAvailableForOffload) {
        this.numAvailableForOffload = numAvailableForOffload;
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
                "dimensions=" + dimensions +
                ", vehicleId='" + vehicleId + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", colour='" + colour + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", bodyStyle='" + bodyStyle + '\'' +
                '}';
    }
}
