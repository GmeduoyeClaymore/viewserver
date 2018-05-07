package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.order.controllers.contracts.*;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.domain.DeliveryOrder;
import com.shotgun.viewserver.order.domain.JourneyOrder;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.types.NegotiationResponse;
import com.shotgun.viewserver.payments.IPaymentController;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserPersistenceController;
import com.shotgun.viewserver.user.UserTransformationController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.viewserver.core.Utils.fromArray;

@Controller(name = "deliveryOrderController")
public class DeliveryOrderController implements NegotiationNotifications, OrderCreationController,UserPersistenceController, NegotiatedOrderController, SinglePaymentOrderController, JourneyBasedOrderController {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryOrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;
    private IPaymentController paymentController;
    private IMapsController mapsController;

    public DeliveryOrderController(IDatabaseUpdater iDatabaseUpdater,
                                   IMessagingController messagingController,
                                   DeliveryAddressController deliveryAddressController,
                                   IPaymentController paymentController,
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
                if(order.getDestination() == null){
                    throw new RuntimeException("Delivery order should have destination");
                }
                deliveryAddressController.addOrUpdateDeliveryAddress(order.getDestination());
                if(order.getOrigin() == null){
                    throw new RuntimeException("Delivery order should have an origin");
                }
                deliveryAddressController.addOrUpdateDeliveryAddress(order.getOrigin());
                order.transitionTo(JourneyOrder.JourneyOrderStatus.PENDINGSTART);
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

    @ControllerAction(path = "acceptResponse", isSynchronous = true)
    public void acceptResponseToOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "partnerId")String partnerId){
        this.transform(
                orderId,
                order -> {
                    if(!fromArray(order.getResponses()).anyMatch(c->c.getPartnerId().equals(partnerId))){
                        getLogger().warn(partnerId + " has not responded to this order aborting");
                        return false;
                    }
                    User partner = getUserForId(partnerId,User.class);
                    if(partner == null){
                        throw new RuntimeException("Unable to find user for id " + partnerId);
                    }
                    Vehicle vehicle = partner.getVehicle();
                    if(vehicle == null){
                        throw new RuntimeException("In order to accept this order the partner must have a vehicle registered");
                    }
                    order.set("vehicle", vehicle);
                    order.acceptResponse(partnerId);
                    return  true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if(!res.getPartnerId().equals(partnerId)){
                                    if(res.getResponseStatus().equals(NegotiationResponse.NegotiationResponseStatus.RESPONDED)) {
                                        notifyJobRejected(orderId, res.getPartnerId());
                                    }
                                }else{
                                    notifyJobAccepted(orderId,res.getPartnerId());

                                }
                            }
                    );
                },
                DeliveryOrder.class
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
    public IPaymentController getPaymentController() {
        return paymentController;
    }

    @Override
    public IMapsController getMapsController() {
        return mapsController;
    }
}
