package com.shotgun.viewserver.order.types;

public  enum OrderContentType {
    Delivery(1),
    Personell(5),
    Product(6),
    Hire(3),
    Rubbish(2);

    private int contentTypeId;

    OrderContentType(int contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public int getContentTypeId() {
        return contentTypeId;
    }
}


