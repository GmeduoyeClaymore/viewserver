package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.payments.StripeApiKey;
import com.shotgun.viewserver.servercomponents.*;
import com.shotgun.viewserver.setup.MongoApplicationSetup;
import com.shotgun.viewserver.setup.MongoTestApplicationSetup;
import com.shotgun.viewserver.setup.ShotgunApplicationGraph;
import com.shotgun.viewserver.setup.loaders.ApplicationGraphLoaderCollection;
import com.shotgun.viewserver.setup.loaders.CompositeRecordLoaderCollection;
import com.shotgun.viewserver.setup.loaders.CsvRecordLoaderCollection;
import com.shotgun.viewserver.setup.loaders.MongoRecordLoaderCollection;
import com.shotgun.viewserver.user.NexmoControllerKey;
import io.viewserver.adapters.common.DirectTableUpdater;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.h2.H2ConnectionFactory;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.Utils;
import io.viewserver.network.EndpointFactoryRegistry;
import io.viewserver.server.BasicServer;
import io.viewserver.server.components.DataSourceComponents;
import io.viewserver.server.components.IBasicServerComponents;
import io.viewserver.server.components.InitialDataLoaderComponent;
import io.viewserver.server.components.ReportServerComponents;
import io.viewserver.server.setup.H2ApplicationSetup;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import static com.shotgun.viewserver.PropertyUtils.loadProperties;


public class  ShotgunServerLauncher{
    HashMap<String,Predicate<MutablePicoContainer>> ENVIRONMENT_CONFIGURATIONS = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(ShotgunServerLauncher.class);
    //private BasicServer server;
    private List<BasicServer> servers = new ArrayList<>();

    public ShotgunServerLauncher(){
        ENVIRONMENT_CONFIGURATIONS.put("mock",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("mock2",ShotgunServerLauncher::ConfigureForMockEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("integration",ShotgunServerLauncher::ConfigureForMongoFeatureTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("integration_teamcity",ShotgunServerLauncher::ConfigureForMongoFeatureTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("integration_running",ShotgunServerLauncher::ConfigureForMongoFeatureTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("it",ShotgunServerLauncher::ConfigureForMockFeatureTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("it_running",ShotgunServerLauncher::ConfigureForMockFeatureTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("test",ShotgunServerLauncher::ConfigureForTestEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("staging",ShotgunServerLauncher::ConfigureForStagingEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("paul",ShotgunServerLauncher::ConfigureForStagingEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("gbemiga",ShotgunServerLauncher::ConfigureForDevelopmentEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("gbemiga2",ShotgunServerLauncher::ConfigureForDevelopmentEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("gbemiga3",ShotgunServerLauncher::ConfigureForDevelopmentEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("prod",ShotgunServerLauncher::ConfigureForProdEnvironment);
        ENVIRONMENT_CONFIGURATIONS.put("pre-prod",ShotgunServerLauncher::ConfigureForPreProdEnvironment);
    }

    private static void SharedConfig(MutablePicoContainer container){
        ClientVersionInfo cvi = new ClientVersionInfo(get("server.endpoint"), get("server.version"));
        ShotgunBasicServerComponents basicServerComponent = new ShotgunBasicServerComponents(get("server.name"),EndpointFactoryRegistry.createEndpoints(get("server.endpoint")),cvi,Boolean.parseBoolean(get("server.disconnectOnTimeout")),
                Integer.parseInt(get("server.timeoutInterval")),Integer.parseInt(get("server.heartBeatInterval")),() -> container.getComponent(IDatabaseUpdater.class), Boolean.parseBoolean(get("server.isMaster")));
        container.addComponent(cvi);
        container.addComponent(basicServerComponent);
        container.addComponent(basicServerComponent.getServerCatalog());
        container.addComponent(basicServerComponent.getExecutionContext());
        container.addComponent(basicServerComponent.getConfigurator());
        container.addComponent(ReportServerComponents.class);
        container.addComponent(DataSourceComponents.class);
        container.addComponent(ShotgunSubscriptionComponent.class);
        container.addComponent(ShotgunApplicationGraph.class);
        container.addComponent(InitialDataLoaderComponent.class);
        container.addComponent(UserOrderNotificationComponent.class);
        container.addComponent(BasicServer.class);
    }

    private static boolean ConfigureForMockEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new H2ConnectionFactory("","",get("h2.db.path")));
        container.addComponent(H2ApplicationSetup.class);
        container.addComponent(DirectTableUpdater.class);
        container.addComponent(new ImageUploadLocation(get("upload.location")));
        container.addComponent(new MockRunnableShotgunControllersComponents(
                container.getComponent(IBasicServerComponents.class),
                container.getComponent(IDatabaseUpdater.class),
                get("csv.data.path"),
                container.getComponent(ImageUploadLocation.class)
        ));
        container.addComponent(new CompositeRecordLoaderCollection(
                () -> new ApplicationGraphLoaderCollection(container.getComponent(IApplicationGraphDefinitions.class)),
                () -> new CsvRecordLoaderCollection(get("csv.data.path"))
        ));
        return true;
    }

    private static boolean ConfigureForMockFeatureTestEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new H2ConnectionFactory("","",get("h2.db.path")));
        container.addComponent(H2ApplicationSetup.class);
        container.addComponent(DirectTableUpdater.class);
        container.addComponent(new MockTestShotgunControllersComponents(
                container.getComponent(IBasicServerComponents.class),
                container.getComponent(IDatabaseUpdater.class),
                get("csv.data.path")
        ));
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
        return ConfigureForRealEnvironment(container,false, false);
    }

    private static boolean ConfigureForTestEnvironment(MutablePicoContainer container) {
        return ConfigureForRealEnvironment(container,true, true);
    }

    private static boolean ConfigureForRealEnvironment(MutablePicoContainer container, boolean blockRemoteSending, boolean doMockPaymentController) {
        SharedConfig(container);
        container.addComponent(new NexmoControllerKey(get("nexmo.domain",true), get("nexmo.key"),get("nexmo.secret")));
        container.addComponent(new StripeApiKey(get("stripe.key"),get("stripe.secret"), doMockPaymentController));
        container.addComponent(new ImageUploadLocation(get("upload.location")));
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
                () -> new MongoRecordLoaderCollection(container.getComponent(MongoConnectionFactory.class), get("server.name"))
        ));
        container.addComponent(S3RealShotgunControllersComponents.class);
        return true;
    }
    public static boolean ConfigureForDevelopmentEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        boolean doMockPaymentController = true;
        boolean blockRemoteSending = true;
        container.addComponent(new NexmoControllerKey(get("nexmo.domain",true), get("nexmo.key"),get("nexmo.secret")));
        container.addComponent(new StripeApiKey(get("stripe.key"),get("stripe.secret"), doMockPaymentController));
        container.addComponent(new ImageUploadLocation(get("upload.location")));
        container.addComponent(new MessagingApiKey(get("messaging.api.key"), blockRemoteSending));
        container.addComponent(new BasicAWSCredentials(get("awsCredentials.accessKey"),get("awsCredentials.secretKey")));
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
                () -> new MongoRecordLoaderCollection(container.getComponent(MongoConnectionFactory.class), get("server.name"))
        ));
        container.addComponent(S3RealShotgunControllersComponents.class);
        return true;
    }
    private static boolean ConfigureForMongoFeatureTestEnvironment(MutablePicoContainer container) {
        SharedConfig(container);
        container.addComponent(new NexmoControllerKey(get("nexmo.domain",true), get("nexmo.key"),get("nexmo.secret")));
        container.addComponent(new StripeApiKey(get("stripe.key"),get("stripe.secret"), true));
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
                () -> new MongoRecordLoaderCollection(container.getComponent(MongoConnectionFactory.class), get("server.name"))
        ));
        container.addComponent(new MockTestShotgunControllersComponents(
                container.getComponent(IBasicServerComponents.class),
                container.getComponent(IDatabaseUpdater.class),
                get("csv.data.path")
                ));
        return true;
    }

    private static String get(String property){
        return get(property,false);
    }
    private static String get(String property, boolean optional){

        String property1 = System.getProperty(property);
        if(property1 == null || "".equals(property)){
            if(!optional)
                throw new RuntimeException(String.format("\"%s\" is a required prop",property));
            else
                return null;
        }
        return Utils.replaceSystemTokens(property1);
    }


    public rx.Observable run(String environment, boolean bootstrap, boolean complete) {

        ExecutionContext.blockThreadAssertion  = true;

        loadProperties(environment);

        MutablePicoContainer container = new PicoBuilder().withCaching().withLifecycle().build();

        Predicate<MutablePicoContainer> mutablePicoContainerPredicate = ENVIRONMENT_CONFIGURATIONS.get(environment);

        if(mutablePicoContainerPredicate == null){
            throw new RuntimeException("Cannot find environment with name " + environment);
        }

        mutablePicoContainerPredicate.test(container);

        if(bootstrap){
            container.getComponent(IApplicationSetup.class).run(complete);
        }

        BasicServer server = container.getComponent(BasicServer.class);

        server.setServerName(get("server.name"));

        log.info("MILESTONE: Kicking off basic server {}",System.getProperty("server.endpoint"));

        server.registerComponent(() -> container.getComponent(UserOrderNotificationComponent.class));
        rx.Observable result = server.start();

        log.info("MILESTONE: Kicked off basic server {}",System.getProperty("server.endpoint"));
        this.servers.add(server);
        ExecutionContext.blockThreadAssertion  = false;

        return result;
    }

    public void stop(){
        servers.forEach(c-> c.stop());
    }

}


