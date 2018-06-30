package com.shotgun.viewserver.user;

import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.maps.LatLng;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.order.types.TransitionUtils;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.viewserver.core.Utils.fromArray;
import static io.viewserver.core.Utils.toArray;
import static io.viewserver.core.Utils.toList;


public interface User extends DynamicJsonBackedObject{
    String getUserId();
    String getEmail();
    default LatLng getLocation(){
        Double latitude = this.getLatitude();
        Double longitude = this.getLongitude();
        return LatLng.from(latitude == null ? 0 : latitude, longitude == null ? 0 : longitude);
    }

    UserRating[] getRatings();
    AppMessage[] getPendingMessages();
    String[] getBlockedByUsers();
    UserRelationship[] getRelationships();
    SavedPaymentCard[] getPaymentCards();
    SavedBankAccount getBankAccount();
    Double getLatitude();
    Double getLongitude();
    String getFirstName();
    String getLastName();
    String getFcmToken();
    String getOperatingSystem();
    DeliveryAddress getDeliveryAddress();
    Date getCreated();
    Date getDob();
    HashMap<String,Object> getSelectedContentTypes();
    String getPassword();
    String getContactNo();
    String getType();
    Integer getRange();
    String getStripeCustomerId();
    String getStripeAccountId();
    String getImageUrl();
    Integer getChargePercentage();
    Double getRatingAvg();
    Boolean getOnline();
    Vehicle getVehicle();
    UserStatus getUserStatus();
    UserAppStatus getUserAppStatus();
    Integer getVersion();



    default UserRating addRating(String fromUserId,String title,String orderId, int rating, String comments, UserRating.RatingType type){
        Optional<UserRating> result = fromArray(getRatings()).filter(c->c.getOrderId().equals(orderId)).findAny();
        UserRating userRating;
        if(result.isPresent()){
            userRating = result.get();
        }
        else{
            userRating = JSONBackedObjectFactory.create(UserRating.class);
        }
        userRating.set("fromUserId",fromUserId);
        userRating.set("orderId",orderId);
        userRating.set("rating",rating);
        userRating.set("title",title);
        userRating.set("updatedDate",new Date());
        userRating.set("comments",comments);
        userRating.set("ratingType",type);
        UserRating response = userRating;

        if(!result.isPresent()){
            List<UserRating> responses;
            responses = toList(this.getRatings());
            responses.add(response);
            this.set("ratings",toArray(responses, UserRating[]::new));
        }
        int length = getRatings().length;
        if(getRatings() != null && length > 0){
            int sum = fromArray(getRatings()).map(UserRating::getRating).mapToInt(Integer::intValue).sum();
            this.set("ratingAvg", (double)sum/(double)length);
        }
        return userRating;
    }

    default UserRating addRating(UserRating rating){
        if(rating.getOrderId() == null){
            throw new RuntimeException("Rating cannot be null");
        }
        List<UserRating> ratings = fromArray(getRatings()).filter(c->!c.getOrderId().equals(rating.getOrderId())).collect(Collectors.toList());
        ratings.add(rating);
        this.set("ratings",toArray(ratings, UserRating[]::new));

        int length = getRatings().length;
        if(getRatings() != null && length > 0){
            int sum = fromArray(getRatings()).map(UserRating::getRating).mapToInt(Integer::intValue).sum();
            this.set("ratingAvg", (double)sum/(double)length);
        }
        return rating;
    }

    default void clearPendingMessages(){
        this.set("pendingMessages", null);
    }

    default void addOrUpdateRelationship(String toUserId,UserRelationshipStatus relationshipStatus, UserRelationshipType relationshipType){
        Optional<UserRelationship> result = findUserRelationship(toUserId);
        UserRelationship relationship;
        if(result.isPresent()){
            relationship = result.get();
        }
        else{
            relationship = JSONBackedObjectFactory.create(UserRelationship.class);
            relationship.set("toUserId",toUserId);
        }

        UserRelationshipStatus existingStatus = relationship.getRelationshipStatus();
        if(UserRelationshipStatus.BLOCKEDBYME.equals(existingStatus) && UserRelationshipStatus.BLOCKED.equals(relationshipStatus)){
            relationshipStatus = UserRelationshipStatus.BLOCKEDBYME;
        }
        relationship.set("relationshipStatus", TransitionUtils.transition(relationship.getRelationshipStatus(),relationshipStatus));
        relationship.set("relationshipType",relationshipType);

        if(!result.isPresent()){
            List relationships = toList(this.getRelationships());
            relationships.add(relationship);
            this.set("relationships",toArray(relationships, UserRelationship[]::new));
        }
        recalculateBlockedList();

    }

    default Optional<UserRelationship> findUserRelationship(String toUserId) {
        return fromArray(getRelationships()).filter(c->c.getToUserId().equals(toUserId)).findAny();
    }

    default void recalculateBlockedList(){
        List<String> blockedList = fromArray(getRelationships()).filter(c->c.getRelationshipStatus().equals(UserRelationshipStatus.BLOCKED)).map(c->c.getToUserId()).collect(Collectors.toList());
        this.set("blockByUsers", blockedList.toArray(new String[blockedList.size()]));
    }

    default boolean isBlocked(String partnerUserId){
        if(this.getRelationships() == null){
            return false;
        }
        UserRelationship relationship = getRelationship(partnerUserId);
        UserRelationshipStatus relationshipStatus = relationship.getRelationshipStatus();
        return relationshipStatus.equals(UserRelationshipStatus.BLOCKED) || relationshipStatus.equals(UserRelationshipStatus.BLOCKEDBYME);
    }

    default UserRelationship getRelationship(String partnerUserId){
        return fromArray(getRelationships()).filter(c-> c.getToUserId().equals(partnerUserId)).findAny().orElse(null);
    }

    default void addPendingMessage(AppMessage savedPaymentCard){
        List<AppMessage> pendingMessages = toList(this.getPendingMessages());
        pendingMessages.add(savedPaymentCard);
        this.set("pendingMessages",toArray(pendingMessages, AppMessage[]::new));
    }
    default void addPaymentCard(SavedPaymentCard savedPaymentCard){
        List<SavedPaymentCard> savedPaymentCards = toList(this.getPaymentCards());
        savedPaymentCards.add(savedPaymentCard);
        this.set("paymentCards",toArray(savedPaymentCards, SavedPaymentCard[]::new));
    }

    default void setDefaultPaymentCard(String cardId){
        Optional<SavedPaymentCard> paymentCard = fromArray(getPaymentCards()).filter(c->c.getCardId().equals(cardId)).findAny();

        if(paymentCard.isPresent()){
            List<SavedPaymentCard> paymentCards = toList(this.getPaymentCards());

            for(int i=0; 0< paymentCards.size(); i++){
                SavedPaymentCard currentCard = paymentCards.get(i);
                boolean isDefault = currentCard.getCardId().equals(paymentCard.get().getCardId());
                currentCard.set("isDefault", isDefault);

                paymentCards.set(i, currentCard);
            }

            this.set("paymentCards",toArray(paymentCards, SavedPaymentCard[]::new));
        }
    }

    default void deletePaymentCard(String cardId){
        Optional<SavedPaymentCard> paymentCard = fromArray(getPaymentCards()).filter(c->c.getCardId().equals(cardId)).findAny();

        if(paymentCard.isPresent()){
            List<SavedPaymentCard> paymentCards = toList(this.getPaymentCards());
            paymentCards.remove(paymentCard.get());
            this.set("paymentCards",toArray(paymentCards, SavedPaymentCard[]::new));
        }
    }

    default void setBankAccount(SavedBankAccount savedBankAccount){
        this.set("bankAccount", savedBankAccount);
    }


}


