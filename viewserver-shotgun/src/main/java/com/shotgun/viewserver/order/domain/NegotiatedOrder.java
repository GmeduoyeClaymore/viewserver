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

    default void respond(String partnerId, Date date, Integer price){
        Optional<NegotiationResponse> result = fromArray(getResponses()).filter(c->c.getPartnerId().equals(partnerId)).findAny();
        NegotiationResponse negotiationResponse;
        if(result.isPresent()){
            negotiationResponse = result.get();
        }
        else{
            negotiationResponse = JSONBackedObjectFactory.create(NegotiationResponse.class);
        }
        negotiationResponse.transitionTo(NegotiationResponse.NegotiationResponseStatus.RESPONDED);
        negotiationResponse.set("date",date);
        negotiationResponse.set("partnerId",partnerId);
        negotiationResponse.set("price",price);
        NegotiationResponse response = negotiationResponse;
        if(!result.isPresent()){
            List<NegotiationResponse> responses = toList(this.getResponses());
            responses.add(response);
            this.set("responses",toArray(responses, NegotiationResponse[]::new));
        }
        this.transitionTo(NegotiationOrderStatus.RESPONDED);
    }

    default NegotiatedOrder transitionTo(NegotiationOrderStatus status){
        NegotiationOrderStatus propertyValue = TransitionUtils.transition(this.getNegotiatedResponseStatus(),status);
        this.set("negotiatedOrderStatus", propertyValue);
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
        NegotiationResponse propertyValue = first.get();
        transitionTo(NegotiationOrderStatus.ASSIGNED).set("assignedPartner", propertyValue);
        if(propertyValue.getPrice() != null) {
            this.set("amount", propertyValue.getPrice());
        }
        for(NegotiationResponse fill : responses){
            if(!fill.getPartnerId().equals(partnerId)){
                fill.transitionTo(NegotiationResponse.NegotiationResponseStatus.REJECTED);
            }else{
                fill.transitionTo(NegotiationResponse.NegotiationResponseStatus.ACCEPTED);
            }
        }
        this.set("responses",responses);
    }

    default void rejectResponse(String partnerId){
        if(!fromArray(this.getResponses()).anyMatch(c-> c.getPartnerId().equals(partnerId))){
            throw new RuntimeException("Cannot find a response from partner to decline" + partnerId + " to cancel ");
        }
        transitionTo(NegotiationOrderStatus.RESPONDED);
        set("assignedPartner", null);
        NegotiationResponse[] responses = getResponses();
        fromArray(responses).forEach(
                res -> {
                    NegotiationResponse fill = res;
                    if(fill.getPartnerId().equals(partnerId)){
                        fill.transitionTo(NegotiationResponse.NegotiationResponseStatus.DECLINED);
                    }
                }
        );
        this.set("responses",responses);
    }

    default void cancel(){
        this.transitionTo(NegotiatedOrder.NegotiationOrderStatus.CANCELLED);
        NegotiationResponse[] responses = getResponses();
        fromArray(responses).forEach(
                res -> {
                    NegotiationResponse fill = res;
                    fill.transitionTo(NegotiationResponse.NegotiationResponseStatus.CANCELLED);
                }
        );
    }

    default void assignJob(String partnerUserId){
        NegotiationResponse negotiationResponse = JSONBackedObjectFactory.create(NegotiationResponse.class);
        negotiationResponse.transitionTo(NegotiationResponse.NegotiationResponseStatus.ACCEPTED);
        negotiationResponse.set("date",this.getRequiredDate());
        negotiationResponse.set("partnerId",partnerUserId);
        negotiationResponse.set("price",this.getAmount());
        List<NegotiationResponse> responses = toList(this.getResponses());
        responses.add(negotiationResponse);
        this.set("responses",toArray(responses, NegotiationResponse[]::new));
        this.set("assignedPartner", negotiationResponse);
    }

    public static enum NegotiationOrderStatus implements OrderEnumBase<NegotiationOrderStatus> {
        REQUESTED(OrderStatus.PLACED),
        RESPONDED(OrderStatus.PLACED),
        ASSIGNED(OrderStatus.ACCEPTED),
        STARTED(OrderStatus.INPROGRESS),
        PARTNERCOMPLETE(OrderStatus.INPROGRESS),
        CANCELLED(OrderStatus.CANCELLED),
        CUSTOMERCOMPLETE(OrderStatus.COMPLETED);

        List<NegotiationOrderStatus> permittedFrom = new ArrayList<>();
        List<NegotiationOrderStatus> permittedTo = new ArrayList<>();

        private OrderStatus orderStatus;

        static{
            REQUESTED.to(RESPONDED,ASSIGNED,CANCELLED);
            RESPONDED.to(ASSIGNED,REQUESTED,CANCELLED);
            ASSIGNED.to(STARTED,RESPONDED,CANCELLED);
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

