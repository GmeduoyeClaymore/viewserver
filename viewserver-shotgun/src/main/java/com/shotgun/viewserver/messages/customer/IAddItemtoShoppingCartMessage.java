package com.shotgun.viewserver.messages.customer;

/**
 * Created by Gbemiga on 13/10/17.
 */
public interface IAddItemtoShoppingCartMessage {
    int getCustomerId();
    int getProductId();
    int quantity();
}
