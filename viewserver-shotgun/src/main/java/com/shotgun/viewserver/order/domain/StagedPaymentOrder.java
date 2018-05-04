package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.order.types.NegotiationResponse;
import com.shotgun.viewserver.order.types.OrderEnumBase;
import com.shotgun.viewserver.order.types.TransitionEnumBase;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.*;
import java.util.stream.Collectors;

import static io.viewserver.core.Utils.fromArray;
import static io.viewserver.core.Utils.toArray;
import static io.viewserver.core.Utils.toList;

public interface StagedPaymentOrder extends BasicOrder, DynamicJsonBackedObject {

    OrderPaymentStage[] getPaymentStages();

    default String addPaymentStage(int percentage, String name, String description, OrderPaymentStage.PaymentStageType stageType, OrderPaymentStage.PaymentStageStatus status){
        if(fromArray(getPaymentStages()).anyMatch(c-> c.getName().equals(name))){
            throw new RuntimeException("This order contains another payment stage called " + name);
        }
        List<OrderPaymentStage> paymentStages = toList(getPaymentStages());
        OrderPaymentStage paymentStage = JSONBackedObjectFactory.create(OrderPaymentStage.class);
        UUID uuid = UUID.randomUUID();
        paymentStage.set("quantity",percentage);
        paymentStage.set("name",name);
        paymentStage.set("description",description);
        paymentStage.set("paymentStageType",stageType);
        paymentStage.set("paymentStageStatus",status);
        paymentStage.set("id",uuid.toString());
        paymentStage.set("lastUpdated",new Date());
        paymentStages.add(paymentStage);
        this.set("paymentStages",toArray(paymentStages, OrderPaymentStage[]::new));
        return uuid.toString();
    }

    default int getAmountForStage(String paymentStageId){
        OrderPaymentStage orderPaymentStage = getOrderPaymentStage(paymentStageId);
        Integer quantity = orderPaymentStage.getQuantity();
        if(quantity == null){
            throw new RuntimeException("Payment stage quantity cannot be null");
        }
        if(orderPaymentStage.getPaymentStageType().equals(OrderPaymentStage.PaymentStageType.Fixed)){
            return quantity;
        }

        if(orderPaymentStage.getPaymentStageType().equals(OrderPaymentStage.PaymentStageType.Percentage)){
            Integer orderTotal = getAmount();
            if(orderTotal == null){
                throw new RuntimeException("Cannot calculate percentage payment as order total is null");
            }
            return (int) ((int) (orderTotal.doubleValue() / 100) * quantity.doubleValue());
        }

        throw new RuntimeException("Unrecognised payment stage type " + orderPaymentStage.getPaymentStageType());
    }

    default void startPaymentStage(String paymentStageId){
        OrderPaymentStage stage = getOrderPaymentStage(paymentStageId);
        if(fromArray(getPaymentStages()).anyMatch(c -> c.getPaymentStageStatus().equals(OrderPaymentStage.PaymentStageStatus.Started))){
            throw new RuntimeException("Cannot start payment stage  \"" + paymentStageId + "\" as order already contains a started payment stage");
        }
        stage.transitionTo(OrderPaymentStage.PaymentStageStatus.Started);
        stage.set("lastUpdated",new Date());
    }

    default void completePaymentStage(String paymentStageId){
        OrderPaymentStage stage = getOrderPaymentStage(paymentStageId);
        stage.transitionTo(OrderPaymentStage.PaymentStageStatus.Complete);
        stage.set("lastUpdated",new Date());
    }

    default void payForPaymentStage(String paymentStageId, String charge){
        OrderPaymentStage stage = getOrderPaymentStage(paymentStageId);
        stage.transitionTo(OrderPaymentStage.PaymentStageStatus.Complete);
        stage.set("paymentId",charge);
        stage.set("lastUpdated",new Date());
    }

    default OrderPaymentStage getOrderPaymentStage(String paymentStageId) {
        Optional<OrderPaymentStage> first = fromArray(getPaymentStages()).filter(c -> c.getId().equals(paymentStageId)).findFirst();
        if(!first.isPresent()){
            throw new RuntimeException("This order doesn't contain a payment stage " + paymentStageId);
        }
        return first.get();
    }

    default void removePaymentStage(String paymentStageId){
        List<OrderPaymentStage> stages = fromArray(getPaymentStages()).filter(c -> !c.getId().equals(paymentStageId)).collect(Collectors.toList());
        this.set("paymentStages",toArray(stages, OrderPaymentStage[]::new));
    }
}

