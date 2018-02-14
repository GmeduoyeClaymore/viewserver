package com.shotgun.viewserver.delivery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dimensions{
    long height;
    long width;
    long length;
    long weight;

    public Dimensions() {
    }

    public Dimensions(long height, long width, long length, long weight) {
        this.height = height;
        this.width = width;
        this.length = length;
        this.weight = weight;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }


    public long getVolumeMetresCubed(){
        return (this.height * this.length * this.width) / 1000000000l;
    }

    @Override
    public String toString() {
        return "Dimensions{" +
                "height=" + height +
                ", width=" + width +
                ", length=" + length +
                ", weight=" + weight +
                '}';
    }
}
