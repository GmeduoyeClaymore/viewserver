package com.shotgun.viewserver.constants;

import com.shotgun.viewserver.order.types.TransitionEnumBase;

import java.util.ArrayList;
import java.util.List;

public enum OrderStatus implements TransitionEnumBase<OrderStatus> {
    PLACED,
    ACCEPTED,
    INPROGRESS,
    COMPLETED,
    CANCELLED;

    static {
        PLACED.to(ACCEPTED, CANCELLED);
        ACCEPTED.to(INPROGRESS, PLACED, CANCELLED, COMPLETED);
        INPROGRESS.to(COMPLETED, CANCELLED);
    }

    List<OrderStatus> permittedFrom = new ArrayList<>();
    List<OrderStatus> permittedTo = new ArrayList<>();

    @Override
    public OrderStatus getStatus() {
        return this;
    }

    @Override
    public List<OrderStatus> getPermittedFrom() {
        return permittedFrom;
    }

    @Override
    public List<OrderStatus> getPermittedTo() {
        return permittedTo;
    }
}

