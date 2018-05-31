package com.shotgun.viewserver.order.controllers.contracts;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.maps.*;
import com.shotgun.viewserver.order.contracts.JourneyNotifications;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.order.domain.JourneyOrder;
import com.shotgun.viewserver.user.User;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;

import java.util.Date;

public interface JourneyBasedOrderController extends JourneyNotifications, OrderUpdateController, OrderTransformationController {

    @ControllerAction(path = "startJourney", isSynchronous = true)
    default ListenableFuture startJourney(@ActionParam(name = "orderId")String orderId){
        return this.transform(
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

    @ControllerAction(path = "calculatePriceEstimate", isSynchronous = true)
    default Integer calculatePriceEstimate(@ActionParam(name = "order") JourneyOrder order) {
        if(order.getOrderProduct() == null){
            throw new RuntimeException("Unable to calculate amount estimate as no product specified on order");
        }
        DistanceAndDuration distanceAndDuration = order.getDistanceAndDuration() == null ? getDistanceAndDuration(order) : order.getDistanceAndDuration();
        return JourneyOrder.amountCalc(distanceAndDuration, order.getOrderProduct().getPrice());
    }

    @ControllerAction(path = "completeJourney", isSynchronous = true)
    default ListenableFuture completeJourney(@ActionParam(name = "orderId")String orderId){
        return this.transform(
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
        if(order.getOrigin() == null){
            throw new RuntimeException("Cannot calculate distance and duration as origin is null");
        }
        if(order.getDestination() == null){
            throw new RuntimeException("Cannot calculate distance and duration as destination is null");
        }
        fromTo[0] = order.getOrigin().getLatLong();
        fromTo[1] = order.getDestination().getLatLong();
        return  getMapsController().getDistanceAndDuration(new DirectionRequest(fromTo,"driving"));
    }

    IMapsController getMapsController();

}
