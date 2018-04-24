package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.order.Product;

import java.util.Date;
import java.util.List;

public class PersonellOrder{
    enum OrderType{
        PRICE,
        DAY_RATE
    }
    enum States{
        REQUESTED,
        RESPONDED,
        ASSIGNED,
        INPROGRESS,
        PARTNERCOMPLETE,
        CUSTOMERCOMPLETE,
    }

    public class PersonellOrderFill{
        public UserKey partnerId;
        public int filledPrice;
        public Date estimatedDate;
    }

    public DeliveryAddress origin;
    public int estimatedCost;
    public int noPeopleRequired;

    public ProductKey requiredWorkerType;
    public Date requiredDate;
    public OrderType orderType;
    public States orderState;

    public List<PersonellOrderFill> responses;
    public PersonellOrderFill assignedPartner;
    public List<PaymentStage> stage;
}




