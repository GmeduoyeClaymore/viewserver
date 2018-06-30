package com.shotgun.viewserver.images;

import java.net.URI;

public class SystemUri {
    private String extension = null;
    private String context;
    private String imageId;
    public SystemUri(URI requestURI) {
        String url = requestURI.toString();
        context = getContextFromUri(url);
        String imageIdWithExtension = getImageIdFromUri(url);
        int indexOfPeriod = imageIdWithExtension.indexOf(".");
        if(indexOfPeriod >-1){
            imageId = imageIdWithExtension.substring(0, indexOfPeriod);
            extension = imageIdWithExtension.substring(indexOfPeriod);
        }else{
            imageId = imageIdWithExtension;
        }
    }

    public String getExtension() {
        return extension;
    }

    private String getContextFromUri(String requestURI) {
        return getPartIndex(requestURI, 1);
    }

    private String getImageIdFromUri(String requestURI) {
        return getPartIndex(requestURI, 2);
    }

    private String getPartIndex(String requestURI, int idx) {
        String[] parts = requestURI.split("/");
        if(parts.length != 3){
            throw new RuntimeException("Unexpected URI format - " + requestURI);
        }
        return parts[idx];
    }

    public SystemUri(String context, String imageId) {
        this.context = context;
        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }

    public String toString() {
        return String.format("mongo://%s/%s",context,imageId);
    }
}
