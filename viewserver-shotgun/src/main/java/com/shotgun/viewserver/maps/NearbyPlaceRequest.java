package com.shotgun.viewserver.maps;

public class NearbyPlaceRequest{
    private double lattitude;
    private double longtitude;
    private String rankby;

    public NearbyPlaceRequest(double longtitude, double lattitude, String rankby) {
        this.longtitude = longtitude;
        this.lattitude = lattitude;
        this.rankby = rankby;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public String getRankby() {
        return rankby;
    }

    public void setRankby(String rankby) {
        this.rankby = rankby;
    }

    public String toQueryString(String key,boolean advanced){
        String locationParam = advanced ? "latlng" : "location";
        return String.format("%s=%s,%s&key=%s&rankby=%s",locationParam,this.lattitude,this.longtitude,key,this.rankby);
    }

    @Override
    public String toString() {
        return "NearbyPlaceRequest{" +
                "lattitude=" + lattitude +
                ", longtitude=" + longtitude +
                ", rankby='" + rankby + '\'' +
                '}';
    }
}
