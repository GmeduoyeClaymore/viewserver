package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.contracts.HireNotifications;
import com.shotgun.viewserver.order.controllers.contracts.*;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.domain.*;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller(name = "hireOrderController")
public class HireOrderController implements HireNotifications, NegotiationNotifications, OrderCreationController, NegotiatedOrderController, SinglePaymentOrderController, LinkedDeliveryOrderController, JourneyBasedOrderController{

    private static final Logger logger = LoggerFactory.getLogger(RubbishOrderController.class);
    private DeliveryOrderController deliveryOrderController;
    private IDatabaseUpdater iDatabaseUpdater;
    private IMapsController iMapsController;
    private IPaymentController paymentController;
    private IMessagingController iMessagingController;

    public HireOrderController(DeliveryOrderController deliveryOrderController, IDatabaseUpdater iDatabaseUpdater, IMapsController iMapsController, IPaymentController paymentController, IMessagingController iMessagingController) {
        this.deliveryOrderController = deliveryOrderController;
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.iMapsController = iMapsController;
        this.paymentController = paymentController;
        this.iMessagingController = iMessagingController;
    }


    @ControllerAction(path = "markItemReady", isSynchronous = true)
    public void markItemReady(@ActionParam(name = "orderId")String orderId) {
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(HireOrder.HireOrderStatus.ITEMREADY);
                    return true;
                },
                HireOrder.class
        );
    }

    @ControllerAction(path = "offHireItem", isSynchronous = true)
    public void offHireItem(@ActionParam(name = "orderId")String orderId) {
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(HireOrder.HireOrderStatus.OFFHIRE);
                    return true;
                },
                HireOrder.class
        );
    }

    @ControllerAction(path = "startJourney", isSynchronous = true)
    public void startJourney(@ActionParam(name = "orderId")String orderId) {
        LinkedDeliveryOrderController.super.startJourney(orderId);
        LinkedDeliveryOrder deliveryOrder = getOrderForId(orderId, LinkedDeliveryOrder.class);
        HireOrder parentOrder = getOrderForId(deliveryOrder.getSourceOrderId(), HireOrder.class);
        parentOrder.transitionTo(deliveryOrder.getOrderLeg().equals(LinkedDeliveryOrder.OrderLeg.Inbound) ? HireOrder.HireOrderStatus.OFFHIRE : HireOrder.HireOrderStatus.OUTFORDELIVERY);
        notifyHireItemOutForDelivery(parentOrder);
    }

    @Override
    @ControllerAction(path = "completeJourney", isSynchronous = true)
    public void completeJourney(@ActionParam(name = "orderId")String orderId) {
        LinkedDeliveryOrderController.super.startJourney(orderId);
        LinkedDeliveryOrder deliveryOrder = getOrderForId(orderId, LinkedDeliveryOrder.class);
        HireOrder parentOrder = getOrderForId(deliveryOrder.getSourceOrderId(), HireOrder.class);
        parentOrder.transitionTo(deliveryOrder.getOrderLeg().equals(LinkedDeliveryOrder.OrderLeg.Inbound) ? HireOrder.HireOrderStatus.RETURNED : HireOrder.HireOrderStatus.ONHIRE);
        notifyItemOnHire(parentOrder);
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "order")HireOrder order){
        return this.create(
                order,
                (rec,ord) -> {
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);
                    rec.addValue("orderLocation", order.getOrigin());
                    return true;
                },
                ord -> {
                    if(ord.getPartnerUserId() != null){
                        notifyItemReady(ord.getOrderId(),ord);
                    }
                }
        );
    }

    @Override
    public DeliveryOrderController getDeliveryOrderController() {
        return deliveryOrderController;
    }

    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return iDatabaseUpdater;
    }

    @Override
    public IMapsController getMapsController() {
        return iMapsController;
    }

    @Override
    public IPaymentController getPaymentController() {
        return paymentController;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public IMessagingController getMessagingController() {
        return iMessagingController;
    }
}
