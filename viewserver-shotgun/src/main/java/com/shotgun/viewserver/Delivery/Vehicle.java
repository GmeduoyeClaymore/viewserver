package com.shotgun.viewserver.delivery;

public  class Vehicle{
    private Dimensions dimensions;
    String vehicleId;
    String registrationNumber;
    String colour;
    String make;
    String model;
    String vehicleTypeId;
    Integer numAvailableForOffload;

    public Vehicle() {
    }

    public Vehicle(Dimensions dimensions,String make, String model, String vehicleTypeId,String colour, String reg) {
        this.dimensions = dimensions;
        this.model = model;
        this.make = make;
        this.vehicleTypeId = vehicleTypeId;
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

    public String getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(String vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
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
                ", vehicleTypeId='" + vehicleTypeId + '\'' +
                '}';
    }
}
