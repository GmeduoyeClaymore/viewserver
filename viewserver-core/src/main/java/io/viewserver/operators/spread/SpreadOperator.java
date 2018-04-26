package io.viewserver.operators.spread;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.Column;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SpreadOperator  extends ConfigurableOperatorBase<ISpreadConfig> {

    private static final Logger log = LoggerFactory.getLogger(SpreadOperator.class);
    private final Input input;
    private final Output output;
    private final ITableStorage tableStorage;
    private ISpreadFunctionRegistry spreadColumnRegistry;
    private TIntObjectHashMap<TIntHashSet> spreadAssociations = new TIntObjectHashMap<>(8, 0.75f, -1);
    private TIntIntMap inputToOutputMappings = new TIntIntHashMap();
    private int associationCounter = 0;

    private HashMap<String,ColumnHolder> spreadColumnsByName = new HashMap<>();
    private ISpreadFunction spreadFunction;
    private String sourceColumn;
    private boolean removeInputColumn;
    private boolean retainSourceRow;

    public SpreadOperator(String name, IExecutionContext executionContext, ITableStorage tableStorage, ICatalog catalog, ISpreadFunctionRegistry spreadColumnRegistry) {
        super(name, executionContext, catalog);
        this.tableStorage = tableStorage;
        this.spreadColumnRegistry = spreadColumnRegistry;
        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        this.tableStorage.initialise(1024, output.getSchema(), output.getCurrentChanges());
        register();
    }

    @Override
    protected void processConfig(ISpreadConfig config) {

        if(config.spreadFunctionName() == null){
            throw new RuntimeException("Spread function should be defined");
        }
        if(config.getInputColumnName() == null){
            throw new RuntimeException("Input column should be defined");
        }
        this.spreadFunction = this.spreadColumnRegistry.resolve(config.spreadFunctionName());
        this.sourceColumn = config.getInputColumnName();
        this.retainSourceRow = config.retainSourceRow();
        this.removeInputColumn = config.removeInputColumn();
        for(Map.Entry<String,ColumnHolder> entry : spreadColumnsByName.entrySet()){
            output.getSchema().removeColumn(entry.getValue().getColumnId());
        }
        spreadColumnsByName.clear();
    }


    private class Input extends InputBase {

        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        protected void onSchemaReset() {
            super.onSchemaReset();
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            String name = columnHolder.getName();
            if (name != null) {
                if(!(name.equals(SpreadOperator.this.sourceColumn) && removeInputColumn)) {
                    ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(name, columnHolder);
                    output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
                }

            }
        }

        private ColumnHolder getOrCreateSpreadColumn(Column configColumn) {
            if(spreadColumnsByName.containsKey(configColumn.getName())){
                return spreadColumnsByName.get(configColumn.getName());
            }
            ColumnHolder holder = ColumnHolderUtils.createColumnHolder(configColumn);
            output.getSchema().addColumn(holder);
            SpreadOperator.this.tableStorage.initialiseColumn(holder);
            spreadColumnsByName.put(holder.getName(),holder);
            return holder;
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
        }

        @Override
        public void onDataClear() {
            super.onDataClear();
            spreadAssociations.clear();
        }

        private List<Map.Entry<Column, Object[]>> getSpreadValues(int row) {
            ISpreadFunction spreadFunction = SpreadOperator.this.spreadFunction;
            if(spreadFunction == null){
                throw new RuntimeException("No spread function has been defined");
            }
            String sourceColumn = SpreadOperator.this.sourceColumn;
            if(sourceColumn == null){
                throw new RuntimeException("No source column has been defined");
            }

            ColumnHolder columnHolder = this.getProducer().getSchema().getColumnHolder(sourceColumn);
            if(columnHolder == null){
                throw new RuntimeException(String.format("Source column %s has not been found",sourceColumn));
            }

            return spreadFunction.getValues(row, columnHolder);
        }


        @Override
        protected void onRowAdd(int row) {
            TIntHashSet ourRows = spreadAssociations.get(row);
            if (ourRows == null) {
                ourRows = new TIntHashSet(8, 0.75f, -1);
                spreadAssociations.put(row, ourRows);
            }

            addSpreadValues(row, ourRows);
        }

        private int getMaxRowIndex(List<Map.Entry<Column, Object[]>> values) {
            int maxRowIndex = 0;
            for(Map.Entry<Column, Object[]> val : values){
                if(val.getValue().length > maxRowIndex){
                    maxRowIndex = val.getValue().length;
                }
            }
            return maxRowIndex;
        }


        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            log.info("Starting to process update for row {}",row);
            TIntHashSet ourRows = spreadAssociations.get(row);
            if (ourRows == null) {
                throw new RuntimeException("Attempting to update a row that doesnt exist");
            }

            addSpreadValues(row, ourRows);

        }

        private void addSpreadValues(int row, TIntHashSet ourRows) {

            int[] ints = ourRows.toArray();

            List<Map.Entry<Column, Object[]>> values = getSpreadValues(row);
            int maximimumRowIndex = getMaxRowIndex(values);
/*            if(retainSourceRow) {
                mapRow(row, ourRows, associationCounter++, isAdd);
            }*/
            for(int i = 0;i< maximimumRowIndex;i++){
                boolean isAdd;
                int outputRow;
                if(i < ints.length){
                    isAdd = false;
                    outputRow = ints[i];
                }else{
                    isAdd = true;
                    outputRow = associationCounter++;
                }
                log.info("Row {} has just been added in {}",outputRow,"add");
                for(Map.Entry<Column, Object[]> val : values){
                    ColumnHolder spreadColumn = getOrCreateSpreadColumn(val.getKey());
                    Object[] value = val.getValue();
                    Object result = i < value.length ? value[i] : null;
                    ColumnHolderUtils.setValue(spreadColumn,outputRow, result);
                }
                mapRow(row, ourRows, outputRow, isAdd);
            }
            if(ints.length > maximimumRowIndex){
                for(int i=maximimumRowIndex-1;i<ints.length;i++){
                    int intRowToRemove = ints[i];
                    inputToOutputMappings.remove(intRowToRemove);
                    ourRows.remove(intRowToRemove);
                    output.handleRemove(intRowToRemove);
                }
            }
        }

        private void mapRow(int row, TIntHashSet ourRows, int outputRow, boolean isAdd) {
            if(isAdd) {
                ourRows.add(outputRow);
                inputToOutputMappings.put(outputRow, row);
                output.handleAdd(outputRow);
            }else{
                output.handleUpdate(outputRow);
            }
        }

        @Override
        protected void onRowRemove(int row) {
            TIntHashSet ourRows = spreadAssociations.get(row);
            if (ourRows == null) {
                throw new RuntimeException("Attempting to remove a row that doesnt exist");
            }
            for(int i : ourRows.toArray()){
                log.info("Row {} has just been removed in remove",i);
                inputToOutputMappings.remove(i);
                output.handleRemove(i);
                ourRows.remove(i);
            }
        }

    }


    private class Output extends MappedOutputBase {
        private final SpreadOperator.MappedColumnHolderFactory columnHolderFactory;

        public Output(String name, IOperator owner) {
            super(name, owner);

            columnHolderFactory = new SpreadOperator.MappedColumnHolderFactory();
        }

        @Override
        public SpreadOperator.MappedColumnHolderFactory getColumnHolderFactory() {
            return columnHolderFactory;
        }
    }

    private class MappedColumnHolderFactory extends ColumnHolderFactory {

        @Override
        public IRowMapper getRowMapper(IRowMapper upstreamMapper) {
            return new SpreadOperator.RowMapper(upstreamMapper);
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
            return inputToOutputMappings.get(row);
        }
    }


}
