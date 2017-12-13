package com.shotgun.viewserver;

public class CreatePaymentCustomerResponse {
    String customerId;
    String paymentToken;

    public CreatePaymentCustomerResponse(String customerId, String paymentToken) {
        this.customerId = customerId;
        this.paymentToken = paymentToken;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
}
