package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.controllers.contracts.*;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.domain.OrderPaymentStage;
import com.shotgun.viewserver.order.domain.PersonellOrder;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static io.viewserver.core.Utils.fromArray;

@Controller(name = "personellOrderController")
public class PersonellOrderController  implements NegotiationNotifications,PaymentNotifications, OrderTransformationController, OrderCreationController, NegotiatedOrderController, StagedPaymentController, SinglePaymentOrderController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRatingController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;
    private PaymentController paymentController;

    public PersonellOrderController(IDatabaseUpdater iDatabaseUpdater,
                                    IMessagingController messagingController,
                                    DeliveryAddressController deliveryAddressController,
                                    PaymentController paymentController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.deliveryAddressController = deliveryAddressController;
        this.paymentController = paymentController;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentMethodId")String paymentMethodId, @ActionParam(name = "order")PersonellOrder order){
        return this.create(
                order,
                paymentMethodId,
                (rec,ord) -> {
                    DeliveryAddress jobLocation = order.getOrigin();
                    if(jobLocation == null){
                        throw new RuntimeException("Job location cannot be null");
                    }
                    deliveryAddressController.addOrUpdateDeliveryAddress(jobLocation);
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);
                    rec.addValue("orderLocation", jobLocation);
                    return true;
                },
                ord -> {
                    if(ord.getPartnerUserId() != null){
                        notifyJobAssigned(ord.getOrderId(),ord.getPartnerUserId());
                    }
                }
        );
    }

    @ControllerAction(path = "logDayStarted", isSynchronous = true)
    public String logDayStarted(@ActionParam(name = "orderId")String orderId){
        AtomicReference<String> paymentStageId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    Date date = new Date();
                    paymentStageId.set(order.addPaymentStage(order.getAmount(), "Work started at " + date, String.format("Day rate work started at " + date), OrderPaymentStage.PaymentStageType.Fixed, OrderPaymentStage.PaymentStageStatus.Started));
                    return true;
                },
                order -> {
                    notifyPaymentStageStarted(orderId,order.getCustomerUserId(), order.getOrderPaymentStage(paymentStageId.get()).getDescription());
                },
                PersonellOrder.class
        );
        return paymentStageId.get();
    }

    @ControllerAction(path = "logDayComplete", isSynchronous = true)
    public void logDayComplete(@ActionParam(name = "orderId")String orderId){
        AtomicReference<OrderPaymentStage> paymentStage = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    Optional<OrderPaymentStage> activeDay = fromArray(order.getPaymentStages()).filter(c->c.getPaymentStageStatus().equals(OrderPaymentStage.PaymentStageStatus.Started)).findAny();
                    if(!activeDay.isPresent()){
                        throw new RuntimeException("Cannot find an open day to complete");
                    }
                    paymentStage.set(activeDay.get());
                    order.completePaymentStage(activeDay.get().getId());
                    return true;
                },
                order -> {
                    notifyPaymentStageComplete(orderId,order.getCustomerUserId(), paymentStage.get().getDescription());
                },
                PersonellOrder.class
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
}
