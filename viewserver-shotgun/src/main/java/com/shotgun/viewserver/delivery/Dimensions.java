package com.shotgun.viewserver.delivery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dimensions{
    private double volume;
    long weight;

    public Dimensions() {
    }

    public Dimensions(double volume, long weight) {
        this.volume = volume;
        this.weight = weight;
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


    @Override
    public String toString() {
        return "Dimensions{" +
                "volume=" + volume +
                ", weight=" + weight +
                '}';
    }
}
