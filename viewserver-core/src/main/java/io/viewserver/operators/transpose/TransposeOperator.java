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

package io.viewserver.operators.transpose;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.*;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by nickc on 28/10/2014.
 */
public class TransposeOperator extends ConfigurableOperatorBase<ITransposeConfig> {
    private static final Logger log = LoggerFactory.getLogger(TransposeOperator.class);
    private final Input input;
    private final Output output;
    private TIntIntHashMap[] outRows;
    private Object[] pivotValues;
    private List<String> keyColumnNames;
    private String pivotColumnName;
    private ITableStorage tableStorage;
    private ColumnHolder pivotColumn;
    private ColumnHolder[] keyColumns;
    private IntHashSet keys;
    private int[] keyComponents;

    public TransposeOperator(String name, ExecutionContext executionContext, ICatalog catalog, ITableStorage tableStorage) {
        super(name, executionContext, catalog);
        this.tableStorage = tableStorage;

        keys = new IntHashSet(8, 0.75f, -1);

        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        tableStorage.initialise(128, output.getSchema(), output.getCurrentChanges());
    }

    @Override
    protected ITransposeConfig mergePendingConfig(ITransposeConfig pendingConfig, ITransposeConfig newConfig) {
        if (!ObjectUtils.equals(pendingConfig.getKeyColumns(), newConfig.getKeyColumns())) {
            throw new IllegalStateException("Cannot merge configs with conflicting key columns");
        }
        if (!ObjectUtils.equals(pendingConfig.getPivotColumn(), newConfig.getPivotColumn())) {
            throw new IllegalStateException("Cannot merge configs with conflicting pivot columns");
        }
        if ((pendingConfig.getPivotValues() != null || newConfig.getPivotValues() != null) && !Arrays.equals(pendingConfig.getPivotValues(), newConfig.getPivotValues())) {
            throw new IllegalStateException("Cannot merge configs with conflicting pivot values");
        }
        return pendingConfig;
    }

    @Override
    protected void processConfig(ITransposeConfig config) {
        if (configChanged(config)) {
            this.keyColumnNames = config.getKeyColumns();
            this.keyColumns = new ColumnHolder[this.keyColumnNames.size()];
            if (this.keyComponents == null || this.keyComponents.length < this.keyColumns.length) {
                this.keyComponents = new int[this.keyColumns.length];
            }
            this.pivotColumnName = config.getPivotColumn();
            pivotValues = config.getPivotValues();

            input.removedRows = new TIntArrayList[pivotValues.length];
            outRows = new TIntIntHashMap[pivotValues.length];
            for (int i = 0; i < pivotValues.length; i++) {
                input.removedRows[i] = new TIntArrayList(8, -1);
                outRows[i] = new TIntIntHashMap(128, 0.75f, -1, -1);
            }

            input.resetSchema();
        }
    }

    private boolean configChanged(ITransposeConfig config) {
        if (this.config == null) {
            return true;
        }

        boolean pivotValuesChanged = config.getPivotValues().length != this.config.getPivotValues().length;
        if (!pivotValuesChanged) {
            for (int i = 0; i < config.getPivotValues().length; i++) {
                if (!config.getPivotValues()[i].equals(this.config.getPivotValues()[i])) {
                    pivotValuesChanged = true;
                    break;
                }
            }
        }

        return !config.getKeyColumns().equals(this.config.getKeyColumns())
                || !config.getPivotColumn().equals(this.config.getPivotColumn())
                || pivotValuesChanged;
    }

    private int getKey(int row, boolean usePreviousValues) {
        if (keyColumns == null || keyColumns.length == 0) {
            return 0;
        }

        int groupKey;
        if (keyColumns.length == 1) {
            groupKey = getKeyComponent(keyColumns[0], row, usePreviousValues);
        } else {
            for (int i = 0; i < keyColumns.length; i++) {
                keyComponents[i] = getKeyComponent(keyColumns[i], row, usePreviousValues);
            }
            groupKey = Arrays.hashCode(keyComponents);
        }
        int groupId = keys.addInt(groupKey);
        if (groupId < 0) {
            groupId = -groupId - 1;
        }
        return groupId;
    }

    private int getKeyComponent(ColumnHolder columnHolder, int row, boolean usePreviousValues) {
        switch (columnHolder.getType()) {
            case Bool: {
                IColumnBool groupByColumn = (IColumnBool) columnHolder;
                return (usePreviousValues ? groupByColumn.getPreviousBool(row) : groupByColumn.getBool(row)) ? 1 : 0;
            }
            case NullableBool: {
                IColumnNullableBool groupByColumn = (IColumnNullableBool) columnHolder;
                return (usePreviousValues ? groupByColumn.getPreviousNullableBool(row) : groupByColumn.getNullableBool(row)).getNumericValue();
            }
            case Byte: {
                IColumnByte groupByColumn = (IColumnByte) columnHolder;
                return usePreviousValues ? groupByColumn.getPreviousByte(row) : groupByColumn.getByte(row);
            }
            case Short: {
                IColumnShort groupByColumn = (IColumnShort) columnHolder;
                return usePreviousValues ? groupByColumn.getPreviousShort(row) : groupByColumn.getShort(row);
            }
            case Int: {
                IColumnInt groupByColumn = (IColumnInt) columnHolder;
                return usePreviousValues ? groupByColumn.getPreviousInt(row) : groupByColumn.getInt(row);
            }
            default: {
                throw new IllegalArgumentException("Cannot group on a column of type " + columnHolder.getType());
            }
        }
    }

    public IInput getInput() {
        return input;
    }

    public IOutput getOutput() {
        return output;
    }

    private class Input extends InputBase {
        private TIntArrayList[] removedRows;

        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            int keyColumnId = keyColumnNames.indexOf(columnHolder.getName());
            if (keyColumnId > -1) {
                keyColumns[keyColumnId] = columnHolder;
                ((IWritableColumn) columnHolder.getColumn()).storePreviousValues();
                ColumnHolder outHolder = ColumnHolderUtils.createColumnHolder(columnHolder.getName(), columnHolder.getType());
                outHolder.setMetadata(columnHolder.getMetadata());
                output.getSchema().addColumn(outHolder);
                tableStorage.initialiseColumn(outHolder);
                return;
            }

            if (columnHolder.getName().equals(pivotColumnName)) {
                pivotColumn = columnHolder;
                ((IWritableColumn) columnHolder.getColumn()).storePreviousValues();
                return;
            }

            // otherwise, it's a pivoted column
            for (int i = 0; i < pivotValues.length; i++) {
                String name = pivotColumnName + pivotValues[i] + "_" + columnHolder.getName();
                ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(name, columnHolder, i);
                output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
            }
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
        }

        @Override
        protected void onRowAdd(int row) {
            Object pivotValue = ColumnHolderUtils.getValue(pivotColumn, row);
            int pivotValueIndex = -1;
            for (int i = 0; i < pivotValues.length; i++) {
                if (pivotValues[i].equals(pivotValue)) {
                    pivotValueIndex = i;
                    break;
                }
            }

            // ignore rows with bad pivot values
            if (pivotValueIndex == -1) {
                return;
            }

            int key = getKey(row, false);
            ensureOutputCapacity(key);

            removedRows[pivotValueIndex].remove(key);

            boolean isNewGroup = !output.isRowActive(key);
            if (isNewGroup) {
                output.handleAdd(key);

                for (int i = 0; i < keyColumns.length; i++) {
                    switch (keyColumns[i].getType()) {
                        case Bool: {
                            ((IWritableColumnBool) output.getSchema().getColumnHolder(keyColumnNames.get(i)).getColumn()).setBool(key, ((IColumnBool) keyColumns[i]).getBool(row));
                            break;
                        }
                        case NullableBool: {
                            ((IWritableColumnNullableBool) output.getSchema().getColumnHolder(keyColumnNames.get(i)).getColumn()).setNullableBool(key, ((IColumnNullableBool) keyColumns[i]).getNullableBool(row));
                            break;
                        }
                        case Byte: {
                            ((IWritableColumnByte) output.getSchema().getColumnHolder(keyColumnNames.get(i)).getColumn()).setByte(key, ((IColumnByte) keyColumns[i]).getByte(row));
                            break;
                        }
                        case Short: {
                            ((IWritableColumnShort) output.getSchema().getColumnHolder(keyColumnNames.get(i)).getColumn()).setShort(key, ((IColumnShort) keyColumns[i]).getShort(row));
                            break;
                        }
                        case Int: {
                            ((IWritableColumnInt) output.getSchema().getColumnHolder(keyColumnNames.get(i)).getColumn()).setInt(key, ((IColumnInt) keyColumns[i]).getInt(row));
                            break;
                        }
                    }
                }
            } else {
                output.handleUpdate(key);
            }

            outRows[pivotValueIndex].put(key, row);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            // ASSUMPTION!
            // this operator will generally have a GroupByOperator (or similar) plugged in to it
            // it is somewhat unlikely that the values in either the key columns or the pivot column will change
            // therefore, we don't deal with that
            int key = getKey(row, false);
            output.handleUpdate(key);
        }

        @Override
        protected void onRowRemove(int row) {
            int key = getKey(row, true);
            output.handleUpdate(key);

            Object pivotValue = ColumnHolderUtils.getPreviousValue(pivotColumn, row);
            int pivotValueIndex = -1;
            for (int i = 0; i < pivotValues.length; i++) {
                if (pivotValues[i].equals(pivotValue)) {
                    pivotValueIndex = i;
                    break;
                }
            }
            removedRows[pivotValueIndex].add(key);

            // TODO: do this better! (if we need to)
            String prefix = pivotColumnName + pivotValue + "_";
            List<ColumnHolder> columnHolders = output.getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder.getName().startsWith(prefix)) {
                    output.getCurrentChanges().markDirty(key, columnHolder.getColumnId());
                }
            }
        }

        @Override
        public void onAfterCommit() {
            super.onAfterCommit();

            if (removedRows != null) {
                for (int i = 0; i < removedRows.length; i++) {
                    if (removedRows[i].size() > 0) {
                        int i2 = i;
                        removedRows[i].forEach((row) -> {
                            outRows[i2].remove(row);
                            return true;
                        });
                        removedRows[i].resetQuick();
                    }
                }
            }
        }

        private void ensureOutputCapacity(int capacity) {
            tableStorage.ensureCapacity(capacity, output.getSchema());
        }
    }

    private class Output extends MappedOutputBase {
        private MappedColumnHolderFactory columnHolderFactory;

        public Output(String name, IOperator owner) {
            super(name, owner);

            columnHolderFactory = new MappedColumnHolderFactory();
        }

        @Override
        public MappedColumnHolderFactory getColumnHolderFactory() {
            return columnHolderFactory;
        }
    }

    private class MappedColumnHolderFactory extends ColumnHolderFactory {
        private int index;

        @Override
        public IRowMapper getRowMapper(IRowMapper upstreamMapper) {
            return new RowMapper(upstreamMapper, index);
        }

        public ColumnHolder createColumnHolder(String name, ColumnHolder sourceColumn, int index) {
            this.index = index;
            return super.createColumnHolder(name, sourceColumn);
        }
    }

    private class RowMapper extends RowMapperBase {
        private int index;

        public RowMapper(IRowMapper upstreamMapper, int index) {
            super(upstreamMapper);
            this.index = index;
        }

        @Override
        public int lookupInboundRow(int row) {
            return outRows[index].get(row);
        }
    }


}
