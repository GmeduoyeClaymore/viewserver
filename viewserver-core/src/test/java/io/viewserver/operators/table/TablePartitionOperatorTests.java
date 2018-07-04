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

package io.viewserver.operators.table;

import io.viewserver.catalog.Catalog;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Test;

/**
 * Created by bemm on 12/02/2015.
 */
public class TablePartitionOperatorTests {
    @Test
    public void addRowsToSourceTableInPartition() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        Catalog catalog = new Catalog(executionContext);

        Table table = createTable(executionContext, catalog);

        TablePartitionOperator tablePartitionOperator = new TablePartitionOperator("partition1", executionContext, catalog, "market", 1);
        table.getOutput().plugIn(tablePartitionOperator.getInput());

        tablePartitionOperator.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        addRowToTable(table, 1, 3000);

        executionContext.commit();
    }

    @Test
    public void addRowsToSourceTableNotInPartition() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        Catalog catalog = new Catalog(executionContext);

        Table table = createTable(executionContext, catalog);

        TablePartitionOperator tablePartitionOperator = new TablePartitionOperator("partition1", executionContext, catalog, "market", 1);
        table.getOutput().plugIn(tablePartitionOperator.getInput());

        tablePartitionOperator.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        addRowToTable(table, 2, 3000);

        executionContext.commit();
    }

    @Test
    public void addRowsToPartition() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        Catalog catalog = new Catalog(executionContext);

        Table table = createTable(executionContext, catalog);

        table.getOutput().plugIn(new ChangeRecorder("source_rec", executionContext, catalog).getInput());

        TablePartitionOperator tablePartitionOperator = new TablePartitionOperator("partition1", executionContext, catalog, "market", 1);
        table.getOutput().plugIn(tablePartitionOperator.getInput());

        tablePartitionOperator.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        addRowToTable(tablePartitionOperator, null, 3000);

        executionContext.commit();
    }

    @Test
    public void updateRowInPartition() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        Catalog catalog = new Catalog(executionContext);

        Table table = createTable(executionContext, catalog);

        table.getOutput().plugIn(new ChangeRecorder("source_rec", executionContext, catalog).getInput());

        TablePartitionOperator tablePartitionOperator = new TablePartitionOperator("partition1", executionContext, catalog, "market", 1);
        table.getOutput().plugIn(tablePartitionOperator.getInput());

        tablePartitionOperator.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        tablePartitionOperator.updateRow(1, row -> row.setInt("notional", 3000));

        executionContext.commit();
    }

    @Test
    public void removeRowFromPartition() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        Catalog catalog = new Catalog(executionContext);

        Table table = createTable(executionContext, catalog);

        table.getOutput().plugIn(new ChangeRecorder("source_rec", executionContext, catalog).getInput());

        TablePartitionOperator tablePartitionOperator = new TablePartitionOperator("partition1", executionContext, catalog, "market", 1);
        table.getOutput().plugIn(tablePartitionOperator.getInput());

        tablePartitionOperator.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        tablePartitionOperator.removeRow(0);

        executionContext.commit();
    }

    @Test
    public void cannotSetPartitionValue() throws Exception {
        IExecutionContext executionContext = new ExecutionContext(1);
        Catalog catalog = new Catalog(executionContext);

        Table table = createTable(executionContext, catalog);

        TablePartitionOperator tablePartitionOperator = new TablePartitionOperator("partition1", executionContext, catalog, "market", 1);
        table.getOutput().plugIn(tablePartitionOperator.getInput());

        tablePartitionOperator.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        tablePartitionOperator.updateRow(0, row -> row.setInt("market", 2));

        executionContext.commit();
    }

    private Table createTable(IExecutionContext executionContext, Catalog catalog) {
        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        addRowToTable(table, 1, 1000);
        addRowToTable(table, 2, 1000);
        addRowToTable(table, 1, 2000);
        return table;
    }

    private void addRowToTable(ITable table, Integer market, int notional) {
        table.addRow(row -> {
            if (market != null) {
                row.setInt("market", market);
            }
            row.setInt("notional", notional);
        });
    }
}
