package io.viewserver.server.components;

import io.viewserver.catalog.ICatalog;
import io.viewserver.controller.ControllerUtils;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataLoaderComponent implements IInitialDataLoaderComponent {
    private IRecordLoaderCollection recordLoaderCollection;
    private static final Logger logger = LoggerFactory.getLogger(DataLoaderComponent.class);
    private IExecutionContext executionContext;
    private ICatalog serverCatalog;
    private List<Subscription> subscriptions;
    private Executor backgroundExecutor = Executors.newFixedThreadPool(1,new NamedThreadFactory("dataLoader"));
    public DataLoaderComponent(IExecutionContext executionContext, ICatalog serverCatalog, IRecordLoaderCollection recordLoaderCollection) {
        this.executionContext = executionContext;
        this.serverCatalog = serverCatalog;
        this.recordLoaderCollection = recordLoaderCollection;
        subscriptions = new ArrayList<>();
    }

    @Override
    public Observable start() {
        logger.info("Starting initial data loader");
        if(recordLoaderCollection.getDataLoaders().isEmpty()){
            logger.info("Returning straight away as no record loaders are found");
            return Observable.just(true);
        }

        List<Observable<Object>> readyObservables = new ArrayList<>();
        List<Observable<IOperator>> operatorObservables = new ArrayList<>();
        logger.info("Found {} loaders", recordLoaderCollection.getDataLoaders().entrySet().size());
        List<String> pendingLoaders = new ArrayList<>(recordLoaderCollection.getDataLoaders().keySet());
        for(Map.Entry<String,IRecordLoader> loaderEntry : recordLoaderCollection.getDataLoaders().entrySet()){
            IRecordLoader recordLoader = loaderEntry.getValue();
            SchemaConfig schemaConfig = recordLoader.getSchemaConfig();
            OperatorCreationConfig creationConfig = recordLoader.getCreationConfig();
            logger.info("Loader {} loaders waiting for table", loaderEntry.getKey());
            Observable<IOperator> operator = getOperator(loaderEntry.getKey(), schemaConfig, creationConfig);
            operatorObservables.add(operator);
            this.subscriptions.add(operator.subscribe(kt -> onOperator(kt,loaderEntry.getValue())));
            readyObservables.add(loaderEntry.getValue().readyObservable().map(res -> logReady(res,loaderEntry.getKey(),pendingLoaders)));
        }
        FuncN<?> onCompletedAll = new FuncN<Object>() {
            @Override
            public Object call(Object... objects) {
                logger.info(String.format("COMPLETED FINISHED WAITING Record loaders - %s",readyObservables.size()));
                return true;
            }
        };
        FuncN<?> onAllOperatorsInPlace = new FuncN<Object>() {
            @Override
            public Object call(Object... objects) {
                logger.info(String.format("COMPLETED FINISHED WAITING Record loaders - %s",readyObservables.size()));
                return true;
            }
        };
        Observable started = ObservableUtils.zip(operatorObservables).observeOn(Schedulers.from(backgroundExecutor)).take(1).timeout(30,TimeUnit.SECONDS,Observable.error(new RuntimeException("All Operators for loading not registered in 30 seconds"))).flatMap(
                res -> recordLoaderCollection.start()
        );
        return started.take(1).observeOn(Schedulers.from(executionContext.getReactor().getExecutor()));
    }

    private Object logReady(Object res, String key, List<String> pendingLoaders) {
        synchronized (pendingLoaders){
            pendingLoaders.remove(key);
            logger.info("Completed {} still waiting for {}\n",key,String.join("\n",pendingLoaders));
        }
        return res;
    }

    private void onOperator(IOperator operator, IRecordLoader recordLoader){
        logger.info("Successfully operator table " + operator.getPath() + " that we were waiting for");
        recordLoader.loadRecords(operator);
    }

    @Override
    public void stop() {
        for (Subscription sub: subscriptions) {
            sub.unsubscribe();
        }
        recordLoaderCollection.close();
    }

    private Observable<IOperator> getOperator(String tableOperator, SchemaConfig schemaConfig, OperatorCreationConfig creationConfig) {

        IOperator operator = this.serverCatalog.getOperatorByPath(tableOperator);
        if(operator != null){
            logger.info("Already got so just returning - " + tableOperator);
            return Observable.just(operator);
        }
        if(creationConfig.getOperator().equals(CreationStrategy.WAIT)){
            logger.info("Waiting for table - " + tableOperator);
            return this.serverCatalog.waitForOperatorAtThisPath(tableOperator).map(c-> {
                logger.info("Found table - " + tableOperator);
                return c;
            });
        }
        if(creationConfig.getOperator().equals(CreationStrategy.CREATE)){
            logger.info("Creating table - " + tableOperator);
            int slashIndex = tableOperator.lastIndexOf("/");
            ICatalog serverCatalog = this.serverCatalog;
            String operatorNameToCreate = tableOperator;

            if(slashIndex >= 0){
                operatorNameToCreate = tableOperator.substring(slashIndex + 1);
                String catalogPath = tableOperator.substring(0,slashIndex);
                logger.info(String.format("Attempting to create operator named %s in catalog %s",operatorNameToCreate,catalogPath));
                String finalOperatorNameToCreate = operatorNameToCreate;
                return getCatalog(creationConfig, catalogPath).map(cat -> createTable(schemaConfig, cat, finalOperatorNameToCreate));
            }

            return Observable.just(createTable(schemaConfig, serverCatalog, operatorNameToCreate));

        }
        throw new RuntimeException("Unable to resolve operator named " + tableOperator + " change the creation config to allow creation maybe ?? or work out why this operator isnt there anymore");
    }



    private KeyedTable createTable(SchemaConfig schemaConfig, ICatalog serverCatalog, String operatorNameToCreate) {
        TableKeyDefinition tableKeyDefinition = new TableKeyDefinition(new ArrayList<>(schemaConfig.getKeyColumns()).toArray(new String[schemaConfig.getKeyColumns().size()]));
        logger.info("Creating table {} in catalog {}", operatorNameToCreate, serverCatalog.getName());
        KeyedTable operator = new KeyedTable(operatorNameToCreate, executionContext, serverCatalog, ColumnHolderUtils.getSchema(schemaConfig), new ChunkedColumnStorage(1024), tableKeyDefinition);
        operator.initialise(8);
        return operator;
    }

    private Observable<ICatalog> getCatalog(OperatorCreationConfig creationConfig, String catalogPath) {
        ICatalog serverCatalog = this.serverCatalog.getDescendant(catalogPath);
        if(serverCatalog != null){
            return Observable.just(serverCatalog);
        }

        if(creationConfig.getCatalog().equals(CreationStrategy.WAIT)){
            logger.info("Waiting for catalog - " + catalogPath);
            return this.serverCatalog.waitForOperatorAtThisPath(catalogPath).map(
                    cat -> {
                        logger.info("Found catalog - " + catalogPath);
                        return (ICatalog)cat;
                    }
            );
        }
        if(creationConfig.getCatalog().equals(CreationStrategy.CREATE)){
            logger.info(String.format("Could not find a catalog for path %s so creating",catalogPath));
            serverCatalog = this.serverCatalog.createDescendant(catalogPath);
            return  Observable.just(serverCatalog);
        }

        throw new RuntimeException("Unable to create catalog at path " + catalogPath + " change the creation config to allow catalog creation maybe ?? or work out why this catalog isnt there anymore");
    }
}
