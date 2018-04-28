package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.types.OrderEnumBase;
import com.shotgun.viewserver.maps.DistanceAndDuration;
import com.shotgun.viewserver.maps.LatLng;
import com.shotgun.viewserver.order.types.TransitionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface JourneyOrder extends BasicOrder, SinglePaymentOrder {

    default JourneyOrder logJourneyEnd(Date date, LatLng location, DistanceAndDuration duration){
        this.set("journeyEnd",date);
        this.set("endLocation",location);
        this.set("distanceAndDuration",duration);
        this.set("amount",amountCalc(duration));
        return transitionTo(JourneyOrderStatus.PARTNERCOMPLETE);
    }

    default JourneyOrder logJourneyStart(Date date, LatLng location){
        this.set("journeyStart",date);
        this.set("startLocation",location);
        return transitionTo(JourneyOrderStatus.ENROUTE);
    }

    default JourneyOrder transitionTo(JourneyOrderStatus status){
        this.set("journeyOrderStatus", TransitionUtils.transition(getJourneyOrderStatus(), status));
        this.setOrderStatus(status.getOrderStatus());
        return this;
    }

    default int amountCalc(DistanceAndDuration duration) {
        return duration.getDistance() * 10 + duration.getDuration() * 100;
    }

    DistanceAndDuration getDistanceAndDuration();

    JourneyOrderStatus getJourneyOrderStatus();

    LatLng getStartLocation();

    LatLng getEndLocation();

    Date getJourneyStart();

    Date getJourneyEnd();

    int getJourneyDuration();

    int getJourneyDistance();

    String getPartnerId();

    public static enum JourneyOrderStatus implements OrderEnumBase<JourneyOrderStatus> {
        ENROUTE(OrderStatus.INPROGRESS),
        PARTNERCOMPLETE(OrderStatus.INPROGRESS);

        List<JourneyOrderStatus> permittedFrom = new ArrayList<>();
        List<JourneyOrderStatus> permittedTo = new ArrayList<>();

        static {
            ENROUTE.to(PARTNERCOMPLETE);
        }

        private OrderStatus orderStatus;

        JourneyOrderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
        }

        @Override
        public OrderStatus getOrderStatus() {
            return this.orderStatus;
        }

        @Override
        public JourneyOrderStatus getStatus() {
            return this;
        }

        @Override
        public List<JourneyOrderStatus> getPermittedFrom() {
            return permittedFrom;
        }

        @Override
        public List<JourneyOrderStatus> getPermittedTo() {
            return permittedTo;
        }
    }
}
