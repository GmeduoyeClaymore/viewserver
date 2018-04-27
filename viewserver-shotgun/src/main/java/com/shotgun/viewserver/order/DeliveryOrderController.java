package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.types.OrderContentType;
import com.shotgun.viewserver.messaging.IMessagingController;
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

@Controller(name = "deliveryOrderController")
public class DeliveryOrderController implements OrderNotificationController{

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private DeliveryAddressController deliveryAddressController;

    public DeliveryOrderController(IDatabaseUpdater iDatabaseUpdater,
                                     IMessagingController messagingController,
                                     DeliveryAddressController deliveryAddressController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.deliveryAddressController = deliveryAddressController;
    }



    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentMethodId")String paymentMethodId, @ActionParam(name = "delivery")DeliveryOrder order){

        String customerId = (String) ControllerContext.get("userId");
        if(customerId == null){
            throw new RuntimeException("User id must be set in the controller context before this method is called");
        }
        if(order.getAssignedPartnerResponse() != null) {
            order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.ASSIGNED);
        }else{
            order.transitionTo(NegotiatedOrder.NegotiationOrderStatus.REQUESTED);
        }

        deliveryAddressController.addOrUpdateDeliveryAddress(order.getDestination());
        deliveryAddressController.addOrUpdateDeliveryAddress(order.getOrigin());

        Date now = new Date();
        String orderId = ControllerUtils.generateGuid();

        order.set("orderId", orderId);

        String assignedPartnerId = order.getAssignedPartnerResponse() != null ? order.getAssignedPartnerResponse().getPartnerId() : null;
        IRecord orderRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("created", now)
                .addValue("requiredDate", order.getOpeningDate())
                .addValue("status", order.getOrderStatus().name())
                .addValue("orderLocation", order.getOrigin())
                .addValue("orderContentTypeId", OrderContentType.Delivery.getContentTypeId())
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

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public IMessagingController getMessagingController() {
        return messagingController;
    }
}
