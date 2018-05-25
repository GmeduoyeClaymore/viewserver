package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.maps.DistanceAndDuration;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.controllers.contracts.JourneyBasedOrderController;
import com.shotgun.viewserver.order.controllers.contracts.OrderCreationController;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.controllers.contracts.NegotiatedOrderController;
import com.shotgun.viewserver.order.controllers.contracts.SinglePaymentOrderController;
import com.shotgun.viewserver.order.domain.JourneyOrder;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.domain.RubbishOrder;
import com.shotgun.viewserver.payments.IPaymentController;
import com.shotgun.viewserver.user.User;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Controller(name = "rubbishOrderController")
public class RubbishOrderController implements NegotiationNotifications, OrderCreationController, NegotiatedOrderController, SinglePaymentOrderController, JourneyBasedOrderController {

    private static final Logger logger = LoggerFactory.getLogger(RubbishOrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;
    private IPaymentController paymentController;
    private IMapsController mapsController;

    public RubbishOrderController(IDatabaseUpdater iDatabaseUpdater,
                                  IMessagingController messagingController,
                                  DeliveryAddressController deliveryAddressController,
                                  IPaymentController paymentController, IMapsController mapsController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.deliveryAddressController = deliveryAddressController;
        this.paymentController = paymentController;
        this.mapsController = mapsController;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "order")RubbishOrder order){
        return this.create(
            order,
            (rec,ord) -> {
                deliveryAddressController.addOrUpdateDeliveryAddress(order.getOrigin());
                order.transitionTo(JourneyOrder.JourneyOrderStatus.PENDINGSTART);
                if(ord.getPartnerUserId() != null){
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.ASSIGNED);
                    ord.assignJob(ord.getPartnerUserId());
                }else{
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);

                }
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

    @ControllerAction(path = "completeJourney", isSynchronous = true)
    public void completeJourney(@ActionParam(name = "orderId")String orderId){
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    order.transitionTo(JourneyOrder.JourneyOrderStatus.PARTNERCOMPLETE);
                    return true;
                },
                order -> {
                    notifyJourneyComplete(orderId,order);
                },
                JourneyOrder.class
        );
    }

    @ControllerAction(path = "calculatePriceEstimate", isSynchronous = true)
    public Integer calculatePriceEstimate(@ActionParam(name = "order") JourneyOrder order) {
        if(order.getOrderProduct() == null){
            throw new RuntimeException("Unable to calculate amount estimate as no product specified on order");
        }
        return order.getOrderProduct().getPrice();
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

    @Override
    public IMapsController getMapsController() {
        return mapsController;
    }
}
