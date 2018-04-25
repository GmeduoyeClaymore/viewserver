package com.shotgun.viewserver.delivery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Delivery {
    private String deliveryId;
    private String driverId;
    private Date created;
    private int distance;
    private int duration;
    private DeliveryAddress origin;
    private DeliveryAddress destination;

    public Delivery() {
        origin = new DeliveryAddress();
        destination = new DeliveryAddress();
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
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
