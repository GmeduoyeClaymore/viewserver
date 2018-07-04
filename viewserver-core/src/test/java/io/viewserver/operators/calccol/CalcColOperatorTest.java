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

package io.viewserver.operators.calccol;

import io.viewserver.BenchmarkTestBase;
import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.TestReactor;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.IColumnInt;
import io.viewserver.schema.column.chunked.ChunkedColumnInt;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * Created by bemm on 15/10/2014.
 */
public class CalcColOperatorTest extends BenchmarkTestBase {
    @Test
    @Ignore
    public void benchmark() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                ExecutionContext executionContext = new ExecutionContext(1);
                executionContext.setReactor(new TestReactor());
                Catalog catalog = new Catalog(executionContext);

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Int);
                schema.addColumn("product", ColumnType.Int);

                Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 1500000; i++) {
                    ITableRowUpdater updater = new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setInt("product", random.nextInt(10000));
                        }
                    };
                    table.addRow(updater);
                }
                executionContext.commit();
                benchmarks.stopBenchmark("populate table");

                CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
                calc.configure(new ICalcColConfig() {
                    @Override
                    public java.util.List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                        return Arrays.asList(new CalcColOperator.CalculatedColumn("calc1", "market + product"),
                                new CalcColOperator.CalculatedColumn("calc2", "market - product"),
                                new CalcColOperator.CalculatedColumn("calc3", "market * product"));
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

//        calc.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

                benchmarks.startBenchmark("add calculated columns");
                executionContext.commit();
                benchmarks.stopBenchmark("add calculated columns");

                IColumnInt calc1 = (IColumnInt) calc.getOutput().getSchema().getColumnHolder("calc1");
                IColumnInt calc2 = (IColumnInt) calc.getOutput().getSchema().getColumnHolder("calc2");
                IColumnInt calc3 = (IColumnInt) calc.getOutput().getSchema().getColumnHolder("calc3");
                for (int pass = 1; pass < 4; pass++) {
                    benchmarks.startBenchmark("iterate calculated values (pass " + pass + ")");
                    for (int i = 0; i < 1500000; i++) {
                        calc1.getInt(i);
                        calc2.getInt(i);
                        calc3.getInt(i);
                    }
                    benchmarks.stopBenchmark("iterate calculated values (pass " + pass + ")");
                }
            }
        });
        Thread.sleep(5000);
    }

    @Test
    public void canAddRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(1500000);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });
        executionContext.commit();

        CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        calc.configure(new ICalcColConfig() {
            @Override
            public java.util.List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("calc1", "market + product"));
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

        calc.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 3);
                row.setInt("product", 4);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canUpdateRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(1500000);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });
        executionContext.commit();

        CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        calc.configure(new ICalcColConfig() {
            @Override
            public java.util.List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("calc1", "market + product"));
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

        calc.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 3);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canRemoveRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(1500000);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });
        executionContext.commit();

        CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        calc.configure(new ICalcColConfig() {
            @Override
            public java.util.List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("calc1", "market + product"));
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

        calc.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(0);

        executionContext.commit();
    }

    @Test
    public void canAddColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(1500000);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });
        executionContext.commit();

        CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        calc.configure(new ICalcColConfig() {
            @Override
            public java.util.List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("calc1", "market + product"));
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

        calc.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        ColumnHolder clientColumn = ColumnHolderUtils.createColumnHolder("client", ColumnType.Int);
        clientColumn.setColumn(new ChunkedColumnInt(clientColumn, table.getOutput().getCurrentChanges(), null, 8, 1024));
        schema.addColumn(clientColumn);

        executionContext.commit();
    }

    @Test
    public void canRemoveColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(1500000);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });
        executionContext.commit();

        CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        calc.configure(new ICalcColConfig() {
            @Override
            public java.util.List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("calc1", "market + product"));
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

        calc.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.addColumn("client", ColumnType.Int);

        executionContext.commit();

        schema.removeColumn(2);

        executionContext.commit();
    }

    @Test
    public void whenRemovingAColumnUsedByACalculationShouldRemoveCalculationColumn() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(1500000);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });
        executionContext.commit();

        CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        calc.configure(new ICalcColConfig() {
            @Override
            public java.util.List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("calc1", "market + product"));
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

        calc.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.removeColumn(1);

        executionContext.commit();

        Assert.assertNull(calc.getOutput().getSchema().getColumnHolder("calc1"));
    }

    @Test
    public void testRegexCalcCol() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market1", ColumnType.Int);
        schema.addColumn("product1", ColumnType.Int);
        schema.addColumn("market2", ColumnType.Int);
        schema.addColumn("product2", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(1500000);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market1", 1);
                row.setInt("product1", 2);
                row.setInt("market2", 3);
                row.setInt("product2", 4);
            }
        });

        CalcColOperator calc = new CalcColOperator("calc", executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        calc.configure(new ICalcColConfig() {
            @Override
            public List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("expr$1", "market$1 + product$1", "market([0-9]+)"));
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

        calc.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();

        calc.configure(new ICalcColConfig() {
            @Override
            public List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return Arrays.asList(new CalcColOperator.CalculatedColumn("market3", "market1 + market2"),
                        new CalcColOperator.CalculatedColumn("product3", "product1 + product2"));
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

        executionContext.commit();
    }
}
