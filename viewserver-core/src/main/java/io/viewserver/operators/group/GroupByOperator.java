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

package io.viewserver.operators.group;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.operators.*;
import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.operators.group.summary.ISummaryFactory;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.SchemaChange;
import io.viewserver.schema.column.*;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bemm on 02/10/2014.
 */
public class GroupByOperator extends ConfigurableOperatorBase<IGroupByConfig> {
    private static final Logger log = LoggerFactory.getLogger(GroupByOperator.class);
    private Input input;
    private Output output;
    private String[] groupBy;
    private IGroupByConfig.Summary[] summaryConfig;
    private final List<ISummary> summaries = new ArrayList<>();
    private SummaryRegistry summaryRegistry;
    private ITableStorage tableStorage;
    private String countColumnName;
    private final List<BitSet> subtotals = new ArrayList<>();
    private ArrayList<TIntHashSet> groupIdToRowIds = new ArrayList<>(8);
    private int[] groupIdComponents;

    public GroupByOperator(String name, IExecutionContext executionContext, ICatalog catalog, SummaryRegistry summaryRegistry, ITableStorage tableStorage) {
        super(name, executionContext, catalog);
        this.summaryRegistry = summaryRegistry;
        this.tableStorage = tableStorage;

        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);
        register();
    }

    @Override
    protected IGroupByConfig mergePendingConfig(IGroupByConfig pendingConfig, IGroupByConfig newConfig) {
        if (!ObjectUtils.equals(pendingConfig.getCountColumnName(), newConfig.getCountColumnName())) {
            throw new IllegalStateException("Cannot merge configs with conflicting count column names");
        }
        if (!ObjectUtils.equals(pendingConfig.getGroupBy(), newConfig.getGroupBy())) {
            throw new IllegalStateException("Cannot merge configs with conflicting group by columns");
        }
        if (!ObjectUtils.equals(pendingConfig.getSubtotals(), newConfig.getSubtotals())) {
            throw new IllegalStateException("Cannot merge configs with conflicting subtotals setting");
        }
        return new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return pendingConfig.getGroupBy();
            }

            @Override
            public List<Summary> getSummaries() {
                HashSet<Summary> summaries = new OrderedHashSet<>();
                summaries.addAll(pendingConfig.getSummaries());
                summaries.addAll(newConfig.getSummaries());
                return new ArrayList<>(summaries);
            }

            @Override
            public String getCountColumnName() {
                return pendingConfig.getCountColumnName();
            }

            @Override
            public List<String> getSubtotals() {
                return pendingConfig.getSubtotals();
            }
        };
    }

    @Override
    protected void processConfig(IGroupByConfig config) {
        if (configChanged(config)) {
            List<String> groupBy = config.getGroupBy();
            if (groupIdComponents == null || groupIdComponents.length < groupBy.size()) {
                groupIdComponents = new int[groupBy.size()];
            }
            this.groupBy = groupBy.toArray(new String[groupBy.size()]);
            input.groupByColumns = new ColumnHolder[this.groupBy.length];

            List<IGroupByConfig.Summary> summaries = config.getSummaries();
            this.summaryConfig = summaries.toArray(new IGroupByConfig.Summary[summaries.size()]);

            this.countColumnName = config.getCountColumnName();

            subtotals.clear();
            if (config.getSubtotals() != null && config.getSubtotals().size() != 0) {
                for (String subtotal : config.getSubtotals()) {
                    BitSet subtotalColumns = new BitSet();
                    if (subtotal != null && !"".equals(subtotal)) {
                        for (String subtotalColumn : subtotal.split("\\|")) {
                            int index = groupBy.indexOf(subtotalColumn);
                            if (index == -1) {
                                throw new IllegalArgumentException("Invalid subtotal configuration - no group column '" + subtotalColumn + "'");
                            }
                            subtotalColumns.set(index);
                        }
                    }
                    subtotals.add(subtotalColumns);
                }
            } else {
                BitSet subtotalColumns = new BitSet();
                for (int i = 0; i < groupBy.size(); i++) {
                    subtotalColumns.set(i);
                }
                subtotals.add(subtotalColumns);
            }

            input.resetSchema();
        }
    }

    private boolean configChanged(IGroupByConfig config) {
        if (this.config == null) {
            return true;
        }

        if (!ObjectUtils.equals(config.getCountColumnName(), this.config.getCountColumnName())) {
            return true;
        }

        if (config.getGroupBy().size() != this.config.getGroupBy().size()) {
            return true;
        }
        for (String groupBy : config.getGroupBy()) {
            if (!this.config.getGroupBy().contains(groupBy)) {
                return true;
            }
        }

        if (config.getSummaries().size() != this.config.getSummaries().size()) {
            return true;
        }
        for (IGroupByConfig.Summary summary : config.getSummaries()) {
            if (!this.config.getSummaries().contains(summary)) {
                return true;
            }
        }

        if (config.getSubtotals().size() != this.config.getSubtotals().size()) {
            return true;
        }
        for (String subtotal : config.getSubtotals()) {
            if (!this.config.getSubtotals().contains(subtotal)) {
                return true;
            }
        }

        return false;
    }


    public IInput getInput() {
        return input;
    }

    public IOutput getOutput() {
        return output;
    }

    private class Input extends InputBase {
        private ColumnHolder[] groupByColumns;
        private final IntHashSet groupIds;
        private ColumnHolderInt countHolder;

        public Input(String name, IOperator owner) {
            super(name, owner);

            groupIds = new IntHashSet(128, 0.75f, -1);
        }

        @Override
        protected void onSchemaReset() {
            List<ColumnHolder> columnHolders = input.getProducer().getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < groupBy.length; i++) {
                groupByColumns[i] = getProducer().getSchema().getColumnHolder(groupBy[i]);
                if (groupByColumns[i] == null) {
                    throw new RuntimeException("No column '" + groupBy[i] + "' to group by");
                }
                ((IWritableColumn) groupByColumns[i].getColumn()).storePreviousValues();

                ColumnType type = groupByColumns[i].getType();
                if (type == ColumnType.Bool) {
                    // when grouping by a boolean column, the outbound column will be a NullableBool column
                    type = ColumnType.NullableBool;
                }
                ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder(groupBy[i], type);
                ColumnMetadata metadata = ColumnHolderUtils.createColumnMetadata(type);
                switch (type) {
                    case NullableBool: {
                        ((ColumnMetadataNullableBool) metadata).setNullValue(NullableBool.Null);
                        break;
                    }
                    case Byte: {
                        ((ColumnMetadataByte) metadata).setNullValue((byte) -1);
                        break;
                    }
                    case Short: {
                        ((ColumnMetadataShort) metadata).setNullValue(Short.MIN_VALUE);
                        break;
                    }
                    case Int: {
                        ((ColumnMetadataInt) metadata).setNullValue(Integer.MIN_VALUE);
                        break;
                    }
                    case String: {
                        ((ColumnMetadataString) metadata).setNullValue(null);
                        break;
                    }
                }
                columnHolder.setMetadata(metadata);
                output.getSchema().addColumn(columnHolder);
            }

            countHolder = (ColumnHolderInt) ColumnHolderUtils.createColumnHolder(countColumnName == null ? "count" : countColumnName, ColumnType.Int);
            output.getSchema().addColumn(countHolder);

            tableStorage.initialise(128, output.getSchema(), output.getCurrentChanges());

            summaries.clear();
            int summaryCount = GroupByOperator.this.summaryConfig.length;
            for (int i = 0; i < summaryCount; i++) {
                IGroupByConfig.Summary summaryConfig = GroupByOperator.this.summaryConfig[i];
                if (summaryConfig.isRegex()) {

                    if(summaryConfig.getTarget() != null) {
                        Pattern pattern = Pattern.compile(summaryConfig.getTarget());

                        for (int j = 0; j < count; j++) {
                            ColumnHolder columnHolder = columnHolders.get(j);
                            Matcher matcher = pattern.matcher(columnHolder.getName());
                            if (matcher.find()) {
                                String name = summaryConfig.getName();
                                for (int k = 0; k <= matcher.groupCount(); k++) {
                                    name = name.replace("$" + k, matcher.group(k));
                                }
                                addSummary(new IGroupByConfig.Summary(name, summaryConfig.getFunction(), columnHolder.getName(), false, summaryConfig.getArguments()));
                            }
                        }
                    }
                } else {
                    addSummary(summaryConfig);
                }
            }
        }

        private void addSummary(IGroupByConfig.Summary summaryConfig) {
            ISummaryFactory summaryFactory = summaryRegistry.getSummaryFactory(summaryConfig.getFunction());
            ISummary summary = summaryFactory.createSummary(summaryConfig.getName(), summaryConfig.getTarget(), summaryConfig.getArguments());
            summaries.add(summary);
            Context context = new Context(getProducer().getSchema(), output.getSchema(), countHolder);
            summary.initialise(context);
            output.getSchema().addColumn(context.getResultColumn());
        }

        @Override
        protected void onSchemaChange(SchemaChange schemaChange) {
            // we don't care about columns being added, since the aggregated schema is completely different to
            // the input schema
            // TODO: handle removal of columns that matter
        }

        @Override
        public void onData() {
            super.onData();

            int count = summaries.size();
            for (int i = 0; i < count; i++) {
                summaries.get(i).onAfterCommit();
            }
        }

        @Override
        protected void onDataReset() {
            groupIdToRowIds.clear();

            groupIds.clear();
            ((IWritableColumnInt)countHolder.getColumn()).resetAll();

            int count = summaries.size();
            for (int i = 0; i < count; i++) {
                summaries.get(i).reset();
            }

            super.onDataReset();
        }

        @Override
        protected void onRowAdd(int row) {
            int subtotalCount = subtotals.size();
            int summaryCount = summaries.size();
            for (int i = 0; i < subtotalCount; i++) {
                BitSet subtotal = subtotals.get(i);
                int groupId = getGroupId(row, false, subtotal);
                ensureOutputCapacity(groupId);

                boolean isNewGroup = !output.isRowActive(groupId);
                if (isNewGroup) {
                    createNewGroup(groupId, row, subtotal);
                }

                updateCount(groupId, 1);

                if (summaryConfig != null) {
                    for (int j = 0; j < summaryCount; j++) {
                        ISummary summary = summaries.get(j);
                        if (isNewGroup) {
                            summary.onGroupAdd(groupId);
                        }
                        summary.onGroupEnter(groupId, row);
                    }
                }
            }
        }

        private boolean updateCount(int groupId, int delta) {
            IWritableColumnInt countColumn = (IWritableColumnInt) countHolder.getColumn();
            int previousCount = countColumn.getInt(groupId);
            int count = previousCount + delta;
            if (count == 0) {
                if (log.isTraceEnabled()) {
                    log.trace("Removing group and row for group id {}", groupId);
                }
                int summaryCount = summaries.size();
                for (int i = 0; i < summaryCount; i++) {
                    summaries.get(i).onGroupRemove(groupId);
                }
                output.handleRemove(groupId);
            }
            countColumn.setInt(groupId, count);
            return count != 0;
        }

        private void createNewGroup(int groupId, int row, BitSet subtotal) {
            if (log.isTraceEnabled()) {
                log.trace("Adding row for group id {}", groupId);
            }
            output.handleAdd(groupId);

            for (int i = 0; i < groupBy.length; i++) {
                ColumnHolder outHolder = output.getSchema().getColumnHolder(i);
                switch (groupByColumns[i].getType()) {
                    case Bool: {
                        // when grouping by a boolean column, the outbound column will be a NullableBool column
                        NullableBool value = subtotal.get(i) ? NullableBool.fromBoolean(((IColumnBool) groupByColumns[i]).getBool(row)) : NullableBool.Null;
                        ((IWritableColumnNullableBool) outHolder.getColumn()).setNullableBool(groupId, value);
                        break;
                    }
                    case NullableBool: {
                        NullableBool value = subtotal.get(i) ? ((IColumnNullableBool) groupByColumns[i]).getNullableBool(row) : NullableBool.Null;
                        ((IWritableColumnNullableBool) outHolder.getColumn()).setNullableBool(groupId, value);
                        break;
                    }
                    case Byte: {
                        byte value = subtotal.get(i) ? ((IColumnByte) groupByColumns[i]).getByte(row) : ((ColumnHolderByte) outHolder).getMetadata().getNullValue();
                        ((IWritableColumnByte) outHolder.getColumn()).setByte(groupId, value);
                        break;
                    }
                    case Short: {
                        short value = subtotal.get(i) ? ((IColumnShort) groupByColumns[i]).getShort(row) : ((ColumnHolderShort) outHolder).getMetadata().getNullValue();
                        ((IWritableColumnShort) outHolder.getColumn()).setShort(groupId, value);
                        break;
                    }
                    case Int: {
                        int value = subtotal.get(i) ? ((IColumnInt) groupByColumns[i]).getInt(row) : ((ColumnHolderInt) outHolder).getMetadata().getNullValue();
                        ((IWritableColumnInt) outHolder.getColumn()).setInt(groupId, value);
                        break;
                    }
                    case String: {
                        String value = subtotal.get(i) ? ((IColumnString) groupByColumns[i]).getString(row) : ((ColumnHolderString) outHolder).getMetadata().getNullValue();
                        ((IWritableColumnString) outHolder.getColumn()).setString(groupId, value);
                        break;
                    }
                }
            }

            TIntHashSet rowIds = new TIntHashSet(128, 0.75f, -1);
            groupIdToRowIds.add(rowIds);
        }

        private void ensureOutputCapacity(int capacity) {
            List<ColumnHolder> columnHolders = output.getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ((IGrowableColumn) columnHolders.get(i).getColumn()).ensureCapacity(capacity);
            }
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            int subtotalCount = subtotals.size();
            int summaryCount = summaries.size();
            for (int i = 0; i < subtotalCount; i++) {
                BitSet subtotal = subtotals.get(i);
                int oldGroupId = getGroupId(row, true, subtotal);
                int newGroupId = getGroupId(row, false, subtotal);
                if (oldGroupId != newGroupId) {
                    boolean groupRemoved = !removeRowFromGroup(row, oldGroupId);

                    boolean isNewGroup = !output.isRowActive(newGroupId);
                    if (isNewGroup) {
                        createNewGroup(newGroupId, row, subtotal);
                    }
                    addRowToGroup(row, newGroupId);

                    for (int j = 0; j < summaryCount; j++) {
                        ISummary summary = summaries.get(j);
                        if (!groupRemoved) {
                            summary.onGroupLeave(oldGroupId, row);
                        }
                        if (isNewGroup) {
                            summary.onGroupAdd(newGroupId);
                        }
                        summary.onGroupEnter(newGroupId, row);
                    }
                } else {
                    // TODO: add dirty checks here
                    for (int j = 0; j < summaryCount; j++) {
                        ISummary summary = summaries.get(j);
                        if (summary.hasChanges(rowFlags)) {
                            summary.onRowUpdate(newGroupId, row);
                        }
                    }
                }
            }
        }

        private void addRowToGroup(int rowId, int groupId) {
            groupIdToRowIds.get(groupId).add(rowId);
            updateCount(groupId, 1);
        }

        private boolean removeRowFromGroup(int rowId, int groupId) {
            if (groupId >= groupIdToRowIds.size()) {
                log.warn("About to crap out with bad group ID! row={}, group={}", rowId, groupId);
                log.warn(OutputPrinter.getOutput(input.getProducer(), true));
                log.warn(OutputPrinter.getOutput(input.getProducer(), false));
            }
            TIntHashSet rowsInGroup = groupIdToRowIds.get(groupId);
            rowsInGroup.remove(rowId);
            return updateCount(groupId, -1);
        }

        @Override
        protected void onRowRemove(int row) {
            int subtotalCount = subtotals.size();
            int summaryCount = summaries.size();
            for (int i = 0; i < subtotalCount; i++) {
                int group = getGroupId(row, true, subtotals.get(i));
                if (removeRowFromGroup(row, group)) {
                    for (int j = 0; j < summaryCount; j++) {
                        summaries.get(j).onGroupLeave(group, row);
                    }
                }
            }
        }

        private int getGroupId(int row, boolean usePreviousValues, BitSet subtotal) {
            if (groupByColumns == null || groupByColumns.length == 0) {
                return 0;
            }

            int groupKey;
            if (groupByColumns.length == 1) {
                groupKey = subtotal.get(0) ? getGroupIdComponent(groupByColumns[0], row, usePreviousValues) : -1;
            } else {

                for (int i = 0; i < groupByColumns.length; i++) {
                    groupIdComponents[i] = subtotal.get(i) ? getGroupIdComponent(groupByColumns[i], row, usePreviousValues) : -1;
                }
                groupKey = Arrays.hashCode(groupIdComponents);
            }
            int groupId = groupIds.addInt(groupKey);
            if (groupId < 0) {
                groupId = -groupId - 1;
            }
            return groupId;
        }

        private int getGroupIdComponent(ColumnHolder columnHolder, int row, boolean usePreviousValues) {
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
                case String: {
                    IColumnString groupByColumn = (IColumnString) columnHolder;
                    return usePreviousValues ? (groupByColumn.getPreviousString(row) != null ? groupByColumn.getPreviousString(row).hashCode() : -1) : (groupByColumn.getString(row) != null ? groupByColumn.getString(row).hashCode() : -1);
                }
                default: {
                    throw new IllegalArgumentException("Cannot group on a column of type " + columnHolder.getType());
                }
            }
        }
    }

    private class Output extends OutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void clearSchema() {
            super.clearSchema();
        }

        @Override
        public void clearData() {
            super.clearData();
        }
    }

    private class Context implements ISummaryContext {
        private final Schema inboundSchema;
        private Schema outboundSchema;
        private ColumnHolderInt countHolder;
        private ColumnHolder resultColumn;

        public Context(Schema inboundSchema, Schema outboundSchema, ColumnHolderInt countHolder) {
            this.inboundSchema = inboundSchema;
            this.outboundSchema = outboundSchema;
            this.countHolder = countHolder;
        }

        @Override
        public Schema getInboundSchema() {
            return inboundSchema;
        }

        @Override
        public Schema getOutboundSchema() {
            return outboundSchema;
        }

        @Override
        public void markDirty(int groupId) {
            output.handleUpdate(groupId);
            output.getCurrentChanges().markDirty(groupId,resultColumn.getColumnId());
        }

        @Override
        public ITableStorage getTableStorage() {
            return tableStorage;
        }

        @Override
        public IColumnWatcher getColumnWatcher() {
            return output.getCurrentChanges();
        }

        @Override
        public ColumnHolder getResultColumn() {
            return resultColumn;
        }

        @Override
        public void setResultColumn(ColumnHolder resultColumn) {
            this.resultColumn = resultColumn;
        }

        @Override
        public ColumnHolderInt getCountHolder() {
            return countHolder;
        }

        @Override
        public void forEachRowInGroup(int groupId, TIntProcedure procedure) {
            groupIdToRowIds.get(groupId).forEach(procedure);
        }
    }
}
