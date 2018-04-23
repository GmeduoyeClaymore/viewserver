package io.viewserver.operators.dimension;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.DataSourceHelper;
import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class DimensionMapperOperator extends ConfigurableOperatorBase<IDimensionMapConfig> {

    protected DimensionMapper dimensionMapper;
    private static final Logger log = LoggerFactory.getLogger(DimensionMapperOperator.class);
    private final Input input;
    private final Output output;
    private final ITableStorage tableStorage;
    private boolean removeInputColumns;
    private HashMap<String,Dimension> indexedDimensions = new HashMap<>();
    private HashMap<String,Dimension> indexedDimensionsBySourceColumn = new HashMap<>();
    private HashMap<String,ColumnHolder> dimensionColumns = new HashMap<>();
    private String dataSourceName;

    public DimensionMapperOperator(String name, IExecutionContext executionContext, ITableStorage tableStorage, ICatalog catalog, DimensionMapper dimensionMapper) {
        super(name, executionContext, catalog);
        this.tableStorage = tableStorage;
        this.dimensionMapper = dimensionMapper;

        input = new Input(Constants.IN, this, true);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        this.tableStorage.initialise(1024, output.getSchema(), output.getCurrentChanges());
        register();
    }

    @Override
    protected void processConfig(IDimensionMapConfig config) {
        indexedDimensions.clear();
        indexedDimensionsBySourceColumn.clear();
        if(config.getDimensions() == null){
            throw new RuntimeException("Dimensions should not be nul");
        }
        for(Dimension dim : config.getDimensions()){
            if(dim.isImported()){
                continue;
            }
            indexedDimensions.put(dim.getName(),dim);
            indexedDimensionsBySourceColumn.put(dim.getSourceColumnName(),dim);
        }
        this.dataSourceName = config.getDataSourceName();
        this.removeInputColumns = config.removeInputColumns();
    }


    private class Input extends InputBase {

        public Input(String name, IOperator owner, boolean isLeft) {
            super(name, owner);
        }

        @Override
        protected void onSchemaReset() {
            super.onSchemaReset();
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            String name = columnHolder.getName();
            String dimensionName  = null;
            if(indexedDimensionsBySourceColumn.containsKey(name)){
                Dimension dim = indexedDimensionsBySourceColumn.get(name);
                dimensionName = dim.getName();
                if(!(dim.isGlobal() && output.getSchema().getColumnHolder(dimensionName) != null)){
                    ColumnHolder holder = DataSourceHelper.createColumnHolder(dim.isGlobal() ? "global" : dataSourceName, dimensionName,dim.getContentType(),dim.getCardinality(),dimensionMapper);
                    dimensionColumns.put(dimensionName, holder);
                    output.getSchema().addColumn(holder);
                    DimensionMapperOperator.this.tableStorage.initialiseColumn(holder);
                }

            }
            if(!(indexedDimensionsBySourceColumn.containsKey(name) && removeInputColumns) && !name.equals(dimensionName)) {
                ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(name, columnHolder);
                output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
            }
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
        }


        @Override
        protected void onRowAdd(int row) {
            for(Map.Entry<String,ColumnHolder> dimEntry : dimensionColumns.entrySet()){
                Dimension dim = indexedDimensions.get(dimEntry.getKey());
                IOutput producer = this.getProducer();
                Schema schema = producer.getSchema();
                String sourceColumnName = dim.getSourceColumnName();
                ColumnHolder columnHolder = schema.getColumnHolder(sourceColumnName);
                if(columnHolder == null){
                    log.warn(String.format("Source column %s has not been found in datasource %s", sourceColumnName, DimensionMapperOperator.this.dataSourceName));
                }
                mapDimColumn(row, dimEntry, dim, columnHolder);
            }
            output.handleAdd(row);
        }



        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            for(Map.Entry<String,ColumnHolder> dimEntry : dimensionColumns.entrySet()){
                Dimension dim = indexedDimensions.get(dimEntry.getKey());
                ColumnHolder columnHolder = this.getProducer().getSchema().getColumnHolder(dim.getSourceColumnName());
                if(columnHolder == null){
                    log.warn(String.format("Source column %s has not been found in datasource %s",dim.getSourceColumnName(), DimensionMapperOperator.this.dataSourceName));
                }
                if(rowFlags == null || rowFlags.isDirty(columnHolder.getColumnId())){
                    mapDimColumn(row, dimEntry, dim, columnHolder);
                }
            }
            output.handleUpdate(row);
        }

        @Override
        protected void onRowRemove(int row) {
            output.handleRemove(row);
        }
    }

    private void mapDimColumn(int row, Map.Entry<String, ColumnHolder> dimEntry, Dimension dim, ColumnHolder columnHolder) {
        if(columnHolder.getMetadata().isFlagged(ColumnFlags.DIMENSION)){
            return;
        }
        Object value = ColumnHolderUtils.getValue(columnHolder, row);
        int map = DimensionMapperOperator.this.dimensionMapper.map(dim.isGlobal() ? "global" : DimensionMapperOperator.this.dataSourceName, dim.getName(), dim.getContentType(), value);
        ColumnHolderUtils.setDimensionValue(dimEntry.getValue(),row, map);
    }

    private class Output extends MappedOutputBase {
        private final DimensionMapperOperator.MappedColumnHolderFactory columnHolderFactory;

        public Output(String name, IOperator owner) {
            super(name, owner);

            columnHolderFactory = new DimensionMapperOperator.MappedColumnHolderFactory();
        }

        @Override
        public DimensionMapperOperator.MappedColumnHolderFactory getColumnHolderFactory() {
            return columnHolderFactory;
        }
    }

    private class MappedColumnHolderFactory extends ColumnHolderFactory {

        @Override
        public IRowMapper getRowMapper(IRowMapper upstreamMapper) {
            return new DimensionMapperOperator.RowMapper(upstreamMapper);
        }

        public ColumnHolder createColumnHolder(String name, ColumnHolder sourceColumn, boolean isLeft) {
            return super.createColumnHolder(name, sourceColumn);
        }
    }

    private class RowMapper extends RowMapperBase {

        public RowMapper(IRowMapper upstreamMapper) {
            super(upstreamMapper);
        }

        @Override
        public int lookupInboundRow(int row) {
            return row;
        }
    }

}


