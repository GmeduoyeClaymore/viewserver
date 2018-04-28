package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.contracts.NegotiationNotifications;
import com.shotgun.viewserver.order.OrderController;
import com.shotgun.viewserver.order.OrderTransformationController;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.domain.OrderPaymentStage;
import com.shotgun.viewserver.order.domain.PersonellOrder;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.types.OrderContentType;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static io.viewserver.core.Utils.fromArray;

@Controller(name = "personellOrderController")
public class PersonellOrderController  implements NegotiationNotifications,PaymentNotifications, OrderTransformationController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;

    public PersonellOrderController(IDatabaseUpdater iDatabaseUpdater,
                                   IMessagingController messagingController,
                                   DeliveryAddressController deliveryAddressController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.deliveryAddressController = deliveryAddressController;
    }

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentMethodId")String paymentMethodId, @ActionParam(name = "order")PersonellOrder order){

        String customerId = (String) ControllerContext.get("userId");
        if(customerId == null){
            throw new RuntimeException("User id must be set in the controller context before this method is called");
        }
        if(order.getAssignedPartnerResponse() != null) {
            order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.ASSIGNED);
        }else{
            order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);
        }

        deliveryAddressController.addOrUpdateDeliveryAddress(order.getJobLocation());

        Date now = new Date();
        String orderId = ControllerUtils.generateGuid();
        order.set("orderId", orderId);

        String assignedPartnerId = order.getAssignedPartnerResponse() != null ? order.getAssignedPartnerResponse().getPartnerId() : null;
        IRecord orderRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("created", now)
                .addValue("requiredDate", order.getOpeningDate())
                .addValue("status", order.getOrderStatus().name())
                .addValue("orderLocation", order.getJobLocation())
                .addValue("orderContentTypeId", OrderContentType.Personell.getContentTypeId())
                .addValue("lastModified", now)
                .addValue("userId", customerId)
                .addValue("assignedPartnerUserId", assignedPartnerId)
                .addValue("paymentMethodId", paymentMethodId)
                .addValue("orderDetails", order);

        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, OrderDataSource.getDataSource().getSchema(), orderRecord);

        if(assignedPartnerId != null){
            notifyJobAssigned(orderId,assignedPartnerId);
        }

        return orderId;
    }

    @ControllerAction(path = "logDayStarted", isSynchronous = true)
    public void logDayStarted(@ActionParam(name = "orderId")String orderId){
        AtomicReference<String> paymentStageId = new AtomicReference<>();
        this.transform(
                orderId,
                order -> {
                    Date date = new Date();
                    paymentStageId.set(order.addPaymentStage(order.getOrderTotal(), "Work started at " + date, String.format("Day rate work started at " + date), OrderPaymentStage.PaymentStageType.Fixed));
                    return true;
                },
                order -> {
                    notifyPaymentStageStarted(orderId,order.getCustomerUserId(), order.getOrderPaymentStage(paymentStageId.get()).getDescription());
                },
                PersonellOrder.class
        );

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
}
