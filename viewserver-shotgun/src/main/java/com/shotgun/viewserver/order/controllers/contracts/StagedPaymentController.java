package com.shotgun.viewserver.order.controllers.contracts;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.domain.OrderPaymentStage;
import com.shotgun.viewserver.order.domain.StagedPaymentOrder;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import rx.observable.ListenableFutureObservable;

import java.util.concurrent.atomic.AtomicReference;

public interface StagedPaymentController extends NegotiatedOrderController, OrderTransformationController, PaymentNotifications {


    @Override
    @ControllerAction(path = "customerCompleteAndPay", isSynchronous = true)
    default ListenableFuture customerCompleteAndPay(@ActionParam(name = "orderId") String orderId) {
        ListenableFuture listenableFuture = NegotiatedOrderController.super.customerCompleteAndPay(orderId);
        this.transform(
                orderId,
                order -> {
                    order.completeAllPaymentStages();
                    return true; },
                StagedPaymentOrder.class
        );
        return listenableFuture;
    }

    @ControllerAction(path = "addPaymentStage", isSynchronous = true)
    default ListenableFuture addPaymentStage(
            @ActionParam(name = "orderId")String orderId,
            @ActionParam(name = "amount")int amount,
            @ActionParam(name = "name")String name,
            @ActionParam(name = "description")String description,
            @ActionParam(name = "paymentStageType")OrderPaymentStage.PaymentStageType paymentStageType) {
        AtomicReference<String> stagedPaymentId = new AtomicReference<>();
        return ListenableFutureObservable.to(this.transformObservable(
                orderId,
                order -> {
                    stagedPaymentId.set(order.addPaymentStage(amount, name,description, paymentStageType, OrderPaymentStage.PaymentStageStatus.None, true));
                    return true; },
                order -> {
                    notifyPaymentStageAdded(order.getOrderId(), order.getPartnerUserId(), name,order.getTitle());
                },
                StagedPaymentOrder.class
        ).map(res -> stagedPaymentId.get()));
    }

    @ControllerAction(path = "removePaymentStage", isSynchronous = true)
    default void removePaymentStage(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "paymentStageId") String paymentStageId) {
        this.transform(
                orderId,
                order -> {
                    order.removePaymentStage(paymentStageId);
                    return true;
                },
                StagedPaymentOrder.class
        );
    }

    @ControllerAction(path = "startPaymentStage", isSynchronous = true)
    default ListenableFuture startPaymentStage(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "paymentStageId") String paymentStageId) {
        return this.transform(
                orderId,
                order -> {
                    order.startPaymentStage(paymentStageId);
                    return true;
                },
                order -> {
                    notifyPaymentStageStarted(order.getOrderId(), order.getCustomerUserId(), order.getOrderPaymentStage(paymentStageId).getName());
                },
                StagedPaymentOrder.class
        );
    }
    @ControllerAction(path = "completePaymentStage", isSynchronous = true)
    default ListenableFuture completePaymentStage(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "paymentStageId") String paymentStageId) {
        return this.transform(
                orderId,
                order -> {
                    order.completePaymentStage(paymentStageId);
                    return true;
                },
                order -> {
                    notifyPaymentStageComplete(order.getOrderId(), order.getCustomerUserId(), order.getOrderPaymentStage(paymentStageId).getName());
                },
                StagedPaymentOrder.class
        );
    }

    @ControllerAction(path = "payForPaymentStage", isSynchronous = false)
    default ListenableFuture payForPaymentStage(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "paymentStageId") String paymentStageId) {
        AtomicReference<String> paymentId = new AtomicReference();
        return ListenableFutureObservable.to(this.transformAsyncObservable(
                orderId,
                order -> {
                    return getPaymentController().createCharge(order.getAmountForStage(paymentStageId),order.getPaymentMethodId(), order.getCustomerUserId(), order.getPartnerUserId(), order.getDescription()).map(
                            chargeId -> {
                                order.payForPaymentStage(paymentStageId, chargeId);
                                paymentId.set(chargeId);
                                return true;

                            }
                    );
                },
                order -> {
                    notifyPaymentStagePaid(order.getOrderId(), order.getPartnerUserId(), order.getOrderPaymentStage(paymentStageId).getName());
                },
                StagedPaymentOrder.class
        ).map( res -> paymentId.get()));
    }



    IPaymentController getPaymentController();
}
