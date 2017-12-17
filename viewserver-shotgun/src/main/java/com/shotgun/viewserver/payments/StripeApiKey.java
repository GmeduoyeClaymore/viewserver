package com.shotgun.viewserver.payments;

/**
 * Created by Gbemiga on 17/12/17.
 */
public class StripeApiKey {

    private String publicKey;
    private String privateKey;

    public StripeApiKey(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }
}
