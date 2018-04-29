package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.domain.OrderPaymentStage;
import com.shotgun.viewserver.order.domain.SinglePaymentOrder;
import com.shotgun.viewserver.order.domain.StagedPaymentOrder;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;

import java.util.concurrent.atomic.AtomicReference;

public interface SinglePaymentOrderController extends OrderTransformationController, PaymentNotifications {

    @ControllerAction(path = "customerCompleteAndPay", isSynchronous = true)
    public default String customerCompleteAndPay(@ActionParam(name = "orderId") String orderId) {
        AtomicReference<String> paymentId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(OrderStatus.COMPLETED);
                    Integer amount = order.getAmount();
                    if(amount == null){
                        throw new RuntimeException("Cannot complete order as unable to get the amount " + orderId);
                    }
                    paymentId.set(getPaymentController().createCharge(amount,order.getPaymentMethodId(), order.getCustomerUserId(), order.getPartnerUserId(), order.getDescription()));
                    updateOrderRecord(order);
                    return true;
                },
                order -> {
                    notifyJobCompleted(order.getOrderId(), order.getCustomerUserId());
                },
                SinglePaymentOrder.class
        );
        return paymentId.get();
    }



    PaymentController getPaymentController();


}
