package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.order.types.TransitionEnumBase;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface OrderPaymentStage extends DynamicJsonBackedObject {

    PaymentStageStatus getPaymentStageStatus();
    String getId();
    String getPaymentId();
    String getName();
    String getDescription();
    Integer getQuantity();
    Date getLastUpdated();
    PaymentStageType getPaymentStageType();

    default OrderPaymentStage transitionTo(PaymentStageStatus status){
        this.set("paymentStageStatus", status);
        return this;
    }

    enum PaymentStageType{
        Fixed,
        Percentage
    }

    enum PaymentStageStatus implements TransitionEnumBase<PaymentStageStatus> {
        None,
        Started,
        Complete,
        Paid;

        List<PaymentStageStatus> permittedFrom = new ArrayList<>();
        List<PaymentStageStatus> permittedTo = new ArrayList<>();


        static{
            None.to(Started, Complete);
            Started.to(Complete);
            Complete.to(Paid);
        }

        @Override
        public PaymentStageStatus getStatus() {
            return this;
        }

        @Override
        public List<PaymentStageStatus> getPermittedFrom() {
            return permittedFrom;
        }

        @Override
        public List<PaymentStageStatus> getPermittedTo() {
            return permittedTo;
        }
    }
}
