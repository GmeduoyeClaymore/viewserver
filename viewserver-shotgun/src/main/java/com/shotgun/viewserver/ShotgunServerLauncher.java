package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.payments.StripeApiKey;
import com.shotgun.viewserver.servercomponents.*;
import com.shotgun.viewserver.setup.FirebaseApplicationSetup;
import com.shotgun.viewserver.setup.FirebaseTestApplicationSetup;
import com.shotgun.viewserver.setup.ShotgunApplicationGraph;
import com.shotgun.viewserver.setup.loaders.CompositeRecordLoaderCollection;
import com.shotgun.viewserver.setup.loaders.CsvRecordLoaderCollection;
import com.shotgun.viewserver.setup.loaders.FireBaseRecordLoaderCollection;
import com.shotgun.viewserver.setup.loaders.H2RecordLoaderCollection;
import com.shotgun.viewserver.user.NexmoControllerKey;
import io.viewserver.adapters.common.DirectTableUpdater;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseTableUpdater;
import io.viewserver.adapters.h2.H2ConnectionFactory;
import io.viewserver.adapters.jdbc.JdbcConnectionFactory;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.Utils;
import io.viewserver.network.EndpointFactoryRegistry;
import io.viewserver.server.BasicServer;
import io.viewserver.server.components.*;
import io.viewserver.server.setup.H2ApplicationSetup;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.HashMap;
import java.util.function.Predicate;

import static com.shotgun.viewserver.PropertyUtils.loadProperties;

public class ShotgunServerLauncher{
    HashMap<String,Predicate<MutablePicoContainer>> ENVIRONMENT_CONFIGURATIONS = new HashMap<>();
    private BasicServer server;

    public ShotgunServerLauncher(){
        ENVIRONMENT_CONFIGURATIONS.put("mock",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("integration",ShotgunServerLauncher::ConfigureForEndToEndTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("integration_running",ShotgunServerLauncher::ConfigureForEndToEndTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("it",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("it_running",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("test",ShotgunServerLauncher::ConfigureForRealEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("prod",ShotgunServerLauncher::ConfigureForRealEnvironment);
    }

    private static void SharedConfig(MutablePicoContainer container){
        ShotgunBasicServerComponents basicServerComponent = new ShotgunBasicServerComponents(EndpointFactoryRegistry.createEndpoints(get("server.endpoint")));
        container.addComponent(basicServerComponent);
        container.addComponent(basicServerComponent.getServerCatalog());
        container.addComponent(basicServerComponent.getExecutionContext());
        container.addComponent(basicServerComponent.getConfigurator());
        container.addComponent(ReportServerComponents.class);
        container.addComponent(DataSourceComponents.class);
        container.addComponent(BasicSubscriptionComponent.class);
        container.addComponent(ShotgunApplicationGraph.class);
        container.addComponent(InitialDataLoaderComponent.class);
        container.addComponent(BasicServer.class);
    }

    private static boolean ConfigureForTestEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new H2ConnectionFactory("","",get("h2.db.path")));
        container.addComponent(H2ApplicationSetup.class);
        container.addComponent(RealShotgunControllersComponents.class);
        container.addComponent(DirectTableUpdater.class);
        container.addComponent(new CompositeRecordLoaderCollection(
                () -> new H2RecordLoaderCollection(container.getComponent(JdbcConnectionFactory.class)),
                () -> new CsvRecordLoaderCollection(get("csv.data.path"))
        ));
        return true;
    }

    private static boolean ConfigureForMockEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new H2ConnectionFactory("","",get("h2.db.path")));
        container.addComponent(H2ApplicationSetup.class);
        container.addComponent(MockShotgunControllersComponents.class);
        container.addComponent(DirectTableUpdater.class);
        container.addComponent(new CompositeRecordLoaderCollection(
                () -> new H2RecordLoaderCollection(container.getComponent(JdbcConnectionFactory.class)),
                () -> new CsvRecordLoaderCollection(get("csv.data.path"))
        ));
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
        container.addComponent(new FirebaseApplicationSetup(
                container.getComponent(FirebaseConnectionFactory.class),
                container.getComponent(IApplicationGraphDefinitions.class),
                get("csv.data.path")
        ));
        container.addComponent(DatasourceFirebaseTableUpdater.class);
        container.addComponent(FireBaseRecordLoaderCollection.class);
        container.addComponent(RealShotgunControllersComponents.class);
        return true;
    }
    private static boolean ConfigureForEndToEndTestEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new NexmoControllerKey(get("nexmo.key"),get("nexmo.secret")));
        container.addComponent(new StripeApiKey(get("stripe.key"),get("stripe.secret")));
        container.addComponent(new BasicAWSCredentials(get("awsCredentials.accessKey"),get("awsCredentials.secretKey")));
        container.addComponent(new MessagingApiKey(get("messaging.api.key")));
        container.addComponent(new VehicleDetailsApiKey(get("vehicle.details.key")));
        container.addComponent(new MapsControllerKey(get("google.mapsControllerKey"),false));
        container.addComponent(new FirebaseConnectionFactory(get("firebase.keyfilePath")));
        container.addComponent(new FirebaseTestApplicationSetup(
                container.getComponent(FirebaseConnectionFactory.class),
                container.getComponent(IApplicationGraphDefinitions.class),
                get("csv.data.path")
        ));
        container.addComponent(DatasourceFirebaseTableUpdater.class);
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


    public void run(String environment, boolean bootstrap) {

        ExecutionContext.blockThreadAssertion  = true;

        loadProperties(environment);

        MutablePicoContainer container = (new PicoBuilder()).withCaching().withLifecycle().build();

        Predicate<MutablePicoContainer> mutablePicoContainerPredicate = ENVIRONMENT_CONFIGURATIONS.get(environment);

        if(mutablePicoContainerPredicate == null){
            throw new RuntimeException("Cannot find environment with name " + environment);
        }

        mutablePicoContainerPredicate.test(container);

        if(bootstrap){
            container.getComponent(IApplicationSetup.class).run();
            return;
        }

        server = container.getComponent(BasicServer.class);
        server.start();
        ExecutionContext.blockThreadAssertion  = false;
    }

    public void stop(){
        if(server != null){
            server.stop();
        }
    }


}


