package com.shotgun.viewserver.payments;

/**
 * Created by Gbemiga on 17/12/17.
 */
public class StripeApiKey {

    private String publicKey;
    private String privateKey;
    private boolean mockPaymentController;

    public StripeApiKey(String publicKey, String privateKey, boolean mockPaymentController) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.mockPaymentController = mockPaymentController;
    }

    public boolean isMockPaymentController() {
        return mockPaymentController;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }
}
