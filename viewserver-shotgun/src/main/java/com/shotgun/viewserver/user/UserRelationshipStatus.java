package com.shotgun.viewserver.user;

import com.shotgun.viewserver.order.types.TransitionEnumBase;

import java.util.ArrayList;
import java.util.List;

public enum UserRelationshipStatus  implements TransitionEnumBase<UserRelationshipStatus> {
    UNKNOWN,
    ACCEPTED,
    REQUESTED,
    REQUESTEDBYME,
    BLOCKED,
    REJECTED;

    static {
        UNKNOWN.to(ACCEPTED);
        UNKNOWN.to(BLOCKED);
        ACCEPTED.to(UNKNOWN);
        ACCEPTED.to(BLOCKED,UNKNOWN);
        REQUESTEDBYME.to(REJECTED,BLOCKED);
        REQUESTED.to(REJECTED,BLOCKED);
        BLOCKED.to(UNKNOWN);
    }

    List<UserRelationshipStatus> permittedFrom = new ArrayList<>();
    List<UserRelationshipStatus> permittedTo = new ArrayList<>();

    @Override
    public UserRelationshipStatus getStatus() {
        return this;
    }

    @Override
    public List<UserRelationshipStatus> getPermittedFrom() {
        return permittedFrom;
    }

    @Override
    public List<UserRelationshipStatus> getPermittedTo() {
        return permittedTo;
    }
}


