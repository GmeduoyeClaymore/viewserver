package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.payments.StripeApiKey;
import com.shotgun.viewserver.user.NexmoControllerKey;
import io.viewserver.server.IViewServerConfiguration;

public interface IShotgunViewServerConfiguration extends IViewServerConfiguration {
    boolean isMock();
    String getFirebaseKeyPath();
    MessagingApiKey getMessagingApiKey();
    BasicAWSCredentials getAwsCredentials();
    MapsControllerKey getMapsKey();
    NexmoControllerKey getNexmoKey();
    StripeApiKey getStripeKey();
    VehicleDetailsApiKey getVehicleDetailsKey();
}

