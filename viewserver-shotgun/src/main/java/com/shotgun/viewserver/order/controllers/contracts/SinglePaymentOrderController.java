package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.order.domain.SinglePaymentOrder;
import com.shotgun.viewserver.payments.IPaymentController;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserRating;
import com.shotgun.viewserver.user.UserTransformationController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;

import java.util.concurrent.atomic.AtomicReference;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

public interface SinglePaymentOrderController extends OrderTransformationController, PaymentNotifications {

    @ControllerAction(path = "customerCompleteAndPay", isSynchronous = true)
    public default String customerCompleteAndPay(@ActionParam(name = "orderId") String orderId) {
        AtomicReference<String> paymentId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(OrderStatus.COMPLETED);
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
                SinglePaymentOrder.class
        );
        return paymentId.get();
    }

    @ControllerAction(path = "calculatePriceEstimate", isSynchronous = true)
    public default Integer calculatePriceEstimate(@ActionParam(name = "order") BasicOrder order) {
        if(order.getOrderProduct() == null){
            throw new RuntimeException("Unable to calculate amount estimate as no product specified on order");
        }
        return order.getOrderProduct().getPrice();
    }

    IPaymentController getPaymentController();


}

