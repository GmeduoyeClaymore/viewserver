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

package io.viewserver.operators.filter;

import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.ChangeRecorder;
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

import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * Created by nickc on 03/10/2014.
 */
public class FilterOperatorTest {
    @Test
    public void benchmark() throws Exception {
        for (int x = 0; x < 5; x++) {
            System.gc(); System.gc();

            ExecutionContext executionContext = new ExecutionContext();
            FunctionRegistry functionRegistry = new FunctionRegistry();
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

            FilterOperator filter = new FilterOperator("filter", executionContext, catalog, executionContext.getExpressionParser());
            filter.configure(new IFilterConfig() {
                @Override
                public FilterOperator.FilterMode getMode() {
                    return FilterOperator.FilterMode.Filter;
                }

                @Override
                public String getExpression() {
                    return "(market in [1,2,3]) && product == 1";
                }

                @Override
                public Map<String, String> getColumnAliases() {
                    return null;
                }
            }, new CommandResult());
            table.getOutput().plugIn(filter.getInput());

            filter.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

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
        FunctionRegistry functionRegistry = new FunctionRegistry();
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
                row.setInt("product", 1);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, executionContext.getExpressionParser());
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Filter;
            }

            @Override
            public String getExpression() {
                return "market == 1";
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(filter.getInput());

        filter.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

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
    public void canFilterOnNullableBool() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        FunctionRegistry functionRegistry = new FunctionRegistry();
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.NullableBool);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setNullableBool("market", NullableBool.True);
                row.setInt("product", 1);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, executionContext.getExpressionParser());
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Filter;
            }

            @Override
            public String getExpression() {
                return "market == false";
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(filter.getInput());

        filter.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

    }

    @Test
    public void canRemoveRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        FunctionRegistry functionRegistry = new FunctionRegistry();
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
                row.setInt("product", 1);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, executionContext.getExpressionParser());
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Filter;
            }

            @Override
            public String getExpression() {
                return "market == 1";
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(filter.getInput());

        filter.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(0);

        executionContext.commit();
    }

    @Test
    public void canAddColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        FunctionRegistry functionRegistry = new FunctionRegistry();
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
                row.setInt("product", 1);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, executionContext.getExpressionParser());
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Filter;
            }

            @Override
            public String getExpression() {
                return "market == 1";
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(filter.getInput());

        filter.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        ColumnHolder clientColumn = ColumnHolderUtils.createColumnHolder("client", ColumnType.Int);
        clientColumn.setColumn(new ChunkedColumnInt(clientColumn, table.getOutput().getCurrentChanges(), null, 8, 1024));
        schema.addColumn(clientColumn);

        executionContext.commit();
    }

    @Test
    public void canRemoveUnfilteredColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        FunctionRegistry functionRegistry = new FunctionRegistry();
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
                row.setInt("product", 1);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, executionContext.getExpressionParser());
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Filter;
            }

            @Override
            public String getExpression() {
                return "market == 1";
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(filter.getInput());

        filter.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.removeColumn(1);

        executionContext.commit();
    }

    @Test
    public void canRemoveFilteredColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        FunctionRegistry functionRegistry = new FunctionRegistry();
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
                row.setInt("product", 1);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, executionContext.getExpressionParser());
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Filter;
            }

            @Override
            public String getExpression() {
                return "market == 1";
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(filter.getInput());

        filter.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        schema.removeColumn(0);

        executionContext.commit();
    }

    @Test
    public void reusesRowNumbers() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        FunctionRegistry functionRegistry = new FunctionRegistry();
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
                row.setInt("product", 1);
            }
        });
        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("product", 2);
            }
        });
        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 3);
                row.setInt("product", 3);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, executionContext.getExpressionParser());
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Filter;
            }

            @Override
            public String getExpression() {
                return "market == 1";
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(filter.getInput());

        filter.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(0);

        executionContext.commit();

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 1);
            }
        });

        executionContext.commit();
    }
}
