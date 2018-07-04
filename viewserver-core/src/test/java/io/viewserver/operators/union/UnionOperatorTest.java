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

package io.viewserver.operators.union;

import com.google.common.util.concurrent.ListenableFuture;
import cucumber.api.java.ca.I;
import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.messages.IMessage;
import io.viewserver.network.*;
import io.viewserver.network.netty.NettyNetworkAdapter;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.IInput;
import io.viewserver.operators.TestReactor;
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.operators.filter.IFilterConfig;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.reactor.EventLoopReactor;
import io.viewserver.reactor.INetworkMessageListener;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.MultiThreadedEventLoopReactor;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by bemm on 06/10/2014.
 */
public class UnionOperatorTest {
    @Test
    public void benchmark() throws Exception {

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
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });

        Schema schema2 = new Schema();
        schema2.addColumn("product", ColumnType.Int);
        schema2.addColumn("market", ColumnType.Int);

        Table table2 = new Table("table2", executionContext, catalog, schema2, new ChunkedColumnStorage(1024));
        table2.initialise(8);

        table2.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 1);
                row.setInt("market", 2);
            }
        });

        UnionOperator union = new UnionOperator("union", executionContext, catalog, new ChunkedColumnStorage(1024));
        IInput in1 = union.getOrCreateInput("in1", 1);
        table.getOutput().plugIn(in1);
        IInput in2 = union.getOrCreateInput("in2", 2);
        table2.getOutput().plugIn(in2);

        union.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

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
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });

        Schema schema2 = new Schema();
        schema2.addColumn("product", ColumnType.Int);
        schema2.addColumn("market", ColumnType.Int);

        Table table2 = new Table("table2", executionContext, catalog, schema2, new ChunkedColumnStorage(1024));
        table2.initialise(8);

        table2.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 1);
                row.setInt("market", 2);
            }
        });

        UnionOperator union = new UnionOperator("union", executionContext, catalog, new ChunkedColumnStorage(1024));
        IInput in1 = union.getOrCreateInput("in1", 1);
        table.getOutput().plugIn(in1);
        IInput in2 = union.getOrCreateInput("in2", 2);
        table2.getOutput().plugIn(in2);

        union.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 3);
            }
        });

        table2.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 3);
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
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
            }
        });

        Schema schema2 = new Schema();
        schema2.addColumn("product", ColumnType.Int);
        schema2.addColumn("market", ColumnType.Int);

        Table table2 = new Table("table2", executionContext, catalog, schema2, new ChunkedColumnStorage(1024));
        table2.initialise(8);

        table2.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 1);
                row.setInt("market", 2);
            }
        });

        UnionOperator union = new UnionOperator("union", executionContext, catalog, new ChunkedColumnStorage(1024));
        IInput in1 = union.getOrCreateInput("in1", 1);
        table.getOutput().plugIn(in1);
        IInput in2 = union.getOrCreateInput("in2", 2);
        table2.getOutput().plugIn(in2);

        union.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(0);

        table2.removeRow(0);

        executionContext.commit();
    }

    @Test
    public void canAddAndRemoveProducers() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
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
                row.setInt("product", 1);
            }
        });

        Schema schema2 = new Schema();
        schema2.addColumn("product", ColumnType.Int);
        schema2.addColumn("market", ColumnType.Int);

        Table table2 = new Table("table2", executionContext, catalog, schema2, new ChunkedColumnStorage(1024));
        table2.initialise(8);

        table2.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 2);
                row.setInt("market", 2);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, null);
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Transparent;
            }

            @Override
            public String getExpression() {
                return null;
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table2.getOutput().plugIn(filter.getInput());

        UnionOperator union = new UnionOperator("union", executionContext, catalog, new ChunkedColumnStorage(1024));
        IInput in1 = union.getOrCreateInput("in1", 1);
        table.getOutput().plugIn(in1);

        union.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        IInput in2 = union.getOrCreateInput("in2", 2);
        filter.getOutput().plugIn(in2);

        executionContext.commit();

        filter.getOutput().unplug(in2);

        executionContext.commit();

        filter.getOutput().plugIn(in2);

        executionContext.commit();

        filter.resetSchema();
        executionContext.commit();
    }

    @Test
    @Ignore
    public void tearDownBehaviour() throws Throwable {
        ExecutionContext executionContext = new ExecutionContext(1);

        Catalog catalog = new Catalog(executionContext);

        IReactor reactor = new EventLoopReactor("reactor", new Network(null, executionContext, catalog, new INetworkAdapter() {
            @Override
            public void registerListener(INetworkMessageListener listener) {

            }

            @Override
            public void start() {

            }

            @Override
            public void setReactor(IReactor reactor) {

            }

            @Override
            public void listen(IEndpoint endpoint) {

            }

            @Override
            public ListenableFuture<IChannel> connect(IEndpoint endpoint) {
                return null;
            }

            @Override
            public String getCatalogNameForChannel(IChannel channel) {
                return null;
            }

            @Override
            public void reset() {

            }

            @Override
            public IMessageManager createMessageManager(IChannel channel) {
                return null;
            }

            @Override
            public void setNetworkMessageWheel(INetworkMessageWheel networkMessageWheel) {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public INetworkMessageWheel getNetworkMessageWheel() {
                return new INetworkMessageWheel() {
                    @Override
                    public void startRotating() {

                    }

                    @Override
                    public void stopRotating() {

                    }

                    @Override
                    public void registerNetworkMessageListener(INetworkMessageListener listener) {

                    }

                    @Override
                    public void pushToWheel(IChannel channel, byte[] bytes, int offset, int length) {

                    }

                    @Override
                    public void pushToWheel(IChannel channel, InputStream stream) {

                    }

                    @Override
                    public byte[] encode(IMessage message) {
                        return new byte[0];
                    }
                };
            }
        }));
        executionContext.setReactor(reactor);

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

        Schema schema2 = new Schema();
        schema2.addColumn("product", ColumnType.Int);
        schema2.addColumn("market", ColumnType.Int);

        Table table2 = new Table("table2", executionContext, catalog, schema2, new ChunkedColumnStorage(1024));
        table2.initialise(8);

        table2.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("product", 2);
                row.setInt("market", 2);
            }
        });

        FilterOperator filter = new FilterOperator("filter", executionContext, catalog, null);
        filter.configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return FilterOperator.FilterMode.Transparent;
            }

            @Override
            public String getExpression() {
                return null;
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
        table2.getOutput().plugIn(filter.getInput());

        UnionOperator union = new UnionOperator("union", executionContext, catalog, new ChunkedColumnStorage(1024));
        IInput in1 = union.getOrCreateInput("in1", 1);
        table.getOutput().plugIn(in1);

        union.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        IInput in2 = union.getOrCreateInput("in2", 2);
        filter.getOutput().plugIn(in2);

        executionContext.commit();

        filter.tearDown();

        executionContext.commit();

        table.tearDown();

        executionContext.commit();
    }

    @Test
    public void throwsWhenAdditionalSchemaMissingColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext(1);
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
                row.setInt("product", 2);
            }
        });

        Schema schema2 = new Schema();
        schema2.addColumn("market", ColumnType.Int);

        Table table2 = new Table("table2", executionContext, catalog, schema2, new ChunkedColumnStorage(1024));
        table2.initialise(8);

        table2.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
            }
        });

        UnionOperator union = new UnionOperator("union", executionContext, catalog, new ChunkedColumnStorage(1024));
        IInput in1 = union.getOrCreateInput("in1", 1);
        table.getOutput().plugIn(in1);
        IInput in2 = union.getOrCreateInput("in2", 2);
        table2.getOutput().plugIn(in2);

        union.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();
    }
}
