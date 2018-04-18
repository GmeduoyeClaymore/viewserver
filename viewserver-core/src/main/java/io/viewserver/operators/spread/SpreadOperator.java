package io.viewserver.operators.spread;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.LongHashSet;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.NumberUtils;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpreadOperator  extends ConfigurableOperatorBase<ISpreadConfig> {

    private static final Logger log = LoggerFactory.getLogger(SpreadOperator.class);
    private final Input input;
    private final Output output;
    private final ITableStorage tableStorage;
    private ISpreadFunctionRegistry spreadColumnRegistry;
    private ColumnHolderString spreadColumn;
    private TIntObjectHashMap<TIntHashSet> spreadAssociations = new TIntObjectHashMap<>(8, 0.75f, -1);
    private TIntIntMap inputToOutputMappings = new TIntIntHashMap();
    private int associationCounter = 0;

    private ISpreadFunction spreadFunction;
    private String sourceColumn;
    private String targetColumn;
    private boolean removeInputColumn;

    public SpreadOperator(String name, IExecutionContext executionContext, ITableStorage tableStorage, ICatalog catalog, ISpreadFunctionRegistry spreadColumnRegistry) {
        super(name, executionContext, catalog);
        this.tableStorage = tableStorage;
        this.spreadColumnRegistry = spreadColumnRegistry;
        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        this.tableStorage.initialise(1024, output.getSchema(), output.getCurrentChanges());
    }

    @Override
    protected void processConfig(ISpreadConfig config) {

        if(config.spreadFunctionName() == null){
            throw new RuntimeException("Spread function should be defined");
        }
        if(config.getInputColumnName() == null){
            throw new RuntimeException("Input column should be defined");
        }
        if(config.getOutputColumnName() == null){
            throw new RuntimeException("Output column should be defined");
        }
        this.spreadFunction = this.spreadColumnRegistry.resolve(config.spreadFunctionName());
        this.sourceColumn = config.getInputColumnName();
        this.targetColumn = config.getOutputColumnName();
        this.removeInputColumn = config.removeInputColumn();
        if(spreadColumn != null){
            output.getSchema().removeColumn(spreadColumn.getColumnId());
        }

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
                if(name.equals(SpreadOperator.this.sourceColumn)){
                    spreadColumn = (ColumnHolderString) ColumnHolderUtils.createColumnHolder( SpreadOperator.this.targetColumn , ColumnType.String);
                    output.getSchema().addColumn(spreadColumn);
                    SpreadOperator.this.tableStorage.initialiseColumn(spreadColumn);

                }
                if(!(name.equals(SpreadOperator.this.sourceColumn) && removeInputColumn)) {
                    ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(name, columnHolder);
                    output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
                }

            }
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
        }

        @Override
        public void onDataClear() {
            super.onDataClear();
            spreadAssociations.clear();
        }

        private String[] getSpreadValues(int row) {
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

            String[] values = getSpreadValues(row);
            for(String val : values){
                int outputRow = addSpreadValue(ourRows, val, "add");
                inputToOutputMappings.put(outputRow,row);
                output.handleAdd(outputRow);
            }
        }

        private int addSpreadValue(TIntHashSet ourRows, String val, String operation) {
            int outputRow  = associationCounter++;
            log.info("Row {} has just been added in {}",outputRow, operation);
            ourRows.add(outputRow);
            ((IWritableColumnString)spreadColumn.getColumn()).setString(outputRow, val);
            return outputRow;
        }


        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            log.info("Starting to process update for row {}",row);
            TIntHashSet ourRows = spreadAssociations.get(row);
            if (ourRows == null) {
                throw new RuntimeException("Attempting to update a row that doesnt exist");
            }
            String[] values = getSpreadValues(row);
            String[] existingValues = new String[ourRows.size()];


            for(int i : ourRows.toArray()){
                boolean valueHasBeenRemoved = true;
                String string = spreadColumn.getString(i);
                for(String str: values){
                    if(str == string){
                        valueHasBeenRemoved = false;
                        break;
                    }
                }
                if(valueHasBeenRemoved){
                    log.info("Row {} has just been removed in update",i);
                    output.handleRemove(i);
                    ourRows.remove(i);
                }
            }

            for(String  str : existingValues){
                boolean valueAlreadyExists = false;
                for(int i : ourRows.toArray()){
                    String string = spreadColumn.getString(i);
                    if(str == string){
                        valueAlreadyExists = true;
                        break;
                    }
                }
                if(!valueAlreadyExists){
                    int addedRow = addSpreadValue(ourRows,str, "update");
                    output.handleAdd(addedRow);
                }
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
