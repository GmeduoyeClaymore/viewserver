package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.order.Product;

import java.util.Date;
import java.util.List;

public class HireOrder{
    Date fromDate;
    int noDays;
    Product hireProduct;
    List<HireOrderFill> responses;
    public class HireOrderFill{
        int filledPrice;
        Date estimatedDate;
    }
}
