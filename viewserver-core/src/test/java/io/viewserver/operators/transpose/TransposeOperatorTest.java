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

package io.viewserver.operators.transpose;

import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.calccol.ICalcColConfig;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 28/10/2014.
 */
public class TransposeOperatorTest {
    @Test
    public void canAddRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("bucket", ColumnType.Int);
        schema.addColumn("hitRate", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("bucket", 1);
                row.setInt("hitRate", 1);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("bucket", 2);
                row.setInt("hitRate", 2);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("bucket", 1);
                row.setInt("hitRate", 3);
            }
        });

        TransposeOperator transpose = new TransposeOperator("transpose", executionContext, catalog, new ChunkedColumnStorage(1024));
        transpose.configure(new ITransposeConfig() {
            @Override
            public List<String> getKeyColumns() {
                return Arrays.asList("market");
            }

            @Override
            public String getPivotColumn() {
                return "bucket";
            }

            @Override
            public Object[] getPivotValues() {
                return new Object[] { 1, 2};
            }
        }, new CommandResult());
        table.getOutput().plugIn(transpose.getInput());

        transpose.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();
    }

    @Test
    public void canRemoveRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("bucket", ColumnType.Int);
        schema.addColumn("hitRate", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("bucket", 1);
                row.setInt("hitRate", 1);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("bucket", 2);
                row.setInt("hitRate", 2);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("bucket", 1);
                row.setInt("hitRate", 3);
            }
        });

        TransposeOperator transpose = new TransposeOperator("transpose", executionContext, catalog, new ChunkedColumnStorage(1024));
        transpose.configure(new ITransposeConfig() {
            @Override
            public List<String> getKeyColumns() {
                return Arrays.asList("market");
            }

            @Override
            public String getPivotColumn() {
                return "bucket";
            }

            @Override
            public Object[] getPivotValues() {
                return new Object[] { 1, 2};
            }
        }, new CommandResult());
        table.getOutput().plugIn(transpose.getInput());

        transpose.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();

        table.removeRow(1);

        executionContext.commit();
    }

    @Test
    public void testCalcColumn() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("bucket", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("bucket", 1);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("bucket", 2);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("bucket", 1);
            }
        });

        CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        calc.configure(new ICalcColConfig() {
            @Override
            public List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("hitRate", "market / (bucket * 1.0f)"));
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }

            @Override
            public boolean isDataRefreshedOnColumnAdd() {
                return true;
            }
        }, new CommandResult());
        table.getOutput().plugIn(calc.getInput());

        TransposeOperator transpose = new TransposeOperator("transpose", executionContext, catalog, new ChunkedColumnStorage(1024));
        transpose.configure(new ITransposeConfig() {
            @Override
            public List<String> getKeyColumns() {
                return Arrays.asList("market");
            }

            @Override
            public String getPivotColumn() {
                return "bucket";
            }

            @Override
            public Object[] getPivotValues() {
                return new Object[] { 1, 2};
            }
        }, new CommandResult());
        calc.getOutput().plugIn(transpose.getInput());

        transpose.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();
    }
}
