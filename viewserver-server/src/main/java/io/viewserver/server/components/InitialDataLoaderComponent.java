package io.viewserver.server.components;

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.rx.RxUtils;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.Table;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            RxUtils.subscribeOnExecutionContext(loaderEntry.getValue().getRecords(loaderEntry.getKey()), executionContext, rec-> addRecordToTableOperator(loaderEntry.getKey(),loaderEntry.getValue().getSchemaConfig(),rec), err -> logger.error("Issue loading record", err));
        }
    }

    private void addRecordToTableOperator(String tableOperator, SchemaConfig schemaConfig, IRecord rec) {
        KeyedTable operator = (KeyedTable)serverCatalog.getOperator(tableOperator);
        if(operator == null){
            TableKeyDefinition tableKeyDefinition = new TableKeyDefinition(new ArrayList<>(schemaConfig.getKeyColumns()).toArray(new String[schemaConfig.getKeyColumns().size()]));
            operator = new KeyedTable(tableOperator, executionContext, serverCatalog, DataSourceHelper.getSchema(schemaConfig), new ChunkedColumnStorage(1024), tableKeyDefinition);
            operator.initialise(8);
        }
        try{
            logger.info("Added record rec to operator " + tableOperator);
            RecordUtils.addRecordToTableOperator(operator, rec);
        }catch (Exception ex){
            logger.error("Problem loading record",ex);
        }

    }
}
