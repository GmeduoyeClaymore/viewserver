package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.payments.StripeApiKey;
import com.shotgun.viewserver.servercomponents.DirectTableUpdater;
import com.shotgun.viewserver.servercomponents.FirebaseTableUpdater;
import com.shotgun.viewserver.servercomponents.MockShotgunControllersComponents;
import com.shotgun.viewserver.servercomponents.RealShotgunControllersComponents;
import com.shotgun.viewserver.setup.FirebaseApplicationSetup;
import com.shotgun.viewserver.setup.ShotgunApplicationGraph;
import com.shotgun.viewserver.setup.loaders.CsvRecordLoaderCollection;
import com.shotgun.viewserver.setup.loaders.FireBaseRecordLoaderCollection;
import com.shotgun.viewserver.user.NexmoControllerKey;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.server.BasicServer;
import io.viewserver.server.setup.H2ApplicationSetup;
import io.viewserver.server.setup.IApplicationSetup;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.Predicate;

public class ShotgunServerLauncher{
    MutablePicoContainer container = (new PicoBuilder()).withCaching().withLifecycle().build();
    HashMap<String,Predicate<MutablePicoContainer>> ENVIRONMENT_CONFIGURATIONS = new HashMap<>();

    public ShotgunServerLauncher(){
        ENVIRONMENT_CONFIGURATIONS.put("mock",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("it",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("test",ShotgunServerLauncher::ConfigureForRealEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("prod",ShotgunServerLauncher::ConfigureForRealEnvironment);
    }

    private static void SharedConfig(MutablePicoContainer container){
        container.addComponent(BasicServer.class);
        container.addComponent(ShotgunApplicationGraph.class);
    }

    private static boolean ConfigureForMockEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(H2ApplicationSetup.class);
        container.addComponent(MockShotgunControllersComponents.class);
        container.addComponent(DirectTableUpdater.class);
        container.addComponent(CsvRecordLoaderCollection.class);
        return true;
    }
    private static boolean ConfigureForRealEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new NexmoControllerKey(get("nexmo.key"),get("nexmo.secret")));
        container.addComponent(new StripeApiKey(get("stripe.key"),get("stripe.secret")));
        container.addComponent(new BasicAWSCredentials(get("awsCredentials.accessKey"),get("awsCredentials.secretKey")));
        container.addComponent(new MessagingApiKey(get("messaging.api.key")));
        container.addComponent(new VehicleDetailsApiKey(get("vehicle.details.key")));
        container.addComponent(new MapsControllerKey(get("google.mapsControllerKey"),false));
        container.addComponent(new FirebaseConnectionFactory(get("firebase.keyfilePath")));
        container.addComponent(FirebaseApplicationSetup.class);
        container.addComponent(FirebaseTableUpdater.class);
        container.addComponent(FireBaseRecordLoaderCollection.class);
        container.addComponent(RealShotgunControllersComponents.class);
        return true;
    }

    private static String get(String property){
        return System.getProperty(property);
    }


    public void run(String environment, boolean bootstrap) throws IOException {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        container.addComponent(ShotgunApplicationGraph.class);

        Properties props = new Properties();
        try(InputStream resourceStream = loader.getResourceAsStream(String.format("%s.properties",environment))) {
            props.load(resourceStream);
        }
        ENVIRONMENT_CONFIGURATIONS.get(environment).test(container);

        if(bootstrap){
            container.getComponent(IApplicationSetup.class).run();
            return;
        }

        BasicServer server = container.getComponent(BasicServer.class);
        server.start();
    }
}
