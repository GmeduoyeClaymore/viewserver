package com.shotgun.viewserver.delivery;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ProductKey{
    @JsonCreator
    public ProductKey(String key) {
        this.key = key;
    }
    public String key;

    @Override
    public String toString() {
        return key;
    }
}
