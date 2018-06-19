package com.shotgun.viewserver.servercomponents;

import com.fasterxml.jackson.databind.Module;
import com.shotgun.viewserver.ContainsProduct;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.ClusterDataSource;
import com.shotgun.viewserver.user.*;
import io.viewserver.Constants;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.IRecord;
import io.viewserver.network.IEndpoint;
import io.viewserver.network.IPeerSession;
import io.viewserver.network.SessionManager;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.operators.spread.SpreadFunctionRegistry;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.server.BasicServer;
import io.viewserver.server.components.NettyBasicServerComponent;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class ShotgunBasicServerComponents extends NettyBasicServerComponent{

    private final Debouncer debouncer;
    private boolean disconnectOnTimeout;
    private int timeoutInterval;
    private int heartbeatInterval;
    private BasicServer.Callable<IDatabaseUpdater> iDatabaseUpdaterFactory;
    private boolean isInitiallyMaster;
    private Subscription connectionCountSubscription;
    private ClientVersionInfo clientVersionInfo;
    private static final Logger log = LoggerFactory.getLogger(ShotgunBasicServerComponents.class);
    private List<Subscription> subscriptions;
    private List<ClusterServerConnectionWatcher> watchers;
    ScheduledExecutorService connectionCountExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("connectionCount"));
    private boolean isStopped;

    public ShotgunBasicServerComponents(String serverName,List<IEndpoint> endpointList,ClientVersionInfo clientVersionInfo,boolean disconnectOnTimeout, int timeoutInterval,int heartbeatInterval,BasicServer.Callable<IDatabaseUpdater> iDatabaseUpdaterFactory, boolean isInitiallyMaster) {
        super(serverName,endpointList);
        this.clientVersionInfo = clientVersionInfo;
        this.disconnectOnTimeout = disconnectOnTimeout;
        this.timeoutInterval = timeoutInterval;
        this.heartbeatInterval = heartbeatInterval;
        this.iDatabaseUpdaterFactory = iDatabaseUpdaterFactory;
        this.isInitiallyMaster = isInitiallyMaster;
        this.subscriptions = new ArrayList<>();
        this.watchers = new ArrayList<>();
        this.debouncer = new Debouncer();

        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
    }


    @Override
    public Observable start() {
        super.start();
        this.serverNetwork.setHeartbeatInterval(heartbeatInterval);
        this.serverNetwork.setDisconnectOnTimeout(disconnectOnTimeout);
        this.serverNetwork.setTimeoutInterval(timeoutInterval);
        this.getExecutionContext().getFunctionRegistry().register("containsProduct", ContainsProduct.class);
        this.getExecutionContext().getFunctionRegistry().register("getResponseField", GetPartnerResponseField.class);
        this.getExecutionContext().getFunctionRegistry().register("getOrderField", GetOrderField.class);
        this.getExecutionContext().getFunctionRegistry().register("getRelationship", GetRelationship.class);
        this.getExecutionContext().getFunctionRegistry().register("isOrderVisible", IsOrderVisible.class);
        this.getExecutionContext().getFunctionRegistry().register("isBlocked", IsBlocked.class);
        this.getExecutionContext().getFunctionRegistry().register("getLatLongFromAddress", GetLatLongFromAddress.class);
        this.getExecutionContext().getFunctionRegistry().register("isAfter", IsAfter.class);
        SpreadFunctionRegistry spreadColumnRegistry = this.getExecutionContext().getSpreadColumnRegistry();
        spreadColumnRegistry.register("getProductIdsFromContentTypeJSON", ProductSpreadFunction.class);
        spreadColumnRegistry.register("getCategoryIdsFromContentTypeJSON", CategorySpreadFunction.class);
        spreadColumnRegistry.register("getPartnerResponseIdsFromOrderDetail", DateNegotiatedOrderResponseSpreadFunction.class);

        spreadColumnRegistry.register(UserRelationshipsSpreadFunction.NAME, UserRelationshipsSpreadFunction.class);
        this.connectionCountSubscription = this.serverNetwork.getSessionManager().getOutput(Constants.OUT).observable().subscribeOn(Schedulers.from(connectionCountExecutor)).subscribe(c->modifyConnectionCount(c));
        return null;
    }

    @Override
    public void stop() {
        this.isStopped = true;
        IDatabaseUpdater updater = iDatabaseUpdaterFactory.call();
        updater.stop();
        super.stop();
        if(connectionCountSubscription != null){
            connectionCountSubscription.unsubscribe();
        }
        this.subscriptions.forEach(c->c.unsubscribe());
        this.watchers.forEach(c->c.close());
    }

    private void modifyConnectionCount(OperatorEvent operatorEvent) {
        if(isStopped){
            return;
        }
        if(operatorEvent.getEventType().equals(EventType.ROW_ADD) || operatorEvent.getEventType().equals(EventType.ROW_REMOVE) || operatorEvent.getEventType().equals(EventType.ROW_UPDATE)){
            int sessionCount = getNonClusterConnections();
            if(operatorEvent.getEventType().equals(EventType.ROW_ADD)){
                log.info("Modifying connection count as session added - {}", sessionCount);
            }
            else if(operatorEvent.getEventType().equals(EventType.ROW_UPDATE)){
                log.info("Modifying connection count as session updated - {}", sessionCount);
            }
            else{
                log.info("Modifying connection count as session removed - {}", sessionCount);
            }
            debouncer.debounce("connectionCount",() -> this.recalculateConnectionCount(sessionCount), 50, TimeUnit.MILLISECONDS);
        }
    }


    private void recalculateConnectionCount(int sessionCount) {
        log.info("No connections is - {}",sessionCount);
        IRecord record = new Record()
                .addValue("url",clientVersionInfo.getServerEndPoint())
                .addValue("clientVersion", clientVersionInfo.getCompatableClientVersion())
                .addValue("isMaster", anotherServerIsNotMaster(isInitiallyMaster))
                .addValue("noConnections", sessionCount);
        IDatabaseUpdater updater = iDatabaseUpdaterFactory.call();
        updater.addOrUpdateRow(TableNames.CLUSTER_TABLE_NAME,ClusterDataSource.getDataSource().getSchema(),record,IRecord.UPDATE_LATEST_VERSION).subscribeOn(Schedulers.from(connectionCountExecutor)).subscribe((res) -> log.info("No connections modified to - {}",sessionCount));
    }

    private boolean anotherServerIsNotMaster(boolean isMaster) {
        if(!isMaster){
            return isMaster;
        }
        KeyedTable table = (KeyedTable) this.getCatalog().getOperatorByPath(TableNames.CLUSTER_TABLE_NAME);
        IRowSequence rows = (table.getOutput().getAllRows());
        while(rows.moveNext()){
            String url = (String) ColumnHolderUtils.getColumnValue(table, "url", rows.getRowId());
            if(!url.equals(this.clientVersionInfo.getServerEndPoint()) && (Boolean.TRUE.equals(ColumnHolderUtils.getColumnValue(table, "isMaster", rows.getRowId())))){
                return false;
            }
        }
        return isMaster;
    }

    private int getNonClusterConnections() {
        IOutput output = this.serverNetwork.getSessionManager().getOutput(Constants.OUT);
        IRowSequence rows = ( output.getAllRows());
        Integer noConnections = 0;
        while(rows.moveNext()){
            String authenticationToken = (String) ColumnHolderUtils.getValue(output.getSchema().getColumnHolder(SessionManager.USERTYPE_COLUMN),rows.getRowId());
            if(authenticationToken != null){
                log.info("Detected connection - {}", ColumnHolderUtils.getValue(output.getSchema().getColumnHolder(SessionManager.SESSIONID_COLUMN),rows.getRowId()));
                noConnections++;
            }
        }
        return noConnections;
    }


    private ICatalog getCatalog() {
        return this.serverNetwork.getSessionManager().getCatalog();
    }


    @Override
    public void listen() {
        Observable<IOperator> clusterTableObservable = this.getCatalog().waitForOperatorAtThisPath(TableNames.CLUSTER_TABLE_NAME);
        log.info("MILESTONE: Waiting for cluster table");
        clusterTableObservable.subscribe(
                iOperator -> {
                    log.info("MILESTONE: Got cluster table");
                    KeyedTable clusterTable = (KeyedTable)iOperator;
                    if(clusterTable == null){
                        throw new RuntimeException("Cannot listen as cannot find load balancer table");
                    }
                    clusterTable.getOutput().observable().subscribe( ev -> trackOtherServerInCluster(ev));
                    IRecord record = new Record()
                            .addValue("url",clientVersionInfo.getServerEndPoint())
                            .addValue("version", -1)
                            .addValue("isMaster", anotherServerIsNotMaster(isInitiallyMaster))
                            .addValue("noConnections", 0)
                            .addValue("isOffline", false)
                            .addValue("clientVersion", clientVersionInfo.getCompatableClientVersion());
                    IDatabaseUpdater updater = iDatabaseUpdaterFactory.call();
                    log.info("MILESTONE: Attempting to update cluster before calling server listen");
                    updater.addOrUpdateRow(TableNames.CLUSTER_TABLE_NAME,ClusterDataSource.getDataSource().getSchema(),record,IRecord.UPDATE_LATEST_VERSION)
                            .subscribe(c-> ShotgunBasicServerComponents.super.listen());
                }
        );

    }

    private void trackOtherServerInCluster(OperatorEvent ev) {
        if(ev.getEventType().equals(EventType.ROW_ADD)){
            HashMap<String,Object> rowValues = (HashMap<String, Object>) ev.getEventData();
            String url = (String) rowValues.get("url");
            if(url.equals(this.clientVersionInfo.getServerEndPoint())){
                //this is me
                return;
            }
            String preferedTransportUrl = getPreferedTransportUrl(url);
            log.info("{} is tracking {} in cluster",this.clientVersionInfo.getServerEndPoint(), preferedTransportUrl);
            ClusterServerConnectionWatcher watcher = new ClusterServerConnectionWatcher(preferedTransportUrl,this.clientVersionInfo);
            this.watchers.add(watcher);
            this.subscriptions.add(watcher.waitForDeath().take(1).subscribe(c -> onOtherServerDies(watcher, url)));
        }
    }

    private void onOtherServerComesBackToLife(IPeerSession session, ClusterServerConnectionWatcher watcher, String  url) {
        log.info("Server {} has come back to life",session);
        this.subscriptions.add(watcher.waitForDeath().take(1).subscribe(c -> onOtherServerDies(watcher,url)));
    }

    private String getPreferedTransportUrl(String url) {
        String[] parts = url.split(",");
        return parts[0];
    }


    private void onOtherServerDies(ClusterServerConnectionWatcher watcher, String  url) {
        boolean otherServerMaster = isOtherServerMaster(url);
        IDatabaseUpdater updater = iDatabaseUpdaterFactory.call();
        this.subscriptions.add(watcher.waitForConnection().take(1).subscribe(c -> onOtherServerComesBackToLife((IPeerSession) c,watcher,url)));
        if(otherServerMaster){
            log.error("{} has detetcted that MASTER {} has died in cluster",this.clientVersionInfo.getServerEndPoint(), url);
        }else{
            log.error("{} has detetcted that SLAVE {} has died in cluster",this.clientVersionInfo.getServerEndPoint(), url);
        }
        if(otherServerMaster){
            if(iAmTheServerWithTheLeastConnectionsInTheCluster(url)) {
                log.info("{} has detetcted that MASTER {} has died in cluster. I'm becoming the new master",this.clientVersionInfo.getServerEndPoint(), url);

                IRecord record = new Record()
                        .addValue("url", clientVersionInfo.getServerEndPoint())
                        .addValue("clientVersion", clientVersionInfo.getCompatableClientVersion())
                        .addValue("isMaster", true);
                updater.addOrUpdateRow(TableNames.CLUSTER_TABLE_NAME, ClusterDataSource.getDataSource().getSchema(), record,IRecord.UPDATE_LATEST_VERSION).subscribe();
                record = new Record()
                        .addValue("url", url)
                        .addValue("isOffline", true)
                        .addValue("noConnections", 0)
                        .addValue("isMaster", false);
                updater.addOrUpdateRow(TableNames.CLUSTER_TABLE_NAME, ClusterDataSource.getDataSource().getSchema(), record,IRecord.UPDATE_LATEST_VERSION ).subscribe();
            }else{
                log.info("{} has detetcted that MASTER {} has died in cluster. I am not next in line for the throne",this.clientVersionInfo.getServerEndPoint(), url);
            }
        }else{
            IRecord record = new Record()
                    .addValue("url", url)
                    .addValue("isOffline", true)
                    .addValue("noConnections", 0)
                    .addValue("isMaster", false);
            updater.addOrUpdateRow(TableNames.CLUSTER_TABLE_NAME, ClusterDataSource.getDataSource().getSchema(), record,IRecord.UPDATE_LATEST_VERSION ).subscribe();
        }
    }

    private boolean isOtherServerMaster(String url) {
        KeyedTable table = (KeyedTable) this.getCatalog().getOperatorByPath(TableNames.CLUSTER_TABLE_NAME);
        return Boolean.TRUE.equals(ColumnHolderUtils.getColumnValue(table, "isMaster", url));
    }

    private boolean iAmTheServerWithTheLeastConnectionsInTheCluster(String disconnectedUrl) {
        KeyedTable table = (KeyedTable) this.getCatalog().getOperatorByPath(TableNames.CLUSTER_TABLE_NAME);
        IRowSequence rows = (table.getOutput().getAllRows());
        Integer noConnectionsOnAlternative = Integer.MAX_VALUE;
        String alternativeUrl = null;
        while(rows.moveNext()){
            String url = (String) ColumnHolderUtils.getColumnValue(table, "url", rows.getRowId());
            if(!url.equals(disconnectedUrl) && !(Boolean.TRUE.equals(ColumnHolderUtils.getColumnValue(table, "isOffline", rows.getRowId())))){
                Integer noConnections = (Integer) ColumnHolderUtils.getColumnValue(table, "noConnections", rows.getRowId());
                if(noConnections < noConnectionsOnAlternative || (noConnections.equals(noConnectionsOnAlternative) && url.hashCode() < alternativeUrl.hashCode())){
                    alternativeUrl = url;
                    noConnectionsOnAlternative = noConnections;
                }
            }
        }
        return clientVersionInfo.getServerEndPoint().equals(alternativeUrl);
    }


    public class Debouncer {
        private final ConcurrentHashMap<Object, Future<?>> delayedMap = new ConcurrentHashMap<>();

        /**
         * Debounces {@code callable} by {@code delay}, i.e., schedules it to be executed after {@code delay},
         * or cancels its execution if the method is called with the same key within the {@code delay} again.
         */
        public void debounce(final Object key, final Runnable runnable, long delay, TimeUnit unit) {
            log.info("Scheduled - " + runnable);
            final Future<?> prev = delayedMap.put(key, connectionCountExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("Running - " + runnable);
                        if(ShotgunBasicServerComponents.this.isStopped){
                            log.info("Not running as connection stopped");
                        }else{
                            runnable.run();
                        }
                    } finally {
                        delayedMap.remove(key);
                    }
                }
            }, delay, unit));
            if (prev != null) {
                prev.cancel(false);
            }
        }
    }


}



