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

import io.viewserver.BenchmarkTestBase;
import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.TestReactor;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnInt;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by bemm on 02/10/2014.
 */
public class GroupByOperatorTest extends BenchmarkTestBase {
    @Test
    public void benchmark() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                IExecutionContext executionContext = new ExecutionContext();
                executionContext.setReactor(new TestReactor());
                Catalog catalog = new Catalog(executionContext);

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Int);
                schema.addColumn("notional", ColumnType.Int);

                Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 1500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setInt("notional", random.nextInt(1000));
                        }
                    });
                }
                benchmarks.stopBenchmark("populate table");

                GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
                group.configure(new IGroupByConfig() {
                    @Override
                    public List<String> getGroupBy() {
                        return Arrays.asList("market");
                    }

                    @Override
                    public List<Summary> getSummaries() {
                        return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
                    }

                    @Override
                    public String getCountColumnName() {
                        return null;
                    }

                    @Override
                    public List<String> getSubtotals() {
                        return null;
                    }

                }, new CommandResult());
                table.getOutput().plugIn(group.getInput());

                group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

                benchmarks.startBenchmark("commit added rows");
                executionContext.commit();
                benchmarks.stopBenchmark("commit added rows");
            }
        });
    }

    @Test
    public void benchmarkMinSummary() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                IExecutionContext executionContext = new ExecutionContext();
                executionContext.setReactor(new TestReactor());
                Catalog catalog = new Catalog(executionContext);

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Int);
                schema.addColumn("notional", ColumnType.Int);

                Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 1500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setInt("notional", random.nextInt());
                        }
                    });
                }
                benchmarks.stopBenchmark("populate table");

                GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
                group.configure(new IGroupByConfig() {
                    @Override
                    public List<String> getGroupBy() {
                        return Arrays.asList("market");
                    }

                    @Override
                    public List<Summary> getSummaries() {
                        return Arrays.asList(new Summary("notionalsum", "min", "notional"));
                    }

                    @Override
                    public String getCountColumnName() {
                        return null;
                    }

                    @Override
                    public List<String> getSubtotals() {
                        return null;
                    }

                }, new CommandResult());
                table.getOutput().plugIn(group.getInput());

                group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

                benchmarks.startBenchmark("commit added rows");
                executionContext.commit();
                benchmarks.stopBenchmark("commit added rows");
            }
        });
    }

    @Test
    public void benchmarkDoubleGroupBy() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                IExecutionContext executionContext = new ExecutionContext();
                executionContext.setReactor(new TestReactor());
                Catalog catalog = new Catalog(executionContext);

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Int);
                schema.addColumn("day", ColumnType.Int);
                schema.addColumn("notional", ColumnType.Int);

                Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 1500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setInt("day", random.nextInt(80));
                            row.setInt("notional", random.nextInt(1000));
                        }
                    });
                }
                benchmarks.stopBenchmark("populate table");

                GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
                group.configure(new IGroupByConfig() {
                    @Override
                    public List<String> getGroupBy() {
                        return Arrays.asList("market", "day");
                    }

                    @Override
                    public List<Summary> getSummaries() {
                        return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
                    }

                    @Override
                    public String getCountColumnName() {
                        return null;
                    }

                    @Override
                    public List<String> getSubtotals() {
                        return null;
                    }

                }, new CommandResult());
                table.getOutput().plugIn(group.getInput());

                group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

                benchmarks.startBenchmark("commit added rows");
                executionContext.commit();
                benchmarks.stopBenchmark("commit added rows");
            }
        });
    }

    @Test
    public void canAddRows() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("notional", 300);
            }
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 200);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canUpdateRows() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 200);
            }
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("notional", 300);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canRemoveRows() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 200);
            }
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(1);

        executionContext.commit();
    }

    @Test
    public void removingLastRowInGroupRemovesGroup() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("notional", 200);
            }
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(1);

        executionContext.commit();
    }

    @Test
    public void canAddColumns() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 200);
            }
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        ColumnHolder clientColumn = ColumnHolderUtils.createColumnHolder("client", ColumnType.Int);
        clientColumn.setColumn(new ChunkedColumnInt(clientColumn, table.getOutput().getCurrentChanges(), null, 8, 1024));
        schema.addColumn(clientColumn);

        executionContext.commit();
    }

    @Test
    public void canHaveRegexSummaries() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional1", ColumnType.Int);
        schema.addColumn("notional2", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional1", 100);
                row.setInt("notional2", 200);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional1", 1000);
                row.setInt("notional2", 2000);
            }
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(
                        new Summary("notionalsum$1", "sum", "notional([0-9]+)", true)
                );
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();
    }

    @Test
    public void canRemoveColumns() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);
        schema.addColumn("client", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 200);
            }
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.removeColumn(2);

        executionContext.commit();
    }

    @Test
    public void testMinSummary() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow((row) -> {
            row.setInt("market", 1);
            row.setInt("notional", 5);
        });
        table.addRow((row) -> {
            row.setInt("market", 1);
            row.setInt("notional", 4);
        });
        table.addRow((row) -> {
            row.setInt("market", 1);
            row.setInt("notional", 6);
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(new Summary("min", "min", "notional", new Object[] { 3 }));
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.addRow((row) -> {
            row.setInt("market", 1);
            row.setInt("notional", 2);
        });
        table.addRow((row) -> {
            row.setInt("market", 1);
            row.setInt("notional", 1);
        });
        table.addRow((row) -> {
            row.setInt("market", 1);
            row.setInt("notional", 3);
        });

        executionContext.commit();

        table.removeRow(3);
        table.removeRow(4);
        table.removeRow(5);

        executionContext.commit();
    }

    @Test
    public void resetsCorrectly() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("notional", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("notional", 300);
            }
        });

        GroupByOperator group = new GroupByOperator("group", executionContext, catalog, new SummaryRegistry(), new ChunkedColumnStorage(1024));
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return Arrays.asList("market");
            }

            @Override
            public List<Summary> getSummaries() {
                return Arrays.asList(new Summary("notionalsum", "sum", "notional"));
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
        table.getOutput().plugIn(group.getInput());

        group.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        group.resetData();

        executionContext.commit();
    }
}
