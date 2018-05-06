package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.types.OrderContentType;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import java.util.Date;
import java.util.Optional;

import static io.viewserver.core.Utils.fromArray;

public interface PersonellOrder extends BasicOrder, VariablePeopleOrder, NegotiatedOrder, DynamicJsonBackedObject, StagedPaymentOrder {
    DeliveryAddress getOrigin();

    default String logDayStarted(){
        Optional<OrderPaymentStage> activeDay = fromArray(getPaymentStages()).filter(c->c.getPaymentStageStatus().equals(OrderPaymentStage.PaymentStageStatus.Started)).findAny();
        if(activeDay.isPresent()){
            throw new RuntimeException("Order already has a started day");
        }
        Date date = new Date();
        this.set("dayStarted", true);
        return addPaymentStage(getAmount(), "Work started at " + date, String.format("Day rate work started at " + date), OrderPaymentStage.PaymentStageType.Fixed, OrderPaymentStage.PaymentStageStatus.Started);

    }

    default OrderPaymentStage logDayComplete(){
        Optional<OrderPaymentStage> activeDay = fromArray(getPaymentStages()).filter(c->c.getPaymentStageStatus().equals(OrderPaymentStage.PaymentStageStatus.Started)).findAny();
        if(!activeDay.isPresent()){
            throw new RuntimeException("Cannot find an open day to complete");
        }
        this.set("dayStarted", false);
        this.completePaymentStage(activeDay.get().getId());
        return activeDay.get();
    }

    @Override
    default OrderContentType getOrderContentType(){
        return OrderContentType.Personell;
    }
}
