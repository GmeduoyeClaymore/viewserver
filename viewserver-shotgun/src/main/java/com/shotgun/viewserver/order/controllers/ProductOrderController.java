package com.shotgun.viewserver.order.controllers;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.controllers.contracts.OrderCreationController;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.controllers.contracts.LinkedDeliveryOrderController;
import com.shotgun.viewserver.order.controllers.contracts.NegotiatedOrderController;
import com.shotgun.viewserver.order.controllers.contracts.SinglePaymentOrderController;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.domain.ProductOrder;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "productOrderController")
public class ProductOrderController implements NegotiationNotifications, OrderCreationController, NegotiatedOrderController, SinglePaymentOrderController, LinkedDeliveryOrderController{

    private static final Logger logger = LoggerFactory.getLogger(RubbishOrderController.class);
    private DeliveryOrderController deliveryOrderController;
    private IDatabaseUpdater iDatabaseUpdater;
    private IMapsController iMapsController;
    private IPaymentController paymentController;
    private IMessagingController iMessagingController;
    private ICatalog systemCatalog;

    public ProductOrderController(DeliveryOrderController deliveryOrderController, IDatabaseUpdater iDatabaseUpdater, IMapsController iMapsController, IPaymentController paymentController, IMessagingController iMessagingController, ICatalog systemCatalog) {
        this.deliveryOrderController = deliveryOrderController;
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.iMapsController = iMapsController;
        this.paymentController = paymentController;
        this.iMessagingController = iMessagingController;
        this.systemCatalog = systemCatalog;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    ListenableFuture<String> createOrder(@ActionParam(name = "paymentMethodId")String paymentMethodId, @ActionParam(name = "order")ProductOrder order){
        return this.create(
                order,
                paymentMethodId,
                (rec,ord) -> {
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);
                    rec.addValue("orderLocation", order.getProductAddress());
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
