package com.shotgun.viewserver.delivery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Delivery {
    private String deliveryId;
    private String driverId;
    private Date from;
    private Date till;
    private Date created;
    private int distance;
    private int duration;
    private DeliveryAddress origin;
    private DeliveryAddress destination;
    private int numRequiredForOffload;
    private boolean isFixedPrice;
    private int fixedPriceValue;

    public Delivery() {
        origin = new DeliveryAddress();
        destination = new DeliveryAddress();
    }



    public Date getFrom() {
        return from;
    }

    public Date getTill() {
        return till;
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

    public void setTill(Date till) {
        this.till = till;
    }

    public void setFrom(Date from) {
        this.from = from;
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

    public int getNumRequiredForOffload() {
        return numRequiredForOffload;
    }

    public void setNumRequiredForOffload(int numRequiredForOffload) {
        this.numRequiredForOffload = numRequiredForOffload;
    }

    public boolean getIsFixedPrice() {
        return isFixedPrice;
    }

    public void setIsFixedPrice(boolean fixedPrice) {
        isFixedPrice = fixedPrice;
    }

    public int getFixedPriceValue() {
        return fixedPriceValue;
    }

    public void setFixedPriceValue(int fixedPriceValue) {
        this.fixedPriceValue = fixedPriceValue;
    }
}
