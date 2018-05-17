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

package io.viewserver.operators.join;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.IntHashSet;
import io.viewserver.collections.LongHashSet;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.NumberUtils;
import io.viewserver.execution.JoinColumnNamer;
import io.viewserver.operators.*;
import io.viewserver.schema.column.*;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by bemm on 27/10/2014.
 */
public class JoinOperator extends ConfigurableOperatorBase<IJoinConfig> {
    private static final Logger log = LoggerFactory.getLogger(JoinOperator.class);
    private static final int OUTER_JOIN_ROW_ID = Integer.MAX_VALUE;
    private final Input leftInput;
    private final Input rightInput;
    private final Output output;
    private String[] leftJoinColumns;
    private String[] rightJoinColumns;
    private ColumnHolder[] leftJoinHolders;
    private ColumnHolder[] rightJoinHolders;
    private IntHashSet joinKeys = new IntHashSet(8, 0.75f, -1);
    private TIntObjectHashMap<TIntHashSet> leftAssociations = new TIntObjectHashMap<>(8, 0.75f, -1);
    private TIntObjectHashMap<TIntHashSet> rightAssociations = new TIntObjectHashMap<>(8, 0.75f, -1);
    private LongHashSet outRows = new LongHashSet(8, 0.75f, -1);
    private boolean isLeftJoinOuter;
    private boolean isRightJoinOuter;
    private IColumnNameResolver columnNameResolver;
    private boolean alwaysResolveNames;
    private int[] joinKeyComponents;
    private String columnsOnOutput = "";
    private boolean hasVerifiedJoinColumns;

    public JoinOperator(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);

        leftInput = new Input("left", this, true);
        addInput(leftInput);

        rightInput = new Input("right", this, false);
        addInput(rightInput);

        output = new Output(Constants.OUT, this);
        addOutput(output);
        register();
    }

    @Override
    protected IJoinConfig mergePendingConfig(IJoinConfig pendingConfig, IJoinConfig newConfig) {
        if (!Arrays.equals(pendingConfig.getLeftJoinColumns(), newConfig.getLeftJoinColumns())) {
            throw new IllegalStateException("Cannot merge configs with conflicting left join columns");
        }
        if (!Arrays.equals(pendingConfig.getRightJoinColumns(), newConfig.getRightJoinColumns())) {
            throw new IllegalStateException("Cannot merge configs with conflicting left join columns");
        }
        if (pendingConfig.isLeftJoinOuter() != newConfig.isLeftJoinOuter()) {
            throw new IllegalStateException("Cannot merge configs with conflicting left outer join flag");
        }
        if (pendingConfig.isRightJoinOuter() != newConfig.isRightJoinOuter()) {
            throw new IllegalStateException("Cannot merge configs with conflicting right outer join flag");
        }
        return newConfig;
    }

    @Override
    protected void processConfig(IJoinConfig config) {
        String[] newLeftJoinColumns = config.getLeftJoinColumns();
        if (!Arrays.equals(newLeftJoinColumns, leftJoinColumns)) {
            leftJoinColumns = newLeftJoinColumns;
            leftJoinHolders = new ColumnHolder[leftJoinColumns.length];
            if (joinKeyComponents == null || joinKeyComponents.length < leftJoinColumns.length) {
                joinKeyComponents = new int[leftJoinColumns.length];
            }
            resetSchema();
        }
        boolean newIsLeftJoinOuter = config.isLeftJoinOuter();
        if (newIsLeftJoinOuter != isLeftJoinOuter) {
            isLeftJoinOuter = newIsLeftJoinOuter;
            resetSchema();
        }
        String[] newRightJoinColumns = config.getRightJoinColumns();
        if (!Arrays.equals(newRightJoinColumns, rightJoinColumns)) {
            rightJoinColumns = newRightJoinColumns;
            rightJoinHolders = new ColumnHolder[rightJoinColumns.length];
            resetSchema();
        }
        boolean newIsRightJoinOuter = config.isRightJoinOuter();
        if (newIsRightJoinOuter != isRightJoinOuter) {
            isRightJoinOuter = newIsRightJoinOuter;
            resetSchema();
        }
        columnNameResolver = config.getColumnNameResolver();
        alwaysResolveNames = config.getAlwaysResolveNames();

        String leftPrefix = config.getLeftPrefix();
        String rightPrefix = config.getRightPrefix();

        if(leftPrefix != null || rightPrefix != null){
            columnNameResolver = new JoinColumnNamer(leftPrefix, rightPrefix);
        }

//        log.trace("{} - processed config - ljc={}, rjc={}", getName(),
//                Arrays.asList(leftJoinColumns),
//                Arrays.asList(rightJoinColumns));
    }

    @Override
    protected void onDataClear() {
        super.onDataClear();

        outRows.clear();
        joinKeys.clear();
    }

    private int getJoinKey(int row, boolean isLeft, boolean usePreviousValues) {
        ColumnHolder[] joinColumns = isLeft ? leftJoinHolders : rightJoinHolders;
        String[] joinColumnNames = isLeft ? leftJoinColumns : rightJoinColumns;
        if (joinColumns == null || joinColumns.length == 0) {
            return 0;
        }


        if(!hasVerifiedJoinColumns) {
            String error = "";
            for (int i = 0; i < joinColumns.length; i++) {
                if (error.length() > 0) {
                    error += ",";
                }
                if (joinColumns[i] == null) {
                    error += joinColumnNames[i];
                }

            }
            if (error.length() > 0) {
                throw new RuntimeException(String.format("Attempting to join on keys \"%s\" but keys \"%s\" have not been found. Columns available on output are  \"%s\"", String.join(",", joinColumnNames), error, columnsOnOutput));
            }
            hasVerifiedJoinColumns = true;
        }

        if (joinColumns.length == 1) {
            return getJoinKeyComponent(joinColumns[0], row, usePreviousValues);
        }

        for (int i = 0; i < joinColumns.length; i++) {
            joinKeyComponents[i] = getJoinKeyComponent(joinColumns[i], row, usePreviousValues);
        }
        int groupKey = Arrays.hashCode(joinKeyComponents);
        int groupId = joinKeys.addInt(groupKey);
        if (groupId < 0) {
            groupId = -groupId - 1;
        }
        return groupId;
    }

    private int getJoinKeyComponent(ColumnHolder columnHolder, int row, boolean usePreviousValues) {
        switch (columnHolder.getType()) {
            case Bool: {
                IColumnBool joinColumn = (IColumnBool) columnHolder;
                return (usePreviousValues ? joinColumn.getPreviousBool(row) : joinColumn.getBool(row)) ? 1 : 0;
            }
            case NullableBool: {
                IColumnNullableBool joinColumn = (IColumnNullableBool) columnHolder;
                return (usePreviousValues ? joinColumn.getPreviousNullableBool(row) : joinColumn.getNullableBool(row)).getNumericValue();
            }
            case Byte: {
                IColumnByte joinColumn = (IColumnByte) columnHolder;
                return usePreviousValues ? joinColumn.getPreviousByte(row) : joinColumn.getByte(row);
            }
            case Short: {
                IColumnShort joinColumn = (IColumnShort) columnHolder;
                return usePreviousValues ? joinColumn.getPreviousShort(row) : joinColumn.getShort(row);
            }
            case Int: {
                IColumnInt joinColumn = (IColumnInt) columnHolder;
                return usePreviousValues ? joinColumn.getPreviousInt(row) : joinColumn.getInt(row);
            }
            case String: {
                IColumnString joinColumn = (IColumnString) columnHolder;
                if(usePreviousValues){
                    String previousString = joinColumn.getPreviousString(row);
                    return previousString != null ? previousString.hashCode() : "".hashCode();
                }else{
                    String string = joinColumn.getString(row);
                    return string != null ? string.hashCode() : "".hashCode();
                }
            }
            default: {
                throw new IllegalArgumentException("Column: " + columnHolder.getName() +  " Cannot group on a column of type " + columnHolder.getType());
            }
        }
    }

    public IOutput getOutput() {
        return output;
    }

    private class Input extends InputBase {
        private boolean isLeft;
        private TLongArrayList removedRows = new TLongArrayList(8, -1);

        public Input(String name, IOperator owner, boolean isLeft) {
            super(name, owner);
            this.isLeft = isLeft;
        }

        @Override
        protected void onSchemaReset() {
            if (isLeft) {
                if (leftJoinHolders != null) {
                    Arrays.fill(leftJoinHolders, null);
                }
            } else {
                if (rightJoinHolders != null) {
                    Arrays.fill(rightJoinHolders, null);
                }
            }
            hasVerifiedJoinColumns = false;
            columnsOnOutput = "";
            super.onSchemaReset();
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            String name = columnHolder.getName();
            if(columnsOnOutput.length() > 0){
                columnsOnOutput += ",";
            }
            columnsOnOutput += name;
            if(alwaysResolveNames || output.getSchema().getColumnHolder(name) != null) {
                if (columnNameResolver != null) {
                    name = columnNameResolver.resolveColumnName(name, this.isLeft);
                } else {
                    name = getProducer().getOwner().getName() + "_" + name;
                }
            }

            if (name != null) {
                if(output.getSchema().getColumnHolder(name) != null){
                    log.warn("Cannot add column \"{}\" as it is already contained in the schema",name);
                }else{
                    ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(name, columnHolder, isLeft);
                    output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
                }


            }

            String[] joinColumns = isLeft ? leftJoinColumns : rightJoinColumns;
            ColumnHolder[] joinHolders = isLeft ? leftJoinHolders : rightJoinHolders;
            for (int i = 0; i < joinColumns.length; i++) {
                if (columnHolder.getName().equals(joinColumns[i])) {
                    joinHolders[i] = columnHolder;
                    ((IWritableColumn)columnHolder.getColumn()).storePreviousValues();
                    break;
                }
            }
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            hasVerifiedJoinColumns = false;
        }

        @Override
        public void onDataClear() {
            super.onDataClear();

            if (isLeft) {
                leftAssociations.clear();
            } else {
                rightAssociations.clear();
            }
            removedRows.reset();
        }

        @Override
        protected void onRowAdd(int row) {
            int joinKey = getJoinKey(row, isLeft, false);

            TIntObjectHashMap<TIntHashSet> ourAssociations = isLeft ? leftAssociations : rightAssociations;
            TIntHashSet ourRows = ourAssociations.get(joinKey);
            if (ourRows == null) {
                ourRows = new TIntHashSet(8, 0.75f, -1);
                ourAssociations.put(joinKey, ourRows);
            }
            boolean hadOuterJoinRow = ourRows.remove(OUTER_JOIN_ROW_ID);
            ourRows.add(row);

            TIntObjectHashMap<TIntHashSet> theirAssociations = isLeft ? rightAssociations : leftAssociations;
            TIntHashSet theirRows = theirAssociations.get(joinKey);
            if (theirRows != null) {
                TIntIterator iterator = theirRows.iterator();
                while (iterator.hasNext()) {
                    int theirRowId = iterator.next();
                    if (hadOuterJoinRow) {
                        long outputRowKey = getOutputRowKey(isLeft ? OUTER_JOIN_ROW_ID : theirRowId, isLeft ? theirRowId : OUTER_JOIN_ROW_ID);
                        int outputRow = getOutputRow(outputRowKey);
                        if (log.isTraceEnabled()) {
                            log.trace("{} - isLeft={} - onRowAdd({}) - REMOVE - join key={}, output key={}, output row={}",
                                    getOwner().getName(), isLeft, row, joinKey, outputRowKey, outputRow);
                        }
                        output.handleRemove(outputRow);
                        removedRows.add(outputRowKey);
                    }
                    long outputRowKey = getOutputRowKey(isLeft ? row : theirRowId, isLeft ? theirRowId : row);
                    removedRows.remove(outputRowKey);
                    int outputRow = getOutputRow(outputRowKey);
                    if (log.isTraceEnabled()) {
                        log.trace("{} - isLeft={} - onRowAdd({}) - ADD - join key={}, output key={}, output row={}",
                                getOwner().getName(), isLeft, row, joinKey, outputRowKey, outputRow);
                    }
                    output.handleAdd(outputRow);
                }
            } else {
                boolean isOuterJoin = isLeft ? isLeftJoinOuter : isRightJoinOuter;
                if (isOuterJoin) {
                    long outputRowKey = getOutputRowKey(isLeft ? row : OUTER_JOIN_ROW_ID, isLeft ? OUTER_JOIN_ROW_ID : row);
                    removedRows.remove(outputRowKey);
                    int outputRow = getOutputRow(outputRowKey);
                    if (log.isTraceEnabled()) {
                        log.trace("{} - isLeft={} - onRowAdd({}) - ADD (OJ) - join key={}, output key={}, output row={}",
                                getOwner().getName(), isLeft, row, joinKey, outputRowKey, outputRow);
                    }
                    output.handleAdd(outputRow);
                    theirRows = new TIntHashSet(8, 0.75f, -1);
                    theirRows.add(OUTER_JOIN_ROW_ID);
                    theirAssociations.put(joinKey, theirRows);
                }
            }
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            int joinKey = getJoinKey(row, isLeft, false);
            int previousJoinKey = getJoinKey(row, isLeft, true);
            if (joinKey != previousJoinKey) {
                onRowRemove(row);
                onRowAdd(row);
            } else {
                TIntObjectHashMap<TIntHashSet> theirAssociations = isLeft ? rightAssociations : leftAssociations;
                TIntHashSet theirRows = theirAssociations.get(joinKey);
                if (theirRows != null) {
                    TIntIterator iterator = theirRows.iterator();
                    while (iterator.hasNext()) {
                        int theirRowId = iterator.next();
                        long outputRowKey = getOutputRowKey(isLeft ? row : theirRowId, isLeft ? theirRowId : row);
                        int outboundRow = outRows.index(outputRowKey);
                        if (outboundRow >= 0) {
                            if (log.isTraceEnabled()) {
                                log.trace("{} - isLeft={} - onRowUpdate({}) - UPDATE - join key={}, previous join key={}, output key={}, output row={}",
                                        getOwner().getName(), isLeft, row, joinKey, previousJoinKey, outputRowKey, outboundRow);
                            }
                            output.handleUpdate(outboundRow);
                        }
                    }
                }
            }
        }

        @Override
        protected void onRowRemove(int row) {
            int previousJoinKey = getJoinKey(row, isLeft, true);
            TIntObjectHashMap<TIntHashSet> ourAssociations = isLeft ? leftAssociations : rightAssociations;
            TIntHashSet ourRows = ourAssociations.get(previousJoinKey);
            if(ourRows == null){
                log.error("Unable to find association for key " + previousJoinKey);
                return;
            }
            ourRows.remove(row);
            boolean isOuterJoin = isLeft ? isRightJoinOuter : isLeftJoinOuter;

            TIntObjectHashMap<TIntHashSet> theirAssociations = isLeft ? rightAssociations : leftAssociations;
            TIntHashSet theirRows = theirAssociations.get(previousJoinKey);

            boolean addingOuterJoinRows = false;
            if (isOuterJoin && ourRows.isEmpty()) {
                addingOuterJoinRows = true;
            }

            boolean otherSideHasRows = false;
            if (theirRows != null) {
                TIntIterator iterator = theirRows.iterator();
                while (iterator.hasNext()) {
                    int theirRowId = iterator.next();
                    if (theirRowId != OUTER_JOIN_ROW_ID) {
                        otherSideHasRows = true;
                    }
                    long theirRowKey = getOutputRowKey(isLeft ? row : theirRowId, isLeft ? theirRowId : row);
                    int outboundRow = outRows.index(theirRowKey);
                    if (outboundRow >= 0) {
                        removedRows.add(theirRowKey);
                        if (log.isTraceEnabled()) {
                            log.trace("{} - isLeft={} - onRowRemove({}) - REMOVE - previous join key={}, output key={}, output row={}",
                                    getOwner().getName(), isLeft, row, previousJoinKey, theirRowKey, outboundRow);
                        }
                        output.handleRemove(outboundRow);
                        if (addingOuterJoinRows && theirRowId != OUTER_JOIN_ROW_ID) {
                            long outputRowKey = getOutputRowKey(isLeft ? OUTER_JOIN_ROW_ID : theirRowId, isLeft ? theirRowId : OUTER_JOIN_ROW_ID);
                            int outputRow = getOutputRow(outputRowKey);
                            if (log.isTraceEnabled()) {
                                log.trace("{} - isLeft={} - onRowRemove({}) - ADD (OJ) - previous join key={}, output key={}, output row={}",
                                        getOwner().getName(), isLeft, row, previousJoinKey, outputRowKey, outputRow);
                            }
                            output.handleAdd(outputRow);
                        }
                    }
                }
            }

            if (addingOuterJoinRows && otherSideHasRows) {
                ourRows.add(OUTER_JOIN_ROW_ID);
            }
        }

        @Override
        public void onAfterCommit() {
            super.onAfterCommit();

            removedRows.forEach(removeRowProc);
            removedRows.reset();
        }

        private final TLongProcedure removeRowProc = (row) -> {
            outRows.remove(row);
            return true;
        };
    }

    private int getOutputRow(int leftInboundRow, int rightInboundRow) {
        long outputRowKey = getOutputRowKey(leftInboundRow, rightInboundRow);
        return getOutputRow(outputRowKey);
    }

    private int getOutputRow(long outputRowKey) {
        int outputRow = outRows.addLong(outputRowKey);
        if (outputRow < 0) {
            outputRow = -outputRow - 1;
        }
        return outputRow;
    }

    private long getOutputRowKey(int leftInboundRow, int rightInboundRow) {
        if (leftInboundRow == OUTER_JOIN_ROW_ID && rightInboundRow == OUTER_JOIN_ROW_ID) {
            System.out.println("now");
        }
        return NumberUtils.packLong(leftInboundRow, rightInboundRow);
    }

    private class Output extends MappedOutputBase {
        private final MappedColumnHolderFactory columnHolderFactory;

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
        private boolean isLeft;

        @Override
        public IRowMapper getRowMapper(IRowMapper upstreamMapper) {
            return new RowMapper(upstreamMapper, isLeft);
        }

        public ColumnHolder createColumnHolder(String name, ColumnHolder sourceColumn, boolean isLeft) {
            this.isLeft = isLeft;
            return super.createColumnHolder(name, sourceColumn);
        }
    }

    private class RowMapper extends RowMapperBase {
        private boolean isLeft;

        public RowMapper(IRowMapper upstreamMapper, boolean isLeft) {
            super(upstreamMapper);
            this.isLeft = isLeft;
        }

        @Override
        public int lookupInboundRow(int row) {
            long mapping = outRows.get(row);
            int inboundRow = isLeft ? NumberUtils.unpackLongHi(mapping) : NumberUtils.unpackLongLo(mapping);
            return inboundRow == OUTER_JOIN_ROW_ID ? -1 : inboundRow;
        }
    }
}
