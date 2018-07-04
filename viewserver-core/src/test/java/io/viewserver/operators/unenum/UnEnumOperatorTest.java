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

package io.viewserver.operators.unenum;

import io.viewserver.BenchmarkTestBase;
import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.TestReactor;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.IColumnString;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;


public class UnEnumOperatorTest extends BenchmarkTestBase {
    @Test
    @Ignore
    public void benchmark() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.setReactor(new TestReactor());
                FunctionRegistry functionRegistry = new FunctionRegistry();
                Catalog catalog = new Catalog(executionContext);

                final DataSource dataSource = new DataSource();
                dataSource.setName("test");
                dataSource.setSchema(new SchemaConfig());
                dataSource.getSchema().getColumns().addAll(Arrays.asList(
                        new Column("market", ContentType.String),
                        new Column("product", ContentType.Int)
                ));
                dataSource.getDimensions().addAll(Arrays.asList(
                        new Dimension("market","market", Cardinality.Byte, dataSource.getSchema().getColumn("market").getType())
                ));

                DimensionMapper dimensionMapper = new DimensionMapper();
                Dimension marketDimension = dataSource.getDimension("market");
                dimensionMapper.registerDimension(dataSource.getName(), marketDimension.getName(), marketDimension.getContentType());

                for (int i = 0; i < 10; i++) {
                    dimensionMapper.mapString(dataSource.getName(), marketDimension.getName(), UUID.randomUUID().toString());
                }

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Byte);
                schema.addColumn("product", ColumnType.Int);

                Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
                table.initialise(1500000);

                benchmarks.startBenchmark("populate table");
                final Random random = new Random(new Date().getTime());
                for (int i = 0; i < 1500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setByte("market", (byte) random.nextInt(10));
                            row.setInt("product", random.nextInt(10000));
                        }
                    });
                }
                executionContext.commit();
                benchmarks.stopBenchmark("populate table");

                UnEnumOperator unenum = new UnEnumOperator("unenum", executionContext, catalog, dimensionMapper);
                unenum.configure(new IUnEnumConfig() {
                    @Override
                    public IDataSource getDataSource() {
                        return dataSource;
                    }

                    @Override
                    public List<String> getDimensions() {
                        return null;
                    }
                }, new CommandResult());
                table.getOutput().plugIn(unenum.getInput());

                benchmarks.startBenchmark("commit unenum");
                executionContext.commit();
                benchmarks.stopBenchmark("commit unenum");

                benchmarks.startBenchmark("enumerate rows");
                IColumnString marketColumn = (IColumnString) unenum.getOutput().getSchema().getColumnHolder("market");
                for (int i = 0; i < 1500000; i++) {
                    marketColumn.getString(i);
                }
                benchmarks.stopBenchmark("enumerate rows");
            }
        });
    }

    @Test
    public void canAddRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        FunctionRegistry functionRegistry = new FunctionRegistry();
        Catalog catalog = new Catalog(executionContext);

        final DataSource dataSource = new DataSource();
        dataSource.setSchema(new SchemaConfig());
        dataSource.setName("test");
        dataSource.getSchema().getColumns().addAll(Arrays.asList(
                new Column("market", ContentType.String),
                new Column("product", ContentType.Int)
        ));
        dataSource.getDimensions().addAll(Arrays.asList(
                new Dimension("market", "market",Cardinality.Byte, dataSource.getSchema().getColumn("market").getType())
        ));

        DimensionMapper dimensionMapper = new DimensionMapper();
        Dimension marketDimension = dataSource.getDimension("market");
        dimensionMapper.registerDimension(dataSource.getName(), marketDimension);

        for (int i = 0; i < 10; i++) {
            dimensionMapper.mapString(dataSource.getName(), marketDimension.getName(), "market" + i);
        }

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Byte);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(10);

        final Random random = new Random(new Date().getTime());
        for (int i = 0; i < 10; i++) {
            table.addRow(new ITableRowUpdater() {
                @Override
                public void setValues(ITableRow row) {
                    row.setByte("market", (byte)random.nextInt(10));
                    row.setInt("product", random.nextInt(10000));
                }
            });
        }

        table.getOutput().plugIn(new ChangeRecorder("table_rec", executionContext, catalog).getInput());

        UnEnumOperator unenum = new UnEnumOperator("unenum", executionContext, catalog, dimensionMapper);
        unenum.configure(new IUnEnumConfig() {
            @Override
            public IDataSource getDataSource() {
                return dataSource;
            }

            @Override
            public List<String> getDimensions() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(unenum.getInput());

        unenum.getOutput().plugIn(new ChangeRecorder("unenum_rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setByte("market", (byte) 4);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canUpdateRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        FunctionRegistry functionRegistry = new FunctionRegistry();
        Catalog catalog = new Catalog(executionContext);

        final DataSource dataSource = new DataSource();
        dataSource.setName("test");
        dataSource.setSchema(new SchemaConfig());
        dataSource.getSchema().getColumns().addAll(Arrays.asList(
                new Column("market", ContentType.String),
                new Column("product", ContentType.Int)
        ));
        dataSource.getDimensions().addAll(Arrays.asList(
                new Dimension("market", "market",Cardinality.Byte, dataSource.getSchema().getColumn("market").getType())
        ));

        DimensionMapper dimensionMapper = new DimensionMapper();
        Dimension marketDimension = dataSource.getDimension("market");
        dimensionMapper.registerDimension(dataSource.getName(), marketDimension);

        for (int i = 0; i < 10; i++) {
            dimensionMapper.mapString(dataSource.getName(), marketDimension.getName(), "market" + i);
        }

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Byte);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(10);

        final Random random = new Random(new Date().getTime());
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            table.addRow(new ITableRowUpdater() {
                @Override
                public void setValues(ITableRow row) {
                    row.setByte("market", (byte) finalI);
                    row.setInt("product", random.nextInt(10000));
                }
            });
        }

        table.getOutput().plugIn(new ChangeRecorder("table_rec", executionContext, catalog).getInput());

        UnEnumOperator unenum = new UnEnumOperator("unenum", executionContext, catalog, dimensionMapper);
        unenum.configure(new IUnEnumConfig() {
            @Override
            public IDataSource getDataSource() {
                return dataSource;
            }

            @Override
            public List<String> getDimensions() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(unenum.getInput());

        unenum.getOutput().plugIn(new ChangeRecorder("unenum_rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setByte("market", (byte)4);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canRemoveRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        FunctionRegistry functionRegistry = new FunctionRegistry();
        Catalog catalog = new Catalog(executionContext);

        final DataSource dataSource = new DataSource();
        dataSource.setName("test");
        dataSource.setSchema(new SchemaConfig());
        dataSource.getSchema().getColumns().addAll(Arrays.asList(
                new Column("market", ContentType.String),
                new Column("product", ContentType.Int)
        ));
        dataSource.getDimensions().addAll(Arrays.asList(
                new Dimension("market", Cardinality.Byte, dataSource.getSchema().getColumn("market").getType())
        ));

        DimensionMapper dimensionMapper = new DimensionMapper();
        Dimension marketDimension = dataSource.getDimension("market");
        dimensionMapper.registerDimension(dataSource.getName(), marketDimension);

        for (int i = 0; i < 10; i++) {
            dimensionMapper.mapString(dataSource.getName(), marketDimension.getName(), "market" + i);
        }

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Byte);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(10);

        final Random random = new Random(new Date().getTime());
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            table.addRow(new ITableRowUpdater() {
                @Override
                public void setValues(ITableRow row) {
                    row.setByte("market", (byte) finalI);
                    row.setInt("product", random.nextInt(10000));
                }
            });
        }

        table.getOutput().plugIn(new ChangeRecorder("table_rec", executionContext, catalog).getInput());

        UnEnumOperator unenum = new UnEnumOperator("unenum", executionContext, catalog, dimensionMapper);
        unenum.configure(new IUnEnumConfig() {
            @Override
            public IDataSource getDataSource() {
                return dataSource;
            }

            @Override
            public List<String> getDimensions() {
                return null;
            }
        }, new CommandResult());
        table.getOutput().plugIn(unenum.getInput());

        unenum.getOutput().plugIn(new ChangeRecorder("unenum_rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(3);

        executionContext.commit();
    }
}
