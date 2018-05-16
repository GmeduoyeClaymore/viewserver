package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.controllers.contracts.NegotiatedOrderController;
import com.shotgun.viewserver.order.controllers.contracts.OrderCreationController;
import com.shotgun.viewserver.order.controllers.contracts.OrderTransformationController;
import com.shotgun.viewserver.order.controllers.contracts.StagedPaymentController;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.domain.OrderPaymentStage;
import com.shotgun.viewserver.order.domain.PersonellOrder;
import com.shotgun.viewserver.order.domain.SupportsImageOrder;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@Controller(name = "personellOrderController")
public class PersonellOrderController implements NegotiationNotifications, PaymentNotifications, OrderTransformationController, OrderCreationController, NegotiatedOrderController, StagedPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PersonellOrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;
    private IPaymentController paymentController;
    private IImageController imageController;

    public PersonellOrderController(IDatabaseUpdater iDatabaseUpdater,
                                    IMessagingController messagingController,
                                    DeliveryAddressController deliveryAddressController,
                                    IPaymentController paymentController,
                                    IImageController imageController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.deliveryAddressController = deliveryAddressController;
        this.paymentController = paymentController;
        this.imageController = imageController;
    }

    @ControllerAction(path = "addOrderImage", isSynchronous = false)
    public String addOrderImage(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "imageData") String imageData) {
        String fileName = orderId + "/" + ControllerUtils.generateGuid() + ".jpg";
        String completeFileName = imageController.saveImage(BucketNames.shotgunclientimages.name(), fileName, imageData);
        this.transform(orderId, order -> {
            order.addImage(completeFileName);
            return true;
        },
        SupportsImageOrder.class
        );
        return completeFileName;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentMethodId") String paymentMethodId, @ActionParam(name = "order") PersonellOrder order) {
        return this.create(
                order,
                paymentMethodId,
                (rec, ord) -> {
                    DeliveryAddress origin = order.getOrigin();
                    if (origin == null) {
                        throw new RuntimeException("Job origin cannot be null");
                    }
                    deliveryAddressController.addOrUpdateDeliveryAddress(origin);
                    if (ord.getPartnerUserId() != null) {
                        order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.ASSIGNED);
                        ord.assignJob(ord.getPartnerUserId());
                    } else {
                        order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);

                    }
                    rec.addValue("orderLocation", origin);
                    return true;
                },
                ord -> {
                    if (ord.getPartnerUserId() != null) {
                        notifyJobAssigned(ord.getOrderId(), ord.getPartnerUserId());
                    }
                }
        );
    }

    @ControllerAction(path = "startPaymentStage", isSynchronous = true)
    public void startPaymentStage(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "paymentStageId") String paymentStageId) {
        this.transform(
                orderId,
                order -> {
                    order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.STARTED);
                    order.startPaymentStage(paymentStageId);
                    return true;
                },
                order -> {
                    notifyPaymentStageStarted(order.getOrderId(), order.getCustomerUserId(), order.getOrderPaymentStage(paymentStageId).getName());
                },
                PersonellOrder.class
        );
    }

    @ControllerAction(path = "logDayStarted", isSynchronous = true)
    public String logDayStarted(@ActionParam(name = "orderId") String orderId) {
        AtomicReference<String> paymentStageId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    paymentStageId.set(order.logDayStarted());
                    return true;
                },
                order -> {
                    notifyPaymentStageStarted(orderId, order.getCustomerUserId(), order.getOrderPaymentStage(paymentStageId.get()).getDescription());
                },
                PersonellOrder.class
        );
        return paymentStageId.get();
    }

    @ControllerAction(path = "logDayComplete", isSynchronous = true)
    public String logDayComplete(@ActionParam(name = "orderId") String orderId) {
        AtomicReference<OrderPaymentStage> paymentStage = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    paymentStage.set(order.logDayComplete());
                    return true;
                },
                order -> {
                    notifyPaymentStageComplete(orderId, order.getCustomerUserId(), paymentStage.get().getDescription());
                },
                PersonellOrder.class
        );
        return paymentStage.get().getId();
    }

    @ControllerAction(path = "partnerCompleteJob", isSynchronous = true)
    public void partnerCompleteJob(@ActionParam(name = "orderId") String orderId) {
        this.transform(
                orderId,
                order -> {
                    order.partnerCompleteJob();
                    return true;
                },
                order -> {
                    notifyPartnerCompleteJob(orderId, order);
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
