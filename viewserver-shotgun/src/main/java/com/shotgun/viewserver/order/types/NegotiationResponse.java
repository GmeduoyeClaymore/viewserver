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
        ACCEPTED,
        CANCELLED;

        static {
            RESPONDED.to(DECLINED, ACCEPTED,CANCELLED);
            ACCEPTED.to(CANCELLED,DECLINED);
            NONE.to(RESPONDED);
            DECLINED.to(RESPONDED);
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
