package com.shotgun.viewserver.order.types;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface NegotiationResponse extends DynamicJsonBackedObject {
    public String getPartnerId();
    public NegotiationResponseStatus getResponseStatus();
    Date getDate();
    Integer getPrice();

    default NegotiationResponse transitionTo(NegotiationResponseStatus status){
        set("responseStatus",this.getResponseStatus() == null ? status : this.getResponseStatus().transitionTo(status));
        return this;
    }
    public enum NegotiationResponseStatus implements TransitionEnumBase<NegotiationResponseStatus> {
        NONE,
        RESPONDED,
        DECLINED,
        REJECTED,
        ACCEPTED,
        CANCELLED;

        static {
            RESPONDED.to(DECLINED, ACCEPTED,CANCELLED,REJECTED);
            ACCEPTED.to(CANCELLED,DECLINED,REJECTED);
            NONE.to(RESPONDED, ACCEPTED);
            DECLINED.to(RESPONDED,REJECTED);
            REJECTED.to(RESPONDED);
            CANCELLED.to(RESPONDED);
        }


        private List<NegotiationResponseStatus> permittedFrom = new ArrayList<>();
        private List<NegotiationResponseStatus> permittedTo = new ArrayList<>();

        public NegotiationResponseStatus getStatus(){
            return this;
        }

        public List<NegotiationResponseStatus> getPermittedFrom(){
            return permittedFrom;
        }

        public List<NegotiationResponseStatus> getPermittedTo(){
            return permittedTo;
        }

    }

}
