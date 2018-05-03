package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;

import java.util.Date;

import static com.shotgun.viewserver.ControllerUtils.getUserId;
import static io.viewserver.core.Utils.fromArray;


public interface NegotiatedOrderController extends OrderUpdateController, NegotiationNotifications, OrderTransformationController {

    @ControllerAction(path = "respondToOrder", isSynchronous = true)
    default void respondToOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "requiredDate")Date requiredDate, @ActionParam(name = "amount")Integer amount){
        String partnerId = getUserId();
        this.transform(
                orderId,
                order -> {
                    if(fromArray(order.getResponses()).anyMatch(c->c.getPartnerId().equals(partnerId))){
                        getLogger().info(partnerId + "Has already responded to this order aborting");
                        return false;
                    }
                    order.respond(partnerId, requiredDate, amount);
                    return  true;
                },
                order -> {
                    notifyJobResponded(orderId, order.getCustomerUserId());
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "cancelResponsePartner", isSynchronous = true)
    default void cancelResponsePartner(@ActionParam(name = "orderId")String orderId){
        String partnerId = getUserId();
        this.transform(
                orderId,
                order -> {
                    order.cancelResponse(partnerId);
                    return  true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if(!res.getPartnerId().equals(partnerId)){
                                    notifyJobBackOnTheMarket(order.getOrderId(), res.getPartnerId());
                                }
                            }
                    );
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "cancelResponseCustomer", isSynchronous = true)
    default void cancelResponse(@ActionParam(name = "orderId")String orderId,  @ActionParam(name = "orderId")String partnerId){
        this.transform(
                orderId,
                order -> {
                    if(!fromArray(order.getResponses()).anyMatch(c->c.getPartnerId().equals(partnerId))){
                        getLogger().info(partnerId + "Has already responded to this order aborting");
                        return false;
                    }
                    order.cancelResponse(partnerId);
                    return  true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if(!res.getPartnerId().equals(partnerId)){
                                    notifyJobBackOnTheMarket(order.getOrderId(), res.getPartnerId());
                                }else{
                                    notifyResponseCancelled(order.getOrderId(), res.getPartnerId());
                                }
                            }
                    );
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "acceptResponse", isSynchronous = true)
    default void acceptResponseToOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "partnerId")String partnerId){
        this.transform(
                orderId,
                order -> {
                    if(!fromArray(order.getResponses()).anyMatch(c->c.getPartnerId().equals(partnerId))){
                        getLogger().warn(partnerId + " has not responded to this order aborting");
                        return false;
                    }
                    order.acceptResponse(partnerId);
                    return  true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if(!res.getPartnerId().equals(partnerId)){
                                    notifyJobRejected(orderId,res.getPartnerId());
                                }else{
                                    notifyJobAccepted(orderId,res.getPartnerId());

                                }
                            }
                    );
                },
                NegotiatedOrder.class
        );
    }



}
