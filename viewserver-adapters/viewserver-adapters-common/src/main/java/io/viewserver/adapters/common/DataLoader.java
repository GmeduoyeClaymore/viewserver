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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.IPool;
import io.viewserver.core.Pool;
import io.viewserver.core.PooledItem;
import io.viewserver.datasource.*;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class DataLoader implements IDataLoader {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private ITableUpdater tableUpdater;
    private IExecutionContext executionContext;
    protected DimensionMapper dimensionMapper;
    protected DataSource dataSource;
    private RowUpdater rowUpdater;
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
    private TableKeyDefinition tableKeyDefinition;

    public DataLoader(String name, IDataAdapter dataAdapter, IDataQueryProvider dataQueryProvider) {
        this.name = name;
        this.dataAdapter = dataAdapter;
        this.dataQueryProvider = dataQueryProvider;
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
    public void configure(ITableUpdater tableUpdater, DimensionMapper dimensionMapper, DataSource dataSource, FunctionRegistry functionRegistry, IExpressionParser expressionParser, IExecutionContext executionContext) {
        this.dimensionMapper = dimensionMapper;
        this.dataSource = dataSource;
        this.tableUpdater = tableUpdater;
        this.rowUpdater = new RowUpdater(dimensionMapper, functionRegistry, expressionParser);
        this.rowUpdater.setDataSource(dataSource);
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
            rowUpdater.setTable(table);

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
}

