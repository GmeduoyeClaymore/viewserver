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

package io.viewserver.adapters.common;

import io.viewserver.core.*;
import io.viewserver.datasource.*;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.expression.parser.ExpressionVisitorImpl;
import io.viewserver.expression.tree.*;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnMetadata;
import io.viewserver.schema.column.ColumnType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class DataLoader implements IDataLoader {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private ITableUpdater tableUpdater;
    private FunctionRegistry functionRegistry;
    private IExpressionParser expressionParser;
    private ExecutionContext executionContext;
    protected DimensionMapper dimensionMapper;
    protected DataSource dataSource;
    private final RowUpdater rowUpdater;
    protected String name;
    protected IDataAdapter dataAdapter;
    protected IDataQueryProvider dataQueryProvider;
    protected ITable table;
    protected Schema schema;
    protected List<PooledItem<Record>> pendingRecords = new ArrayList<>();
    private int batchSize = 10000;
    protected int snapshotSize;
    protected int processedRecords = 0;
    protected SettableFuture<Boolean> loadDataFuture = SettableFuture.create();
    private IPool<Record> recordPool;
    private Map<String, IExpression> expressions = new HashMap<>();
    private TableKeyDefinition tableKeyDefinition;

    public DataLoader(String name, IDataAdapter dataAdapter, IDataQueryProvider dataQueryProvider) {
        this.name = name;
        this.dataAdapter = dataAdapter;
        this.dataQueryProvider = dataQueryProvider;
        this.rowUpdater = new RowUpdater();
    }

    @Override
    public String getName() {
        return name;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public IDataLoader withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    @JsonIgnore
    public ITable getTable() {
        return table;
    }



    @Override
    public IDataAdapter getDataAdapter() {
        return dataAdapter;
    }

    @Override
    @JsonIgnore
    public SettableFuture<Boolean> getLoadDataFuture() {
        return loadDataFuture;
    }


    public IDataQueryProvider getDataQueryProvider() {
        return dataQueryProvider;
    }

    @Override
    public void configure(ITableUpdater tableUpdater, DimensionMapper dimensionMapper, DataSource dataSource, FunctionRegistry functionRegistry, IExpressionParser expressionParser, ExecutionContext executionContext) {
        this.dimensionMapper = dimensionMapper;
        this.dataSource = dataSource;
        this.tableUpdater = tableUpdater;
        this.functionRegistry = functionRegistry;
        this.expressionParser = expressionParser;
        this.executionContext = executionContext;
    }

    @Override
    public void createTable() {
        schema = getSchema();
        TableKeyDefinition tableKeyDefinition = getTableKeyDefinition();
        if (dataAdapter instanceof IWritableDataAdapter) {
            ((IWritableDataAdapter) dataAdapter).setSchema(schema);
            ((IWritableDataAdapter) dataAdapter).setTableKeyDefinition(tableKeyDefinition);
        }
        if (dataQueryProvider instanceof IWritableDataQueryProvider) {
            ((IWritableDataQueryProvider) dataQueryProvider).setSchema(schema);
        }
        if (tableUpdater instanceof IKeyedTableUpdater && ((IKeyedTableUpdater) tableUpdater).getTableKeyDefinition() == null) {
            ((IKeyedTableUpdater) tableUpdater).setTableKeyDefinition(tableKeyDefinition);
        }
        final CountDownLatch latch = new CountDownLatch(1);
        executionContext.getReactor().scheduleTask(() -> {
            table = tableUpdater.createTable(name, schema);

            // writable, non-system datasource tables can be reset by a table edit command
            if (dataSource != null && dataSource.hasOption(DataSourceOption.IsWritable) && !dataSource.hasOption(DataSourceOption.IsSystem)) {
                table.setAllowDataReset(true);
//            table.deferOperation(this::performLoadData);
            } else {
//            performLoadData();
            }
            latch.countDown();
        }, 0, -1);
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public ListenableFuture load() {
        log.debug("Starting loading for data source '{}'", name);

        try {
            loadData();
        }catch (Throwable ex){
            this.loadDataFuture.setException(ex);
        }
        return this.loadDataFuture;
    }

    protected TableKeyDefinition getTableKeyDefinition() {
        if (tableKeyDefinition == null) {
            if (dataSource != null && dataSource.hasOption(DataSourceOption.IsKeyed) && dataSource.getSchema() != null) {
                return DataSourceHelper.getTableKeyDefinition(dataSource);
            }
            if (dataAdapter instanceof IWritableDataAdapter) {
                return ((IWritableDataAdapter) dataAdapter).getDerivedTableKeyDefinition();
            }
        }
        return tableKeyDefinition;
    }

    @Override
    public void setTableKeyDefinition(TableKeyDefinition tableKeyDefinition) {
        this.tableKeyDefinition = tableKeyDefinition;
    }

    protected Schema getSchema() {
        if (schema != null) {
            return schema;
        }
        if (dataSource != null && dataSource.getSchema() != null) {
            return DataSourceHelper.getSchema(dataSource, dimensionMapper);
        }
        if (dataAdapter != null && dataQueryProvider != null) {
            return dataAdapter.getDerivedSchema();
        }
        return null;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    protected boolean loadData() {
        loadSnapshot(record -> {
            this.addOrUpdateRecord(record, true);
        });
        processPendingRecords(true);
        return true;
    }

    protected void loadSnapshot(Consumer<IRecord> consumer) {
        log.debug("Loading snapshot for data source '{}'", name);
        snapshotSize = dataAdapter.getRecords(dataQueryProvider != null ? dataQueryProvider.getSnapshotQuery() : null, consumer);
    }

    protected void addOrUpdateRecord(IRecord record, boolean isSnapshot) {
        PooledItem<Record> pendingRecord = getRecordPool().take();
        pendingRecord.getItem().initialiseFromRecord(record);
        pendingRecords.add(pendingRecord);
        if (pendingRecords.size() < batchSize) {
            return;
        }

        processPendingRecords(isSnapshot);
    }

    private IPool<Record> getRecordPool() {
        if (recordPool == null) {
            recordPool = new Pool<>(Record::new, batchSize, Record::clear);
        }
        return recordPool;
    }

    protected void processPendingRecords(boolean isSnapshot) {
        List<PooledItem<Record>> pendingRecords = this.pendingRecords;
        this.pendingRecords = new ArrayList<>();

        if (pendingRecords.isEmpty()) {
            if (isSnapshot) {
                log.debug("{} - empty snapshot loaded", name);
                loadDataFuture.set(true);
            }
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        executionContext.getReactor().scheduleTask(() -> {
            int count = pendingRecords.size();
            for (int i = 0; i < count; i++) {
                PooledItem<Record> pooledRecord = pendingRecords.get(i);
                rowUpdater.record = pooledRecord.getItem();
                tableUpdater.addOrUpdateRow(rowUpdater);
                pooledRecord.release();
            }
            log.debug("{} {} rows added or updated", this.name, count);

            processedRecords += count;
            if (isSnapshot && processedRecords >= snapshotSize) {
                loadDataFuture.set(true);
            }
            latch.countDown();
        }, 0, -1);
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }

    protected class RowUpdater implements ITableRowUpdater {
        protected IRecord record;

        @Override
        public Object getValue(String columnName) {
            return record.getValue(columnName);
        }

        public void setRecord(IRecord record) {
            this.record = record;
        }

        @Override
        public void setValues(ITableRow row) {
            List<ColumnHolder> columnHolders = table.getOutput().getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }

                Dimension dimension = null;
                String columnName = columnHolder.getName();
                if (dataSource != null) {
                    dimension = dataSource.getDimension(columnName);
                }
                ColumnMetadata metadata = columnHolder.getMetadata();
                io.viewserver.datasource.ColumnType dataType = null;
                if (metadata != null) {
                    if (metadata.isFlagged(ColumnFlags.DATASOURCE_CALCULATION)) {
                        continue;
                    }
                    dataType = metadata.getDataType();
                }
                if (dataType == null) {
                    dataType = mapToDataType(columnHolder.getType());
                }
                if (dimension != null) {
                    int id = -1;
                    switch (dataType) {
                        case Bool: {
                            id = dimensionMapper.mapBool(dataSource, dimension, record.getBool(columnName));
                            break;
                        }
                        case NullableBool: {
                            id = dimensionMapper.mapNullableBool(dataSource, dimension, record.getNullableBool(columnName));
                            break;
                        }
                        case Byte: {
                            id = dimensionMapper.mapByte(dataSource, dimension, record.getByte(columnName));
                            break;
                        }
                        case Short: {
                            id = dimensionMapper.mapShort(dataSource, dimension, record.getShort(columnName));
                            break;
                        }
                        case Int: {
                            id = dimensionMapper.mapInt(dataSource, dimension, record.getInt(columnName));
                            break;
                        }
                        case Long: {
                            id = dimensionMapper.mapLong(dataSource, dimension, record.getLong(columnName));
                            break;
                        }
                        case String: {
                            id = dimensionMapper.mapString(dataSource, dimension, record.getString(columnName));
                            break;
                        }
                    }
                    setDimensionValue(row, columnName, dimension, id);
                } else {
                    switch (dataType) {
                        case Bool: {
                            row.setBool(columnName, record.getBool(columnName));
                            break;
                        }
                        case NullableBool: {
                            row.setNullableBool(columnName, record.getNullableBool(columnName));
                            break;
                        }
                        case Byte: {
                            row.setByte(columnName, record.getByte(columnName));
                            break;
                        }
                        case Short: {
                            row.setShort(columnName, record.getShort(columnName));
                            break;
                        }
                        case Int: {
                            row.setInt(columnName, record.getInt(columnName));
                            break;
                        }
                        case Long: {
                            row.setLong(columnName, record.getLong(columnName));
                            break;
                        }
                        case Float: {
                            row.setFloat(columnName, record.getFloat(columnName));
                            break;
                        }
                        case Double: {
                            row.setDouble(columnName, record.getDouble(columnName));
                            break;
                        }
                        case String: {
                            String value = record.getString(columnName);
                            if (value != null) {
                                row.setString(columnName, value.intern());
                            }
                            break;
                        }
                        case Date: {
                            Date date = record.getDate(columnName);
                            if (date != null) {
                                row.setLong(columnName, date.getTime());
                            }
                            break;
                        }
                        case DateTime: {
                            Date dateTime = record.getDateTime(columnName);
                            if (dateTime != null) {
                                row.setLong(columnName, dateTime.getTime());
                            }
                            break;
                        }
                    }
                }
            }

            if (dataSource != null) {
                List<CalculatedColumn> calculatedColumns = dataSource.getCalculatedColumns();
                int calculatedColumnsCount = calculatedColumns.size();
                for (int i = 0; i < calculatedColumnsCount; i++) {
                    CalculatedColumn calculatedColumn = calculatedColumns.get(i);
                    IExpression expression = getExpression(calculatedColumn, this);
                    Dimension dimension = null;
                    String columnName = calculatedColumn.getName();
                    if (dataSource != null) {
                        dimension = dataSource.getDimension(columnName);
                    }
                    int id = -1;
                    switch (expression.getType()) {
                        case Bool: {
                            boolean value = ((IExpressionBool) expression).getBool(row.getRowId());
                            if (dimension != null) {
                                id = dimensionMapper.mapBool(dataSource, dimension, value);
                            } else {
                                row.setBool(columnName, value);
                            }
                            break;
                        }
                        case NullableBool: {
                            NullableBool value = ((IExpressionNullableBool) expression).getNullableBool(row.getRowId());
                            if (dimension != null) {
                                id = dimensionMapper.mapNullableBool(dataSource, dimension, value);
                            } else {
                                row.setNullableBool(columnName, value);
                            }
                            break;
                        }
                        case Byte: {
                            byte value = ((IExpressionByte) expression).getByte(row.getRowId());
                            if (dimension != null) {
                                id = dimensionMapper.mapByte(dataSource, dimension, value);
                            } else {
                                row.setByte(columnName, value);
                            }
                            break;
                        }
                        case Short: {
                            short value = ((IExpressionShort) expression).getShort(row.getRowId());
                            if (dimension != null) {
                                id = dimensionMapper.mapShort(dataSource, dimension, value);
                            } else {
                                row.setShort(columnName, value);
                            }
                            break;
                        }
                        case Int: {
                            int value = ((IExpressionInt) expression).getInt(row.getRowId());
                            if (dimension != null) {
                                id = dimensionMapper.mapInt(dataSource, dimension, value);
                            } else {
                                row.setInt(columnName, value);
                            }
                            break;
                        }
                        case Long: {
                            long value = ((IExpressionLong) expression).getLong(row.getRowId());
                            if (dimension != null) {
                                id = dimensionMapper.mapLong(dataSource, dimension, value);
                            } else {
                                row.setLong(columnName, value);
                            }
                            break;
                        }
                        case Float: {
                            row.setFloat(columnName, ((IExpressionFloat) expression).getFloat(row.getRowId()));
                            break;
                        }
                        case Double: {
                            row.setDouble(columnName, ((IExpressionDouble) expression).getDouble(row.getRowId()));
                            break;
                        }
                        case String: {
                            String value = ((IExpressionString) expression).getString(row.getRowId());
                            if (dimension != null) {
                                id = dimensionMapper.mapString(dataSource, dimension, value);
                            } else {
                                row.setString(columnName, value);
                            }
                            break;
                        }
                    }
                    if (dimension != null) {
                        setDimensionValue(row, columnName, dimension, id);
                    }
                }
            }
        }

        protected io.viewserver.datasource.ColumnType mapToDataType(ColumnType columnType) {
            switch (columnType) {
                case Bool: return io.viewserver.datasource.ColumnType.Bool;
                case NullableBool: return io.viewserver.datasource.ColumnType.NullableBool;
                case Byte: return io.viewserver.datasource.ColumnType.Byte;
                case Short: return io.viewserver.datasource.ColumnType.Short;
                case Int: return io.viewserver.datasource.ColumnType.Int;
                case Long: return io.viewserver.datasource.ColumnType.Long;
                case Float: return io.viewserver.datasource.ColumnType.Float;
                case Double: return io.viewserver.datasource.ColumnType.Double;
                case String: return io.viewserver.datasource.ColumnType.String;
                default: throw new IllegalArgumentException(String.format("Unknown column type '%s'", columnType));
            }
        }

        protected void setDimensionValue(ITableRow row, String columnName, Dimension dimension, int id) {
            switch (dimension.getCardinality()) {
                case Boolean: {
                    row.setBool(columnName, id == NullableBool.True.getNumericValue());
                    break;
                }
                case Byte: {
                    row.setByte(columnName, (byte) (id & 0xff));
                    break;
                }
                case Short: {
                    row.setShort(columnName, (short) (id & 0xffff));
                    break;
                }
                case Int: {
                    row.setInt(columnName, id);
                    break;
                }
            }
        }
    }

    protected IExpression getExpression(CalculatedColumn column, RowUpdater rowUpdater) {
        IExpression expression = expressions.get(column.getName());
        if (expression == null) {
            expression = expressionParser.parse(column.getExpression(), new DataSourceExpressionVisitor(functionRegistry, rowUpdater));
            expressions.put(column.getName(), expression);
        }
        return expression;
    }

    private class DataSourceExpressionVisitor extends ExpressionVisitorImpl {
        private RowUpdater rowUpdater;

        public DataSourceExpressionVisitor(FunctionRegistry functionRegistry, RowUpdater rowUpdater) {
            super(schema, functionRegistry, null, dimensionMapper, null);
            this.rowUpdater = rowUpdater;
        }

        @Override
        protected IExpression getColumnExpression(String columnName) {
            Column column = dataSource.getSchema().getColumn(columnName);
            if (column == null) {
                throw new IllegalArgumentException("No such column '" + columnName + "'");
            }
            return new DataSourceColumnExpression(column, rowUpdater);
        }
    }

    private class DataSourceColumnExpression implements IExpression, IExpressionBool, IExpressionNullableBool, IExpressionByte, IExpressionShort,
            IExpressionInt, IExpressionLong, IExpressionFloat, IExpressionDouble, IExpressionString {
        private Column column;
        private RowUpdater rowUpdater;

        public DataSourceColumnExpression(Column column, RowUpdater rowUpdater) {
            this.column = column;
            this.rowUpdater = rowUpdater;
        }

        @Override
        public ColumnType getType() {
            return column.getType().getColumnType();
        }

        @Override
        public boolean getBool(int row) {
            return rowUpdater.record.getBool(column.getName());
        }

        @Override
        public NullableBool getNullableBool(int row) {
            return rowUpdater.record.getNullableBool(column.getName());
        }

        @Override
        public byte getByte(int row) {
            return rowUpdater.record.getByte(column.getName());
        }

        @Override
        public double getDouble(int row) {
            return rowUpdater.record.getDouble(column.getName());
        }

        @Override
        public float getFloat(int row) {
            return rowUpdater.record.getFloat(column.getName());
        }

        @Override
        public int getInt(int row) {
            return rowUpdater.record.getInt(column.getName());
        }

        @Override
        public long getLong(int row) {
            switch (column.getType()) {
                case Date: {
                    return rowUpdater.record.getDate(column.getName()).getTime();
                }
                case DateTime: {
                    return rowUpdater.record.getDateTime(column.getName()).getTime();
                }
                default: {
                    return rowUpdater.record.getLong(column.getName());
                }
            }
        }

        @Override
        public short getShort(int row) {
            return rowUpdater.record.getShort(column.getName());
        }

        @Override
        public String getString(int row) {
            return rowUpdater.record.getString(column.getName());
        }
    }
}
