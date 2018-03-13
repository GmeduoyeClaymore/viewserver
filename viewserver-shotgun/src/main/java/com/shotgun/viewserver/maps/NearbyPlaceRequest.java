package com.shotgun.viewserver.maps;

public class NearbyPlaceRequest{
    private double latitude;
    private double longitude;
    private String rankby;

    public NearbyPlaceRequest() {
    }

    public NearbyPlaceRequest(double longitude, double latitude, String rankby) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.rankby = rankby;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getRankby() {
        return rankby;
    }

    public void setRankby(String rankby) {
        this.rankby = rankby;
    }

    public String toQueryString(String key,boolean advanced){
        String locationParam = advanced ? "latlng" : "location";
        return String.format("%s=%s,%s&key=%s&rankby=%s",locationParam,this.latitude,this.longitude,key,this.rankby);
    }

    @Override
    public String toString() {
        return "NearbyPlaceRequest{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", rankby='" + rankby + '\'' +
                '}';
    }
}
