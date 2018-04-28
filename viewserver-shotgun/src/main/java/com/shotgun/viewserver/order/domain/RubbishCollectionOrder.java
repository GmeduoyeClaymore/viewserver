package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.Product;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import java.util.Date;
import java.util.List;

public class RubbishCollectionOrder{
    DeliveryAddress origin;
    String customerId;
    int estimatedCost;
    int noPeopleRequired;

    Product rubbishType;
    Date requiredDate;

    RubbishCollectionOrderFill assignedPartner;
    List<RubbishCollectionOrderFill> responses;

    public class RubbishCollectionOrderFill{
        String partnerId;
        int filledPrice;
        Date estimatedDate;
    }


}

