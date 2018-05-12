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

package io.viewserver;

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
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.memorymapped.MemoryMappedColumnStorage;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by nick on 27/10/15.
 */
public class MemoryMappedColumnTest {
    @Test
    public void test() throws Throwable {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);
        String[] colNames = new String[50];
        for (int i = 0; i < 50; i++) {
            colNames[i] = String.format("column%d", i);
            schema.addColumn(colNames[i], ColumnType.Int);
        }

        MemoryMappedColumnStorage storage = new MemoryMappedColumnStorage(Paths.get(System.getProperty("java.io.tmpdir"), "testtable"));
//        ChunkedColumnStorage storage = new ChunkedColumnStorage(1024);
        Table table = new Table("table", executionContext, catalog, schema, storage);
        table.initialise(100000000);

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

        Random random = new Random(System.currentTimeMillis());
        ITableRowUpdater updater = new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", random.nextInt(10));
                row.setInt("product", random.nextInt(10000));
                for (int i = 0; i < 50; i++) {
                    row.setInt(colNames[i], random.nextInt(100));
                }
            }
        };
        for (int i = 0; i < 100000000; i++) {
            table.addRow(updater);
            if (i % 100000 == 0) {
                executionContext.commit();
                System.out.println(i);
            }
        }

        executionContext.commit();

        IOutput indexOutput = index.getOrCreateOutput(Constants.OUT, new QueryHolderConfig(getDimension("product"),false, 1));
        indexOutput.plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        new CountDownLatch(1).await();
    }

    private Dimension getDimension(String name) {
        return new Dimension(name, Cardinality.Int, ContentType.Int);
    }
}
