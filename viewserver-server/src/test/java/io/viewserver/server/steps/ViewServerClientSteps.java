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

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import io.viewserver.client.ClientSubscription;
import io.viewserver.client.CommandResult;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.operators.validator.ValidationOperator;
import io.viewserver.operators.validator.ValidationOperatorColumn;
import io.viewserver.operators.validator.ValidationOperatorRow;
import io.viewserver.operators.validator.ValidationUtils;
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
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ViewServerClientSteps {
    private final int timeout = 20;
    private final TimeUnit timeUnit = TimeUnit.SECONDS;
    private ViewServerClientContext clientContext;
    private static final Logger logger = LoggerFactory.getLogger(ViewServerClientSteps.class);
    private String keyColumn;
    private List<CountDownLatch> latches = new ArrayList<>();

    public ViewServerClientSteps(ViewServerClientContext clientContext) {
        this.clientContext = clientContext;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        logger.info("MILESTONE: Running scenatio - {}",scenario.getName());
        latches.forEach(
                lat -> lat.countDown()
        );
        latches.clear();
        clientContext.closeClients();
    }
    @After
    public void afterScenario() {
        latches.forEach(
                lat -> lat.countDown()
        );
        latches.clear();
        clientContext.closeClients();
    }

    @And("^a client named \"([^\"]*)\" connected to \"([^\"]*)\"$")
    public void a_connected_client(String name, String url) {
        a_connected_client_with_authentication(name, url, "compatibleVersion", "1.0.0");
    }

    @And("^a client named \"([^\"]*)\" connected to \"([^\"]*)\" with authentication \"([^\"]*)\" and clientVersion \"([^\"]*)\"$")
    public void a_connected_client_with_authentication(String name, String url, String authName, String clientVersion) {
        int timeout = 120;
        CountDownLatch latch = new CountDownLatch(1);
        this.latches.add(latch);
        AtomicReference<Throwable> errResult = new AtomicReference<>(null);
        try {
            clientContext.create(name, url, authName, clientVersion).take(1).subscribe(
                    res -> {
                        logger.info("MILESTONE: Successfully created client - {} - {}",name,url);
                        latch.countDown();
                    },
                    err -> {
                        errResult.set(err);
                        logger.error("MILESTONE: Client - {} - {} not successfully created.",name,url);
                        latch.countDown();
                    }
            );
            if(!latch.await(timeout,TimeUnit.SECONDS)){
                throw new RuntimeException(String.format("Could not get connected client in %s seconds", timeout));
            }
            if(errResult.get() != null){
                throw errResult.get();
            }

        }catch (Throwable ex){
            logger.error("Problem getting connected client",ex);

        }
    }

    @And("^a client named \"([^\"]*)\" connect failed to \"([^\"]*)\" with authentication \"([^\"]*)\" and clientVersion \"([^\"]*)\"$")
    public void a_connect_failed_client_with_authentication(String name, String url, String authName, String clientVersion) {
        int timeout = 30;
        try {
            clientContext.create(name, url, authName, clientVersion).timeout(timeout,TimeUnit.SECONDS).take(1).toBlocking().first();
        }catch (Exception ex){
            logger.error("Client did not connect as expected",ex);
        }
    }

    @And("^sleep for (\\d+) millis$")
    public void sleep_for_millis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void setValuesFromDataTable(DataTable parameters, IDimensionConsumer consumer, Predicate<DataTableRow> filter) {
        List<DataTableRow> rows = parameters.getGherkinRows().stream().filter(filter).collect(Collectors.toList());
        for (int i = 0; i < rows.size(); i++) {
            List<String> cells = rows.get(i).getCells();
            String name = cells.get(0);
            if("Name".equals(name)){
                continue;
            }
            String type = cells.get(1);
            if("Type".equals(name)){
                continue;
            }
            String[] values = cells.get(2).split(",");
            Object[] typedValues = new Object[values.length];
            for (int j = 0; j < values.length; j++) {
                String value = clientContext.replaceParams(values[j]);
                switch (type.toLowerCase()) {
                    case "integer": {
                        typedValues[j] = Integer.parseInt(value);
                        break;
                    }
                    case "double": {
                        typedValues[j] = Double.parseDouble(value);
                        break;
                    }
                    case "boolean": {
                        typedValues[j] = Boolean.parseBoolean(value);
                        break;
                    }
                    case "string": {
                        typedValues[j] = value;
                        break;
                    }
                    default: {
                        throw new RuntimeException(String.format("Unrecognised type \"%s\"", type));
                    }
                }
            }
            String exclude = cells.size() > 3 ? cells.get(3).trim() : null;
            consumer.call(name, ValueLists.valueListOf(typedValues), "exclude".equals(exclude) );
        }
    }

    @When("^\"(.*)\" subscribed to report \"([^\"]*)\"$")
    public void I_subscribe_to_report(String clientName, String reportId){
        logger.info("Client {} subscribing to report {}",clientName,reportId);
        ClientConnectionContext clientConnectionContext = clientContext.get(clientName);
        clientConnectionContext.getReportContext().setReportName(reportId);

        CountDownLatch subscribeLatch = new CountDownLatch(1);
        trySubscribe(clientName,reportId, subscribeLatch);
        try {
            subscribeLatch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertNotNull("Could not detect successful subscription to " + reportId + " after " + timeout + " " + timeUnit, clientConnectionContext.getSubscription("report" + reportId));
    }

    @When("^\"(.*)\" subscribed to dimension \"([^\"]*)\" of report \"([^\"]*)\" with data source \"([^\"]*)\" with parameters$")
    public void I_subscribe_to_dimension_of_report(String clientName, String dimensionName,String reportId,String dataSourceName, DataTable parameters){
        logger.info(String.format("Client %s subscribing to dimension %s from data source %s",clientName,dimensionName,dataSourceName));
        ClientConnectionContext clientConnectionContext = clientContext.get(clientName);
        clientConnectionContext.getReportContext().setReportName(reportId);
        CountDownLatch subscribeLatch = new CountDownLatch(1);
        report_parameters(clientName,parameters);
        dimension_filters(clientName,parameters);
        trySubscribeDimension(clientName,dimensionName,dataSourceName, subscribeLatch);
        try {
            subscribeLatch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertNotNull("Could not detect successful subscription to dimension " + dimensionName + " dataSource " + dataSourceName + " after " + timeout + " " + timeUnit, clientConnectionContext.getSubscription(String.format("dimension_%s_%s",dataSourceName,dimensionName)));
    }


    @When("^\"(.*)\" subscribed to operator \"([^\"]*)\"$")
    public void I_subscribe_to_operator(String clientName, String operatorName) throws Exception {
        ClientConnectionContext clientConnectionContext = clientContext.get(clientName);

        CountDownLatch subscribeLatch = new CountDownLatch(1);
        Options options = new Options();
        options.setOffset(0);
        options.setLimit(100);
        trySubscribeOperator(clientName, operatorName,options, subscribeLatch);
        try {
            subscribeLatch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertNotNull("Could not detect successful subscription to " + operatorName + " after " + timeout + " " + timeUnit, clientConnectionContext.getSubscription(operatorName));
    }

    @When("^\"([^\"]*)\" subscribed to report \"([^\"]*)\" with parameters$")
    public void subscribedToReportWithParameters(String clientName, String reportName, DataTable parameters){
        report_parameters(clientName,parameters);
        dimension_filters(clientName,parameters);
        I_subscribe_to_report(clientName,reportName);
    }

    @Given("^\"(.*)\" report parameters$")
    public void report_parameters(String clientName, DataTable parameters) {
        ClientConnectionContext ctxt = clientContext.get(clientName);
        if(parameters != null) {
            setValuesFromDataTable(parameters, (name, valuesList, excluded) -> ctxt.getReportContext().getParameterValues().put(name, valuesList), c -> !c.getCells().get(0).startsWith("dimension_"));
        }else{
            clientContext.get(clientName).getReportContext().getParameterValues().clear();
        }
    }
    @Given("^\"(.*)\" report dataSource \"(.*)\"$")
    public void report_parameters(String clientName, String dataSourceName) {
        ClientConnectionContext ctxt = clientContext.get(clientName);
        ctxt.getReportContext().setDataSourceName(dataSourceName);
    }

    @And("^\"(.*)\" dimension filters$")
    public void dimension_filters(String clientName, DataTable filters) {
        if(filters != null) {
            ClientConnectionContext ctxt = clientContext.get(clientName);
            ctxt.getReportContext().getDimensionValues().clear();
            setValuesFromDataTable(filters, (name, valuesList, excluded) ->
                    ctxt.getReportContext().getDimensionValues().add(new ReportContext.DimensionValue(name.substring("dimension_".length()), valuesList, excluded)), c -> c.getCells().get(0).startsWith("dimension_"));
        }else{
            clientContext.get(clientName).getReportContext().getDimensionValues().clear();
        }
    }


    @When("^\"(.*)\" All data sources are built$")
    public void All_datasources_are_initialized(String clientName) throws Exception {
        ClientConnectionContext clientConnectionContext = clientContext.get(clientName);

        CountDownLatch subscribeLatch = new CountDownLatch(1);
        Options options = new Options();
        options.setOffset(0);
        options.setLimit(100);
        trySubscribeOperator(clientName, "/datasources",options, subscribeLatch);
        subscribeLatch.await(timeout, timeUnit);


        ClientSubscription subscription = clientConnectionContext.getSubscription("/datasources");

        final String[] waitingFor = {getUnbuiltDataSourceNames(subscription)};

        if (waitingFor[0] != null) {
            System.out.println("Waiting for data sources " + waitingFor[0]);
            subscription.dataChangedObservable().map(c -> {
                System.out.println("Received operator event " + c.getEventType() + " data is " + c.getEventData());
                waitingFor[0] = getUnbuiltDataSourceNames(subscription);
                return waitingFor[0];
            }).filter(res -> res == null).take(1).timeout(timeout, timeUnit).toBlocking().toIterable().iterator().next();
            subscribeLatch = new CountDownLatch(1);
            trySubscribeOperator(clientName, "/datasources",options, subscribeLatch);
            subscribeLatch.await(timeout, timeUnit);
        }
        Assert.assertNull("Some data sources still not built \n" + waitingFor[0], waitingFor[0]);
    }

    public String getUnbuiltDataSourceNames(ClientSubscription subscription) {
        try {
            Assert.assertNotNull(subscription);
            System.out.println("------------------ CHECKING DATA SOURCES - WAITING FOR SNAPSHOT --------------------");
            List<Map<String, Object>> snapshot = subscription.getSnapshot().get(timeout, timeUnit);
            String waitingFor = null;
            boolean hasUnbuiltDataSource = snapshot.isEmpty();
            if (snapshot.isEmpty()) {
                waitingFor = "ALL";
                System.out.println("No data sources found. Waiting !!!");
            }
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> snap : snapshot) {
                String status = (String) snap.get("status");
                if (!"INITIALIZED".equals(status)) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    hasUnbuiltDataSource = true;
                    sb.append(snap.get("name"));
                    System.out.println(String.format("Waiting for data source %s which is in state %s", snap.get("name"), status));
                }
            }
            if (hasUnbuiltDataSource) {
                System.out.println("------------------ WAITING ON DATA SOURCES --------------------");
                Thread.sleep(1000);
                waitingFor = sb.toString();
            }
            return waitingFor;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Given("^\"([^\"]*)\" controller \"([^\"]*)\" action \"([^\"]*)\" invoked with data file \"([^\"]*)\"$")
    public void controller_action_invoked_with_data_file(String clientName, String controllerName, String action, String dataFile) throws InterruptedException, ExecutionException, TimeoutException {
        I_Invoke_Action_On_Controller_With_Data_With_Result(clientName, controllerName, action, TestUtils.getJsonStringFromFile(dataFile), null,null);
    }

    @Given("^\"([^\"]*)\" controller \"([^\"]*)\" action \"([^\"]*)\" invoked with data file \"([^\"]*)\" with parameters$")
    public void controller_action_invoked_with_data_file_with_parameters(String clientName, String controllerName, String action, String dataFile, List<Map<String, String>> records) throws InterruptedException, ExecutionException, TimeoutException {
        I_Invoke_Action_On_Controller_With_Data_With_Result(clientName, controllerName, action, getJsonStringFromFile(dataFile, records), null,null);
    }
    @Given("^\"([^\"]*)\" controller \"([^\"]*)\" action \"([^\"]*)\" invoked with data file \"([^\"]*)\" with parameters and result name suffix \"([^\"]*)\"$")
    public void controller_action_invoked_with_data_file_with_parameters(String clientName, String controllerName, String action, String dataFile, String resultNameSffix,List<Map<String, String>> records) throws InterruptedException, ExecutionException, TimeoutException {
        I_Invoke_Action_On_Controller_With_Data_With_Result(clientName, controllerName, action, getJsonStringFromFile(dataFile, records), null,resultNameSffix);
    }

    @Given("^\"([^\"]*)\" controller \"([^\"]*)\" action \"([^\"]*)\" invoked with parameters$")
    public void controller_action_invoked_with_data(String clientName, String controllerName, String action, List<Map<String, String>> records) throws InterruptedException, ExecutionException, TimeoutException {
        HashMap<String,Object> result = new HashMap<>();
        for(Map<String,String> record : records){
            String paramName = record.get("Name");
            String paramValue = record.get("Value");
            paramValue = TestUtils.replaceReference(paramValue) + "";
            result.put(paramName,  clientContext.replaceParams(paramValue));
        }
        I_Invoke_Action_On_Controller_With_Data_With_Result(clientName, controllerName, action, JacksonSerialiser.getInstance().serialise(result), null, null);
    }

    @Given("^\"([^\"]*)\" controller \"([^\"]*)\" action \"([^\"]*)\" invoked with parameters and result name suffix \"([^\"]*)\"$")
    public void controller_action_invoked_with_data(String clientName, String controllerName, String action,String resultNameSffix, List<Map<String, String>> records) throws InterruptedException, ExecutionException, TimeoutException {
        HashMap<String,Object> result = new HashMap<>();
        for(Map<String,String> record : records){
            String paramName = record.get("Name");
            String paramValue = record.get("Value");

            paramValue = TestUtils.replaceReference(paramValue) + "";
            result.put(paramName,  clientContext.replaceParams(paramValue));
        }
        I_Invoke_Action_On_Controller_With_Data_With_Result(clientName, controllerName, action, JacksonSerialiser.getInstance().serialise(result), null, resultNameSffix);
    }

    private String getJsonStringFromFile(String dataFile, List<Map<String, String>> records) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dataFile);
        if (inputStream == null) {
            throw new RuntimeException("Unable to find resource at at " + dataFile);
        }
        Reader reader = IOUtils.getBufferedReader(inputStream);
        try {
            String json = IOUtils.readStringAndClose(reader, 0);
            json = TestUtils.parseReferencesInJSONFile(json);
            if (records != null) {
                Map<String, String> transpose = transpose(records);
                Map<String, String> params = clientContext.replaceParams(transpose);
                clientContext.addAllParams(params);
                json = ViewServerClientContext.replaceParams(json, params);
            }
            return json;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String>  transpose(List<Map<String, String>> records) {
        HashMap<String,String> result = new HashMap<>();
        for(Map<String,String> record : records){
            result.put(record.get("Name"), clientContext.replaceParams(record.get("Value")));
        }
        return result;
    }



    @When("^\"([^\"]*)\" controller \"([^\"]*)\" action \"([^\"]*)\" invoked with data \"([^\"]*)\" and result \"([^\"]*)\"$")
    public void I_Invoke_Action_On_Controller_With_Data_With_Result(String clientName, String controllerName, String action, String data, String result, String resultNameSuffix) throws InterruptedException, ExecutionException, TimeoutException {
        ClientConnectionContext connectionContext = clientContext.get(clientName);

        ListenableFuture<CommandResult> future = connectionContext.invokeJSONCommand(controllerName, action, data);

        CommandResult actual = future.get(timeout, timeUnit);
        Assert.assertTrue(actual.isStatus());
        connectionContext.setResult(controllerName, action + (resultNameSuffix == null ? "" : resultNameSuffix), actual.getMessage());
        if (result != null) {
            Assert.assertEquals(result, actual.getMessage());
        }
    }

    private void trySubscribeDimension(String clientName, String dimensionName, String dataSourceName, CountDownLatch subscribeLatch) {
        TestSubscriptionEventHandler eventHandler = new TestSubscriptionEventHandler();
        ClientConnectionContext clientConnectionContext = clientContext.get(clientName);
        clientConnectionContext.getReportContext().setDataSourceName(dataSourceName);
        ListenableFuture<ClientSubscription> subFuture = clientConnectionContext.subscribeToDimension(dimensionName,eventHandler);

        Futures.addCallback(subFuture, new FutureCallback<ClientSubscription>() {
            @Override
            public void onSuccess(ClientSubscription clientSubscription) {
                clientConnectionContext.addSubscription(String.format("dimension_%s_%s",dataSourceName,dimensionName), clientSubscription, eventHandler);
                subscribeLatch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                trySubscribeDimension(clientName,dimensionName,dataSourceName,subscribeLatch);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void trySubscribe(String clientName, String reportId, CountDownLatch subscribeLatch) {
        TestSubscriptionEventHandler eventHandler = new TestSubscriptionEventHandler();
        ClientConnectionContext clientConnectionContext = clientContext.get(clientName);
        ListenableFuture<ClientSubscription> subFuture = clientConnectionContext.subscribeToReport(eventHandler);

        Futures.addCallback(subFuture, new FutureCallback<ClientSubscription>() {
            @Override
            public void onSuccess(ClientSubscription clientSubscription) {
                clientConnectionContext.addSubscription("report" + reportId, clientSubscription, eventHandler);
                subscribeLatch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                trySubscribe(clientName,reportId,subscribeLatch);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void trySubscribeOperator(String clientName, String operatorName,Options options, CountDownLatch subscribeLatch) {
        ClientConnectionContext connectionContext = clientContext.get(clientName);
        final TestSubscriptionEventHandler testSubscriptionEventHandler = new TestSubscriptionEventHandler();
        ListenableFuture<ClientSubscription> subFuture = connectionContext.subscribe(operatorName, options,
                testSubscriptionEventHandler);

        Futures.addCallback(subFuture, new FutureCallback<ClientSubscription>() {
            @Override
            public void onSuccess(ClientSubscription clientSubscription) {
                connectionContext.addSubscription(operatorName, clientSubscription, testSubscriptionEventHandler);
                subscribeLatch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                trySubscribe(clientName, operatorName,subscribeLatch);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @And("^\"([^\"]*)\" paging from (\\d+) to (\\d+) by \"([^\"]*)\" (ascending|descending)$")
    public void paging_from_to_by(String clientName, int offset, int limit, String sortColumn, String direction) {
        ClientConnectionContext connectionContext = clientContext.get(clientName);
        connectionContext.getOptions().setOffset(offset);
        connectionContext.getOptions().setLimit(limit);
        connectionContext.getOptions().getColumnsToSort().clear();
        connectionContext.getOptions().addSortColumn(sortColumn, "descending".equals(direction));
    }

    @When("^\"([^\"]*)\" subscribed to report \"([^\"]*)\" with \"([^\"]*)\" is \"([^\"]*)\"$")
    public void subscribedToReportWithOrderId(String clientName, String reportName,String paramName,String paramValue, DataTable records) throws Throwable {
        ClientConnectionContext ctxt = clientContext.get(clientName);
        String s = clientContext.replaceParams(paramValue);

        if(paramName.startsWith("dimension")){
            ctxt.getReportContext().getDimensionValues().add(new ReportContext.DimensionValue(paramName,false, ValueLists.valueListOf(s)));
        }else{
            ctxt.getReportContext().getParameterValues().put(paramName, ValueLists.valueListOf(paramValue));

        }
        I_subscribe_to_report(clientName,reportName);
        repeat("Receiving data " + records, () -> the_following_data_is_received_on_report(clientName,reportName,keyColumn,records,true), 5, 500, 0,false);
    }

    @Then("^\"([^\"]*)\" the following data is received eventually on report \"([^\"]*)\"$")
    public void the_following_data_is_received_eventually(String clientName, String reportId, DataTable records) {
        repeat("Receiving data " + records, () -> the_following_data_is_received_on_report(clientName,reportId,keyColumn,records,false), 10, 500, 0,false);
    }

    @Then("^\"([^\"]*)\" the following data is received eventually on operator \"([^\"]*)\"$")
    public void the_following_data_is_received_eventually_on_operator(String clientName, String reportId, DataTable records) {
        repeat("Receiving data " + records, () -> the_following_data_is_receieved_on_operator(clientName,reportId,keyColumn,records,false), 10, 500, 0,false);
    }

    @Then("^\"([^\"]*)\" the following data is received eventually on dimension \"([^\"]*)\" data source \"([^\"]*)\"$")
    public void the_following_data_is_received_eventually_for_dimension(String clientName, String dimensionName,String dataSourceName, DataTable records) {
        repeat("Receiving data " + records, () -> the_following_data_is_received_for_dimension(clientName,dimensionName,dataSourceName,keyColumn,records,false), 10, 500, 0,false);
    }

    @Then("^\"([^\"]*)\" the following data is received eventually on dimension \"([^\"]*)\" data source \"([^\"]*)\" snapshot$")
    public void the_following_data_is_received_eventually_for_dimension_snapshot(String clientName, String dimensionName,String dataSourceName, DataTable records) {
        repeat("Receiving data " + records, () -> the_following_data_is_received_for_dimension(clientName,dimensionName,dataSourceName,keyColumn,records,true), 10, 500, 0,false);
    }


    @Then("^\"([^\"]*)\" the following data is received eventually on report \"([^\"]*)\" snapshot$")
    public void the_following_data_is_received_eventually_on_report_snapshot(String clientName, String reportId, DataTable records) {
        repeat("Receiving data " + records, () -> the_following_data_is_received_on_report(clientName,reportId,keyColumn,records,true), 10, 500, 0,false);
    }

    @Then("^\"([^\"]*)\" the following data is received eventually on operator \"([^\"]*)\" snapshot$")
    public void the_following_data_is_received_eventually_on_operator_snapshot(String clientName, String reportId, DataTable records) {
        repeat("Receiving data " + records, () -> the_following_data_is_receieved_on_operator(clientName,reportId,keyColumn,records,true), 10, 500, 0,false);
    }

    @Then("^\"([^\"]*)\" the following data is received terminally on report \"([^\"]*)\"$")
    public void the_following_data_is_received_terminally(String clientName, String reportId, DataTable records) {
        repeat("Receiving data " + records, () -> the_following_data_is_received_on_report(clientName,reportId,keyColumn,records,false), 5, 400, 0, true);
    }

    @Then("^\"([^\"]*)\" the following schema is received eventually on report \"([^\"]*)\"$")
    public void the_following_schema_is_received_on_report_eventually(String clientName, String reportId, List<Map<String, String>> records) {
        repeat("Receiving schema " + records, () -> the_following_schema_is_received_on_report(clientName,reportId,records), 5, 500, 0, false);
    }

    @Then("^\"([^\"]*)\" the following schema is received eventually on operator \"([^\"]*)\"$")
    public void the_following_schema_is_received_on_operator_eventually(String clientName, String reportId, List<Map<String, String>> records) {
        repeat("Receiving schema " + records, () -> the_following_schema_is_received_on_operator(clientName,reportId,records), 5, 500, 0, false);
    }

    @Then("^\"([^\"]*)\" the following schema is received eventually on dimension \"([^\"]*)\" of data source \"([^\"]*)\"$")
    public void the_following_schema_is_received_on_dimension_eventually(String clientName, String dimensionName, String dataSourceName, List<Map<String, String>> records) {
        repeat("Receiving schema " + records, () -> the_following_schema_is_received_on_dimension(clientName,dimensionName,dataSourceName,records), 5, 500, 0, false);
    }


    @Then("^\"([^\"]*)\" the following schema is received on report \"([^\"]*)\"$")
    public void the_following_schema_is_received_on_report(String clientName,String reportId, List<Map<String, String>> records) {
        String report = "report";
        the_following_schema(clientName, reportId, records, report);
    }

    @Then("^\"([^\"]*)\" the following schema is received on operator \"([^\"]*)\"$")
    public void the_following_schema_is_received_on_operator(String clientName,String reportId, List<Map<String, String>> records) {
        the_following_schema(clientName, reportId, records, "");
    }

    @Then("^\"([^\"]*)\" the following schema is received on dimension \"([^\"]*)\" of data source \"([^\"]*)\"$")
    public void the_following_schema_is_received_on_dimension(String clientName,String dimensionName,String dataSourceName, List<Map<String, String>> records) {
        the_following_schema(clientName, String.format("%s_%s",dataSourceName,dimensionName), records, "dimension_");
    }

    private void the_following_schema(String clientName, String reportId, List<Map<String, String>> records, String suffix) {
        try {
            ClientConnectionContext connectionContext = clientContext.get(clientName);
            String name = suffix + reportId;
            TestSubscriptionEventHandler eventHandler = connectionContext.getSubscriptionEventHandler(name);

            if(eventHandler == null){
                throw new RuntimeException("Unable to find report for id " + name);
            }

            ValidationOperator operator = eventHandler.getValidationOperator();
            List<ValidationOperatorColumn> expectedColumns = new ArrayList<>();
            List<String> columns = null;
            for (Map<String, String> record : records) {
                if(columns == null) {
                    columns = new ArrayList<>(record.keySet());
                }
                Map<String, String> row = clientContext.replaceParams(record);
                expectedColumns.add(ValidationUtils.toColumn(row));
            }
            operator.validateColumns(expectedColumns);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Then("^\"([^\"]*)\" the following data is received on report \"([^\"]*)\" with row key \"([^\"]*)\"$")
    public void the_following_data_is_received_on_report(String clientName, String reportId, String keyColumn, DataTable records, boolean flatten) {

        String operatorNameSuffix = "report";

        the_following_data_is_received(clientName, reportId, keyColumn, records, flatten, operatorNameSuffix);
    }

    @Then("^\"([^\"]*)\" the following data is received on operator \"([^\"]*)\" with row key \"([^\"]*)\"$")
    public void the_following_data_is_receieved_on_operator(String clientName, String reportId, String keyColumn, DataTable records, boolean flatten) {

        String operatorNameSuffix = "";

        the_following_data_is_received(clientName, reportId, keyColumn, records, flatten, operatorNameSuffix);
    }
    @Then("^\"([^\"]*)\" the following data is received on dimension \"([^\"]*)\" with datasource \"([^\"]*)\" with row key \"([^\"]*)\"$")
    public void the_following_data_is_received_for_dimension(String clientName, String dimensionName, String dataSourceName, String keyColumn, DataTable records, boolean flatten) {

        String operatorNameSuffix = "dimension_";

        the_following_data_is_received(clientName, String.format("%s_%s",dataSourceName,dimensionName), keyColumn, records, flatten, operatorNameSuffix);
    }

    private void the_following_data_is_received(String clientName, String reportId, String keyColumn, DataTable records, boolean flatten, String operatorNameSuffix) {
        try {

            logger.info("Asserting data for \"{}\" subscribing to report {}",clientName,reportId);
            ClientConnectionContext connectionContext = clientContext.get(clientName);
            String name = operatorNameSuffix + reportId;
            TestSubscriptionEventHandler eventHandler = connectionContext.getSubscriptionEventHandler(name);

            if(eventHandler == null){
                throw new RuntimeException("Unable to find subscription to report id " + name + " for client " + clientName);
            }

            ValidationOperator operator = eventHandler.getValidationOperator();
            List<ValidationOperatorRow> validationRows = new ArrayList<>();
            List<String> columns = records.topCells();
            for (Map<String, String> record : records.asMaps(String.class,String.class)) {
                if(columns == null) {
                    columns = new ArrayList<>(record.keySet());
                }
                Map<String, String> row = clientContext.replaceParams(record);
                TestUtils.replaceReferences((HashMap)row);
                clientContext.replaceParams(row);
                validationRows.add(flatten ?  ValidationUtils.toFlattenedRow(row, keyColumn) : ValidationUtils.toRow(row, keyColumn));
            }
            operator.validateRows(c-> clientContext.replaceParams((String)c),validationRows, columns, keyColumn, flatten);
        } catch (Exception ex) {
            throw ex;
        }
    }


    private void repeat(String scenarioLablel, Runnable assertion, int times, int delay, int counter, boolean continueIfPass) {
        if (times == counter && !continueIfPass) {
            Assert.fail(scenarioLablel + " failed after " + times + " retries spanning " + (delay * times) / 1000 + " seconds");
        }
        try {
            assertion.run();
            if(continueIfPass){
                if(counter >= times -1){
                    Assert.assertTrue("Passed on the final time this is a pass", true);
                    return;
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                repeat(scenarioLablel, assertion, times, delay, counter + 1, continueIfPass);
            }
        } catch (Throwable ex) {
            if (times - 1 == counter) {
                throw ex;
            }
            try {
                Thread.sleep(delay );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repeat(scenarioLablel, assertion, times, delay, counter + 1, continueIfPass);
        }
    }

    @Given("^keyColumn is \"([^\"]*)\"$")
    public void keycolumnIs(String arg0){
        this.keyColumn = arg0;
    }


}


interface IDimensionConsumer{
    void call(String valueName, ValueLists.IValueList values, Boolean exclude);
}