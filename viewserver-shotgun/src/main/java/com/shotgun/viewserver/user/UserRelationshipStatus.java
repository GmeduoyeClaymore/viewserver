package com.shotgun.viewserver.user;

import com.shotgun.viewserver.order.types.TransitionEnumBase;

import java.util.ArrayList;
import java.util.List;

public enum UserRelationshipStatus  implements TransitionEnumBase<UserRelationshipStatus> {
    UNKNOWN,
    ACCEPTED,
    REQUESTED,
    REQUESTEDBYME,
    BLOCKEDBYME,
    BLOCKED,
    REJECTED;

    static {
        UNKNOWN.to(ACCEPTED, BLOCKED, REQUESTEDBYME, REQUESTED, BLOCKEDBYME);
        ACCEPTED.to(UNKNOWN);
        ACCEPTED.to(BLOCKED,UNKNOWN,BLOCKEDBYME);
        REQUESTEDBYME.to(REJECTED,BLOCKED, BLOCKEDBYME, UNKNOWN, ACCEPTED);
        REQUESTED.to(REJECTED,BLOCKED, BLOCKEDBYME, UNKNOWN, ACCEPTED);
        BLOCKED.to(UNKNOWN, BLOCKEDBYME);
        BLOCKEDBYME.to(UNKNOWN);
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


