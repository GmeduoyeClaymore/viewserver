package com.shotgun.viewserver.delivery;

import org.joda.time.DateTime;

import java.util.Date;

public class Delivery {
    private String deliveryId;
    private String driverId;
    private Date eta;
    private int noRequiredForOffload;
    private String vehicleTypeId;
    private DeliveryAddress origin;
    private DeliveryAddress destination;

    public Delivery() {
    }

    public Date getEta() {
        return eta;
    }

    public void setEta(Date eta) {
        this.eta = eta;
    }

    public int getNoRequiredForOffload() {
        return noRequiredForOffload;
    }

    public void setNoRequiredForOffload(int noRequiredForOffload) {
        this.noRequiredForOffload = noRequiredForOffload;
    }

    public String getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(String vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public DeliveryAddress getOrigin() {
        return origin;
    }

    public void setOrigin(DeliveryAddress origin) {
        this.origin = origin;
    }

    public DeliveryAddress getDestination() {
        return destination;
    }

    public void setDestination(DeliveryAddress destination) {
        this.destination = destination;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

}
