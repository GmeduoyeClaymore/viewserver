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

import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.CatalogHolder;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.Hasher;
import io.viewserver.core.IExecutionContext;
import io.viewserver.execution.ReportContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

/**
 * Created by nick on 31/03/2015.
 */
public class ReportContextRegistry extends KeyedTable implements ICatalog {
    public static final String OPERATOR_NAME = "reportContextRegistry";
    private static final Logger log = LoggerFactory.getLogger(ReportContextRegistry.class);
    private static final String COL_ID = "id";
    private static final String COL_REPORT_NAME = "reportName";
    private static final String COL_MULTI_CONTEXT_MODE = "multiContextMode";
    private static final String COL_REPORT_CONTEXT = "reportContext";
    private static final String COL_PATH = "path";
    private static final String COL_FIRST_RUN = "firstRunTime";
    private static final String COL_LAST_RUN = "lastRunTime";
    private final List<ReportContext> reportContexts = new ArrayList<>();
    private final ICatalog catalogHolder;

    public ReportContextRegistry(IExecutionContext executionContext, ICatalog catalog, ITableStorage storage) {
        super(OPERATOR_NAME, executionContext, catalog, getSchema(), storage, new TableKeyDefinition(COL_ID));
        initialise(256);

        setSystemOperator(true);

        catalogHolder = new CatalogHolder(this);
    }


    private static Schema getSchema() {
        Schema schema = new Schema();
        schema.addColumn(COL_ID, ColumnType.String);
        schema.addColumn(COL_REPORT_NAME, ColumnType.String);
        schema.addColumn(COL_MULTI_CONTEXT_MODE, ColumnType.String);
        schema.addColumn(COL_REPORT_CONTEXT, ColumnType.String);
        schema.addColumn(COL_PATH, ColumnType.String);
        schema.addColumn(COL_FIRST_RUN, ColumnType.Long);
        schema.addColumn(COL_LAST_RUN, ColumnType.Long);
        return schema;
    }

    public ICatalog getOrCreateCatalogForContext(ReportContext reportContext) {
        long now = new Date().getTime();

        String reportContextJson = reportContext.toString();
        String hash = Hasher.SHA1(reportContextJson);

        TableKey tableKey = new TableKey(hash);
        int rowId = getRow(tableKey);
        if (rowId != -1) {
            updateRow(rowId, row -> {
                row.setLong(COL_LAST_RUN, now);
            });
            return this.getChild(hash);
        } else {
            reportContexts.add(reportContext);

            Catalog reportCatalog = new Catalog(hash, this);

            addRow(tableKey, row -> {
                row.setString(COL_ID, hash);
                row.setString(COL_REPORT_NAME, reportContext.getReportName());
                row.setString(COL_MULTI_CONTEXT_MODE, reportContext.getMultiContextMode());
                row.setString(COL_REPORT_CONTEXT, reportContextJson);
                row.setString(COL_PATH, reportCatalog.getPath());
                row.setLong(COL_FIRST_RUN, now);
                row.setLong(COL_LAST_RUN, now);
            });

            return reportCatalog;
        }
    }

    public List<ReportContext> getAllContexts() {
        return Collections.unmodifiableList(reportContexts);
    }

    @Override
    public ICatalog getParent() {
        return catalogHolder.getParent();
    }

    @Override
    public int registerOperator(IOperator operator) {
        return catalogHolder.registerOperator(operator);
    }

    @Override
    public IOperator getOperator(String name) {
        return catalogHolder.getOperator(name);
    }

    @Override
    public IOperator getOperatorByPath(String name) {
        return catalogHolder.getOperatorByPath(name);
    }

    @Override
    public void unregisterOperator(IOperator operator) {
        catalogHolder.unregisterOperator(operator);
    }

    @Override
    public ICatalog createDescendant(String path) {
        return catalogHolder.createDescendant(path);
    }

    @Override
    public ICatalog getDescendant(String path) {
        return catalogHolder.getDescendant(path);
    }

    @Override
    public void addChild(ICatalog childCatalog) {
        catalogHolder.addChild(childCatalog);
    }

    @Override
    public void removeChild(ICatalog childCatalog) {
        catalogHolder.removeChild(childCatalog);
    }

    @Override
    public Observable<IOperator> waitForOperator(String name) {
        return catalogHolder.waitForOperator(name);
    }

    @Override
    public Observable<ICatalog> waitForChild(String name) {
        return catalogHolder.waitForChild(name);
    }

    @Override
    public Collection<IOperator> getAllOperators() {
        return catalogHolder.getAllOperators();
    }

    @Override
    public ICatalog getChild(String name) {
        return catalogHolder.getChild(name);
    }

    @Override
    public void doTearDown() {
        catalogHolder.tearDown();
        super.doTearDown();
    }
}
