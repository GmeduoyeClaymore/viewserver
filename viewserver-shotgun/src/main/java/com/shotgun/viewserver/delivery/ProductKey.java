package com.shotgun.viewserver.delivery;

public class ProductKey{
    public ProductKey(String key) {
        this.key = key;
    }
    public String key;

    @Override
    public String toString() {
        return key;
    }
}
