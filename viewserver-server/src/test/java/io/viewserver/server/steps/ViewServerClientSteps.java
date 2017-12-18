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

package io.viewserver.server.steps;

import io.viewserver.client.ClientSubscription;
import io.viewserver.client.RowEvent;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ColumnType;
import io.viewserver.execution.ReportContext;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.ITableEditCommand;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.messages.config.IRollingTableConfig;
import io.viewserver.operators.table.ProtoRollingTableConfig;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.util.MapReader;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.formatter.model.DataTableRow;
import org.junit.Assert;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by nick on 10/02/2015.
 */
public class ViewServerClientSteps {
    private ViewServerClientContext clientContext;
    private InProcessViewServerContext serverContext;

    public ViewServerClientSteps(ViewServerClientContext clientContext,
                                 InProcessViewServerContext serverContext) {
        this.clientContext = clientContext;
        this.serverContext = serverContext;
    }

    @After
    public void afterScenario() {
        if (clientContext.client != null) {
            try {
                clientContext.client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientContext.client = null;
        }
    }

    @And("^a client connected to \"(.*)\"$")
    public void a_connected_client(String url) throws Throwable {
        // TODO: add url parsing
        clientContext.client = new TestViewServerClient("client", url);
        clientContext.client.authenticate("open", "cucumber").get();
    }

    @And("^sleep for (\\d+) millis$")
    public void sleep_for_millis(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Given("^report parameters$")
    public void report_parameters(DataTable parameters) throws Throwable {
        clientContext.reportContext.getParameterValues().clear();
        setValuesFromDataTable(parameters, (name, valuesList) -> clientContext.reportContext.getParameterValues().put(name, valuesList));
    }

    @And("^dimension filters$")
    public void dimension_filters(DataTable filters) throws Throwable {
        clientContext.reportContext.getDimensionValues().clear();
        setValuesFromDataTable(filters, (name, valuesList) ->
                clientContext.reportContext.getDimensionValues().add(new ReportContext.DimensionValue(name, valuesList)));
    }

    private void setValuesFromDataTable(DataTable parameters, IConsumer<String, ValueLists.IValueList> consumer) {
        List<DataTableRow> rows = parameters.getGherkinRows();
        for (int i = 1; i < rows.size(); i++) {
            List<String> cells = rows.get(i).getCells();
            String name = cells.get(0);
            String type = cells.get(1);
            String[] values = cells.get(2).split(",");
            Object[] typedValues = new Object[values.length];
            for (int j = 0; j < values.length; j++) {
                switch (type) {
                    case "Integer": {
                        typedValues[j] = Integer.parseInt(values[j]);
                        break;
                    }
                    case "Double": {
                        typedValues[j] = Double.parseDouble(values[j]);
                        break;
                    }
                    case "String": {
                        typedValues[j] = values[j];
                        break;
                    }
                }
            }
            consumer.consume(name, ValueLists.valueListOf(typedValues));
        }
    }

    @When("^I subscribe to report \"([^\"]*)\"$")
    public void I_subscribe_to_report(String reportId) throws Throwable {
        assertClientConnected();
        clientContext.reportContext.setReportName(reportId);

        CountDownLatch subscribeLatch = new CountDownLatch(1);
        trySubscribe(subscribeLatch);
        subscribeLatch.await(20, TimeUnit.SECONDS);

        Assert.assertNotNull(clientContext.getSubscription("report"));
    }

    private void trySubscribe(CountDownLatch subscribeLatch){
        ListenableFuture<ClientSubscription> subFuture = clientContext.client.subscribeToReport(
                clientContext.reportContext,
                clientContext.options,
                new TestSubscriptionEventHandler());

        Futures.addCallback(subFuture, new FutureCallback<ClientSubscription>() {
            @Override
            public void onSuccess(ClientSubscription clientSubscription) {
                clientContext.addSubscription("report", clientSubscription);
                subscribeLatch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                    trySubscribe(subscribeLatch);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @And("^paging from (\\d+) to (\\d+) by \"([^\"]*)\" (ascending|descending)$")
    public void paging_from_to_by(int offset, int limit, String sortColumn, String direction) throws Throwable {
        clientContext.options.setOffset(offset);
        clientContext.options.setLimit(limit);
        clientContext.options.getColumnsToSort().clear();
        clientContext.options.addSortColumn(sortColumn, "descending".equals(direction));
    }

    @Then("^the following data is received$")
    public void the_following_data_is_received(DataTable expectedData) throws Throwable {
        assertClientConnected();
        List<Map<String, Object>> snapshot = new ArrayList<>();

        ClientSubscription subscription = clientContext.getSubscription("report");
        snapshot = subscription.getSnapshot().get();

        snapshot.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((Integer) o1.get("rank")) > ((Integer) o2.get("rank")) ? 1 : -1;
            }
        });

        diff(expectedData, snapshot);
    }

    @And("^the following records in table \"([^\"]*)\"$")
    public void the_following_records_in_table(String tableName, List<Map<String, String>> records) throws Throwable {
        assertClientConnected();
        List<Column> columns = serverContext.dataSource.getSchema().getColumns();
        Iterable<RowEvent> rowEvents = Iterables.transform(records, record -> {
            HashMap<Integer, Object> values = new HashMap<>();
            for (Map.Entry<String, String> value : record.entrySet()) {
                int columnId = -1;
                for (int i = 0; i < columns.size(); i++) {
                    if (columns.get(i).getName().equals(value.getKey())) {
                        columnId = i;
                        break;
                    }
                }
                if (columnId != -1) {
                    switch (columns.get(columnId).getType()) {
                        case Bool: {
                            values.put(columnId, Boolean.parseBoolean(value.getValue()));
                            break;
                        }
                        case Byte: {
                            values.put(columnId, Byte.parseByte(value.getValue()));
                            break;
                        }
                        case Short: {
                            values.put(columnId, Short.parseShort(value.getValue()));
                            break;
                        }
                        case Int: {
                            values.put(columnId, Integer.parseInt(value.getValue()));
                            break;
                        }
                        case Long: {
                            values.put(columnId, Long.parseLong(value.getValue()));
                            break;
                        }
                        case Float: {
                            values.put(columnId, Float.parseFloat(value.getValue()));
                            break;
                        }
                        case Double: {
                            values.put(columnId, Double.parseDouble(value.getValue()));
                            break;
                        }
                        case String: {
                            values.put(columnId, value.getValue());
                            break;
                        }
                    }
                }
            }

            return RowEvent.addRow(values);
        });
        List<io.viewserver.client.Column> tableColumns = new ArrayList<>();
        Map<String, String> record = records.get(0);
        for(String column : record.keySet()){
            Column col = serverContext.dataSource.getSchema().getColumn(column);
            io.viewserver.schema.column.ColumnType type = ColumnHolderUtils.getType(col.getType());
            tableColumns.add(new io.viewserver.client.Column(column, type == io.viewserver.schema.column.ColumnType.String ? io.viewserver.schema.column.ColumnType.Int : type));
        }
        /*ITableEditCommand.ICreationConfig creationConfig= MessagePool.getInstance().get(ITableEditCommand.ICreationConfig.class);
        IRollingTableConfig rollingTableConfig = MessagePool.getInstance().get(IRollingTableConfig.class);
        rollingTableConfig.setSize(records.size());
        creationConfig.setTableType("RollingTable");
        creationConfig.setConfig(new ProtoRollingTableConfig(rollingTableConfig));*/
        clientContext.client.editTable(tableName, rowEvents, tableColumns,null, true).get();
    }

    private void assertClientConnected() throws Exception {
        if (clientContext.client == null) {
            throw new Exception("Must have a connected client first!");
        }
    }

    private Exception diff(DataTable table, List<Map<String, Object>> snapshot) throws Exception {
        if (snapshot.isEmpty()) throw new Exception("Snapshot was empty.");

        List<Map<String, String>> rows = new ArrayList<>();
        for (Map<String, Object> row : snapshot) {
            Map<String, String> r = mapRow(row);
            rows.add(r);
        }
//        new CountDownLatch(1).await();
//        try {
        table.diff(rows);
//        } catch(TableDiffException e) {
//            return e;
//        }
        return null;
    }

    private Map<String, String> mapRow(Map<String, Object> row) {
        Map<String, String> mapped = new TreeMap<>();
        MapReader reader = new MapReader(row);
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                mapped.put(key, "");
            }
//            else if (value instanceof Long) {
//                Object nanos = row.get(key + "_nanos");
//                if (nanos != null) {
//                    Timestamp timestamp = reader.readTimestamp(key);
//                    mapped.put(key, timestamp == null ? "" : KdbFormatter.formatTimestamp(timestamp));
//                }
//                else {
//                    Date date = reader.readDate(key);
//                    mapped.put(key, date == null ? "" : KdbFormatter.formatDate(date));
//                }
//            }
            else if (value instanceof Double) {
                Double aDouble = reader.readDouble(key);
                DecimalFormat df = new DecimalFormat("#");
                df.setMaximumFractionDigits(4);
                df.setMinimumIntegerDigits(1);
                mapped.put(key, aDouble == null ? "" : df.format(aDouble));
            } else if (value instanceof Integer) {
                Integer anInteger = reader.readInteger(key);
                mapped.put(key, anInteger == null ? "" : anInteger.toString());
            } else {
                mapped.put(key, value.toString());
            }
        }
        return mapped;
    }

    private interface IConsumer<T1, T2> {
        void consume(T1 t1, T2 t2);
    }
}
