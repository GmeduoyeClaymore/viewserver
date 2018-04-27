package com.shotgun.viewserver.order.types;

import java.util.List;


public interface TransitionEnumBase<TEnum> {

    public TEnum getStatus();

    public List<TEnum> getPermittedFrom();

    public List<TEnum> getPermittedTo();

    default TEnum from(TEnum... statuses){
        for(TEnum stat : statuses){
            if(!this.getPermittedFrom().contains(stat)){
                this.getPermittedFrom().add(stat);
            }
        }
        return (TEnum) this;
    }

    default TEnum to(TEnum... statuses){
        for(TEnum stat : statuses){
            if(!this.getPermittedTo().contains(stat)){
                this.getPermittedTo().add(stat);
            }
            ((TransitionEnumBase)stat).from(this);
        }
        return (TEnum) this;
    }


    default TEnum transitionTo(TEnum status) {
        if(!this.equals(status) && !this.getPermittedTo().contains(status)){
            throw new RuntimeException("Cannot transition order from status " + this + " to " + status);
        }
        return status;
    }
}
