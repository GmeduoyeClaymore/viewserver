package com.shotgun.viewserver.user;

public class UserRelationship {
    private String relationshipId, fromUserId, toUserId;
    private UserRelationshipStatus status;
    private UserRelationshipType type;

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public UserRelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(UserRelationshipStatus status) {
        this.status = status;
    }

    public UserRelationshipType getType() {
        return type;
    }

    public void setType(UserRelationshipType type) {
        this.type = type;
    }
}
