package io.viewserver.server.components;

import io.viewserver.datasource.*;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.Table;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class InitialDataLoaderComponent implements IInitialDataLoaderComponent {
    private IBasicServerComponents basicServerComponents;
    private IRecordLoaderCollection recordLoaderCollection;
    private static final Logger logger = LoggerFactory.getLogger(InitialDataLoaderComponent.class);

    public InitialDataLoaderComponent(IBasicServerComponents basicServerComponents, IRecordLoaderCollection recordLoaderCollection) {
        this.basicServerComponents = basicServerComponents;
        this.recordLoaderCollection = recordLoaderCollection;
    }

    @Override
    public void start() {
        for(Map.Entry<String,IRecordLoader> loaderEntry : recordLoaderCollection.getDataLoaders().entrySet()){
            loaderEntry.getValue().getRecords(loaderEntry.getKey()).subscribe(rec-> addRecordToTableOperator(loaderEntry.getKey(),loaderEntry.getValue().getSchemaConfig(),rec));
        }
    }

    private void addRecordToTableOperator(String tableOperator, SchemaConfig schemaConfig, IRecord rec) {
        KeyedTable operator = (KeyedTable)basicServerComponents.getServerCatalog().getOperator(tableOperator);
        if(operator == null){
            operator = new KeyedTable(tableOperator, basicServerComponents.getExecutionContext(), basicServerComponents.getServerCatalog(), DataSourceHelper.getSchema(schemaConfig), new ChunkedColumnStorage(1024), new TableKeyDefinition(schemaConfig.getColumns().toArray(new String[0])));
            operator.initialise(8);
        }
        try{
            RecordUtils.addRecordToTableOperator(operator, rec);
        }catch (Exception ex){
            logger.error("Problem loading record",ex);
        }

    }
}
