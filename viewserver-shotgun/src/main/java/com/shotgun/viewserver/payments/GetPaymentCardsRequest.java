package com.shotgun.viewserver.payments;

public class GetPaymentCardsRequest{
    String customerToken;

    public String getCustomerToken() {
        return customerToken;
    }

    public void setCustomerToken(String customerToken) {
        this.customerToken = customerToken;
    }
}
