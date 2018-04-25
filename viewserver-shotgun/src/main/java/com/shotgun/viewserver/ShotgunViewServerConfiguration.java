package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.payments.StripeApiKey;
import com.shotgun.viewserver.user.NexmoControllerKey;
import io.viewserver.server.XmlViewServerConfiguration;

public class ShotgunViewServerConfiguration extends XmlViewServerConfiguration implements IShotgunViewServerConfiguration{
    public static final String VIEWSERVER_ISMOCK = "viewserver.isMock";
    public static final String VIEWSERVER_ISTEST = "viewserver.isTest";
    public static final String VIEWSERVER_FIREBASE_KEY_PATH = "viewserver.firebaseKeyPath";
    public static final String VIEWSERVER_MESSAGING_API_KEY = "viewserver.messagingApiKey";
    public static final String VIEWSERVER_AWS_ACCESS = "viewserver.awsCredentials.accessKey";
    public static final String VIEWSERVER_AWS_SECRET = "viewserver.awsCredentials.secretKey";
    public static final String VIEWSERVER_MAPS_KEY = "viewserver.mapsControllerKey";
    public static final String VIEWSERVER_NEXMO_KEY = "viewserver.nexmo.key";
    public static final String VIEWSERVER_NEXMO_SECRET = "viewserver.nexmo.secret";
    public static final String VIEWSERVER_STRIPE_KEY = "viewserver.stripe.key";
    public static final String VIEWSERVER_STRIPE_SECRET = "viewserver.stripe.secret";
    public static final String VIEWSERVER_VEHICLE_DETAILS_KEY = "viewserver.vehicleDetailsKey";


    public ShotgunViewServerConfiguration(String configurationFile) {
        super(configurationFile);
    }

    @Override
    public String getFirebaseKeyPath() {
        return configuration.getString(VIEWSERVER_FIREBASE_KEY_PATH);
    }

    @Override
    public MessagingApiKey getMessagingApiKey() {
        return new MessagingApiKey(configuration.getString(VIEWSERVER_MESSAGING_API_KEY));
    }

    @Override
    public BasicAWSCredentials getAwsCredentials() {
        return new BasicAWSCredentials(configuration.getString(VIEWSERVER_AWS_ACCESS), configuration.getString(VIEWSERVER_AWS_SECRET));
    }

    @Override
    public MapsControllerKey getMapsKey() {
        return new MapsControllerKey(configuration.getString(VIEWSERVER_MAPS_KEY));
    }

    @Override
    public NexmoControllerKey getNexmoKey() {
        return new NexmoControllerKey(configuration.getString(VIEWSERVER_NEXMO_KEY), configuration.getString(VIEWSERVER_NEXMO_SECRET));
    }

    @Override
    public StripeApiKey getStripeKey() {
        return new StripeApiKey(configuration.getString(VIEWSERVER_STRIPE_KEY), configuration.getString(VIEWSERVER_STRIPE_SECRET));
    }

    @Override
    public VehicleDetailsApiKey getVehicleDetailsKey() {
        return new VehicleDetailsApiKey(configuration.getString(VIEWSERVER_VEHICLE_DETAILS_KEY));
    }

    public boolean isMock() {
        return configuration.getBoolean(VIEWSERVER_ISMOCK);
    }

    public boolean isTest() {
        return configuration.getBoolean(VIEWSERVER_ISTEST);
    }
}
