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
import com.sun.tools.javac.util.List;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.h2.H2ConnectionFactory;
import io.viewserver.core.Utils;
import io.viewserver.network.EndpointFactoryRegistry;
import io.viewserver.network.IEndpoint;
import io.viewserver.server.BasicServer;
import io.viewserver.server.components.*;
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
    HashMap<String,Predicate<MutablePicoContainer>> ENVIRONMENT_CONFIGURATIONS = new HashMap<>();

    public ShotgunServerLauncher(){
        ENVIRONMENT_CONFIGURATIONS.put("mock",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("it",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("test",ShotgunServerLauncher::ConfigureForRealEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("prod",ShotgunServerLauncher::ConfigureForRealEnvironment);
    }

    private static void SharedConfig(MutablePicoContainer container){
        NettyBasicServerComponent basicServerComponent = new NettyBasicServerComponent(List.of(EndpointFactoryRegistry.createEndpoint(get("server.endpoint"))));
        container.addComponent(basicServerComponent);
        container.addComponent(basicServerComponent.getServerCatalog());
        container.addComponent(basicServerComponent.getExecutionContext());
        container.addComponent(basicServerComponent.getConfigurator());
        container.addComponent(ReportServerComponents.class);
        container.addComponent(DataSourceComponents.class);
        container.addComponent(ShotgunApplicationGraph.class);
        container.addComponent(InitialDataLoaderComponent.class);
        container.addComponent(BasicServer.class);
    }

    private static boolean ConfigureForMockEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new H2ConnectionFactory(null,null,get("h2.db.path")));
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

        String property1 = System.getProperty(property);
        if(property1 == null || "".equals(property)){
            throw new RuntimeException(String.format("\"%s\" is a required prop",property));
        }
        return Utils.replaceSystemTokens(property1);
    }


    public void run(String environment, boolean bootstrap) throws IOException {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        Properties props = new Properties();
        try(InputStream resourceStream = loader.getResourceAsStream(String.format("%s.properties",environment))) {
            props.load(resourceStream);
            for(String propName : props.stringPropertyNames()){
                System.setProperty(propName, (String) props.get(propName));
            }
        }

        MutablePicoContainer container = (new PicoBuilder()).withCaching().withLifecycle().build();

        ENVIRONMENT_CONFIGURATIONS.get(environment).test(container);

        if(bootstrap){
            container.getComponent(IApplicationSetup.class).run();
            return;
        }

        BasicServer server = container.getComponent(BasicServer.class);
        server.start();
    }
}
