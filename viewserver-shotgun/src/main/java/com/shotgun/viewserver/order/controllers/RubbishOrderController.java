package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.controllers.contracts.OrderCreationController;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.controllers.contracts.NegotiatedOrderController;
import com.shotgun.viewserver.order.controllers.contracts.SinglePaymentOrderController;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.domain.RubbishOrder;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "rubbishOrderController")
public class RubbishOrderController implements NegotiationNotifications, OrderCreationController, NegotiatedOrderController, SinglePaymentOrderController {

    private static final Logger logger = LoggerFactory.getLogger(RubbishOrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;
    private IPaymentController paymentController;

    public RubbishOrderController(IDatabaseUpdater iDatabaseUpdater,
                                  IMessagingController messagingController,
                                  DeliveryAddressController deliveryAddressController,
                                  IPaymentController paymentController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.deliveryAddressController = deliveryAddressController;
        this.paymentController = paymentController;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentMethodId")String paymentMethodId, @ActionParam(name = "order")RubbishOrder order){
        return this.create(
            order,
            paymentMethodId,
            (rec,ord) -> {
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
        return this.logger;
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
    public IPaymentController getPaymentController() {
        return paymentController;
    }
}
