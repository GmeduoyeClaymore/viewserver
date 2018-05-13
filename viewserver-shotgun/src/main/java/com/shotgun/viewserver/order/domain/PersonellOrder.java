package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.types.OrderContentType;
import com.shotgun.viewserver.order.types.TransitionUtils;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;
import java.util.Optional;

import static io.viewserver.core.Utils.fromArray;

public interface PersonellOrder extends BasicOrder, VariablePeopleOrder, NegotiatedOrder, DynamicJsonBackedObject, StagedPaymentOrder {

    default String logDayStarted(){
        Optional<OrderPaymentStage> activeDay = fromArray(getPaymentStages()).filter(c->c.getPaymentStageStatus().equals(OrderPaymentStage.PaymentStageStatus.Started)).findAny();
        if(activeDay.isPresent()){
            throw new RuntimeException("Order already has a started day");
        }
        Date date = new Date();
        this.set("dayStarted", true);
        this.setOrderStatus(OrderStatus.INPROGRESS);
        return addPaymentStage(getAmount(), "Day Rate Work " + formatDate(date), String.format("Day rate work started at " + formatTime(date)), OrderPaymentStage.PaymentStageType.Fixed, OrderPaymentStage.PaymentStageStatus.Started);

    }

    default String formatDate(Date date) {
        return new DateTime(date).toString(DateTimeFormat.longDate());
    }

    default OrderPaymentStage logDayComplete(){
        Optional<OrderPaymentStage> activeDay = fromArray(getPaymentStages()).filter(c->c.getPaymentStageStatus().equals(OrderPaymentStage.PaymentStageStatus.Started)).findAny();
        if(!activeDay.isPresent()){
            throw new RuntimeException("Cannot find an open day to complete");
        }
        Date dayStarted = activeDay.get().getLastUpdated();
        Date date = new Date();
        activeDay.get().set("description","Day rate work started at " + formatTime(dayStarted) + " till " + formatTime(date));
        this.set("dayStarted", false);
        this.completePaymentStage(activeDay.get().getId());
        return activeDay.get();
    }

    default String formatTime(Date dayStarted) {
        return new DateTime(dayStarted).toString("HH:mm:ss");
    }

    @Override
    default OrderContentType getOrderContentType(){
        return OrderContentType.Personell;
    }

    default void partnerCompleteJob(){
        fromArray(getPaymentStages()).filter(c-> OrderPaymentStage.PaymentStageStatus.Started == c.getPaymentStageStatus() || OrderPaymentStage.PaymentStageStatus.None == c.getPaymentStageStatus()).forEach(
                c-> c.transitionTo(OrderPaymentStage.PaymentStageStatus.Complete)
        );
        this.transitionTo(NegotiatedOrder.NegotiationOrderStatus.PARTNERCOMPLETE);
    }
}
