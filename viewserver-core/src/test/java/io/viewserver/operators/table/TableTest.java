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

import io.viewserver.BenchmarkTestBase;
import io.viewserver.Constants;
import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.datasource.Cardinality;
import io.viewserver.datasource.ContentType;
import io.viewserver.datasource.Dimension;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.index.IIndexConfig;
import io.viewserver.operators.index.IndexOperator;
import io.viewserver.operators.index.QueryHolderConfig;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.schema.column.disk.DiskColumnStorage;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Random;

/**
 * Created by bemm on 30/09/2014.
 */
public class TableTest extends BenchmarkTestBase {
    @Before
    public void setup() {
        System.setProperty("gnu.trove.no_entry.int", "MIN_VALUE");
    }

    @Test
    public void canAddRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        int row = table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 3);
            }
        });

        executionContext.commit();

        table.updateRow(row, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 4);
            }
        });

        executionContext.commit();

        table.removeRow(row);

        executionContext.commit();
    }

    @Test
    public void testDiskStorage() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                ExecutionContext executionContext = new ExecutionContext();
                Catalog catalog = new Catalog(executionContext);

                Schema schema = new Schema();
                schema.addColumn("market", ColumnType.Int);
                schema.addColumn("product", ColumnType.Int);
                schema.addColumn("notional", ColumnType.Double);
                for (int i = 0; i < 40; i++) {
                    schema.addColumn("extra" + i, ColumnType.Int);
                }
                schema.addColumn("rowId", ColumnType.Int);

                Path schemaDirectory = Files.createTempDirectory("table");
                schemaDirectory.toFile().deleteOnExit();
                System.out.println("Using schema directory: " + schemaDirectory);
                DiskColumnStorage storage = new DiskColumnStorage(schemaDirectory);
//                ChunkedColumnStorage storage = new ChunkedColumnStorage(1024);
                Table table = new Table("table", executionContext, catalog, schema, storage);
                table.initialise(8);

                IIndexConfig config = new IIndexConfig() {
                    @Override
                    public String getDataSourceName() {
                        return "dataSource";
                    }

                    @Override
                    public String[] getIndices() {
                        return new String[]{"market","product"};
                    }

                    @Override
                    public OutputConfig[] getOutputs() {
                        return null;
                    }
                };
                IndexOperator index = new IndexOperator("index", executionContext, catalog, config);
                table.getOutput().plugIn(index.getInput());

                Random random = new Random(new Date().getTime());
                benchmarks.startBenchmark("adding rows");
                for (int i = 0; i < 1500000; i++) {
                    table.addRow(new ITableRowUpdater() {
                        @Override
                        public void setValues(ITableRow row) {
                            row.setInt("market", random.nextInt(10));
                            row.setInt("product", random.nextInt(10000));
                            row.setDouble("notional", random.nextDouble());
                            for (int i = 0; i < 40; i++) {
                                row.setInt("extra" + i, random.nextInt(100));
                            }
                            row.setInt("rowId", row.getRowId());
                        }
                    });
                    if (i % 10000 == 0) {
                        executionContext.commit();
                        storage.unloadAllRows(schema);
                    }
                }
                benchmarks.stopBenchmark("adding rows");

//                table.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

                benchmarks.startBenchmark("commit 1");
                executionContext.commit();
                benchmarks.stopBenchmark("commit 1");

                System.gc(); System.gc();

                storage.unloadAllRows(schema);

                IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, new QueryHolderConfig(getDimension("product"),false,1));
                indexOutput.plugIn(new ChangeRecorder("index_rec", executionContext, catalog).getInput());

                benchmarks.startBenchmark("commit 2");
                executionContext.commit();
                benchmarks.stopBenchmark("commit 2");

//                Thread.sleep(5000);
            }
        }, 10);
    }

    private Dimension getDimension(String name) {
        return new Dimension(name, Cardinality.Int, ContentType.Int);
    }
}
