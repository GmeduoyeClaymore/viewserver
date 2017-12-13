package com.shotgun.viewserver;

import com.stripe.model.Card;

import java.util.List;

public class GetPaymentCardsResponse {
    List<Card> paymentCards;

    public GetPaymentCardsResponse(List<Card> paymentCards) {
        this.paymentCards = paymentCards;
    }

    public List<Card> getPaymentCards() {
        return paymentCards;
    }

    public void setPaymentCards(List<Card> paymentCards) {
        this.paymentCards = paymentCards;
    }
}
