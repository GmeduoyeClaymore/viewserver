package com.shotgun.viewserver.order.controllers;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.contracts.HireNotifications;
import com.shotgun.viewserver.order.controllers.contracts.*;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.domain.*;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;


@Controller(name = "hireOrderController")
public class HireOrderController implements HireNotifications, NegotiationNotifications, OrderCreationController, NegotiatedOrderController, SinglePaymentOrderController, LinkedDeliveryOrderController, JourneyBasedOrderController{

    private static final Logger logger = LoggerFactory.getLogger(RubbishOrderController.class);
    private DeliveryOrderController deliveryOrderController;
    private IDatabaseUpdater iDatabaseUpdater;
    private IMapsController iMapsController;
    private IPaymentController paymentController;
    private IMessagingController iMessagingController;
    private ICatalog systemCatalog;

    public HireOrderController(DeliveryOrderController deliveryOrderController, IDatabaseUpdater iDatabaseUpdater, IMapsController iMapsController, IPaymentController paymentController, IMessagingController iMessagingController, ICatalog systemCatalog) {
        this.deliveryOrderController = deliveryOrderController;
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.iMapsController = iMapsController;
        this.paymentController = paymentController;
        this.iMessagingController = iMessagingController;
        this.systemCatalog = systemCatalog;
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
    public ListenableFuture startJourney(@ActionParam(name = "orderId")String orderId) {
        ListenableFuture result = LinkedDeliveryOrderController.super.startJourney(orderId);
        ControllerContext context = ControllerContext.Current();
        getOrderForId(orderId, LinkedDeliveryOrder.class).flatMap(
                deliveryOrder -> {
                    try {
                        ControllerContext.create(context);
                        return getOrderForId(deliveryOrder.getSourceOrderId(), HireOrder.class).map(
                                parentOrder -> {
                                    parentOrder.transitionTo(deliveryOrder.getOrderLeg().equals(LinkedDeliveryOrder.OrderLeg.Inbound) ? HireOrder.HireOrderStatus.OFFHIRE : HireOrder.HireOrderStatus.OUTFORDELIVERY);
                                    notifyHireItemOutForDelivery(parentOrder);
                                    return parentOrder;
                                }
                        );
                    }finally {
                        ControllerContext.closeStatic();
                    }
                }
        );
        return result;
    }

    @Override
    @ControllerAction(path = "completeJourney", isSynchronous = true)
    public ListenableFuture completeJourney(@ActionParam(name = "orderId")String orderId) {
        ListenableFuture result = LinkedDeliveryOrderController.super.startJourney(orderId);
        getOrderForId(orderId, LinkedDeliveryOrder.class).flatMap(
                deliveryOrder -> {
                    return getOrderForId(deliveryOrder.getSourceOrderId(), HireOrder.class).map(
                            parentOrder -> {
                                parentOrder.transitionTo(deliveryOrder.getOrderLeg().equals(LinkedDeliveryOrder.OrderLeg.Inbound) ? HireOrder.HireOrderStatus.RETURNED : HireOrder.HireOrderStatus.ONHIRE);
                                notifyItemOnHire(parentOrder);
                                return parentOrder;
                            }
                    );
                }
        );
        return result;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public ListenableFuture<String> createOrder(@ActionParam(name = "paymentMethodId")String paymentMethodId, @ActionParam(name = "order")HireOrder order){
        return this.create(
                order,
                paymentMethodId,
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
    public ICatalog getSystemCatalog() {
        return systemCatalog;
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
