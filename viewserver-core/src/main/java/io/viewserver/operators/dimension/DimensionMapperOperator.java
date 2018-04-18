package io.viewserver.operators.dimension;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.DataSourceHelper;
import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
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
    private HashMap<String,ColumnHolder> dimensionColumns = new HashMap<>();
    private String dataSourceName;

    public DimensionMapperOperator(String name, IExecutionContext executionContext, ITableStorage tableStorage, ICatalog catalog, DimensionMapper dimensionMapper) {
        super(name, executionContext, catalog);
        this.tableStorage = tableStorage;

        input = new Input(Constants.IN, this, true);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        this.tableStorage.initialise(1024, output.getSchema(), output.getCurrentChanges());
    }

    @Override
    protected void processConfig(IDimensionMapConfig config) {
        indexedDimensions.clear();
        if(config.getDimensions() == null){
            throw new RuntimeException("Dimensions should not be nul");
        }
        for(Dimension dim : config.getDimensions()){
            indexedDimensions.put(dim.getName(),dim);
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
            if(indexedDimensions.containsKey(name)){
                Dimension dim = indexedDimensions.get(name);
                ColumnHolder holder = DataSourceHelper.createColumnHolder(dataSourceName, name,dim.getColumnType(),dim.getCardinality(),dimensionMapper);
                dimensionColumns.put(name, holder);
                output.getSchema().addColumn(holder);
                DimensionMapperOperator.this.tableStorage.initialiseColumn(holder);
            }
            if(!(indexedDimensions.containsKey(name) && removeInputColumns)) {
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
                ColumnHolder columnHolder = this.getProducer().getSchema().getColumnHolder(dim.getSourceColumnName());
                if(columnHolder == null){
                    log.warn(String.format("Source column %s has not been found in datasource %s",dim.getSourceColumnName(), DimensionMapperOperator.this.dataSourceName));
                }
                ColumnHolderUtils.setValue(dimEntry.getValue(),row, DimensionMapperOperator.this.dimensionMapper.map(DimensionMapperOperator.this.dataSourceName, dim.getName(),dim.getColumnType(), ColumnHolderUtils.getValue(columnHolder,row)));
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
                    ColumnHolderUtils.setValue(dimEntry.getValue(),row, DimensionMapperOperator.this.dimensionMapper.map(DimensionMapperOperator.this.dataSourceName, dim.getName(),dim.getColumnType(), ColumnHolderUtils.getValue(columnHolder,row)));
                }
            }
            output.handleUpdate(row);
        }

        @Override
        protected void onRowRemove(int row) {
            output.handleRemove(row);
        }
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


