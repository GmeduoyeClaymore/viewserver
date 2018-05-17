package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.viewserver.core.Utils.*;

public interface StagedPaymentOrder extends BasicOrder, DynamicJsonBackedObject {

    OrderPaymentStage[] getPaymentStages();

    default String addPaymentStage(int quantity, String name, String description, OrderPaymentStage.PaymentStageType stageType, OrderPaymentStage.PaymentStageStatus status, boolean doLimitCheck){
        if(getPaymentStage(name) != null){
            throw new RuntimeException("This order contains another payment stage called " + name);
        }
        List<OrderPaymentStage> paymentStages = toList(getPaymentStages());
        OrderPaymentStage paymentStage = JSONBackedObjectFactory.create(OrderPaymentStage.class);
        if(doLimitCheck) {
            int totalForAllStages = fromArray(getPaymentStages()).mapToInt(stage -> stage.getQuantity()).sum();
            int limit = stageType.equals(OrderPaymentStage.PaymentStageType.Percentage) ? 100 : getAmount();
            boolean blockPaymentStageAddition = false;
            if ((totalForAllStages + quantity) >= limit) {
                quantity = limit - totalForAllStages;
                blockPaymentStageAddition = true;
            }
            this.set("blockPaymentStageAddition",blockPaymentStageAddition);
        }

        UUID uuid = UUID.randomUUID();
        paymentStage.set("quantity",quantity);
        paymentStage.set("name",name);
        paymentStage.set("description",description);
        paymentStage.set("paymentStageType",stageType);
        paymentStage.set("paymentStageStatus",status);
        paymentStage.set("id",uuid.toString());
        paymentStage.set("lastUpdated",new Date());
        paymentStages.add(paymentStage);
        this.set("paymentStages",validateStages(toArray(paymentStages, OrderPaymentStage[]::new)));
        return uuid.toString();
    }

    default OrderPaymentStage getPaymentStage(String name) {
        return fromArray(getPaymentStages()).filter(c-> c.getName().equals(name)).findAny().orElse(null);
    }

    default OrderPaymentStage[] validateStages(OrderPaymentStage[] stages){
        if(stages == null || stages.length <= 1){
            return stages;
        }
        OrderPaymentStage.PaymentStageType type = stages[0].getPaymentStageType();
        for(int i=1;i<stages.length;i++){
            OrderPaymentStage stage = stages[i];
            OrderPaymentStage.PaymentStageType paymentStageType = stage.getPaymentStageType();
            if (type != paymentStageType){
                throw new RuntimeException("Stage " + stage.getName() + " is of type " + paymentStageType + " which is different to the rest of the stages which are type " + type);
            }
        }
        return stages;

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
        this.transitionTo(OrderStatus.INPROGRESS);
        stage.set("lastUpdated",new Date());
    }

    default void completePaymentStage(String paymentStageId){
        OrderPaymentStage stage = getOrderPaymentStage(paymentStageId);
        stage.transitionTo(OrderPaymentStage.PaymentStageStatus.Complete);
        stage.set("lastUpdated",new Date());
    }

    default void payForPaymentStage(String paymentStageId, String charge){
        OrderPaymentStage stage = getOrderPaymentStage(paymentStageId);
        stage.transitionTo(OrderPaymentStage.PaymentStageStatus.Paid);
        stage.set("paymentId",charge);
        stage.set("lastUpdated",new Date());
    }


    default Integer calculateRemainder(){
        if(getOrderStatus().equals(OrderStatus.COMPLETED)){
            return 0;
        }
        if(PaymentType.DAYRATE.equals(this.getPaymentType())){
            Optional<OrderPaymentStage> paymentStage = fromArray(getPaymentStages()).filter(c -> (OrderPaymentStage.PaymentStageStatus.Started).equals(c.getPaymentStageStatus())).findAny();
            if(paymentStage.isPresent()){
                return getAmountForStage(paymentStage.get().getId());
            }
            return 0;
        }else{
            int totalPaidForStages = fromArray(getPaymentStages()).filter(c-> c.getPaymentStageStatus().equals(OrderPaymentStage.PaymentStageStatus.Paid)).mapToInt(stage -> getAmountForStage(stage.getId())).sum();
            if(getAmount() == null){
                throw new RuntimeException("Cannot calculate remainer as no amount has been specified on order");
            }
            return getAmount() - totalPaidForStages;
        }
    }

    default OrderPaymentStage getOrderPaymentStage(String paymentStageId) {
        Optional<OrderPaymentStage> first = fromArray(getPaymentStages()).filter(c -> c.getId().equals(paymentStageId)).findFirst();
        if(!first.isPresent()){
            throw new RuntimeException("This order doesn't contain a payment stage " + paymentStageId);
        }
        return first.get();
    }

    default void removePaymentStage(String paymentStageId){
        Optional<OrderPaymentStage> first = fromArray(getPaymentStages()).filter(c -> c.getId().equals(paymentStageId)).findFirst();
        if(!first.isPresent()){
            return;
        }
        List<OrderPaymentStage> stages = fromArray(getPaymentStages()).filter(c -> !c.getId().equals(paymentStageId)).collect(Collectors.toList());
        this.set("blockPaymentStageAddition",false);
        this.set("paymentStages",toArray(stages, OrderPaymentStage[]::new));
    }

    default void completeAllPaymentStages(){
        fromArray(getPaymentStages()).forEach(
                stage -> completePaymentStage(stage.getId())
        );
    }
}

