package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.types.NegotiationResponse;
import com.shotgun.viewserver.order.types.OrderEnumBase;
import com.shotgun.viewserver.order.types.TransitionUtils;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.viewserver.core.Utils.fromArray;
import static io.viewserver.core.Utils.toArray;
import static io.viewserver.core.Utils.toList;

public interface NegotiatedOrder  extends BasicOrder {
    String getCustomerUserId();
    String getOrderId();
    NegotiationResponse[] getResponses();
    NegotiationResponse getAssignedPartnerResponse();
    NegotiationOrderStatus getNegotiatedResponseStatus();

    default void respond(String partnerId, Date date){
        NegotiationResponse negotiationResponse = JSONBackedObjectFactory.create(NegotiationResponse.class).transitionTo(NegotiationResponse.NegotiationResponseStatus.RESPONDED);
        negotiationResponse.set("date",date);
        negotiationResponse.set("partnerId",partnerId);
        NegotiationResponse response = negotiationResponse;
        List<NegotiationResponse> responses = toList(this.getResponses());
        responses.add(response);
        this.set("responses",toArray(responses, NegotiationResponse[]::new));
        this.transitionTo(NegotiationOrderStatus.RESPONDED);
    }
    Date getOpeningDate();

    default NegotiatedOrder transitionTo(NegotiationOrderStatus status){
        NegotiationOrderStatus propertyValue = TransitionUtils.transition(this.getNegotiatedResponseStatus(),status);
        this.set("negotiatedResponseStatus", propertyValue);
        this.setOrderStatus(TransitionUtils.transition(this.getOrderStatus(),propertyValue.getOrderStatus()));
        return this;
    }

    default void cancelResponse(String partnerId){
        if(!fromArray(this.getResponses()).anyMatch(c-> c.getPartnerId().equals(partnerId))){
            throw new RuntimeException("Cannot find a response from partner to cancel" + partnerId + " to cancel ");
        }
        transitionTo(NegotiationOrderStatus.RESPONDED);
        set("assignedPartner", null);
        NegotiationResponse[] responses = getResponses();
        fromArray(responses).forEach(
                res -> {
                    NegotiationResponse fill = res;
                    if(fill.getPartnerId().equals(partnerId)){
                        fill.transitionTo(NegotiationResponse.NegotiationResponseStatus.CANCELLED);
                    }else{
                        fill.transitionTo(NegotiationResponse.NegotiationResponseStatus.RESPONDED);
                    }
                }
        );
        this.set("responses",responses);
    }
    default void acceptResponse(String partnerId){
        NegotiationResponse[] responses = this.getResponses();
        if(!fromArray(responses).anyMatch(c-> c.getPartnerId().equals(partnerId))){
            throw new RuntimeException("Cannot find a response from partner " + partnerId + " to accept ");
        }


        Optional<NegotiationResponse> first = fromArray(responses).filter(c -> c.getPartnerId().equals(partnerId)).findFirst();
        if(!first.isPresent()){
            throw new RuntimeException("Unable to find responses from " + partnerId + " to orderId " + partnerId);
        }
        this.set("partnerUserId",partnerId);
        transitionTo(NegotiationOrderStatus.ASSIGNED).set("assignedPartner",first.get());
        for(NegotiationResponse fill : responses){
            if(!fill.getPartnerId().equals(partnerId)){
                fill.transitionTo(NegotiationResponse.NegotiationResponseStatus.DECLINED);
            }else{
                fill.transitionTo(NegotiationResponse.NegotiationResponseStatus.ACCEPTED);
            }
        }
        this.set("responses",responses);
    }

    public static enum NegotiationOrderStatus implements OrderEnumBase<NegotiationOrderStatus> {
        REQUESTED(OrderStatus.PLACED),
        RESPONDED(OrderStatus.PLACED),
        ASSIGNED(OrderStatus.ACCEPTED),
        STARTED(OrderStatus.INPROGRESS),
        PARTNERCOMPLETE(OrderStatus.INPROGRESS),
        CUSTOMERCOMPLETE(OrderStatus.COMPLETED);

        List<NegotiationOrderStatus> permittedFrom = new ArrayList<>();
        List<NegotiationOrderStatus> permittedTo = new ArrayList<>();

        private OrderStatus orderStatus;

        static{
            REQUESTED.to(RESPONDED,ASSIGNED);
            RESPONDED.to(ASSIGNED,REQUESTED);
            ASSIGNED.to(STARTED, RESPONDED);
            STARTED.to(PARTNERCOMPLETE);
            PARTNERCOMPLETE.to(CUSTOMERCOMPLETE);
        }

        NegotiationOrderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
        }

        @Override
        public OrderStatus getOrderStatus() {
            return this.orderStatus;
        }

        @Override
        public NegotiationOrderStatus getStatus() {
            return this;
        }

        @Override
        public List<NegotiationOrderStatus> getPermittedFrom() {
            return permittedFrom;
        }

        @Override
        public List<NegotiationOrderStatus> getPermittedTo() {
            return permittedTo;
        }
    }
}

