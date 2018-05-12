package com.shotgun.viewserver.user;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface UserRelationship extends DynamicJsonBackedObject {
    String getToUserId();
    UserRelationshipStatus getRelationshipStatus();
    UserRelationshipType getUserRelationshipType();
}
