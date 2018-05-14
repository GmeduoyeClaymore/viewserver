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
import com.shotgun.viewserver.payments.IPaymentController;
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
public class PersonellOrderController  implements NegotiationNotifications,PaymentNotifications, OrderTransformationController, OrderCreationController, NegotiatedOrderController, StagedPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PersonellOrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;
    private IPaymentController paymentController;

    public PersonellOrderController(IDatabaseUpdater iDatabaseUpdater,
                                    IMessagingController messagingController,
                                    DeliveryAddressController deliveryAddressController,
                                    IPaymentController paymentController) {
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
                    DeliveryAddress origin = order.getOrigin();
                    if(origin == null){
                        throw new RuntimeException("Job origin cannot be null");
                    }
                    deliveryAddressController.addOrUpdateDeliveryAddress(origin);
                    if(ord.getPartnerUserId() != null){
                        order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.ASSIGNED);
                        ord.assignJob(ord.getPartnerUserId());
                    }else{
                        order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);

                    }
                    rec.addValue("orderLocation", origin);
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
                    paymentStageId.set(order.logDayStarted());
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
    public String logDayComplete(@ActionParam(name = "orderId")String orderId){
        AtomicReference<OrderPaymentStage> paymentStage = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    paymentStage.set(order.logDayComplete());
                    return true;
                },
                order -> {
                    notifyPaymentStageComplete(orderId,order.getCustomerUserId(), paymentStage.get().getDescription());
                },
                PersonellOrder.class
        );
        return paymentStage.get().getId();
    }

    @ControllerAction(path = "partnerCompleteJob", isSynchronous = true)
    public void partnerCompleteJob(@ActionParam(name = "orderId")String orderId){
        this.transform(
                orderId,
                order -> {
                    order.partnerCompleteJob();
                    return true;
                },
                order -> {
                    notifyPartnerCompleteJob(orderId,order);
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
    public IPaymentController getPaymentController() {
        return paymentController;
    }
}
