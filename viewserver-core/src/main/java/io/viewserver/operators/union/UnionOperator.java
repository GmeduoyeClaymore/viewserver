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

package io.viewserver.operators.union;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.changequeue.ChangeQueue;
import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.collections.LongHashSet;
import io.viewserver.collections.LongIterator;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.NumberUtils;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;
import io.viewserver.util.ViewServerException;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nickc on 06/10/2014.
 */
public class UnionOperator extends ConfigurableOperatorBase<IUnionConfig> {
    private static final Logger log = LoggerFactory.getLogger(UnionOperator.class);
    private final TIntObjectHashMap<Input> inputsBySourceId;
    private Output output;
    private boolean isFirstSchema = true;
    private ITableStorage tableStorage;
    private final Set<Input> resettingInputs = new HashSet<>();

    public UnionOperator(String name, ExecutionContext executionContext, ICatalog catalog, ITableStorage tableStorage) {
        super(name, executionContext, catalog);
        this.tableStorage = tableStorage;

        inputsBySourceId = new TIntObjectHashMap<>(4);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        tableStorage.initialise(1024, output.getSchema(), output.getCurrentChanges());
    }

    public IInput getOrCreateInput(String name, int sourceId) {
        IInput input = getInputs().get(name);
        if (input == null) {
            input = new Input(name, this, sourceId);
            inputsBySourceId.put(sourceId, (Input) input);
            addInput(input);
        }
        return input;
    }

    @Override
    protected void removeInput(IInput input) {
        super.removeInput(input);
        inputsBySourceId.remove(((Input)input).sourceId);
    }

    public IOutput getOutput() {
        return output;
    }

    @Override
    protected IUnionConfig mergePendingConfig(IUnionConfig pendingConfig, IUnionConfig config) {
        boolean identical = pendingConfig.getInputs().length == config.getInputs().length;
        if (identical) {
            List<IUnionConfig.Input> newInputs = Arrays.asList(config.getInputs());
            for (IUnionConfig.Input input : pendingConfig.getInputs()) {
                if (!newInputs.contains(input)) {
                    identical = false;
                }
            }
        }
        if (!identical) {
            return super.mergePendingConfig(pendingConfig, config);
        }
        return pendingConfig;
    }

    @Override
    protected void onSchemaReset() {
        isFirstSchema = true;
        output.unionChangeQueue.reset();
        super.onSchemaReset();
    }

    @Override
    protected void onDataReset() {
        resettingInputs.clear();
        super.onDataReset();
    }

    private class Input extends InputBase {
        private int sourceId;

        public Input(String name, IOperator owner, int sourceId) {
            super(name, owner);
            this.sourceId = sourceId;
        }

        @Override
        public void onSchemaResetRequested() {
            // only reset the operator if all the inputs are being reset (i.e. we don't need to reset when a new
            // input is plugged in)
            resettingInputs.add(this);
            if (resettingInputs.size() == getInputs().size()) {
                super.onSchemaResetRequested();
            }
        }

        @Override
        protected void onSchemaReset() {
            super.onSchemaReset();

            if (isFirstSchema) {
                output.sourceIdColumnHolder = ColumnHolderUtils.createColumnHolder("sourceId", ColumnType.Int);
                output.getSchema().addColumn(output.sourceIdColumnHolder);
                tableStorage.initialiseColumn(output.sourceIdColumnHolder);
            } else {
                Schema inboundSchema = getProducer().getSchema();
                List<ColumnHolder> outputHolders = output.getSchema().getColumnHolders();
                int count = outputHolders.size();
                for (int i = 0; i < count; i++) {
                    ColumnHolder outputHolder = outputHolders.get(i);
                    if (outputHolder.getName().equals("sourceId")) {
                        continue;
                    }
                    if (inboundSchema.getColumnHolder(outputHolder.getName()) == null) {
                        throw new ViewServerException(String.format("Schema for %s is missing column '%s', which is in the output schema",
                                getName(), outputHolder.getName()));
                    }
                }
            }

            isFirstSchema = false;
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            ColumnHolder outboundColumnHolder;
            if (isFirstSchema) {
                outboundColumnHolder = output.createUnionColumnHolder(columnHolder.getName(), sourceId, columnHolder);
                output.getSchema().addColumn(outboundColumnHolder);
            } else {
                outboundColumnHolder = output.getSchema().getColumnHolder(columnHolder.getName());
                if (outboundColumnHolder == null) {
                    throw new RuntimeException("Schema for " + getName() + " has column '" + columnHolder.getName() + "', which does not exist in the output schema");
                }
                if (outboundColumnHolder.getType() != columnHolder.getType()) {
                    throw new RuntimeException("Column '" + columnHolder.getName() + "' has different type (" + columnHolder.getType() + ") to the output schema (" + outboundColumnHolder.getType() + ")");
                }
                ((IUnionColumnHolder) outboundColumnHolder).mapSource(sourceId, columnHolder.getColumn(), columnHolder.getRowMapper());
            }
            output.unionChangeQueue.mapColumn(outboundColumnHolder, sourceId, columnHolder, getProducer().getCurrentChanges());
        }

        @Override
        public void onDataResetRequested() {
            if (resettingInputs.size() == getInputs().size()) {
                super.onDataResetRequested();
            }
        }

        @Override
        public void onData() {
            if (isDataResetRequested()) {
//                log.debug("Resetting data on {} - {}.{}", this.getOwner().getClass().getName(), this.getOwner().getName(), this.getName());
                removeAllRows();
            }

            super.onData();
        }

        private void removeAllRows() {
            LongIterator iterator = output.rows.iterator();
            while (iterator.hasNext()) {
                long key = iterator.next();
                int sourceId = NumberUtils.unpackLongHi(key);
                if (sourceId == this.sourceId) {
                    output.handleRemove(output.rows.index(key));
                    output.rowsToRemove.add(key);
                }
            }
        }

        @Override
        protected void onRowAdd(int row) {
            int outputRow = getOutputRow(row);
            ((IWritableColumnInt) output.sourceIdColumnHolder.getColumn()).setInt(outputRow, sourceId);
            output.handleAdd(outputRow);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            int outputRow = getOutputRow(row);
            output.handleUpdate(outputRow);
        }

        @Override
        protected void onRowRemove(int row) {
            int outputRow = getOutputRow(row);
            output.handleRemove(outputRow);
            output.rowsToRemove.add(getOutputRowKey(row));
        }

        @Override
        public void onTearDownRequested() {
            // only tear down a union operator if ALL its inputs are being torn down
            if (getInputs().size() > 1) {
                removeAllRows();
                removeInput(this);
            } else {
                super.onTearDownRequested();
            }
        }

        private int getOutputRow(int row) {
            long sourceRowKey = getOutputRowKey(row);
            int mappedRow = output.rows.addLong(sourceRowKey);
            if (mappedRow < 0) {
                mappedRow = -mappedRow - 1;
            }
            return mappedRow;
        }

        private long getOutputRowKey(int row) {
            return NumberUtils.packLong(sourceId, row);
        }
    }

    private class Output extends OutputBase {
        private final TLongArrayList rowsToRemove = new TLongArrayList(128, -1);
        private final LongHashSet rows = new LongHashSet(128, 0.75f, -1);
        private final UnionChangeQueue unionChangeQueue;
        private ColumnHolder sourceIdColumnHolder;

        public Output(String name, IOperator owner) {
            super(name, owner);

            unionChangeQueue = new UnionChangeQueue(this, rows);
        }

        @Override
        public IChangeQueue getCurrentChanges() {
            return unionChangeQueue;
        }

        @Override
        public void clearData() {
            rows.clear();
            super.clearData();
        }

        private ColumnHolder createUnionColumnHolder(String name, int sourceId, ColumnHolder sourceColumn) {
            IUnionColumnHolder unionColumnHolder;
            switch (sourceColumn.getType()) {
                case Bool: {
                    unionColumnHolder = new UnionColumnHolderBool(name, rows);
                    break;
                }
                case NullableBool: {
                    unionColumnHolder = new UnionColumnHolderNullableBool(name, rows);
                    break;
                }
                case Byte: {
                    unionColumnHolder = new UnionColumnHolderByte(name, rows);
                    break;
                }
                case Short: {
                    unionColumnHolder = new UnionColumnHolderShort(name, rows);
                    break;
                }
                case Int: {
                    unionColumnHolder = new UnionColumnHolderInt(name, rows);
                    break;
                }
                case Long: {
                    unionColumnHolder = new UnionColumnHolderLong(name, rows);
                    break;
                }
                case Float: {
                    unionColumnHolder = new UnionColumnHolderFloat(name, rows);
                    break;
                }
                case Double: {
                    unionColumnHolder = new UnionColumnHolderDouble(name, rows);
                    break;
                }
                case String: {
                    unionColumnHolder = new UnionColumnHolderString(name, rows);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Cannot create union column holder for type " + sourceColumn.getType());
                }
            }
            ((ColumnHolder)unionColumnHolder).setMetadata(sourceColumn.getMetadata());
            unionColumnHolder.mapSource(sourceId, sourceColumn.getColumn(), sourceColumn.getRowMapper());
            return (ColumnHolder) unionColumnHolder;
        }

        @Override
        public void onAfterCommit() {
            rowsToRemove.forEach(removeRowProc);
            rowsToRemove.resetQuick();
            super.onAfterCommit();
        }

        private final TLongProcedure removeRowProc = value -> {
            rows.remove(value);
            return true;
        };
    }

    private class UnionChangeQueue extends ChangeQueue {
        private TLongObjectHashMap<IChangeQueue> inboundColumnToChangeQueue = new TLongObjectHashMap<>();
        private TLongIntHashMap outboundColumnToInboundColumn = new TLongIntHashMap();
        private LongHashSet rows;

        public UnionChangeQueue(IOutput owner, LongHashSet rows) {
            super(owner);
            this.rows = rows;
        }

        public void mapColumn(ColumnHolder outboundColumnHolder, int source, ColumnHolder inboundColumnHolder, IChangeQueue sourceChanges) {
            long key = NumberUtils.packLong(source, inboundColumnHolder.getColumnId());
            inboundColumnToChangeQueue.put(key, sourceChanges);

            key = NumberUtils.packLong(source, outboundColumnHolder.getColumnId());
            outboundColumnToInboundColumn.put(key, inboundColumnHolder.getColumnId());
        }

        @Override
        public boolean isDirty(int rowId, int columnId) {
            if (super.isDirty(rowId, columnId)) {
                return true;
            } else if (columnId == output.sourceIdColumnHolder.getColumnId()) {
                return false;
            } else if (rowId == -1) {
                log.warn("Union change queue attempting to check isDirty for row id -1");
                return false;
            }

            long key = rows.get(rowId);
            int source = NumberUtils.unpackLongHi(key);
            int inboundRow = NumberUtils.unpackLongLo(key);

            key = NumberUtils.packLong(source, columnId);
            int inboundColumnId = outboundColumnToInboundColumn.get(key);

            key = NumberUtils.packLong(source, inboundColumnId);
            IChangeQueue upstreamChanges = inboundColumnToChangeQueue.get(key);
            return upstreamChanges.isDirty(inboundRow, inboundColumnId);
        }

        private void reset() {
            inboundColumnToChangeQueue.clear();
            outboundColumnToInboundColumn.clear();
        }
    }
}
