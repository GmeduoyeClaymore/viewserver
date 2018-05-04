package com.shotgun.viewserver.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.maps.LatLng;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static io.viewserver.core.Utils.fromArray;
import static io.viewserver.core.Utils.toArray;
import static io.viewserver.core.Utils.toList;


public interface User extends DynamicJsonBackedObject{
    String getUserId();
    String getEmail();
    String getImageData();
    default LatLng getLocation(){
        Double latitude = this.getLatitude();
        Double longitude = this.getLongitude();
        return new LatLng(latitude == null ? 0 : latitude, longitude == null ? 0 : longitude);
    }

    UserRating[] getRatings();
    SavedPaymentCard[] getPaymentCards();
    Double getLatitude();
    Double getLongitude();
    String getFirstName();
    String getLastName();
    String getFcmToken();
    DeliveryAddress getDeliveryAddress();
    Date getCreated();
    Date getDob();
    HashMap<String,Object> getSelectedContentTypes();
    String getPassword();
    String getContactNo();
    String getType();
    int getRange();
    String getStripeCustomerId();
    String getStripeAccountId();
    String getImageUrl();
    Integer getChargePercentage();
    Double getRatingAvg();
    Boolean getOnline();


    default void addRating(String fromUserId, String orderId, int rating, String comments, UserRating.RatingType type){
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
        userRating.set("comments",comments);
        userRating.set("ratingType",type);
        UserRating response = userRating;
        List<UserRating> responses;
        if(!result.isPresent()){
            responses = toList(this.getRatings());
            responses.add(response);
            this.set("ratings",toArray(responses, UserRating[]::new));
        }
        int length = getRatings().length;
        if(getRatings() != null && length > 0){
            int sum = fromArray(getRatings()).map(UserRating::getRating).mapToInt(Integer::intValue).sum();
            this.set("ratingAvg", (double)sum/(double)length);
        }
    }

    default void addPaymentCard(SavedPaymentCard savedPaymentCard){
        List<SavedPaymentCard> savedPaymentCards = toList(this.getPaymentCards());
        savedPaymentCards.add(savedPaymentCard);
        this.set("paymentCards",toArray(savedPaymentCards, SavedPaymentCard[]::new));
    }

    default void deletePaymentCard(String cardId){
        Optional<SavedPaymentCard> paymentCard = fromArray(getPaymentCards()).filter(c->c.getCardId().equals(cardId)).findAny();

        if(paymentCard.isPresent()){
            List<SavedPaymentCard> paymentCards = toList(this.getPaymentCards());
            paymentCards.remove(paymentCard);
            this.set("paymentCards",toArray(paymentCards, SavedPaymentCard[]::new));
        }
    }
}


