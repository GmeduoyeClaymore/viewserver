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

package io.viewserver.operators.index;

import gnu.trove.map.hash.TObjectIntHashMap;
import io.viewserver.Constants;
import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.CatalogHolder;
import io.viewserver.catalog.ICatalog;
import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.operators.*;
import io.viewserver.operators.sort.RadixSort;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;




/**
 * Created by bemm on 02/10/2014.
 */
public class IndexOperator extends ConfigurableOperatorBase<IIndexConfig> {
    private static final Logger log = LoggerFactory.getLogger(IndexOperator.class);
    private final Input input;
    private final Set<String> indexedColumns = new HashSet<>();
    private final IndexSummaryOutput output;
    private EWAHCompressedBitmap allRows = new EWAHCompressedBitmap();
    private final TIntObjectHashMap outputsByRowKey = new TIntObjectHashMap();
    private final TObjectIntHashMap rowKeysByOutput = new TObjectIntHashMap();


    public IndexOperator(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);

        input = new Input(Constants.IN, this);
        addInput(input);
        output = new IndexSummaryOutput(Constants.OUT, this);
        super.addOutput(output);
        register();
    }

    public IInput getInput() {
        return input;
    }

    @Override
    protected IIndexConfig mergePendingConfig(IIndexConfig pendingConfig, IIndexConfig newConfig) {
        return new IIndexConfig() {
            @Override
            public String[] getIndices() {
                ArrayList<String> indices = new ArrayList<>();
                if (pendingConfig.getIndices() != null) {
                    indices.addAll(Arrays.asList(pendingConfig.getIndices()));
                }
                if (newConfig.getIndices() != null) {
                    indices.addAll(Arrays.asList(newConfig.getIndices()));
                }
                return indices.toArray(new String[indices.size()]);
            }

            @Override
            public Output[] getOutputs() {
                ArrayList<Output> outputs = new ArrayList<>();
                if (pendingConfig.getOutputs() != null) {
                    outputs.addAll(Arrays.asList(pendingConfig.getOutputs()));
                }
                if (newConfig.getOutputs() != null) {
                    outputs.addAll(Arrays.asList(newConfig.getOutputs()));
                }
                return outputs.toArray(new Output[outputs.size()]);
            }
        };
    }

    @Override
    protected void processConfig(IIndexConfig config) {
        if (config.getIndices() != null) {
            for (String indexedColumn : config.getIndices()) {
                this.indexedColumns.add(indexedColumn);
            }
        }
    }

    public IOutput getOrCreateOutput(String name, QueryHolder... queryHolders) {
        Output output = (Output) getOutput(name);
        if (output == null) {
            output = new Output(name, this, queryHolders);
            int rowId = getIndexOutputs().size();
            outputsByRowKey.put(rowId,output);
            rowKeysByOutput.put(output,rowId);
            addOutput(output);
            if (input.getProducer() != null) {
                int count = queryHolders.length;
                for (int i = 0; i < count; i++) {
                    queryHolders[i].setSchema(input.getProducer().getSchema());
                }
            }
            this.output.handleAdd(rowId);
        }
        return output;
    }

    private List<Output> getIndexOutputs() {
        List<Output> result = new ArrayList<>();
        for(IOutput output: this.getOutputs()){
            if(output instanceof Output){
                result.add((Output) output);
            }
        }
        return result;
    }

    private class Input extends InputBase {
        private final Map<String, IndexHolder> indexHolders = new HashMap<>();
        private final List<IndexHolder> indexHolderList = new ArrayList<>();
        private final BitSet dirtyIndexes = new BitSet();
        private TIntArrayList addedRows = new TIntArrayList();
        private TIntArrayList removedRows = new TIntArrayList();

        private Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void onInitialise() {
            super.onInitialise();

            List<Output> outputs = getIndexOutputs();
            int count = outputs.size();
            for (int i = 0; i < count; i++) {
                IOutput output = outputs.get(i);
                if(!(output instanceof Output)){
                    continue;
                }
                for (QueryHolder queryHolder : ((Output) output).queryHolders) {
                    queryHolder.setSchema(getProducer().getSchema());
                }
            }
        }

        @Override
        public void onSchema() {
            // for outputs that were created after the initial commit, we need to fire
            // the schema and snapshot
            List<Output> outputs = getIndexOutputs();
            int count = outputs.size();
            for (int i = 0; i < count; i++) {
                IOutput output = outputs.get(i);
                if(!(output instanceof Output)){
                    continue;
                }
                if (!((Output) output).initialised) {
                    ((Output) output).buildSchema();
                }
            }

            super.onSchema();
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            List<Output> outputs = getIndexOutputs();
            int count = outputs.size();
            for (int i = 0; i < count; i++) {
                IOutput output = outputs.get(i);
                if (((Output) output).initialised) {
                    ((Output) output).addColumn(columnHolder);
                }
            }

            if (indexedColumns.contains(columnHolder.getName())) {
                ((IWritableColumn) columnHolder.getColumn()).storePreviousValues();
                IndexHolder indexHolder = new IndexHolder(columnHolder);
                indexHolders.put(columnHolder.getName(), indexHolder);
                indexHolderList.add(indexHolder);
            }
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            boolean wasIndexed = indexedColumns.contains(columnHolder.getName());

            List<Output> outputs = getIndexOutputs();
            int count = outputs.size();
            for (int i = 0; i < count; i++) {
                IOutput output = outputs.get(i);
                if (((Output) output).initialised) {
                    if (wasIndexed && ((Output) output).isQuerying(columnHolder)) {
                        output.tearDown();
                    } else {
                        ((Output) output).removeColumn(columnHolder);
                    }
                }
            }

            if (wasIndexed) {
                IndexHolder indexHolder = indexHolders.remove(columnHolder.getName());
                count = indexHolderList.size();
                for (int i = 0; i < count; i++) {
                    if (indexHolderList.get(i) == indexHolder) {
                        indexHolderList.remove(i);
                        break;
                    }
                }
            }
        }

        @Override
        public void onData() {
            // for outputs that were created after the initial commit, we need to fire
            // the schema and snapshot
            long start = System.nanoTime();
            List<Output> outputs = getIndexOutputs();
            int count = outputs.size();
            for (int i = 0; i < count; i++) {
                IOutput output = outputs.get(i);
                if (!((Output) output).initialised) {
                    ((Output) output).addSnapshot();
                    ((Output) output).initialised = true;
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("Took {}ms to initialise new outputs", (System.nanoTime() - start) / 1000000f);
            }

            start = System.nanoTime();
            dirtyIndexes.clear();
            IChangeQueue currentChanges = getProducer().getCurrentChanges();
            count = indexHolderList.size();
            for (int i = 0; i < count; i++) {
                IndexHolder indexHolder = indexHolderList.get(i);
                if (indexHolder == null) {
                    continue;
                }
                if (currentChanges.columnHasDirty(indexHolder.columnHolder.getColumnId())) {
                    dirtyIndexes.set(i);
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("Took {}ms to initialise check dirty indices", (System.nanoTime() - start) / 1000000f);
            }

            addedRows.resetQuick();
            removedRows.resetQuick();

            start = System.nanoTime();
            super.onData();
            if (log.isTraceEnabled()) {
                log.trace("Took {}ms to carry out standard process data", (System.nanoTime() - start) / 1000000f);
            }

            if (addedRows.size() > 0) {
                allRows = allRows.or(EWAHCompressedBitmap.bitmapOf(addedRows.toArray()));
            }
            if (removedRows.size() > 0) {
                allRows = allRows.xor(EWAHCompressedBitmap.bitmapOf(removedRows.toArray()));
            }

            start = System.nanoTime();
            count = indexHolderList.size();
            for (int i = 0; i < count; i++) {
                IndexHolder indexHolder = indexHolderList.get(i);
                if (indexHolder == null) {
                    continue;
                }
                indexHolder.applyChanges();
            }
            if (log.isTraceEnabled()) {
                log.trace("Took {}ms to process index removals", (System.nanoTime() - start) / 1000000f);
            }
        }

        @Override
        protected void onRowAdd(int row) {
            int count = indexHolderList.size();
            for (int i = 0; i < count; i++) {
                IndexHolder indexHolder = indexHolderList.get(i);
                if (indexHolder == null) {
                    continue;
                }
                indexHolder.updateIndex(row, true);
            }

            List<Output> outputs = getIndexOutputs();
            count = outputs.size();
            for (int i = 0; i < count; i++) {
                Output output = (Output) outputs.get(i);
                StringBuilder sb = new StringBuilder();
                if (queryMatches(output, row, false, sb)) {
                    output.handleAdd(getOutputRow(output, row));
                    log.info("!!! Matched a row on add " + sb);
                }
                int summaryRowKey = rowKeysByOutput.get(output);
                for(ColumnHolder holder : IndexOperator.this.output.getSchema().getColumnHolders()){
                    IndexOperator.this.output.getCurrentChanges().markDirty(summaryRowKey,holder.getColumnId());
                    IndexOperator.this.output.handleUpdate(summaryRowKey);
                }
            }

            addedRows.add(row);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            int count = indexHolderList.size();
            for (int i = 0; i < count; i++) {
                IndexHolder indexHolder = indexHolderList.get(i);
                if (indexHolder == null) {
                    continue;
                }
                if (dirtyIndexes.get(i) && rowFlags.isDirty(indexHolder.columnHolder.getColumnId())) {
                    indexHolder.updateIndex(row, false);
                }
            }

            // TODO: check dirty flags (efficiently!)
            int outputRow;
            List<Output> outputs = getIndexOutputs();
            count = outputs.size();
            for (int j = 0; j < count; j++) {
                Output output = (Output) outputs.get(j);
                StringBuilder sb = new StringBuilder();
                if (queryMatches(output, row, false, sb)) {
                    outputRow = getOutputRow(output, row);
                    if (output.isRowActive(outputRow)) {
                        output.handleUpdate(outputRow);
                    } else {
                        output.handleAdd(outputRow);
                    }
                    log.info("!!! Matched a row on update " + sb);
                } else if ((outputRow = output.rows.index(row)) != -1) {
                    output.handleRemove(outputRow);
                    output.markRowForRemoval(row);
                }
            }
        }

        @Override
        protected void onRowRemove(int row) {
            int count = indexHolderList.size();
            for (int j = 0; j < count; j++) {
                IndexHolder indexHolder = indexHolderList.get(j);
                if (indexHolder == null) {
                    continue;
                }
                indexHolder.markRowForRemoval(row);
            }

            List<Output> outputs = getIndexOutputs();
            count = outputs.size();
            for (int i = 0; i < count; i++) {
                Output output = (Output) outputs.get(i);
                int outputRow;
                if ((outputRow = output.rows.index(row)) != -1) {
                    output.handleRemove(outputRow);
                    output.markRowForRemoval(row);
                }
            }

            removedRows.add(row);
        }

        private int getOutputRow(Output output, int row) {
            int mappedRow = output.rows.addInt(row);
            if (mappedRow < 0) {
                mappedRow = -mappedRow - 1;
            }
            return mappedRow;
        }

        private boolean queryMatches(Output output, int row, boolean usePrevious, StringBuilder diagnostics) {
            int count = output.queryHolders.length;
            for (int i = 0; i < count; i++) {
                QueryHolder queryHolder = output.queryHolders[i];
                int value = -1;
                switch (queryHolder.getColumnHolder().getType()) {
                    case Bool: {
                        IColumnBool columnHolder = (IColumnBool) queryHolder.getColumnHolder();
                        boolean boolValue = usePrevious ? columnHolder.getPreviousBool(row) : columnHolder.getBool(row);
                        value = NullableBool.fromBoolean(boolValue).getNumericValue();
                        break;
                    }
                    case NullableBool: {
                        IColumnNullableBool columnHolder = (IColumnNullableBool) queryHolder.getColumnHolder();
                        NullableBool nullableBool = usePrevious ? columnHolder.getPreviousNullableBool(row) : columnHolder.getNullableBool(row);
                        value = nullableBool.getNumericValue();
                        break;
                    }
                    case Byte: {
                        IColumnByte columnHolder = (IColumnByte) queryHolder.getColumnHolder();
                        value = usePrevious ? columnHolder.getPreviousByte(row) : columnHolder.getByte(row);
                        break;
                    }
                    case Short: {
                        IColumnShort columnHolder = (IColumnShort) queryHolder.getColumnHolder();
                        value = usePrevious ? columnHolder.getPreviousShort(row) : columnHolder.getShort(row);
                        break;
                    }
                    case Int: {
                        IColumnInt columnHolder = (IColumnInt) queryHolder.getColumnHolder();
                        value = usePrevious ? columnHolder.getPreviousInt(row) : columnHolder.getInt(row);
                        break;
                    }
                }
                if (diagnostics != null) {
                    diagnostics.append("\n\r").append(queryHolder).append(", value=").append(value);
                }
                boolean contains = false;
                int valueCount = queryHolder.values.length;
                for (int j = 0; j < valueCount; j++) {
                    if (queryHolder.values[j] == value) {
                        contains = true;
                        break;
                    }
                }
                if (diagnostics != null) {
                    diagnostics.append(", contains=").append(contains);
                }
                if (contains ^ !queryHolder.exclude) {
                    if (diagnostics != null) {
                        diagnostics.append(", return=false");
                    }
                    return false;
                }
            }
            if (diagnostics != null) {
                diagnostics.append(", return=true");
            }
            return true;
        }
    }

    private class Output extends MappedOutputBase {
        private QueryHolder[] queryHolders;
        private final IntHashSet rows;
        private IColumnHolderFactory columnHolderFactory;
        private boolean initialised;
        private TIntArrayList rowsToRemove = new TIntArrayList(8, -1);

        private Output(String name, IOperator owner, QueryHolder... queryHolders) {
            super(name, owner);
            this.queryHolders = queryHolders;

            rows = new IntHashSet(128, 0.75f, -1);
            columnHolderFactory = new MappedColumnHolderFactory(rows);
        }

        public IntHashSet getRows() {
            return rows;
        }

        @Override
        public void onInitialise() {
            super.onInitialise();

            // if the output was created before the initial commit, then schema and snapshot will come through
            // in the normal way, so skip initialisation
            initialised = true;
        }

        private void initialise() {
            if (!initialised) {
                initialised = true;
                buildSchema();
                addSnapshot();
            }
        }

        private void buildSchema() {
            Schema inputSchema = input.getProducer().getSchema();
            List<ColumnHolder> columnHolders = inputSchema.getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }

                addColumn(columnHolder);
            }
        }

        @Override
        public IColumnHolderFactory getColumnHolderFactory() {
            return columnHolderFactory;
        }

        private void addSnapshot() {
            EWAHCompressedBitmap[] ands = new EWAHCompressedBitmap[queryHolders.length];
            for (int i = 0; i < queryHolders.length; i++) {
                QueryHolder queryHolder = queryHolders[i];
                ColumnHolder columnHolder = queryHolder.getColumnHolder();
                if(columnHolder == null){
                    throw new RuntimeException(String.format("Unable to find column named \"%s\" in the input operator for this index",queryHolder.columnName));
                }
                IndexHolder indexHolder = input.indexHolders.get(columnHolder.getName());
                List<EWAHCompressedBitmap> ors = new ArrayList<>();
                for (int j = 0; j < queryHolder.values.length; j++) {
                    EWAHCompressedBitmap or = indexHolder.indices.get(queryHolder.values[j]);

                    if (or != null) {
                        ors.add(or);
                    }
                }

                if (ors.size() == 0) {
                    ands[i] = EWAHCompressedBitmap.bitmapOf();
                } else if (ors.size() == 1) {
                    ands[i] = ors.get(0);
                } else {
                    ands[i] = EWAHCompressedBitmap.or(ors.toArray(new EWAHCompressedBitmap[ors.size()]));
                }

                if (queryHolder.exclude) {
                    ands[i] = allRows.xor(ands[i]);
                }
            }
            EWAHCompressedBitmap and = EWAHCompressedBitmap.and(ands);
            if (and != null) {
                for (int rowId : and.toArray()) {
                    int mappedRow = rows.addInt(rowId);
                    handleAdd(mappedRow);
                }
            }
        }

        public void addColumn(ColumnHolder columnHolder) {
            ColumnHolder outHolder = columnHolderFactory.createColumnHolder(columnHolder.getName(), columnHolder);
            getSchema().addColumn(outHolder);
            getCurrentChanges().mapColumn(columnHolder, outHolder, input.getProducer().getCurrentChanges());
        }

        public void removeColumn(ColumnHolder columnHolder) {
            getSchema().removeColumn(columnHolder.getColumnId());
        }

        public boolean isQuerying(ColumnHolder columnHolder) {
            int count = queryHolders.length;
            for (int i = 0; i < count; i++) {
                if (queryHolders[i].getColumnHolder() == columnHolder) {
                    return true;
                }
            }
            return false;
        }

        public void markRowForRemoval(int row) {
            rowsToRemove.add(row);
        }

        @Override
        public void onAfterCommit() {
            super.onAfterCommit();

            rowsToRemove.forEach(this::removeRow);
            rowsToRemove.resetQuick();
        }

        private boolean removeRow(int row) {
            rows.remove(row);
            return true;
        }
    }

    private class IndexHolder {
        private ColumnHolder columnHolder;
        private TIntObjectHashMap<EWAHCompressedBitmap> indices;
        private TIntObjectHashMap<TIntArrayList> rowsForRemoval = new TIntObjectHashMap<>();
        private TIntObjectHashMap<TIntArrayList> rowsForAdding = new TIntObjectHashMap<>();
        private int[] adds;

        private IndexHolder(ColumnHolder columnHolder) {
            this.columnHolder = columnHolder;
            indices = new TIntObjectHashMap<>(8);
        }

        public void updateIndex(int row, boolean isNewRow) {
            int value = getValue(row, false);

            if (!isNewRow) {
                int previousValue = getValue(row, true);
                if (previousValue != value) {
                    markRowForRemoval(row);
                    //rows.add(row);
                }
            }

            TIntArrayList rows = rowsForAdding.get(value);
            if (rows == null) {
                rows = new TIntArrayList();
                rowsForAdding.put(value, rows);
            }
            rows.add(row);
        }

        public void markRowForRemoval(int row) {
            int previousValue = getValue(row, true);
            TIntArrayList rows = rowsForRemoval.get(previousValue);
            if (rows == null) {
                rows = new TIntArrayList();
                rowsForRemoval.put(previousValue, rows);
            }
            rows.add(row);
        }

        public void applyChanges() {
            rowsForAdding.forEachEntry(this::applyAdds);
            rowsForRemoval.forEachEntry(this::applyRemoves);
        }

        private boolean applyAdds(int value, TIntArrayList adds) {
            final int addCount = adds.size();
            if (addCount > 0) {
                EWAHCompressedBitmap index = indices.get(value);
                final int[] addsArray = adds.toArray();
                int[] sortedAdds = RadixSort.sort2(addsArray, addCount);
                if (index != null) {
                    indices.put(value, index.or(EWAHCompressedBitmap.bitmapOf(sortedAdds)));
                } else {
                    indices.put(value, EWAHCompressedBitmap.bitmapOf(sortedAdds));
                }
                adds.resetQuick();
            }
            return true;
        }

        private boolean applyRemoves(int value, TIntArrayList removes) {
            if (removes.size() > 0) {
                EWAHCompressedBitmap index = indices.get(value);
                indices.put(value, index.andNot(EWAHCompressedBitmap.bitmapOf(RadixSort.sort2(removes.toArray()))));
                removes.resetQuick();
            }
            return true;
        }

        private int getValue(int row, boolean previous) {
            switch (columnHolder.getType()) {
                case Bool: {
                    IColumnBool column = (IColumnBool) columnHolder;
                    return (previous ? NullableBool.fromBoolean(column.getPreviousBool(row)).getNumericValue() : NullableBool.fromBoolean(column.getBool(row)).getNumericValue());
                }
                case NullableBool: {
                    IColumnNullableBool column = (IColumnNullableBool) columnHolder;
                    return (previous ? column.getPreviousNullableBool(row) : column.getNullableBool(row)).getNumericValue();
                }
                case Byte: {
                    IColumnByte column = (IColumnByte) columnHolder;
                    return previous ? column.getPreviousByte(row) : column.getByte(row);
                }
                case Short: {
                    IColumnShort column = (IColumnShort) columnHolder;
                    return previous ? column.getPreviousShort(row) : column.getShort(row);
                }
                case Int: {
                    IColumnInt column = (IColumnInt) columnHolder;
                    return previous ? column.getPreviousInt(row) : column.getInt(row);
                }
                default: {
                    throw new RuntimeException("Invalid column type for index " + columnHolder.getType() + " name " + columnHolder.getName());
                }
            }
        }
    }


    public class IndexSummaryOutput extends OutputBase {
        public static final String NAME_COLUMN = "name";
        public static final String VALUES = "values";
        private CatalogHolder catalogHolder;

        public IndexSummaryOutput(String name, IOperator owner) {
            super(name, owner);
            ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder(NAME_COLUMN, ColumnType.String);
            columnHolder.setColumn(new NameColumn());
            getSchema().addColumn(columnHolder);
            ColumnHolder columnHolder1 = ColumnHolderUtils.createColumnHolder(VALUES, ColumnType.String);
            columnHolder1.setColumn(new ValuesColumn());
            getSchema().addColumn(columnHolder1);
        }

        @Override
        public void clearSchema() {
        }

        @Override
        public void clearData() {
        }

        private class NameColumn extends ColumnStringBase {
            public NameColumn() {
                super(NAME_COLUMN);
            }

            @Override
            public String getString(int row) {
                IOutput operator = (IOutput)outputsByRowKey.get(row);
                return operator.getName();
            }

            @Override
            public String getPreviousString(int row) {
                return null;
            }

            @Override
            public boolean supportsPreviousValues() {
                return false;
            }
        }


        private class ValuesColumn extends ColumnStringBase {
            private ValuesColumn() {
                super(VALUES);
            }

            @Override
            public String getString(int row) {
                Output operator = (Output)outputsByRowKey.get(row);
                return operator.getRows().toString();
            }

            @Override
            public String getPreviousString(int row) {
                return null;
            }

            @Override
            public boolean supportsPreviousValues() {
                return false;
            }
        }
    }

    public static class QueryHolder {
        private ColumnHolder columnHolder;
        private String columnName;
        private int[] values;
        private Schema schema;
        private boolean exclude;

        public QueryHolder(String columnName, int... values) {
            this.columnName = columnName;
            this.values = values;
        }

        // for deserialisation...yuck
        public QueryHolder() {
        }

        private ColumnHolder getColumnHolder() {
            if (columnHolder == null) {
                columnHolder = schema.getColumnHolder(columnName);
            }
            return columnHolder;
        }

        public String getColumnName() {
            return columnName;
        }

        public int[] getValues() {
            return values;
        }

        public boolean isExclude() {
            return exclude;
        }

        private void setSchema(Schema schema) {
            this.schema = schema;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueryHolder that = (QueryHolder) o;

            if (!columnName.equals(that.columnName)) return false;
            if (!Arrays.equals(values, that.values)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = columnName.hashCode();
            result = 31 * result + Arrays.hashCode(values);
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(values[i]);
            }
            return String.format("%s:%s:[%s]", this.columnName, this.exclude, builder.toString());
        }

        public static QueryHolder include(String columnName, int... values) {
            return new QueryHolder(columnName, values);
        }

        public static QueryHolder exclude(String columnName, int... values) {
            QueryHolder queryHolder = new QueryHolder(columnName, values);
            queryHolder.exclude = true;
            return queryHolder;
        }

        // for deserialisation...yuck
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        // for deserialisation...yuck
        public void setValues(int[] values) {
            this.values = values;
        }

        // for deserialisation...yuck
        public void setExclude(boolean exclude) {
            this.exclude = exclude;
        }
    }

    private class MappedColumnHolderFactory extends ColumnHolderFactory {
        private final IntHashSet rows;

        public MappedColumnHolderFactory(IntHashSet rows) {
            this.rows = rows;
        }

        @Override
        public IRowMapper getRowMapper(IRowMapper upstreamMapper) {
            return new RowMapper(upstreamMapper, rows);
        }
    }

    private class RowMapper extends RowMapperBase {
        private IntHashSet rows;

        private RowMapper(IRowMapper upstreamMapper, IntHashSet rows) {
            super(upstreamMapper);
            this.rows = rows;
        }

        @Override
        public int lookupInboundRow(int row) {
            return rows.get(row);
        }
    }
}
