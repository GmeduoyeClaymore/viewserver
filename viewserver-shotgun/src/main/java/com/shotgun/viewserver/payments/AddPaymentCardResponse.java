package com.shotgun.viewserver.payments;


import com.stripe.model.Card;

import java.util.List;

public class AddPaymentCardResponse{
    String paymentToken;

    public AddPaymentCardResponse(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
}
