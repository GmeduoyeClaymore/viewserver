package com.shotgun.viewserver.order;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.domain.SinglePaymentOrder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@Controller(name = "orderPaymentController")
public class OrderPaymentController implements OrderTransformationController, OrderNotificationController{

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
