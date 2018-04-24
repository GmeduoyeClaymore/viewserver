/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.operators.calccol;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.ContentType;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.HookingContext;
import io.viewserver.expression.tree.*;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.SchemaChange;
import io.viewserver.schema.column.*;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bemm on 15/10/2014.
 */
public class CalcColOperator extends ConfigurableOperatorBase<ICalcColConfig> {
    private static final Logger log = LoggerFactory.getLogger(CalcColOperator.class);
    private Input input;
    private Output output;
    private ITableStorage tableStorage;
    private IExpressionParser expressionParser;
    private ColumnHolder[] calcColumnHolders;
    private int nextCalcColumn = 0;
    private final List<CalculatedColumn> calculatedColumns = new ArrayList<>();
    private final Map<CalculatedColumn, Map<String, String>> calcColAliases = new HashMap<>();
    private final List<CalculatedColumn> regexColumns = new ArrayList<>();
    private final TIntObjectHashMap<String> expressions = new TIntObjectHashMap<>();

    public CalcColOperator(String name, IExecutionContext executionContext, ICatalog catalog, ITableStorage tableStorage, IExpressionParser expressionParser) {
        super(name, executionContext, catalog);
        this.tableStorage = tableStorage;
        this.expressionParser = expressionParser;

        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        calcColumnHolders = new ColumnHolder[8];

        this.tableStorage.initialise(1024, output.getSchema(), output.getCurrentChanges());
        register();
    }

    public IInput getInput() {
        return input;
    }

    public IOutput getOutput() {
        return output;
    }

    @Override
    protected ICalcColConfig mergePendingConfig(ICalcColConfig pendingConfig, ICalcColConfig newConfig) {
        return new ICalcColConfig() {
            @Override
            public List<CalculatedColumn> getCalculatedColumns() {
                ArrayList<CalculatedColumn> calculations = new ArrayList<>();
                List<CalculatedColumn> calculatedColumns = pendingConfig.getCalculatedColumns();
                int count = calculatedColumns.size();
                for (int i = 0; i < count; i++) {
                    CalculatedColumn calculatedColumn = calculatedColumns.get(i);
                    if (!calculations.contains(calculatedColumn)) {
                        calculations.add(calculatedColumn);
                    }
                }
                calculatedColumns = newConfig.getCalculatedColumns();
                count = calculatedColumns.size();
                for (int i = 0; i < count; i++) {
                    CalculatedColumn calculatedColumn = calculatedColumns.get(i);
                    if (!calculations.contains(calculatedColumn)) {
                        calculations.add(calculatedColumn);
                    }
                }
                return new ArrayList<>(calculations);
            }

            @Override
            public Map<String, String> getColumnAliases() {
                HashMap<String, String> aliases = new HashMap<>();
                aliases.putAll(pendingConfig.getColumnAliases());
                aliases.putAll(newConfig.getColumnAliases());
                return aliases;
            }

            @Override
            public boolean isDataRefreshedOnColumnAdd() {
                return pendingConfig.isDataRefreshedOnColumnAdd() || newConfig.isDataRefreshedOnColumnAdd();
            }
        };
    }

    private class Input extends InputBase {
        private BitSet removedColumns = new BitSet();
        private boolean isDataReset;
        private List<ColumnHolder> addedColumns = new ArrayList<>();
        private BitSet addedColumnIds = new BitSet();
        private boolean isDataRefresh;
        private boolean hasUpdates;
        private boolean isDataRefreshedOnColumnAdd = true;

        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void onSchema() {
            this.addedColumnIds.clear();
            boolean isReset = isSchemaResetRequested;
            super.onSchema();

            // if not a reset (i.e. either a schema change or pending config)

            if (!isReset) {
                if (pendingConfig != null) {
                    this.isDataRefreshedOnColumnAdd = pendingConfig.isDataRefreshedOnColumnAdd();

                    List<CalculatedColumn> pendingRegexColumns = new ArrayList<>();
                    List<CalculatedColumn> pendingCalculatedColumns = pendingConfig.getCalculatedColumns();
                    int count = pendingCalculatedColumns.size();
                    for (int i = 0; i < count; i++) {
                        CalculatedColumn calculatedColumn = pendingCalculatedColumns.get(i);
                        Map<String, String> columnAliases = pendingConfig.getColumnAliases();
                        if (columnAliases != null && !columnAliases.isEmpty()) {
                            calcColAliases.put(calculatedColumn, columnAliases);
                        }
                        if (calculatedColumn.sourceColumnRegex != null) {
                            regexColumns.add(calculatedColumn);
                            pendingRegexColumns.add(calculatedColumn);
                        } else {
                            calculatedColumns.add(calculatedColumn);
                            addCalculatedColumn(calculatedColumn);
                        }
                    }

                    processRegexColumns(pendingRegexColumns, output.getSchema().getColumnHolders());
                }

                processRegexColumnsForAddedColumns();
            }
        }

        @Override
        protected void onSchemaReset() {
            for (int i = 0; i < nextCalcColumn; i++) {
                calcColumnHolders[i] = null;
            }
            nextCalcColumn = 0;

            super.onSchemaReset();

            // on a reset:
            // - map pending config
            // - loop through ALL configured calculated columns
            // - iteratively ALL regex columns over ADDED columns (since ALL columns are ADDED in the reset)

            if (pendingConfig != null) {
                List<CalculatedColumn> pendingCalculatedColumns = pendingConfig.getCalculatedColumns();
                int count = pendingCalculatedColumns.size();
                for (int i = 0; i < count; i++) {
                    CalculatedColumn calculatedColumn = pendingCalculatedColumns.get(i);
                    Map<String, String> columnAliases = pendingConfig.getColumnAliases();
                    if (columnAliases != null && !columnAliases.isEmpty()) {
                        calcColAliases.put(calculatedColumn, columnAliases);
                    }
                    if (calculatedColumn.sourceColumnRegex != null) {
                        regexColumns.add(calculatedColumn);
                    } else {
                        calculatedColumns.add(calculatedColumn);
                    }
                }
            }

            int count = calculatedColumns.size();
            for (int i = 0; i < count; i++) {
                addCalculatedColumn(calculatedColumns.get(i));
            }

            processRegexColumnsForAddedColumns();
        }

        @Override
        protected void onSchemaChange(SchemaChange schemaChange) {
            removedColumns.clear();

            super.onSchemaChange(schemaChange);

            // on a normal schema change:
            // - remove calculated columns that depend on removed schema columns
            // TODO: - remove regex calculated columns that matched removed schema columns
            // - loop through NEW configured calculated columns, (in onSchema above)
            // - NEW regex columns over ALL schema columns (in onSchema above)
            // - iteratively ALL regex columns over ADDED columns (in onSchema above)

            IRowFlags rowFlags = new IRowFlags() {
                @Override
                public boolean isDirty(int columnId) {
                    return removedColumns.get(columnId);
                }
            };

            int count = calcColumnHolders.length;
            for (int i = 0; i < count; i++) {
                ColumnHolder calcColumnHolder = calcColumnHolders[i];
                if (calcColumnHolder == null) {
                    continue;
                }
                if (((ICalcColumnHolder) calcColumnHolder).isAffected(rowFlags)) {
                    output.getSchema().removeColumn(calcColumnHolder.getColumnId());
                }
            }
        }

        private void processRegexColumnsForAddedColumns() {
            while (!addedColumns.isEmpty()) {
                List<ColumnHolder> addedColumnsCopy = new ArrayList<>(this.addedColumns);
                addedColumns.clear();
                processRegexColumns(regexColumns, addedColumnsCopy);
            }
        }

        private void processRegexColumns(List<CalculatedColumn> regexColumns, List<ColumnHolder> schemaColumns) {
            int regexColumnCount = regexColumns.size();
            for (int i = 0; i < regexColumnCount; i++) {
                CalculatedColumn regexColumn = regexColumns.get(i);
                int schemaColumnCount = schemaColumns.size();
                for (int j = 0; j < schemaColumnCount; j++) {
                    Matcher matcher = regexColumn.sourceColumnRegex.matcher(schemaColumns.get(j).getName());
                    if (matcher.find()) {
                        String name = regexColumn.name;
                        String expression = regexColumn.expression;
                        for (int k = 0; k <= matcher.groupCount(); k++) {
                            name = name.replace("$" + k, matcher.group(k));
                            expression = expression.replace("$" + k, matcher.group(k));
                        }
                        addCalculatedColumn(regexColumn, name, expression);
                    }
                }
            }
        }

        private void addCalculatedColumn(CalculatedColumn calculatedColumn) {
            addCalculatedColumn(calculatedColumn, calculatedColumn.name, calculatedColumn.expression);
        }

        private void addCalculatedColumn(CalculatedColumn calculatedColumn, String name, String expressionText) {
//            long start = System.nanoTime();
            try {

                ColumnHolder columnHolder = output.getSchema().getColumnHolder(name);
                if (columnHolder != null) {
                    if (!(columnHolder.getColumn() instanceof ICalcColumnHolder)) {
                        throw new IllegalArgumentException("A column already exists with the name '" + name + "'");
                    }

                    String oldExpression = expressions.get(columnHolder.getColumnId());
                    if (expressionText.equals(oldExpression)) {
                        // nothing to do!
                        return;
                    }
                }

                BitSet columnsUsed = new BitSet();
                HookingContext hookingContext = new HookingContext(getExecutionContext().getReactor(), name, output.getSchema());
                IExpression expression = expressionParser.parse(expressionText, output.getSchema(),
                        calcColAliases.get(calculatedColumn), columnsUsed, hookingContext);

                int calcColumnIndex = -1;
                if (columnHolder != null) {
                    if (!columnHolder.getType().equals(expression.getType())) {
                        throw new IllegalArgumentException("Cannot change expression on existing column '" + name + "' as it is of different type");
                    }

                    for (int i = 0; i < calcColumnHolders.length; i++) {
                        if (calcColumnHolders[i] == columnHolder.getColumn()) {
                            calcColumnIndex = i;
                            break;
                        }
                    }
                }

                ColumnHolder calcColumnHolder = input.makeColumnHolder(name, expression, columnsUsed);
                tableStorage.initialiseColumn(calcColumnHolder);
                if (columnHolder != null) {
                    columnHolder.setColumn(calcColumnHolder);
                } else {
                    columnHolder = ColumnHolderUtils.createColumnHolder(name, expression.getType());
                    if (calculatedColumn.dataType != null) {
                        ColumnMetadata metadata = ColumnHolderUtils.createColumnMetadata(expression.getType());
                        metadata.setDataType(calculatedColumn.dataType);
                        columnHolder.setMetadata(metadata);
                    }
                    columnHolder.setColumn(calcColumnHolder);
                    output.getSchema().addColumn(columnHolder);
                }
                calcColumnHolder.setColumnId(columnHolder.getColumnId());
                expressions.put(columnHolder.getColumnId(), expressionText);
                for (int columnId = columnsUsed.nextSetBit(0); columnId >= 0; columnId = columnsUsed.nextSetBit(columnId + 1)) {
                    ((IWritableColumn) output.getSchema().getColumnHolder(columnId).getColumn()).storePreviousValues();
                }
                output.getCurrentChanges().markColumnDirty(columnHolder.getColumnId());
                if (calcColumnIndex == -1) {
                    calcColumnIndex = nextCalcColumn++;
                }
                if (calcColumnHolders.length <= calcColumnIndex) {
                    calcColumnHolders = Arrays.copyOf(calcColumnHolders, calcColumnIndex + 1);
                }
                calcColumnHolders[calcColumnIndex] = calcColumnHolder;
                addedColumns.add(calcColumnHolder);
                addedColumnIds.set(calcColumnHolder.getColumnId());
                if (isDataRefreshedOnColumnAdd) {
                    refreshData();
                }
            } catch (Throwable t) {
                log.error("Error configuring operator {} with expression {}",CalcColOperator.this.getName(), expressionText);
                throw new OperatorConfigurationException(CalcColOperator.this, t, expressionText);
            }

//            log.trace("Took {}ms to add calc col {} [{}]", (System.nanoTime() - start) / 1000000f, name, expressionText);
        }

        private ColumnHolder makeColumnHolder(String name, IExpression expression, BitSet columnsUsed) {
            ColumnHolder columnHolder;
            switch (expression.getType()) {
                case Bool: {
                    columnHolder = new CalcColumnHolderBool(name, null, (IExpressionBool) expression, columnsUsed);
                    break;
                }
                case NullableBool: {
                    columnHolder = new CalcColumnHolderNullableBool(name, null, (IExpressionNullableBool) expression, columnsUsed);
                    break;
                }
                case Byte: {
                    columnHolder = new CalcColumnHolderByte(name, null, (IExpressionByte) expression, columnsUsed);
                    break;
                }
                case Short: {
                    columnHolder = new CalcColumnHolderShort(name, null, (IExpressionShort) expression, columnsUsed);
                    break;
                }
                case Int: {
                    columnHolder = new CalcColumnHolderInt(name, null, (IExpressionInt) expression, columnsUsed);
                    break;
                }
                case Long: {
                    columnHolder = new CalcColumnHolderLong(name, null, (IExpressionLong) expression, columnsUsed);
                    break;
                }
                case Float: {
                    columnHolder = new CalcColumnHolderFloat(name, null, (IExpressionFloat) expression, columnsUsed);
                    break;
                }
                case Double: {
                    columnHolder = new CalcColumnHolderDouble(name, null, (IExpressionDouble) expression, columnsUsed);
                    break;
                }
                case String: {
                    columnHolder = new CalcColumnHolderString(name, null, (IExpressionString) expression, columnsUsed);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Cannot create calculated column for type " + expression.getType());
                }
            }
            return columnHolder;
        }

        @Override
        protected void onDataReset() {
            isDataReset = true;

            output.getCurrentChanges().ensureCapacity(getProducer().getRowCount());

            super.onDataReset();

            int count = calcColumnHolders.length;
            for (int i = 0; i < count; i++) {
                ColumnHolder calcColumnHolder = calcColumnHolders[i];
                if (calcColumnHolder == null) {
                    continue;
                }
                output.getCurrentChanges().markColumnDirty(calcColumnHolder.getColumnId());
                ((ICalcColumnHolder) calcColumnHolder).clearAllCalculated();
            }

            isDataReset = false;
        }

        @Override
        protected void onDataRefresh(IChangeQueue changeQueue) {
            isDataRefresh = true;
            hasUpdates = changeQueue.hasUpdates();
            super.onDataRefresh(changeQueue);
            isDataRefresh = false;
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(columnHolder.getName(), columnHolder);
            output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
            addedColumns.add(columnHolder);
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            int outboundColumnId = output.getOutboundColumnId(columnHolder);
            removedColumns.set(outboundColumnId);
            output.unmapColumn(columnHolder);
        }

        @Override
        protected void onRowAdd(int row) {
            output.handleAdd(row);
            if (!isDataReset) {
                int count = calcColumnHolders.length;
                for (int i = 0; i < count; i++) {
                    ColumnHolder calcColumnHolder = calcColumnHolders[i];
                    if (calcColumnHolder == null) {
                        continue;
                    }
                    ((ICalcColumnHolder) calcColumnHolder).clearCalculated(row);
                }
            }
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            output.handleUpdate(row);
            if (!isDataRefresh || hasUpdates) {
                int count = calcColumnHolders.length;
                for (int i = 0; i < count; i++) {
                    ColumnHolder calcColumnHolder = calcColumnHolders[i];
                    if (calcColumnHolder == null) {
                        continue;
                    }

                    // newly added calc columns will already be flagged as dirty, so we can skip them
                    if (addedColumnIds.get(calcColumnHolder.getColumnId())) {
                        continue;
                    }

                    if (((ICalcColumnHolder) calcColumnHolder).isAffected(rowFlags)) {
                        ((ICalcColumnHolder) calcColumnHolder).clearCalculated(row);
                    }
                }
            }
        }

        @Override
        protected void onRowRemove(int row) {
            output.handleRemove(row);
            int count = calcColumnHolders.length;
            for (int i = 0; i < count; i++) {
                ColumnHolder calcColumnHolder = calcColumnHolders[i];
                if (calcColumnHolder == null) {
                    continue;
                }

                // newly added calc columns will already be flagged as dirty, so we can skip them
                if (addedColumnIds.get(calcColumnHolder.getColumnId())) {
                    continue;
                }

                ((ICalcColumnHolder) calcColumnHolder).clearCalculated(row);
            }
        }
    }

    private class Output extends MappedOutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public IColumnHolderFactory getColumnHolderFactory() {
            return new PassThroughColumnHolderFactory();
        }

        @Override
        public ActiveRowTracker getRowTracker() {
            return input.getProducer().getRowTracker();
        }

        @Override
        public void clearData() {
            // don't clear the row tracker, as it's not ours
        }
    }

    public static class CalculatedColumn {
        private String name;
        private String expression;
        private String sourceColumnRegexString;
        private Pattern sourceColumnRegex;
        private ContentType dataType;

        public CalculatedColumn() {
        }

        public CalculatedColumn(String name, String expression) {
            this.name = name;
            this.expression = expression;
        }

        public CalculatedColumn(String name, String expression, String sourceColumnRegex) {
            this.name = name;
            this.expression = expression;
            sourceColumnRegexString = sourceColumnRegex;
            if (sourceColumnRegex != null) {
                this.sourceColumnRegex = Pattern.compile(sourceColumnRegex);
            }
        }

        @Override
        public String toString() {
            return "CalculatedColumn{" +
                    "name='" + name + '\'' +
                    ", expression='" + expression + '\'' +
                    ", sourceColumnRegexString='" + sourceColumnRegexString + '\'' +
                    '}';
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public void setSourceColumnRegex(Pattern sourceColumnRegex) {
            this.sourceColumnRegex = sourceColumnRegex;
        }

        public String getExpression() {
            return expression;
        }

        public Pattern getSourceColumnRegex() {
            return sourceColumnRegex;
        }

        public ContentType getDataType() {
            return dataType;
        }

        public void setDataType(ContentType dataType) {
            this.dataType = dataType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CalculatedColumn that = (CalculatedColumn) o;

            if (dataType != that.dataType) return false;
            if (!expression.equals(that.expression)) return false;
            if (!name.equals(that.name)) return false;
            if (sourceColumnRegex != null ? !sourceColumnRegex.equals(that.sourceColumnRegex) : that.sourceColumnRegex != null)
                return false;
            if (sourceColumnRegexString != null ? !sourceColumnRegexString.equals(that.sourceColumnRegexString) : that.sourceColumnRegexString != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + expression.hashCode();
            result = 31 * result + (sourceColumnRegexString != null ? sourceColumnRegexString.hashCode() : 0);
            result = 31 * result + (sourceColumnRegex != null ? sourceColumnRegex.hashCode() : 0);
            result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
            return result;
        }
    }
}
