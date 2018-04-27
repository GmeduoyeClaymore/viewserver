package com.shotgun.viewserver.order;

import com.shotgun.viewserver.maps.*;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.user.User;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Controller(name = "journeyBaseOrderController")
public class JourneyBasedOrderController implements OrderNotificationController, OrderUpdateController, OrderTransformationController{

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private IMessagingController messagingController;
    private IDatabaseUpdater iDatabaseUpdater;
    private IMapsController mapsController;

    public JourneyBasedOrderController(IMessagingController messagingController, IDatabaseUpdater iDatabaseUpdater, IMapsController mapsController) {
        this.messagingController = messagingController;
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.mapsController = mapsController;
    }

    @ControllerAction(path = "startJourney", isSynchronous = true)
    public void startJourney(@ActionParam(name = "orderId")String orderId){
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    order.logJourneyStart(new Date(), user.getLocation());
                    return true;
                },
                order -> {
                    notifyJobStarted(orderId,order.getCustomerUserId());
                },
                JourneyOrder.class
        );

    }

    @ControllerAction(path = "completeJourney", isSynchronous = true)
    public void completeJourney(@ActionParam(name = "orderId")String orderId){
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    order.logJourneyEnd(new Date(), user.getLocation(), getDistanceAndDuration(order));
                    return true;
                },
                order -> {
                    notifyJobPartnerComplete(orderId,order.getCustomerUserId());
                },
                JourneyOrder.class
        );

    }

    private DistanceAndDuration getDistanceAndDuration(JourneyOrder order) {
        LatLng[] fromTo = new LatLng[2];
        fromTo[0] = order.getStartLocation();
        fromTo[1] = order.getEndLocation();
        return  mapsController.getDistanceAndDuration(new DirectionRequest(fromTo,"driving"));
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
