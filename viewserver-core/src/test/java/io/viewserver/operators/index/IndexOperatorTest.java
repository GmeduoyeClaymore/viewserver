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

package io.viewserver.operators.index;

import io.viewserver.Constants;
import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.IOutput;
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

/**
 * Created by nickc on 02/10/2014.
 */
public class IndexOperatorTest {
    @Ignore
    public void benchmark() throws Exception {
        for (int x = 0; x < 5; x++) {
            System.gc();
            System.gc();

            ExecutionContext executionContext = new ExecutionContext();

            Catalog catalog = new Catalog(executionContext);

            Schema schema = new Schema();
            schema.addColumn("market", ColumnType.Int);
            schema.addColumn("product", ColumnType.Int);

            Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
            table.initialise(1500000);

            long start = System.nanoTime();
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
            System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms to populate table");

            IndexOperator index = new IndexOperator("index", executionContext, catalog);
            index.configure(new IIndexConfig() {
                @Override
                public String[] getIndices() {
                    return new String[] { "market", "product" };
                }

                @Override
                public Output[] getOutputs() {
                    return null;
                }
            }, new CommandResult());

            table.getOutput().plugIn(index.getInput());

            start = System.nanoTime();
            executionContext.commit();
            System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms to index table");

            start = System.nanoTime();
            IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, new IndexOperator.QueryHolder("market", 1, 2, 3),
                    new IndexOperator.QueryHolder("product", 1));
            System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms to query snapshot");
            indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

            start = System.nanoTime();
            executionContext.commit();
            System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms to commit query");
            for (int i = 0; i < 100000; i++) {
                table.addRow(new ITableRowUpdater() {
                    @Override
                    public void setValues(ITableRow row) {
                        row.setInt("market", random.nextInt(10));
                        row.setInt("product", random.nextInt(10000));
                    }
                });
            }

            start = System.nanoTime();
            executionContext.commit();
            System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms to commit added rows");
        }
    }

    @Test
    public void canUpdateRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);
        executionContext.setReactor(new TestReactor());

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        IndexOperator index = new IndexOperator("index", executionContext, catalog);
        index.configure(new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return new String[] { "market" };
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        }, new CommandResult());

        table.getOutput().plugIn(index.getInput());

        executionContext.commit();

        IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, new IndexOperator.QueryHolder("market", 1));
        indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 2);
            }
        });

        executionContext.commit();

        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canRemoveRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);
        executionContext.setReactor(new TestReactor());

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        IndexOperator index = new IndexOperator("index", executionContext, catalog);
        index.configure(new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return new String[] { "market" };
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        }, new CommandResult());

        table.getOutput().plugIn(index.getInput());

        executionContext.commit();

        IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, new IndexOperator.QueryHolder("market", 1));
        indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(0);

        executionContext.commit();
    }

    @Test
    public void canExcludeValues() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);
        executionContext.setReactor(new TestReactor());

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

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
                row.setInt("market", 2);
                row.setInt("product", 1);
            }
        });

        IndexOperator index = new IndexOperator("index", executionContext, catalog);
        index.configure(new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return new String[] { "market" };
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        }, new CommandResult());

        table.getOutput().plugIn(index.getInput());

        executionContext.commit();

        IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, IndexOperator.QueryHolder.exclude("market", 1));
        indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();
    }
    @Test
    public void canIncludeNullableBoolValues() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);
        executionContext.setReactor(new TestReactor());

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Bool);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setBool("product", false);
            }
        });
        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setBool("product",true);
            }
        });

        IndexOperator index = new IndexOperator("index", executionContext, catalog);
        index.configure(new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return new String[] { "product" };
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        }, new CommandResult());

        table.getOutput().plugIn(index.getInput());

        executionContext.commit();

        IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, IndexOperator.QueryHolder.include("product", NullableBool.True.getNumericValue()));
        indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();
    }

    @Test
    public void canAddColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);
        executionContext.setReactor(new TestReactor());

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        IndexOperator index = new IndexOperator("index", executionContext, catalog);
        index.configure(new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return new String[] { "market" };
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        }, new CommandResult());

        table.getOutput().plugIn(index.getInput());

        executionContext.commit();

        IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, new IndexOperator.QueryHolder("market", 1));
        indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        ColumnHolder clientColumn = ColumnHolderUtils.createColumnHolder("client", ColumnType.Int);
        clientColumn.setColumn(new ChunkedColumnInt(clientColumn, table.getOutput().getCurrentChanges(), null, 8, 1024));
        schema.addColumn(clientColumn);

        executionContext.commit();
    }

    @Test
    public void canRemoveColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);
        executionContext.setReactor(new TestReactor());

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        IndexOperator index = new IndexOperator("index", executionContext, catalog);
        index.configure(new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return new String[] { "market" };
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        }, new CommandResult());

        table.getOutput().plugIn(index.getInput());

        executionContext.commit();

        IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, new IndexOperator.QueryHolder("market", 1));
        indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.removeColumn(1);

        executionContext.commit();
    }

    @Ignore
    public void whenIndexedColumnIsRemovedShouldTearDownOutputsQueryingThatColumn() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);
        executionContext.setReactor(new TestReactor());

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        IndexOperator index = new IndexOperator("index", executionContext, catalog);
        index.configure(new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return new String[] { "market" };
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        }, new CommandResult());

        table.getOutput().plugIn(index.getInput());

        executionContext.commit();

        IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, new IndexOperator.QueryHolder("market", 1));
        indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.removeColumn(0);

        executionContext.commit();

        Assert.assertNull(catalog.getOperator("rec"));
    }
}
