package com.shotgun.viewserver.order.types;

public class TransitionUtils{
    public static  <TEnum extends TransitionEnumBase> TEnum transition(TEnum from,TEnum to){
        if(from == null){
            return to;
        }
        return (TEnum) from.transitionTo(to);
    }
}
