package com.shotgun.viewserver.order;

import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static com.shotgun.viewserver.ControllerUtils.getUserId;
import static io.viewserver.core.Utils.fromArray;


@Controller(name = "negotiatedOrderController")
public class NegotiatedOrderController implements OrderUpdateController, OrderNotificationController, OrderTransformationController{

    private static final Logger logger = LoggerFactory.getLogger(NegotiatedOrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;


    public NegotiatedOrderController(IDatabaseUpdater iDatabaseUpdater,
                                     IMessagingController messagingController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
    }


    @ControllerAction(path = "respondToOrder", isSynchronous = true)
    public void respondToOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "estimatedDate")Date estimatedDate){
        String partnerId = getUserId();
        this.transform(
                orderId,
                order -> {
                    if(fromArray(order.getResponses()).anyMatch(c->c.getPartnerId().equals(partnerId))){
                        logger.info(partnerId + "Has already responded to this order aborting");
                        return false;
                    }
                    order.respond(partnerId, estimatedDate);
                    return  true;
                },
                order -> {
                    notifyJobResponded(orderId, order.getCustomerUserId());
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "cancelResponsePartner", isSynchronous = true)
    public void cancelResponsePartner(@ActionParam(name = "orderId")String orderId){
        String partnerId = getUserId();
        this.transform(
                orderId,
                order -> {
                    order.cancelResponse(partnerId);
                    return  true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if(!res.getPartnerId().equals(partnerId)){
                                    notifyJobBackOnTheMarket(order.getOrderId(), res.getPartnerId());
                                }
                            }
                    );
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "cancelResponseCustomer", isSynchronous = true)
    public void cancelResponse(@ActionParam(name = "orderId")String orderId,  @ActionParam(name = "orderId")String partnerId){
        this.transform(
                orderId,
                order -> {
                    if(!fromArray(order.getResponses()).anyMatch(c->c.getPartnerId().equals(partnerId))){
                        logger.info(partnerId + "Has already responded to this order aborting");
                        return false;
                    }
                    order.cancelResponse(partnerId);
                    return  true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if(!res.getPartnerId().equals(partnerId)){
                                    notifyJobBackOnTheMarket(order.getOrderId(), res.getPartnerId());
                                }else{
                                    notifyResponseCancelled(order.getOrderId(), res.getPartnerId());
                                }
                            }
                    );
                },
                NegotiatedOrder.class
        );
    }

    @ControllerAction(path = "acceptResponse", isSynchronous = true)
    public void acceptResponseToOrder(@ActionParam(name = "orderId")String orderId, @ActionParam(name = "partnerId")String partnerId){
        this.transform(
                orderId,
                order -> {
                    if(!fromArray(order.getResponses()).anyMatch(c->c.getPartnerId().equals(partnerId))){
                        logger.warn(partnerId + " has not responded to this order aborting");
                        return false;
                    }
                    order.acceptResponse(partnerId);
                    return  true;
                },
                order -> {
                    fromArray(order.getResponses()).forEach(
                            res -> {
                                if(!res.getPartnerId().equals(partnerId)){
                                    notifyJobRejected(orderId,res.getPartnerId());
                                }else{
                                    notifyJobAccepted(orderId,res.getPartnerId());

                                }
                            }
                    );
                },
                NegotiatedOrder.class
        );
    }

    private void assertOrderMine(String orderUserId) {
        if(!getUserId().equals(orderUserId)){
            throw new RuntimeException("Can only perform this operation on an order that you own");
        }
    }


    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return iDatabaseUpdater;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public IMessagingController getMessagingController() {
        return messagingController;
    }
}
