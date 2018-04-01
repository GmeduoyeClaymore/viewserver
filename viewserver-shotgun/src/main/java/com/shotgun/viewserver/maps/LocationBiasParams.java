package com.shotgun.viewserver.maps;

import com.shotgun.viewserver.user.User;

public class LocationBiasParams {
    public static String toQueryString(Double lat, Double longitude) {
        return String.format("location=%s,%s&radius=50",lat,longitude);
    }
}
