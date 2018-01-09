package com.shotgun.viewserver.delivery;

/**
 * Created by Gbemiga on 09/01/18.
 */
public class VehicleDetailsQuery {
    String regNo;

    public VehicleDetailsQuery(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String toQueryString(String key){
        return String.format("v=2&api_nullitems=1&auth_apikey=%s&user_tag=&key_VRM=%s",key,regNo);
    }
}
