package com.shotgun.viewserver.user;

import java.util.Date;

public class User{
    private String userId;
    private Date created;
    private Date lastModified;
    private String firstName;
    private String lastName;
    private String password;
    private Date dob;
    private String contactNo;
    private String email;
    private String type;
    private String stripeCustomerId;
    private String stripeAccountId;
    private String stripeDefaultSourceId;
    private int chargePercentage;
    private String selectedContentTypes;
    private String selectedProducts;
    private String imageUrl;
    private String imageData;

    private String fcmToken;
    private Double latitude;
    private Double longitude;


    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStripeDefaultSourceId() {
        return stripeDefaultSourceId;
    }

    public void setStripeDefaultSourceId(String stripeDefaultSourceId) {
        this.stripeDefaultSourceId = stripeDefaultSourceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public int getChargePercentage() {
        return chargePercentage;
    }

    public void setChargePercentage(int chargePercentage) {
        this.chargePercentage = chargePercentage;
    }

    public void setSelectedContentTypes(String selectedContentTypes) {
        this.selectedContentTypes = selectedContentTypes;
    }

    public String getSelectedContentTypes() {
        return selectedContentTypes;
    }


    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
}
