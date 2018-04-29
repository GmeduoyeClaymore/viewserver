package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.maps.*;
import com.shotgun.viewserver.order.contracts.JourneyNotifications;
import com.shotgun.viewserver.order.domain.JourneyOrder;
import com.shotgun.viewserver.user.User;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;

import java.util.Date;

public interface JourneyBasedOrderController extends JourneyNotifications, OrderUpdateController, OrderTransformationController {

    @ControllerAction(path = "startJourney", isSynchronous = true)
    default void startJourney(@ActionParam(name = "orderId")String orderId){
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    order.logJourneyStart(new Date(), user.getLocation());
                    return true;
                },
                order -> {
                    notifyJourneyStarted(orderId,order);
                },
                JourneyOrder.class
        );

    }

    @ControllerAction(path = "completeJourney", isSynchronous = true)
    default void completeJourney(@ActionParam(name = "orderId")String orderId){
        this.transform(
                orderId,
                order -> {
                    User user = (User) ControllerContext.get("user");
                    order.logJourneyEnd(new Date(), user.getLocation(), getDistanceAndDuration(order));
                    return true;
                },
                order -> {
                    notifyJourneyComplete(orderId,order);
                },
                JourneyOrder.class
        );
    }

    default DistanceAndDuration getDistanceAndDuration(JourneyOrder order) {
        LatLng[] fromTo = new LatLng[2];
        fromTo[0] = order.getStartLocation();
        fromTo[1] = order.getEndLocation();
        return  getMapsController().getDistanceAndDuration(new DirectionRequest(fromTo,"driving"));
    }

    IMapsController getMapsController();

}
