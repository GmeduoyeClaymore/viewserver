package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.order.controllers.contracts.JourneyBasedOrderController;
import com.shotgun.viewserver.order.controllers.contracts.OrderCreationController;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.controllers.contracts.NegotiatedOrderController;
import com.shotgun.viewserver.order.controllers.contracts.SinglePaymentOrderController;
import com.shotgun.viewserver.order.domain.DeliveryOrder;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "deliveryOrderController")
public class DeliveryOrderController implements NegotiationNotifications, OrderCreationController, NegotiatedOrderController, SinglePaymentOrderController, JourneyBasedOrderController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRatingController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;
    private PaymentController paymentController;
    private IMapsController mapsController;

    public DeliveryOrderController(IDatabaseUpdater iDatabaseUpdater,
                                   IMessagingController messagingController,
                                   DeliveryAddressController deliveryAddressController,
                                   PaymentController paymentController,
                                   IMapsController mapsController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.deliveryAddressController = deliveryAddressController;
        this.paymentController = paymentController;
        this.mapsController = mapsController;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentMethodId")String paymentMethodId, @ActionParam(name = "order")DeliveryOrder order){
        return this.create(
            order,
            paymentMethodId,
            (rec,ord) -> {
                deliveryAddressController.addOrUpdateDeliveryAddress(order.getDestination());
                deliveryAddressController.addOrUpdateDeliveryAddress(order.getOrigin());
                order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);
                rec.addValue("orderLocation", order.getOrigin());
                return true;
            },
            ord -> {
                if(ord.getPartnerUserId() != null){
                    notifyJobAssigned(ord.getOrderId(),ord.getPartnerUserId());
                }
            }
        );
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public IMessagingController getMessagingController() {
        return messagingController;
    }

    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return iDatabaseUpdater;
    }

    @Override
    public PaymentController getPaymentController() {
        return paymentController;
    }

    @Override
    public IMapsController getMapsController() {
        return mapsController;
    }
}
