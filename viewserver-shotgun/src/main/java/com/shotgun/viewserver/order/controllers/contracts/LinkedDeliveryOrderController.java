package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.order.contracts.HireNotifications;
import com.shotgun.viewserver.order.controllers.DeliveryOrderController;
import com.shotgun.viewserver.order.domain.DeliveryOrder;
import com.shotgun.viewserver.order.domain.JourneyOrder;
import com.shotgun.viewserver.order.domain.LinkedDeliveryOrder;
import com.shotgun.viewserver.order.domain.SourceOrderForLinkedDeliveries;
import com.shotgun.viewserver.user.User;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public interface LinkedDeliveryOrderController extends HireNotifications, OrderCreationController, OrderTransformationController, SinglePaymentOrderController, JourneyBasedOrderController {

    @Override
    default void notifyJourneyComplete(String orderId, JourneyOrder journeyOrder) {
        User user = (User) ControllerContext.get("user");
        LinkedDeliveryOrder deliveryOrder = getOrderForId(orderId, LinkedDeliveryOrder.class);
        SourceOrderForLinkedDeliveries parentOrder = getOrderForId(deliveryOrder.getSourceOrderId(), SourceOrderForLinkedDeliveries.class);
        if(deliveryOrder.getOrderLeg().equals(LinkedDeliveryOrder.OrderLeg.Outbound)){
            sendMessage(orderId,parentOrder.getPartnerUserId(),  "Your item has been delivered",  String.format("%s has  just delivered %s", user.getFirstName() + " " + user.getLastName(), parentOrder.getDescription()));
        }
        else{
            sendMessage(orderId,parentOrder.getPartnerUserId(),  "Your item has been sent back",  String.format("%s has  just received your item back %s", user.getFirstName() + " " + user.getLastName(), parentOrder.getDescription()));
        }
    }

    @Override
    default void notifyJourneyStarted(String orderId, JourneyOrder journeyOrder) {
        User user = (User) ControllerContext.get("user");
        LinkedDeliveryOrder deliveryOrder = getOrderForId(orderId, LinkedDeliveryOrder.class);
        SourceOrderForLinkedDeliveries parentOrder = getOrderForId(deliveryOrder.getSourceOrderId(), SourceOrderForLinkedDeliveries.class);
        if(deliveryOrder.getOrderLeg().equals(LinkedDeliveryOrder.OrderLeg.Outbound)){
            sendMessage(orderId,parentOrder.getCustomerUserId(),  "Your item is on the way",  String.format("%s has  just picked up your %s", user.getFirstName() + " " + user.getLastName(), parentOrder.getDescription()));
        }
        else{
            sendMessage(orderId,parentOrder.getPartnerUserId(),  "Your item is on the way back",  String.format("%s has  just picked up your %s", user.getFirstName() + " " + user.getLastName(), parentOrder.getDescription()));
        }
    }

    @ControllerAction(path = "createOutboundDeliveryOrder", isSynchronous = true)
    default DeliveryOrder createOutboundDeliveryOrder(@ActionParam(name = "orderId")String orderId){
        AtomicReference<DeliveryOrder> deliveryOrder = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    deliveryOrder.set(order.createOutboundDelivery(user));
                    return true;
                },
                SourceOrderForLinkedDeliveries.class
        );
        return deliveryOrder.get();
    }

    @ControllerAction(path = "createInboundDeliveryOrder", isSynchronous = true)
    default DeliveryOrder createInboundDeliveryOrder(@ActionParam(name = "orderId")String orderId){
        AtomicReference<DeliveryOrder> deliveryOrder = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    deliveryOrder.set(order.createOutboundDelivery(user));
                    return true;
                },
                SourceOrderForLinkedDeliveries.class
        );
        return deliveryOrder.get();
    }

    @ControllerAction(path = "advertiseHireForDelivery", isSynchronous = true)
    default String advertiseHireForDelivery(
            @ActionParam(name = "orderId")String orderId,
            @ActionParam(name = "paymentMethodId")String paymentMethodId,
            @ActionParam(name = "partnerId")String partnerId,
            @ActionParam(name = "requiredDate")Date requiredDate){
        AtomicReference<String> deliveryHireId = new AtomicReference<>(new String());
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    DeliveryOrder deliveryOrder = order.createOutboundDelivery(user);
                    deliveryOrder.set("requiredDate", requiredDate);
                    deliveryOrder.set("partnerId", partnerId);
                    deliveryHireId.set(getDeliveryOrderController().createOrder(paymentMethodId, deliveryOrder));
                    return true;
                },
                ord -> {
                    if(ord.getPartnerUserId() != null){
                        notifyItemReady(ord.getOrderId(),ord);
                    }
                },
                SourceOrderForLinkedDeliveries.class
        );
        return deliveryHireId.get();
    }


    @ControllerAction(path = "advertiseHireForCollection", isSynchronous = true)
    default String advertiseHireForCollection(
            @ActionParam(name = "orderId")String orderId,
            @ActionParam(name = "paymentMethodId")String paymentMethodId,
            @ActionParam(name = "partnerId")String partnerId,
            @ActionParam(name = "requiredDate")Date requiredDate){
        AtomicReference<String> deliveryHireId = new AtomicReference<>(new String());
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    DeliveryOrder deliveryOrder = order.createInboundDelivery(user);
                    deliveryOrder.set("requiredDate", requiredDate);
                    deliveryOrder.set("partnerId", partnerId);
                    deliveryHireId.set(getDeliveryOrderController().createOrder(paymentMethodId, deliveryOrder));
                    return true;
                },
                ord -> {
                    if(ord.getPartnerUserId() != null){
                        notifyItemAwaitingCollection(ord.getOrderId(),ord);
                    }
                },
                SourceOrderForLinkedDeliveries.class
        );
        return deliveryHireId.get();
    }

    DeliveryOrderController getDeliveryOrderController();



}
