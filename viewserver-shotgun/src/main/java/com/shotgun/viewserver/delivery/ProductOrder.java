package com.shotgun.viewserver.delivery;

import java.util.Date;
import java.util.List;

public class ProductOrder{

    String customerId;
    int estimatedCost;
    int noPeopleRequired;
    Date requiredDate;
    ProductOrderFill assignedResponse;
    List<ProductOrderItem> productOrderItems;
    List<ProductOrderFill> orderResponses;
    public class ProductOrderFill{
        int filledPrice;
        Date estimatedDate;
    }

    public class ProductOrderItem{
        String productKey;
        int quantity;
    }
}
