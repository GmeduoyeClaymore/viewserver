package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.OrderTransformationController;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.domain.OrderPaymentStage;
import com.shotgun.viewserver.order.domain.SinglePaymentOrder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.domain.StagedPaymentOrder;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@Controller(name = "orderPaymentController")
public class OrderPaymentController implements OrderTransformationController, PaymentNotifications {

    private PaymentController paymentController;
    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private static final Logger logger = LoggerFactory.getLogger(NegotiatedOrderController.class);

    public OrderPaymentController(PaymentController paymentController, IMessagingController messagingController, IDatabaseUpdater iDatabaseUpdater) {
        this.paymentController = paymentController;
        this.messagingController = messagingController;
        this.iDatabaseUpdater = iDatabaseUpdater;
    }

    @ControllerAction(path = "customerCompleteAndPay", isSynchronous = true)
    public String customerCompleteAndPay(@ActionParam(name = "orderId")String orderId ) {
        AtomicReference<String> paymentId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(OrderStatus.COMPLETED);
                    paymentId.set(getPaymentController().createCharge(order.getAmount(),order.getPaymentMethodId(), order.getCustomerUserId(), order.getPartnerUserId(), order.getDescription()));
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

    @ControllerAction(path = "addPaymentStage", isSynchronous = true)
    public String addPaymentStage(
            @ActionParam(name = "orderId")String orderId,
            @ActionParam(name = "percentage")int percentage,
            @ActionParam(name = "name")String name,
            @ActionParam(name = "description")String description,
            @ActionParam(name = "paymentStageType")OrderPaymentStage.PaymentStageType paymentStageType) {
        AtomicReference<String> stagedPaymentId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    stagedPaymentId.set(order.addPaymentStage(percentage, name,description, paymentStageType));
                    return true; },
                StagedPaymentOrder.class
        );
        return stagedPaymentId.get();
    }

    @ControllerAction(path = "startPaymentStage", isSynchronous = true)
    public void startPaymentStage(@ActionParam(name = "orderId")String orderId,@ActionParam(name = "paymentStageId")String paymentStageId) {
        this.transform(
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
    public void completePaymentStage(@ActionParam(name = "orderId")String orderId,@ActionParam(name = "paymentStageId")String paymentStageId) {
        this.transform(
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

    @ControllerAction(path = "payForPaymentStage", isSynchronous = true)
    public String payForPaymentStage(@ActionParam(name = "orderId")String orderId,@ActionParam(name = "paymentStageId")String paymentStageId) {
        AtomicReference<String> paymentId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    String charge = getPaymentController().createCharge(order.getAmount(paymentStageId),order.getPaymentMethodId(), order.getCustomerUserId(), order.getPartnerUserId(), order.getDescription());
                    order.payForPaymentStage(paymentStageId, charge);
                    paymentId.set(charge);
                    return true;
                },
                order -> {
                    notifyPaymentStagePaid(order.getOrderId(), order.getCustomerUserId(), order.getOrderPaymentStage(paymentStageId).getName());
                },
                StagedPaymentOrder.class
        );
        return paymentId.get();
    }

    public PaymentController getPaymentController() {
        return paymentController;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public IMessagingController getMessagingController() {
        return messagingController;
    }

    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return iDatabaseUpdater;
    }
}
