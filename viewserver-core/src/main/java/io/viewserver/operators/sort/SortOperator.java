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

package io.viewserver.operators.sort;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.SchemaChange;
import io.viewserver.schema.column.*;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TIntProcedure;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by nickc on 15/10/2014.
 */
public class SortOperator extends ConfigurableOperatorBase<ISortConfig> {
    private static final Logger log = LoggerFactory.getLogger(SortOperator.class);
    private Input input;
    private Output output;
    private SortDescriptor sortDescriptor;
    private ColumnHolderInt rankColumn;
    private ITableStorage tableStorage;
    private int start = -1;
    private int end = -1;
    private int maxRank;
    private IComparer comparer;
    private final IntHashSet outputRows = new IntHashSet(128, 0.75f, -1);

    public SortOperator(String name, IExecutionContext executionContext, ICatalog catalog, ITableStorage tableStorage) {
        super(name, executionContext, catalog);
        this.tableStorage = tableStorage;

        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);
    }

    public IInput getInput() {
        return input;
    }

    public IOutput getOutput() {
        return output;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    protected ISortConfig mergePendingConfig(ISortConfig pendingConfig, ISortConfig newConfig) {
        return newConfig;
    }

    @Override
    protected void processConfig(ISortConfig config) {
        if (configChanged(config)) {
            this.start = config.getStart();
            this.end = config.getEnd();

            this.resetData();
        }
    }

    private boolean configChanged(ISortConfig config) {
        if (this.config == null) {
            return true;
        }

        return !ObjectUtils.equals(config.getSortDescriptor(), this.config.getSortDescriptor())
                || config.getStart() != this.config.getStart()
                || config.getEnd() != this.config.getEnd();
    }

    @Override
    protected void onDataClear() {
        super.onDataClear();

        outputRows.clear();
    }

    private int getOutputRow(int row) {
        int mappedRow = outputRows.addInt(row);
        if (mappedRow < 0) {
            mappedRow = -mappedRow - 1;
        }
        return mappedRow;
    }

    private class Input extends InputBase {
        private final List<Integer> addedRows = new ArrayList<>();
        private final List<Integer> updatedRows = new ArrayList<>();
        private final List<Integer> removedRows = new ArrayList<>();
        private boolean processingDataChange;
        private final List<ColumnHolder> removedColumns = new ArrayList<>();
        private ColumnHolder[] sortColumnHolders;
        private boolean hasUpdatesWithDirtySortColumns;
        private final TIntArrayList rowsToRemove = new TIntArrayList(8, -1);

        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void onSchema() {
            boolean isReset = isSchemaResetRequested;
            if (isReset) {
                sortDescriptor = null;
                rankColumn = null;
                if (sortColumnHolders != null) {
                    Arrays.fill(sortColumnHolders, null);
                }
            }

            super.onSchema();

            ISortConfig config = pendingConfig != null ? pendingConfig : (isReset ? SortOperator.this.config : null);
            if (config != null) {
                SortOperator.SortDescriptor pendingSortDescriptor = config.getSortDescriptor();
                if (sortDescriptor == null || !sortDescriptor.equals(pendingSortDescriptor)) {
                    //if sort column has not been defined (ie for standard subscription) then sort by the first column by default
                    if (pendingSortDescriptor.columnsToSort.isEmpty()) {
                        pendingSortDescriptor.columnsToSort.add(new SortColumn(output.getSchema().getColumnHolder(0).getName()));
                    }

                    if (sortColumnHolders == null || sortColumnHolders.length != pendingSortDescriptor.columnsToSort.size()) {
                        sortColumnHolders = new ColumnHolder[pendingSortDescriptor.columnsToSort.size()];
                    } else {
                        Arrays.fill(sortColumnHolders, null);
                    }

                    int i = 0;
                    for (SortColumn columnToSort : pendingSortDescriptor.columnsToSort) {
                        ColumnHolder columnHolder = output.getSchema().getColumnHolder(columnToSort.getName());
                        if (columnHolder == null) {
                            //TODO put this back how it was - Bem, please explain your thoughts? :)
//                            log.warn("Cannot sort on column '" + columnToSort.getName() + "' as it does not exist");
                            throw new OperatorConfigurationException(getOwner(), String.format("Cannot sort on column '%s' as it does not exist", columnToSort.getName()));
                        } else {
                            sortColumnHolders[i] = getProducer().getSchema().getColumnHolder(pendingSortDescriptor.columnsToSort.get(i).name);
//                            ((IWritableColumn)sortColumnHolders[i].getContentType()).storePreviousValues();
                            i++;
                        }
                    }

                    ColumnHolder columnHolder = output.getSchema().getColumnHolder(pendingSortDescriptor.columnName);
                    if (columnHolder != null) {
                        ((IWritableColumnInt) columnHolder.getColumn()).resetAll();
                    } else {
                        columnHolder = ColumnHolderUtils.createColumnHolder(pendingSortDescriptor.columnName, ColumnType.Int);
                        rankColumn = (ColumnHolderInt) columnHolder;
                        output.getSchema().addColumn(columnHolder);
                    }

                    sortDescriptor = pendingSortDescriptor;

                    comparer = getComparer();
                }
            }

            tableStorage.initialise(128, output.getSchema(), output.getCurrentChanges());
            if(rankColumn != null) {
                ((IWritableColumn) rankColumn.getColumn()).storePreviousValues();
            }
        }

        @Override
        protected void onSchemaChange(SchemaChange schemaChange) {
            removedColumns.clear();

            super.onSchemaChange(schemaChange);

            int count = removedColumns.size();
            for (int i = 0; i < count; i++) {
                for (SortColumn columnToSort : sortDescriptor.columnsToSort) {
                    if (columnToSort.getName().equals(removedColumns.get(i).getName())) {
                        output.getSchema().removeColumn(rankColumn.getColumnId());
                        rankColumn = null;
                        break;
                    }
                }
            }
        }

        @Override
        protected void onDataReset() {
            boolean isRangeSet = start != -1 && end != -1;

            sort();

            IRowSequence allRows = getProducer().getAllRows();
            int rank;
            while (allRows.moveNext()) {
                int rowId = allRows.getRowId();
                int outputRow = getOutputRow(rowId);
                if (!isRangeSet || ((rank = rankColumn.getInt(outputRow)) >= start && (end == -1 || rank < end))) {
                    if (log.isTraceEnabled()) {
                        log.trace("{} - reset - in-row={}, out-row={}, value={}, rank={}",
                                getOwner().getName(), rowId, outputRow, ColumnHolderUtils.getValue(sortColumnHolders[0], rowId),
                                rankColumn.getInt(outputRow));
                    }
                    output.handleAdd(outputRow);
                }
            }

            setTotalSize();
        }

        @Override
        protected void onDataChange(IChangeQueue changeQueue) {
            // if there was no data previously, then do a full sort (which is quicker!)
            if (output.getRowCount() == 0) {
                onDataReset();
                return;
            }

            hasUpdatesWithDirtySortColumns = false;
            addedRows.clear();
            updatedRows.clear();
            removedRows.clear();

            processingDataChange = true;
            super.onDataChange(changeQueue);
            processingDataChange = false;

            boolean isRangeSet = start != -1 && end != -1;

            int updatedRowsCount = updatedRows.size();
            int removedRowsCount = removedRows.size();

            if (addedRows.isEmpty() && removedRows.isEmpty() && !hasUpdatesWithDirtySortColumns) {
                for (int i = 0; i < updatedRowsCount; i++) {
                    Integer updatedRow = getOutputRow(updatedRows.get(i));
                    int rank = rankColumn.getInt(updatedRow);
                    boolean isInRange = !isRangeSet || (rank >= start && (end == -1 || rank < end));
                    if (isInRange) {
                        output.handleUpdate(updatedRow);
                    }
                }
                return;
            }

            IRowSequence allRows = getProducer().getAllRows();

            sort();
            for (int i = 0; i < removedRowsCount; i++) {
                Integer removedRow = getOutputRow(removedRows.get(i));
                int oldRank = rankColumn.getInt(removedRow);
                if (!isRangeSet || (oldRank >= start && (end == -1 || oldRank < end))) {
                    output.handleRemove(removedRow);
                }
            }

            allRows.reset();
            while (allRows.moveNext()) {
                int rowId = allRows.getRowId();

                int outputRow = getOutputRow(rowId);

                int oldRank = rankColumn.getPreviousInt(outputRow);
                int newRank = rankColumn.getInt(outputRow);

                boolean newInRange = !isRangeSet || (newRank >= start && (end == -1 || newRank < end));
                if (newRank != oldRank) {
                    ((IWritableColumnInt) rankColumn.getColumn()).setInt(outputRow, newRank);
                    boolean oldInRange = !isRangeSet || (oldRank >= start && (end == -1 || oldRank < end));
                    if (addedRows.contains(rowId)) {
                        if (newInRange) {
                            if (log.isTraceEnabled()) {
                                log.trace("{} - change - add - in-row={}, out-row={}, value={}, rank={}",
                                        getOwner().getName(), rowId, outputRow, ColumnHolderUtils.getValue(sortColumnHolders[0], rowId),
                                        newRank);
                            }
                            output.handleAdd(outputRow);
                        }
                    } else {
                        if (newInRange && oldInRange) {
                            if (log.isTraceEnabled()) {
                                log.trace("{} - change - update - in-row={}, out-row={}, value={}, old-rank={}, new-rank={}",
                                        getOwner().getName(), rowId, outputRow, ColumnHolderUtils.getValue(sortColumnHolders[0], rowId),
                                        oldRank, newRank);
                            }
                            output.handleUpdate(outputRow);
                        } else if (newInRange) {
                            if (log.isTraceEnabled()) {
                                log.trace("{} - change - add - in-row={}, out-row={}, value={}, old-rank={}, new-rank={}",
                                        getOwner().getName(), rowId, outputRow, ColumnHolderUtils.getValue(sortColumnHolders[0], rowId),
                                        oldRank, newRank);
                            }
                            output.handleAdd(outputRow);
                        } else if (oldInRange) {
                            if (log.isTraceEnabled()) {
                                log.trace("{} - change - remove - in-row={}, out-row={}, value={}, old-rank={}, new-rank={}",
                                        getOwner().getName(), rowId, outputRow, ColumnHolderUtils.getValue(sortColumnHolders[0], rowId),
                                        oldRank, newRank);
                            }
                            output.handleRemove(outputRow);
                            rowsToRemove.add(rowId);
                        }
                    }
                } else if (newInRange && addedRows.contains(rowId)) {
                    if (log.isTraceEnabled()) {
                        log.trace("{} - change - add - in-row={}, out-row={}, value={}, rank={}",
                                getOwner().getName(), rowId, outputRow, ColumnHolderUtils.getValue(sortColumnHolders[0], rowId),
                                newRank);
                    }
                    output.handleAdd(outputRow);
                } else if (newInRange && updatedRows.contains(rowId)) {
                    if (log.isTraceEnabled()) {
                        log.trace("{} - change - update - in-row={}, out-row={}, value={}, rank={}",
                                getOwner().getName(), rowId, outputRow, ColumnHolderUtils.getValue(sortColumnHolders[0], rowId),
                                newRank);
                    }
                    output.handleUpdate(outputRow);
                }

            }
            setTotalSize();
        }

        private void setTotalSize() {
            output.setMetaDataValue("totalSize", getProducer().getRowCount());
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            ColumnHolder outColumn = output.getColumnHolderFactory().createColumnHolder(columnHolder.getName(), columnHolder);
            output.mapColumn(columnHolder, outColumn, getProducer().getCurrentChanges());
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            removedColumns.add(columnHolder);
            output.unmapColumn(columnHolder);
        }

        @Override
        protected void onRowAdd(int row) {
            if (processingDataChange) {
                addedRows.add(row);
            }
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            if (processingDataChange) {
                updatedRows.add(row);
                if (!hasUpdatesWithDirtySortColumns && isSortDirty(rowFlags)) {
                    hasUpdatesWithDirtySortColumns = true;
                }
            }
        }

        private boolean isSortDirty(IRowFlags rowFlags) {
            int count = sortColumnHolders.length;
            for (int i = 0; i < count; i++) {
                if (rowFlags.isDirty(sortColumnHolders[i].getColumnId())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onRowRemove(int row) {
            if (processingDataChange) {
                removedRows.add(row);
            }
        }

        @Override
        public void onAfterCommit() {
            super.onAfterCommit();

            rowsToRemove.forEach(removeRowProc);
            rowsToRemove.resetQuick();
        }

        private final TIntProcedure removeRowProc = (row) -> {
            outputRows.remove(row);
            return true;
        };

        private void sort() {
            int rowCount = getProducer().getRowCount();
            if (rowCount == 0) {
                return;
            }
            int[] rowIds = new int[rowCount];
            int i = 0;
            IRowSequence allRows = getProducer().getAllRows();
            while (allRows.moveNext()) {
                rowIds[i++] = allRows.getRowId();
            }

            //printSort(rowIds, "Unsorted");

            for (int j = sortDescriptor.columnsToSort.size() - 1; j >= 0; j--) {
                SortColumn columnToSort = sortDescriptor.columnsToSort.get(j);
                ColumnHolder sortColumnHolder = sortColumnHolders[j];
                if (sortColumnHolder == null) {
                    continue;
                }
                rowIds = sortColumn(rowIds, sortColumnHolder, columnToSort.descending, sortDescriptor.columnsToSort.size() > 1);
                printSort(rowIds,sortColumnHolder, "After sort by " + columnToSort.getName());
            }

            // mark the whole column as dirty
            IWritableColumnInt rankColumn = (IWritableColumnInt) SortOperator.this.rankColumn.getColumn();
            allRows.reset();
            log.debug("Writing ranks");
            for (int j=0;j< rowIds.length;j++) {
                int outputRow = getOutputRow(rowIds[j]);
                log.debug("Rank input row {} mapped to {} is {}",rowIds[j], outputRow, j);
                rankColumn.setInt(outputRow, j);
                log.debug("Rank input row {} mapped to {} is {} - dirty {}",rowIds[j], outputRow, rankColumn.getInt(outputRow), output.getCurrentChanges().isDirty(outputRow, SortOperator.this.rankColumn.getColumnId()));
            }
            if (--i > maxRank) {
                maxRank = i;
            }
            log.debug("Finished writing ranks");
        }

        private int[] sortColumn(int[] rowIds, ColumnHolder columnHolder, boolean descending, boolean multiColumn) {
            log.debug("Starting quicksort");
            switch (columnHolder.getType()) {
                case String: {
                    if (multiColumn) {
                        ColumnMergeSort.mergeSort(rowIds, getComparer(columnHolder, descending));
                    } else {
                        ColumnBurstsort.sort(rowIds, (ColumnHolderString) columnHolder);
                        if (descending) {
                            ArrayUtils.reverse(rowIds);
                        }
                    }
                    break;
                }
                case Int: {
                    rowIds = ColumnRadixSort.sort2(rowIds, (ColumnHolderInt) columnHolder, descending);
                    break;
                }
                case Bool: {
                    ColumnBooleanSort.sort(rowIds, (ColumnHolderBool) columnHolder, descending);
                    break;
                }
                default: {
                    if (multiColumn) {
                        ColumnMergeSort.mergeSort(rowIds, getComparer(columnHolder, descending));
                    } else {
                        ColumnQuicksort.quicksort(rowIds, 0, rowIds.length - 1, getComparer(columnHolder, descending));
                    }
                    break;
                }
            }
            log.debug("Finished quicksort");

            return rowIds;
        }

        private IComparer getComparer() {
            boolean isMultiColumn = sortDescriptor.columnsToSort.size() > 1;
            MultiColumnComparer multiColumnComparer = null;
            if (isMultiColumn) {
                multiColumnComparer = new MultiColumnComparer();
            }
            for (int i = 0; i < sortDescriptor.columnsToSort.size(); i++) {
                ColumnHolder sortColumnHolder = sortColumnHolders[i];
                if (sortColumnHolder == null) {
                    continue;
                }
                IComparer columnComparer = getComparer(sortColumnHolder, sortDescriptor.columnsToSort.get(i).descending);
                if (!isMultiColumn) {
                    return columnComparer;
                }
                multiColumnComparer.addColumnComparer(columnComparer);
            }
            return multiColumnComparer;
        }

        private IComparer getComparer(ColumnHolder columnHolder, boolean descending) {
            IComparer columnComparer;
            switch (columnHolder.getType()) {
                case Bool: {
                    columnComparer = new ComparerBool((IColumnBool) columnHolder, descending);
                    break;
                }
                case NullableBool: {
                    columnComparer = new ComparerNullableBool((IColumnNullableBool) columnHolder, descending);
                    break;
                }
                case Byte: {
                    columnComparer = new ComparerByte((IColumnByte) columnHolder, descending);
                    break;
                }
                case Short: {
                    columnComparer = new ComparerShort((IColumnShort) columnHolder, descending);
                    break;
                }
                case Int: {
                    columnComparer = new ComparerInt((IColumnInt) columnHolder, descending);
                    break;
                }
                case Long: {
                    columnComparer = new ComparerLong((IColumnLong) columnHolder, descending);
                    break;
                }
                case Float: {
                    columnComparer = new ComparerFloat((IColumnFloat) columnHolder, descending);
                    break;
                }
                case Double: {
                    columnComparer = new ComparerDouble((IColumnDouble) columnHolder, descending);
                    break;
                }
                case String: {
                    columnComparer = new ComparerString((IColumnString) columnHolder, descending);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Cannot create comparer for type " + columnHolder.getType());
                }
            }
            return columnComparer;
        }
    }

    private void printSort(int[] rowIds, ColumnHolder columnName, String caption) {
        StringBuilder sb = new StringBuilder();
        for(int i =0;i< rowIds.length;i++){
            if(sb.length() > 0){
                sb.append(",");
            }
            sb.append(rowIds[i] + "=" + ColumnHolderUtils.getValue(columnName,rowIds[i]));
        }
        log.debug(caption + " - " + sb.toString());
    }

    private class Output extends MappedOutputBase {
        private final MappedColumnHolderFactory columnHolderFactory;

        public Output(String name, IOperator owner) {
            super(name, owner);
            columnHolderFactory = new MappedColumnHolderFactory();
        }

        @Override
        public IColumnHolderFactory getColumnHolderFactory() {
            return columnHolderFactory;
        }
    }

    private class MappedColumnHolderFactory extends ColumnHolderFactory {
        @Override
        public IRowMapper getRowMapper(IRowMapper upstreamMapper) {
            return new RowMapper(upstreamMapper);
        }
    }

    private class RowMapper extends RowMapperBase {
        private RowMapper(IRowMapper upstreamMapper) {
            super(upstreamMapper);
        }

        @Override
        public int lookupInboundRow(int row) {
            return outputRows.get(row);
        }
    }

    public static class SortColumn {
        private String name;
        private boolean descending;

        public SortColumn(String name) {
            this.name = name;
            this.descending = false;
        }

        public SortColumn(String name, boolean descending) {
            this.name = name;
            this.descending = descending;
        }

        public String getName() {
            return name;
        }

        public boolean isDescending() {
            return descending;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SortColumn that = (SortColumn) o;

            if (descending != that.descending) return false;
            if (!name.equals(that.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (descending ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "SortColumn{" +
                    "name='" + name + '\'' +
                    ", descending=" + descending +
                    '}';
        }
    }

    public static class SortDescriptor {
        private String columnName;
        private List<SortColumn> columnsToSort = new ArrayList<>();

        public SortDescriptor(String columnName) {
            this.columnName = columnName;
        }

        public SortDescriptor(String columnName, String columnToSort, boolean descending) {
            this.columnName = columnName;
            columnsToSort.add(new SortColumn(columnToSort, descending));
        }

        public SortDescriptor(String columnName, Collection<SortColumn> columnsToSort) {
            this.columnName = columnName;
            this.columnsToSort.addAll(columnsToSort);
        }

        public String getColumnName() {
            return columnName;
        }

        public List<SortColumn> getColumnsToSort() {
            return columnsToSort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SortDescriptor that = (SortDescriptor) o;

            if (!columnName.equals(that.columnName)) return false;
            if (!columnsToSort.equals(that.columnsToSort)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = columnName.hashCode();
            result = 31 * result + columnsToSort.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "SortDescriptor{" +
                    "columnName='" + columnName + '\'' +
                    ", columnsToSort=" + columnsToSort +
                    '}';
        }
    }
}
