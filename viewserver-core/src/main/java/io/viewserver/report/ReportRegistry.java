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

package io.viewserver.report;

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.datasource.IDataLoader;
import io.viewserver.datasource.ILocalStorageDataAdapterFactory;
import io.viewserver.datasource.LocalKeyedTableUpdater;
import io.viewserver.operators.table.*;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.util.ViewServerException;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by paulg on 03/11/2014.
 */
public class ReportRegistry {
    public static final String TABLE_NAME = "report_registry";
    public static final String ID_COL = "id";
    public static final String NAME_COL = "name";
    public static final String DATASOURCE_COL = "dataSource";
    public static final String JSON_COL = "json";
    private static final Logger log = LoggerFactory.getLogger(ReportRegistry.class);
    private final IDataLoader dataLoader;

    private ICatalog systemCatalog;
    private IExecutionContext executionContext;
    private ListeningExecutorService dataLoaderExecutor;
    private KeyedTable reportTable;

    private KeyedTable defaultReportTable() {
        KeyedTable keyedTable = new KeyedTable(TABLE_NAME, executionContext, systemCatalog, getSchema(), new ChunkedColumnStorage(32), new TableKeyDefinition(ID_COL));
        keyedTable.initialise(50);
        return keyedTable;
    }

    private static Schema getSchema() {
        Schema schema = new Schema();
        schema.addColumn(ColumnHolderUtils.createColumnHolder(ID_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(JSON_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(DATASOURCE_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(NAME_COL, io.viewserver.schema.column.ColumnType.String));
        return schema;
    }

    private IJsonSerialiser serialiser;

    public ReportRegistry(ICatalog systemCatalog, IExecutionContext executionContext, IJsonSerialiser serialiser,
                          ILocalStorageDataAdapterFactory localStorageDataAdapterFactory, ListeningExecutorService dataLoaderExecutor){
        this.serialiser = serialiser;
        this.systemCatalog = systemCatalog;
        this.executionContext = executionContext;
        this.dataLoaderExecutor = dataLoaderExecutor;

        this.dataLoader = localStorageDataAdapterFactory.getAdapter(TABLE_NAME, "Reports", 100);
    }

    public void register(ReportDefinition report) {
        ITableRowUpdater rowUpdater = new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setString(ID_COL, report.getId());
                row.setString(NAME_COL, report.getName());
                row.setString(DATASOURCE_COL, report.getDataSource());
                row.setString(JSON_COL, serialiser.serialise(report));
            }
        };
        TableKey key = new TableKey(report.getId());
        reportTable.addRow(key, rowUpdater);
    }

    public ReportDefinition getReportById(String reportId) {
        int rowId = reportTable.getRow(new TableKey(reportId));
        if (rowId == -1) {
            throw new ViewServerException("No such report '" + reportId + "'");
        }
        ColumnHolder jsonColHolder = reportTable.getOutput().getSchema().getColumnHolder(JSON_COL);
        Object value = ColumnHolderUtils.getValue(jsonColHolder, rowId);
        String reportDefinitionJson = value.toString();
        return serialiser.deserialise(reportDefinitionJson, ReportDefinition.class);
    }

    public void loadReports() {
        LocalKeyedTableUpdater tableUpdater = new LocalKeyedTableUpdater(executionContext, systemCatalog);
        tableUpdater.setTableKeyDefinition(new TableKeyDefinition("id"));
        dataLoader.configure(tableUpdater,
                null, null, null, null, executionContext);
        dataLoader.createTable();
        dataLoader.load();
        reportTable = (KeyedTable) dataLoader.getTable();
        reportTable.setSystemOperator(true);
    }

    public void defaultRegistry() {
        this.reportTable =  defaultReportTable();
    }
}
