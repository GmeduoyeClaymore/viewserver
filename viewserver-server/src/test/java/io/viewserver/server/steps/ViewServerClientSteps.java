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
import io.viewserver.client.CommandResult;
import io.viewserver.controller.ControllerUtils;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.IGenericJSONCommand;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.network.Command;
import io.viewserver.util.MapReader;
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
import org.h2.util.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ViewServerClientSteps {
    private ViewServerClientContext clientContext;
    private static final Logger logger = LoggerFactory.getLogger(ViewServerClientSteps.class);
    public ViewServerClientSteps(ViewServerClientContext clientContext) {
        this.clientContext = clientContext;
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
                    case "Boolean": {
                        typedValues[j] = Boolean.parseBoolean(values[j]);
                        break;
                    }
                    case "String": {
                        typedValues[j] = values[j];
                        break;
                    }
                    default:{
                        throw new RuntimeException(String.format("Unrecognised type \"%s\"", type));
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

    @When("^All data sources are built$")
    public void All_datasources_are_initialized() throws Throwable {
        assertClientConnected();
        CountDownLatch subscribeLatch = new CountDownLatch(1);
        Options options = new Options();
        options.setOffset(0);
        options.setLimit(100);
        trySubscribeOperator("/datasources",options,subscribeLatch);
        subscribeLatch.await(20, TimeUnit.SECONDS);

        ClientSubscription subscription = clientContext.getSubscription("/datasources");
        Assert.assertNotNull(subscription);

        boolean hasUnbuiltDataSource = false;
        int counter = 0;
        do{
            System.out.println("------------------ CHECKING DATA SOURCES --------------------");
            List<Map<String, Object>> snapshot = subscription.getSnapshot().get();
            for(Map<String,Object> snap :snapshot){
                String status = (String) snap.get("status");
                if(!"INITIALIZED".equals(status)){
                    hasUnbuiltDataSource = true;
                    System.out.println(String.format("Waiting for data source %s which is in state %s",snap.get("name"),status));
                }
            }
            if(hasUnbuiltDataSource){
                System.out.println("------------------ WAITING ON DATA SOURCES --------------------");
                Thread.sleep(1000);
            }
            counter++;
        }while (hasUnbuiltDataSource && counter < 200);

        Assert.assertFalse("Some data sources still not built",hasUnbuiltDataSource);
        subscription.close();



    }



    @Given("^controller \"([^\"]*)\" action \"([^\"]*)\" invoked with data file \"([^\"]*)\"$")
    public void controller_action_invoked_with_data_file(String controllerName, String action, String dataFile) throws Throwable {
        assertClientConnected();
        I_Invoke_Action_On_Controller_With_Data_With_Result(controllerName, action, getJsonFromFile(dataFile), null);
    }

    private String getJsonFromFile(String dataFile) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dataFile);
        if(inputStream == null){
            throw new RuntimeException("Unable to find resource at at " + dataFile);
        }
        Reader reader = IOUtils.getBufferedReader(inputStream);
        try {
            String json = IOUtils.readStringAndClose(reader,0);
            return parseReferencesInJSONFile(json);
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    private String parseReferencesInJSONFile(String json) {
        HashMap<String, Object> result = ControllerUtils.mapDefault(json);
        replaceReferences(result);
        return ControllerUtils.toString(result);
    }

    private void replaceReferences(HashMap<String, Object> result) {
        for (Map.Entry<String,Object> entry : result.entrySet()){
            if(entry.getValue() instanceof HashMap){
                replaceReferences((HashMap)entry.getValue());
            }
            else if(entry.getValue() instanceof String){
                String value = (String) entry.getValue();
                if(value.startsWith("ref://")){
                    String fileReference = value.substring(6);
                    entry.setValue(getJsonFromFile(fileReference));
                }
            }
        }
    }

    @When("^Given controller \"([^\"]*)\" action \"([^\"]*)\" invoked with data \"([^\"]*)\" and result \"([^\"]*)\"$")
    public void I_Invoke_Action_On_Controller_With_Data_With_Result(String controllerName, String action, String data, String result) throws Throwable {
        assertClientConnected();

        IGenericJSONCommand genericJSONCommand = MessagePool.getInstance().get(IGenericJSONCommand.class)
                .setAction(action)
                .setPayload(data)
                .setPath(controllerName);
        Command command = new Command("genericJSON", genericJSONCommand);
        ListenableFuture<CommandResult> future = clientContext.client.sendCommand(command);

        CommandResult actual = future.get(20, TimeUnit.SECONDS);
        Assert.assertTrue(actual.isStatus());
        if(result != null) {
            Assert.assertEquals(result, actual.getMessage());
        }
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

    private void trySubscribeOperator(String operatorName,Options options,CountDownLatch subscribeLatch){
        ListenableFuture<ClientSubscription> subFuture = clientContext.client.subscribe(operatorName, options,
                new TestSubscriptionEventHandler());

        Futures.addCallback(subFuture, new FutureCallback<ClientSubscription>() {
            @Override
            public void onSuccess(ClientSubscription clientSubscription) {
                clientContext.addSubscription(operatorName, clientSubscription);
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
        Thread.sleep(10000000);
        diff(expectedData, snapshot);
    }


    private void assertClientConnected() throws Exception {
        if (clientContext.client == null) {
            throw new Exception("Must have a connected client first!");
        }
    }

    private void diff(DataTable table, List<Map<String, Object>> snapshot) throws Exception {
        if (snapshot.isEmpty()) throw new Exception("Snapshot was empty.");

        List<Map<String, String>> rows = new ArrayList<>();
        for (Map<String, Object> row : snapshot) {
            Map<String, String> r = mapRow(row);
            rows.add(r);
        }

        table.diff(rows);
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
