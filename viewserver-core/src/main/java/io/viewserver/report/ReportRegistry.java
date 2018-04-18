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
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.util.ViewServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReportRegistry  extends KeyedTable  {
    public static final String TABLE_NAME = "report_registry";
    public static final String ID_COL = "id";
    public static final String NAME_COL = "name";
    public static final String DATASOURCE_COL = "dataSource";
    public static final String JSON_COL = "json";
    private IJsonSerialiser serialiser = new JacksonSerialiser();

    private static final Logger log = LoggerFactory.getLogger(ReportRegistry.class);

    public ReportRegistry(ICatalog systemCatalog, IExecutionContext executionContext) {
        super(TABLE_NAME, executionContext, systemCatalog, getSchema(), new ChunkedColumnStorage(32), getTableKeyDefinitions());
        setSystemOperator(true);
        setAllowDataReset(true);
        initialise(8);
    }

    private static Schema getSchema() {
        Schema schema = new Schema();
        schema.addColumn(ColumnHolderUtils.createColumnHolder(ID_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(JSON_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(DATASOURCE_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(NAME_COL, io.viewserver.schema.column.ColumnType.String));
        return schema;
    }

    protected static TableKeyDefinition getTableKeyDefinitions() {
        return new TableKeyDefinition(ID_COL);
    }


    public void register(ReportDefinition report) {
        log.info("Report {} registered",report);
        ITableRowUpdater rowUpdater = row -> {
            row.setString(ID_COL, report.getId());
            row.setString(NAME_COL, report.getName());
            row.setString(DATASOURCE_COL, report.getDataSource());
            row.setString(JSON_COL, serialiser.serialise(report));
        };
        TableKey key = new TableKey(report.getId());
        this.addRow(key, rowUpdater);
    }

    public ReportDefinition getReportById(String reportId) {
        int rowId = this.getRow(new TableKey(reportId));
        if (rowId == -1) {
            throw new ViewServerException("No such report '" + reportId + "'");
        }
        ColumnHolder jsonColHolder = this.getOutput().getSchema().getColumnHolder(JSON_COL);
        Object value = ColumnHolderUtils.getValue(jsonColHolder, rowId);
        String reportDefinitionJson = value.toString();
        return serialiser.deserialise(reportDefinitionJson, ReportDefinition.class);
    }
}
