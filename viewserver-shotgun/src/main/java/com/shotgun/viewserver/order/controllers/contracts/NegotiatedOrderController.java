package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.domain.SinglePaymentOrder;
import com.shotgun.viewserver.order.types.NegotiationResponse;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static com.shotgun.viewserver.ControllerUtils.getUserId;
import static io.viewserver.core.Utils.fromArray;


public interface NegotiatedOrderController extends OrderUpdateController, NegotiationNotifications, OrderTransformationController, PaymentNotifications {

    @ControllerAction(path = "updateOrderAmount", isSynchronous = true)
    default void updateOrderAmount(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "amount") Integer amount) {
        this.transform(
                orderId,
                order -> {
                    order.set("amount", amount);
                    return true;
                },
                NegotiatedOrder.class
        );
    }


    @ControllerAction(path = "respondToOrder", isSynchronous = true)
    default void respondToOrder(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "requiredDate") Date requiredDate, @ActionParam(name = "amount") Integer amount) {
        String partnerId = getUserId();
        this.transform(
                orderId,
                order -> {

                    order.respond(partnerId, requiredDate, amount);
                    return true;
                },
                order -> {
                    notifyJobResponded(orderId, order.getCustomerUserId());
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "cancelResponsePartner", isSynchronous = true)
    default void cancelResponsePartner(@ActionParam(name = "orderId") String orderId) {
        String partnerId = getUserId();
        final OrderStatus[] originalState = new OrderStatus[1];
        this.transform(
                orderId,
                order -> {
                    originalState[0] = order.getOrderStatus();
                    order.cancelResponse(partnerId);
                    return true;
                },
                order -> {
                    if (originalState[0].equals(OrderStatus.ACCEPTED)) {
                        fromArray(order.getResponses()).forEach(
                                res -> {
                                    if (!res.getPartnerId().equals(partnerId)) {
                                        notifyJobBackOnTheMarket(order.getOrderId(),order.getCustomerUserId(),res.getPartnerId());
                                    }
                                }
                        );
                    }

                    notifyResponseCancelled(order.getOrderId(), order.getCustomerUserId());
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "cancelResponseCustomer", isSynchronous = true)
    default void cancelResponseCustomer(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "partnerId") String partnerId) {
        final OrderStatus[] originalState = new OrderStatus[1];
        this.transform(
                orderId,
                order -> {
                    originalState[0] = order.getOrderStatus();
                    if (!fromArray(order.getResponses()).anyMatch(c -> c.getPartnerId().equals(partnerId))) {
                        getLogger().info(partnerId + " Cannot find a response from partner aborting");
                        return false;
                    }
                    order.cancelResponse(partnerId);
                    return true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if (!res.getPartnerId().equals(partnerId)) {
                                    if (originalState[0].equals(OrderStatus.ACCEPTED)) {
                                        notifyJobBackOnTheMarket(order.getOrderId(), res.getPartnerId());
                                    }
                                } else {
                                    notifyResponseCancelled(order.getOrderId(), res.getPartnerId());
                                }
                            }
                    );
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "rejectResponse", isSynchronous = true)
    default void rejectResponse(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "partnerId") String partnerId) {
        this.transform(
                orderId,
                order -> {
                    if (!fromArray(order.getResponses()).anyMatch(c -> c.getPartnerId().equals(partnerId))) {
                        getLogger().warn(partnerId + " has not responded to this order aborting");
                        return false;
                    }
                    order.rejectResponse(partnerId);
                    return true;
                },
                order -> {
                    notifyJobRejected(orderId, partnerId);
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "acceptResponse", isSynchronous = true)
    default void acceptResponseToOrder(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "partnerId") String partnerId) {
        this.transform(
                orderId,
                order -> {
                    if (!fromArray(order.getResponses()).anyMatch(c -> c.getPartnerId().equals(partnerId))) {
                        getLogger().warn(partnerId + " has not responded to this order aborting");
                        return false;
                    }
                    order.acceptResponse(partnerId);
                    return true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if (!res.getPartnerId().equals(partnerId)) {
                                    if (res.getResponseStatus().equals(NegotiationResponse.NegotiationResponseStatus.RESPONDED)) {
                                        notifyJobRejected(orderId, res.getPartnerId());
                                    }
                                } else {
                                    notifyJobAccepted(orderId, res.getPartnerId());

                                }
                            }
                    );
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "partnerStartJob", isSynchronous = true)
    public default void partnerStartJob(@ActionParam(name = "orderId")String orderId){
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.STARTED);
                    return true;
                },
                order -> {
                    notifyPartnerStartJob(orderId,order);
                },
                NegotiatedOrder.class
        );
    }


    @ControllerAction(path = "partnerCompleteJob", isSynchronous = true)
    public default void partnerCompleteJob(@ActionParam(name = "orderId")String orderId){
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.PARTNERCOMPLETE);
                    return true;
                },
                order -> {
                    notifyPartnerCompleteJob(orderId,order);
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "cancelOrder", isSynchronous = true)
    default void cancelOrder(@ActionParam(name = "orderId") String orderId) {
        this.transform(
                orderId,
                order -> {
                    order.cancel();
                    return true;
                },
                order -> {
                    if (NegotiatedOrder.NegotiationOrderStatus.RESPONDED.equals(order.getNegotiatedResponseStatus())) {
                        fromArray(order.getResponses()).forEach(
                                res -> {
                                    notifyJobCancelled(orderId, res.getPartnerId());
                                }
                        );
                    } else {
                        notifyJobCancelled(orderId, order.getPartnerUserId());
                    }


                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "calculatePriceEstimate", isSynchronous = true)
    public default Integer calculatePriceEstimate(@ActionParam(name = "order") BasicOrder order) {
        if(order.getOrderProduct() == null){
            throw new RuntimeException("Unable to calculate amount estimate as no product specified on order");
        }
        return order.getOrderProduct().getPrice();
    }


    @ControllerAction(path = "customerCompleteAndPay", isSynchronous = true)
    public default String customerCompleteAndPay(@ActionParam(name = "orderId") String orderId) {
        AtomicReference<String> paymentId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.CUSTOMERCOMPLETE);
                    Integer amount = order.getAmountToPay();
                    if(amount == null){
                        throw new RuntimeException("Cannot complete order as unable to get the amount " + orderId);
                    }
                    paymentId.set(getPaymentController().createCharge(amount,order.getPaymentMethodId(), order.getCustomerUserId(), order.getPartnerUserId(), order.getDescription()));
                    updateOrderRecord(order);
                    return true;
                },
                order -> {
                    notifyJobCompleted(order.getOrderId(), order.getPartnerUserId());
                },
                NegotiatedOrder.class
        );
        return paymentId.get();
    }

    IPaymentController getPaymentController();



}
