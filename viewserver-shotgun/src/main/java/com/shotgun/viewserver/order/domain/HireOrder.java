package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.types.OrderContentType;
import com.shotgun.viewserver.order.types.OrderEnumBase;
import com.shotgun.viewserver.order.types.TransitionUtils;
import com.shotgun.viewserver.user.User;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public interface HireOrder extends NegotiatedOrder, BasicOrder, StagedPaymentOrder, DynamicJsonBackedObject, SourceOrderForLinkedDeliveries {
    Date getHireStartDate();
    Date getHireEndDate();
    HireOrderStatus getHireOrderStatus();

    @Override
    default OrderContentType getOrderContentType(){
        return OrderContentType.Hire;
    }

    OrderLeg getOrderLeg();
    enum OrderLeg{
        Inbound,
        Outbound
    }

    default HireOrder transitionTo(HireOrderStatus status){
        this.set("hireOrderStatus", TransitionUtils.transition(getHireOrderStatus(), status));
        this.setOrderStatus(status.getOrderStatus());
        return this;
    }

    public static enum HireOrderStatus implements OrderEnumBase<HireOrderStatus> {
        ITEMREADY(OrderStatus.INPROGRESS),
        OUTFORDELIVERY(OrderStatus.INPROGRESS),
        ONHIRE(OrderStatus.INPROGRESS),
        OFFHIRE(OrderStatus.INPROGRESS),
        RETURNED(OrderStatus.INPROGRESS),
        COMPLETE(OrderStatus.INPROGRESS);

        List<HireOrderStatus> permittedFrom = new ArrayList<>();
        List<HireOrderStatus> permittedTo = new ArrayList<>();

        static {
            ITEMREADY.to(OUTFORDELIVERY,ONHIRE);
            OUTFORDELIVERY.to(ONHIRE);
            ONHIRE.to(OFFHIRE);
            ONHIRE.to(OFFHIRE);
            OFFHIRE.to(RETURNED);
            OFFHIRE.to(COMPLETE);
        }

        private OrderStatus orderStatus;

        HireOrderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
        }

        @Override
        public OrderStatus getOrderStatus() {
            return this.orderStatus;
        }

        @Override
        public HireOrderStatus getStatus() {
            return this;
        }

        @Override
        public List<HireOrderStatus> getPermittedFrom() {
            return permittedFrom;
        }

        @Override
        public List<HireOrderStatus> getPermittedTo() {
            return permittedTo;
        }
    }
}

