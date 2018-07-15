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

package io.viewserver.operators.sort;

import io.viewserver.BenchmarkTestBase;
import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.TestReactor;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnInt;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Created by bemm on 15/10/2014.
 */
public class SortOperatorTest extends BenchmarkTestBase {
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
                schema.addColumn("product", ColumnType.Int);

                ChunkedColumnStorage storage = new ChunkedColumnStorage(1024);
//                MemoryMappedColumnStorage storage = new MemoryMappedColumnStorage(Paths.get(System.getProperty("java.io.tmpdir"), "testtable"));
                Table table = new Table("table", executionContext, catalog, schema, storage);
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 2500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setInt("product", random.nextInt(10000));
                        }
                    });
                }
                executionContext.commit();
                benchmarks.stopBenchmark("populate table");

                SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
                sort.configure(new ISortConfig() {
                    @Override
                    public SortOperator.SortDescriptor getSortDescriptor() {
                        return new SortOperator.SortDescriptor("order", "product", false);
                    }

                    @Override
                    public int getStart() {
                        return 0;
                    }

                    @Override
                    public int getEnd() {
                        return 100;
                    }
                }, new CommandResult());
                table.getOutput().plugIn(sort.getInput());

//        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

//        Thread.sleep(5000);

                benchmarks.startBenchmark("sort snapshot");
                executionContext.commit();
                benchmarks.stopBenchmark("sort snapshot");

            }
        });
    }

    @Test
    @Ignore
    public void benchmarkStrings() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.setReactor(new TestReactor());
                Catalog catalog = new Catalog(executionContext);

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Int);
                schema.addColumn("product", ColumnType.String);

                Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 1500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setString("product", UUID.randomUUID().toString());
                        }
                    });
                }
                executionContext.commit();
                benchmarks.stopBenchmark("populate table");

                SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
                sort.configure(new ISortConfig() {
                    @Override
                    public SortOperator.SortDescriptor getSortDescriptor() {
                        return new SortOperator.SortDescriptor("order", "product", false);
                    }

                    @Override
                    public int getStart() {
                        return 0;
                    }

                    @Override
                    public int getEnd() {
                        return 100;
                    }
                }, new CommandResult());
                table.getOutput().plugIn(sort.getInput());

//        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

//        Thread.sleep(5000);

                benchmarks.startBenchmark("sort snapshot");
                executionContext.commit();
                benchmarks.stopBenchmark("sort snapshot");
            }
        });
    }

    @Test
    public void benchmarkBool() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.setReactor(new TestReactor());
                Catalog catalog = new Catalog(executionContext);

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Int);
                schema.addColumn("product", ColumnType.Bool);

                Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 1500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setBool("product", random.nextBoolean());
                        }
                    });
                }
                executionContext.commit();
                benchmarks.stopBenchmark("populate table");

                SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
                sort.configure(new ISortConfig() {
                    @Override
                    public SortOperator.SortDescriptor getSortDescriptor() {
                        return new SortOperator.SortDescriptor("order", "product", false);
                    }

                    @Override
                    public int getStart() {
                        return 0;
                    }

                    @Override
                    public int getEnd() {
                        return 100;
                    }
                }, new CommandResult());
                table.getOutput().plugIn(sort.getInput());

//        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

//        Thread.sleep(5000);

                benchmarks.startBenchmark("sort snapshot");
                executionContext.commit();
                benchmarks.stopBenchmark("sort snapshot");
            }
        });
    }

    @Test
    public void benchmarkDouble() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.setReactor(new TestReactor());
                Catalog catalog = new Catalog(executionContext);

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Int);
                schema.addColumn("product", ColumnType.Double);

                Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 1500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setDouble("product", random.nextDouble());
                        }
                    });
                }
                executionContext.commit();
                benchmarks.stopBenchmark("populate table");

                SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
                sort.configure(new ISortConfig() {
                    @Override
                    public SortOperator.SortDescriptor getSortDescriptor() {
                        return new SortOperator.SortDescriptor("order", "product", false);
                    }

                    @Override
                    public int getStart() {
                        return 0;
                    }

                    @Override
                    public int getEnd() {
                        return 100;
                    }
                }, new CommandResult());
                table.getOutput().plugIn(sort.getInput());

//        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

//        Thread.sleep(5000);

                benchmarks.startBenchmark("sort snapshot");
                executionContext.commit();
                benchmarks.stopBenchmark("sort snapshot");
            }
        });
    }

    @Test
    public void canSetRange() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(1500000);

        final Random random = new Random(new Date().getTime());
        for (int i = 0; i < 1500000; i++) {
            table.addRow(new ITableRowUpdater() {
                @Override
                public void setValues(ITableRow row) {
                    row.setInt("market", random.nextInt(10));
                    row.setInt("product", random.nextInt(10000));
                }
            });
        }
        executionContext.commit();

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        sort.setStart(100);
        sort.setEnd(200);
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();
    }

    @Test
    public void canAddRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 3);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canUpdateRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 3);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.updateRow(1, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 4);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canMixRowEvents() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        addProduct(table, 2);
        addProduct(table, 7);
        addProduct(table, 4);
        addProduct(table, 9);
        addProduct(table, 6);
        addProduct(table, 5);
        addProduct(table, 3);

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(2);
        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 1);
            }
        });
        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 10);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canRemoveRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 3);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(1);

        executionContext.commit();
    }

    @Test
    public void canMixRowEventsWithStrings() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("product", ColumnType.String);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        addProduct(table, "b");
        addProduct(table, "g");
        addProduct(table, "d");
        addProduct(table, "i");
        addProduct(table, "f");
        addProduct(table, "e");
        addProduct(table, "c");

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(2);
        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setString("product", "a");
            }
        });
        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setString("product", "j");
            }
        });

        executionContext.commit();
    }

    private void addProduct(Table table, final int product) {
        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", product);
            }
        });
    }

    private void addProduct(Table table, final String product) {
        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setString("product", product);
            }
        });
    }

    @Test
    public void canAddColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 3);
            }
        });

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        ColumnHolder clientColumn = ColumnHolderUtils.createColumnHolder("client", ColumnType.Int);
        clientColumn.setColumn(new ChunkedColumnInt(clientColumn, table.getOutput().getCurrentChanges(), null, 8, 1024));
        schema.addColumn(clientColumn);

        executionContext.commit();
    }

    @Test
    public void canRemoveColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 3);
            }
        });

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.removeColumn(0);

        executionContext.commit();
    }

    @Test
    public void whenRemovingSortedColumnShouldRemoveRankColumn() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 3);
            }
        });

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.removeColumn(1);

        executionContext.commit();

        Assert.assertNull(sort.getOutput().getSchema().getColumnHolder("order"));
    }

    @Test
    public void canHandleUpdatesWithUnchangedValues() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 3);
            }
        });

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        SortOperator sort = new SortOperator("sort", executionContext, catalog, new ChunkedColumnStorage(1024));
        sort.configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor("order", "product", false);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return 100;
            }
        }, new CommandResult());
        table.getOutput().plugIn(sort.getInput());

        sort.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.getOutput().handleUpdate(0);
        table.getOutput().handleUpdate(1);
        table.getOutput().getCurrentChanges().markColumnDirty(1);

        executionContext.commit();
    }
}
