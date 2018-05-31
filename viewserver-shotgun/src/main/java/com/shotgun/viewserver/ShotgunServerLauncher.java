package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.payments.StripeApiKey;
import com.shotgun.viewserver.servercomponents.*;
import com.shotgun.viewserver.setup.*;
import com.shotgun.viewserver.setup.loaders.*;
import com.shotgun.viewserver.user.NexmoControllerKey;
import io.viewserver.adapters.common.DirectTableUpdater;
import io.viewserver.adapters.h2.H2ConnectionFactory;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
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


public class  ShotgunServerLauncher{
    HashMap<String,Predicate<MutablePicoContainer>> ENVIRONMENT_CONFIGURATIONS = new HashMap<>();
    private BasicServer server;

    public ShotgunServerLauncher(){

        ENVIRONMENT_CONFIGURATIONS.put("mock",ShotgunServerLauncher::ConfigureForTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("integration",ShotgunServerLauncher::ConfigureForEndToEndTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("integration_teamcity",ShotgunServerLauncher::ConfigureForEndToEndTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("integration_running",ShotgunServerLauncher::ConfigureForEndToEndTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("it",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("it_running",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("test",ShotgunServerLauncher::ConfigureForTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("staging",ShotgunServerLauncher::ConfigureForStagingEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("paul",ShotgunServerLauncher::ConfigureForStagingEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("prod",ShotgunServerLauncher::ConfigureForProdEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("pre-prod",ShotgunServerLauncher::ConfigureForPreProdEnvironment);
    }

    private static void SharedConfig(MutablePicoContainer container){
        ShotgunBasicServerComponents basicServerComponent = new ShotgunBasicServerComponents(EndpointFactoryRegistry.createEndpoints(get("server.endpoint")), Boolean.parseBoolean(get("server.disconnectOnTimeout")), Integer.parseInt(get("server.timeoutInterval")));
        container.addComponent(basicServerComponent);
        container.addComponent(basicServerComponent.getServerCatalog());
        container.addComponent(basicServerComponent.getExecutionContext());
        container.addComponent(basicServerComponent.getConfigurator());
        container.addComponent(ReportServerComponents.class);
        container.addComponent(DataSourceComponents.class);
        container.addComponent(BasicSubscriptionComponent.class);
        container.addComponent(ShotgunApplicationGraph.class);
        container.addComponent(InitialDataLoaderComponent.class);
        container.addComponent(UserOrderNotificationComponent.class);
        container.addComponent(BasicServer.class);
    }

    /*private static boolean ConfigureForTestEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new NexmoControllerKey(get("nexmo.key"),get("nexmo.secret")));
        container.addComponent(new StripeApiKey(get("stripe.key"),get("stripe.secret")));
        container.addComponent(new BasicAWSCredentials(get("awsCredentials.accessKey"),get("awsCredentials.secretKey")));
        container.addComponent(new MessagingApiKey(get("messaging.api.key"), true));
        container.addComponent(new VehicleDetailsApiKey(get("vehicle.details.key")));
        container.addComponent(new MapsControllerKey(get("google.mapsControllerKey")));
        container.addComponent(new H2ConnectionFactory("","",get("h2.db.path")));
        container.addComponent(H2ApplicationSetup.class);
        container.addComponent(RealShotgunControllersComponents.class);
        container.addComponent(DirectTableUpdater.class);
        container.addComponent(new CompositeRecordLoaderCollection(
                () -> new ApplicationGraphLoaderCollection(container.getComponent(IApplicationGraphDefinitions.class)),
                () -> new CsvRecordLoaderCollection(get("csv.data.path"))
        ));
        return true;
    }*/

    private static boolean ConfigureForMockEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new H2ConnectionFactory("","",get("h2.db.path")));
        container.addComponent(H2ApplicationSetup.class);
        container.addComponent(MockShotgunControllersComponents.class);
        container.addComponent(DirectTableUpdater.class);
        container.addComponent(new CompositeRecordLoaderCollection(
                () -> new ApplicationGraphLoaderCollection(container.getComponent(IApplicationGraphDefinitions.class)),
                () -> new CsvRecordLoaderCollection(get("csv.data.path"))
        ));
        return true;
    }

    private static boolean ConfigureForPreProdEnvironment(MutablePicoContainer container) {
        return ConfigureForRealEnvironment(container,false, false);
    }

    private static boolean ConfigureForProdEnvironment(MutablePicoContainer container) {
        return ConfigureForRealEnvironment(container,false, false);
    }
    private static boolean ConfigureForStagingEnvironment(MutablePicoContainer container) {
        return ConfigureForRealEnvironment(container,true, false);
    }
    private static boolean ConfigureForTestEnvironment(MutablePicoContainer container) {
        return ConfigureForRealEnvironment(container,true, true);
    }
    private static boolean ConfigureForRealEnvironment(MutablePicoContainer container, boolean blockRemoteSending, boolean doMockPaymentController) {
        SharedConfig(container);
        container.addComponent(new NexmoControllerKey(get("nexmo.key"),get("nexmo.secret")));
        container.addComponent(new StripeApiKey(get("stripe.key"),get("stripe.secret"), doMockPaymentController));
        container.addComponent(new BasicAWSCredentials(get("awsCredentials.accessKey"),get("awsCredentials.secretKey")));
        container.addComponent(new MessagingApiKey(get("messaging.api.key"), blockRemoteSending));
        container.addComponent(new VehicleDetailsApiKey(get("vehicle.details.key")));
        container.addComponent(new MapsControllerKey(get("google.mapsControllerKey")));
        container.addComponent(new MongoConnectionFactory(get("mongo.connectionUri"), get("mongo.databaseName")));
        container.addComponent(new MongoApplicationSetup(
                container.getComponent(MongoConnectionFactory.class),
                get("csv.data.path")
        ));
        container.addComponent(DatasourceMongoTableUpdater.class);
        container.addComponent(new CompositeRecordLoaderCollection(
                () -> new ApplicationGraphLoaderCollection(container.getComponent(IApplicationGraphDefinitions.class)),
                () -> new MongoRecordLoaderCollection(container.getComponent(MongoConnectionFactory.class))
        ));
        container.addComponent(RealShotgunControllersComponents.class);
        return true;
    }
    private static boolean ConfigureForEndToEndTestEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new NexmoControllerKey(get("nexmo.key"),get("nexmo.secret")));
        container.addComponent(new StripeApiKey(get("stripe.key"),get("stripe.secret"), true));
        container.addComponent(new BasicAWSCredentials(get("awsCredentials.accessKey"),get("awsCredentials.secretKey")));
        container.addComponent(new MessagingApiKey(get("messaging.api.key"), true));
        container.addComponent(new VehicleDetailsApiKey(get("vehicle.details.key")));
        container.addComponent(new MapsControllerKey(get("google.mapsControllerKey")));
        container.addComponent(new MongoConnectionFactory(get("mongo.connectionUri"), get("mongo.databaseName")));
        container.addComponent(new MongoTestApplicationSetup(
                container.getComponent(MongoConnectionFactory.class),
                get("csv.data.path")
        ));
        container.addComponent(DatasourceMongoTableUpdater.class);
        container.addComponent(new CompositeRecordLoaderCollection(
                () -> new ApplicationGraphLoaderCollection(container.getComponent(IApplicationGraphDefinitions.class)),
                () -> new MongoRecordLoaderCollection(container.getComponent(MongoConnectionFactory.class))
        ));
        container.addComponent(MockShotgunControllersComponents.class);
        return true;
    }

    private static String get(String property){

        String property1 = System.getProperty(property);
        if(property1 == null || "".equals(property)){
            throw new RuntimeException(String.format("\"%s\" is a required prop",property));
        }
        return Utils.replaceSystemTokens(property1);
    }


    public void run(String environment, boolean bootstrap, boolean complete) {

        ExecutionContext.blockThreadAssertion  = true;

        loadProperties(environment);

        MutablePicoContainer container = (new PicoBuilder()).withCaching().withLifecycle().build();

        Predicate<MutablePicoContainer> mutablePicoContainerPredicate = ENVIRONMENT_CONFIGURATIONS.get(environment);

        if(mutablePicoContainerPredicate == null){
            throw new RuntimeException("Cannot find environment with name " + environment);
        }

        mutablePicoContainerPredicate.test(container);

        if(bootstrap){
            container.getComponent(IApplicationSetup.class).run(complete);
            //return;
        }

        server = container.getComponent(BasicServer.class);
        server.registerComponent(() -> container.getComponent(UserOrderNotificationComponent.class));
        server.start();
        ExecutionContext.blockThreadAssertion  = false;
    }

    public void stop(){
        if(server != null){
            server.stop();
        }
    }


}


