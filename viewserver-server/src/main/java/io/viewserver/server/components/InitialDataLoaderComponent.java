package io.viewserver.server.components;

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.rx.RxUtils;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.Map;

public class InitialDataLoaderComponent implements IInitialDataLoaderComponent {
    private IRecordLoaderCollection recordLoaderCollection;
    private static final Logger logger = LoggerFactory.getLogger(InitialDataLoaderComponent.class);
    private IExecutionContext executionContext;
    private ICatalog serverCatalog;

    public InitialDataLoaderComponent(IExecutionContext executionContext, ICatalog serverCatalog, IRecordLoaderCollection recordLoaderCollection) {
        this.executionContext = executionContext;
        this.serverCatalog = serverCatalog;
        this.recordLoaderCollection = recordLoaderCollection;
    }

    @Override
    public void start() {
        for(Map.Entry<String,IRecordLoader> loaderEntry : recordLoaderCollection.getDataLoaders().entrySet()){
            RxUtils.subscribeOnExecutionContext(loaderEntry.getValue().getRecords(loaderEntry.getKey()), executionContext, rec-> addRecordToTableOperator(loaderEntry.getKey(),loaderEntry.getValue(),rec), err -> logger.error("Issue loading record", err));
        }
    }

    private void addRecordToTableOperator(String tableOperator, IRecordLoader recordLoader, IRecord rec) {
        SchemaConfig schemaConfig = recordLoader.getSchemaConfig();
        OperatorCreationConfig creationConfig = recordLoader.getCreationConfig();
        Observable<KeyedTable> operator = getKeyedTable(tableOperator, schemaConfig, creationConfig);
        try{
            RecordUtils.addRecordToTableOperator(operator, rec);
        }catch (Exception ex){
            logger.error("Problem loading record",ex);
        }

    }

    private Observable<KeyedTable> getKeyedTable(String tableOperator, SchemaConfig schemaConfig, OperatorCreationConfig creationConfig) {

        KeyedTable operator = getOperator(this.serverCatalog.getOperator(tableOperator));
        if(operator != null){
            return Observable.just(operator);
        }
        if(creationConfig.getOperator().equals(CreationStrategy.WAIT)){
            logger.info("Waiting for table - " + tableOperator);
            return this.serverCatalog.getOperatorObservable(tableOperator).map(c-> {
                logger.info("Found table - " + tableOperator);
                return getOperator(c);
            });
        }
        if(creationConfig.getOperator().equals(CreationStrategy.CREATE)){
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

    private KeyedTable getOperator(IOperator operator) {
        if(operator == null){
            return null;
        }
        if(! (operator instanceof  KeyedTable)){
            throw new RuntimeException(String.format("Operator " + operator.getPath() + " is not a keyed table"));
        }
        return (KeyedTable) operator;
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
            return this.serverCatalog.getOperatorObservable(catalogPath).map(
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
