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

import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.DataSource;
import io.viewserver.distribution.INodeMonitor;
import io.viewserver.network.netty.inproc.NettyInProcEndpoint;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.MultiThreadedEventLoopReactor;
import io.viewserver.report.ReportDefinition;
import io.viewserver.server.ViewServerMasterTest;
import io.viewserver.server.ViewServerSlave;
import io.viewserver.server.setup.BootstrapperBase;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by nick on 10/02/2015.
 */
public class ViewServerSteps {
    private IViewServerContext viewServerContext;
    private static final Logger log = LoggerFactory.getLogger(ViewServerSteps.class);
    public ViewServerSteps(IViewServerContext viewServerContext) {
        this.viewServerContext = viewServerContext;
    }

    @After
    public void afterScenario() {
        for (ViewServerSlave slave : viewServerContext.getSlaves()) {
            IReactor reactor = slave.getServerReactor();
            reactor.shutDown();
            reactor.waitForShutdown();
        }
        viewServerContext.getSlaves().clear();

        if (viewServerContext.getMaster() != null) {
            IReactor reactor = viewServerContext.getMaster().getServerReactor();
            reactor.shutDown();
            reactor.waitForShutdown();
            viewServerContext.setMaster(null);
        }
    }

    @Given("^an in-process viewserver with ([0-9]+) slave nodes$")
    public void an_in_process_viewserver_with_slave_nodes(int numSlaves) throws Throwable {
//        System.setProperty("server.bypassDataSources", "true");
        BootstrapperBase.bootstrap(viewServerContext.getMasterConfiguration());

        viewServerContext.setMaster(new ViewServerMasterTest("master", viewServerContext.getMasterConfiguration()));

        viewServerContext.getMaster().run();

        for (int i = 0; i < numSlaves; i++) {
            ViewServerSlave slave = new ViewServerSlave("slave-" + i, new NettyInProcEndpoint("inproc://master"));
            slave.run();
            viewServerContext.getSlaves().add(slave);
        }
    }

    @Given("^an in-process viewserver$")
    public void an_in_process_viewserver() throws Throwable {
//        System.setProperty("server.bypassDataSources", "true");
        BootstrapperBase.bootstrap(viewServerContext.getMasterConfiguration());

        viewServerContext.getMaster().run();
    }


    @Given("^a data source defined by \"([^\"]*)\"$")
    public void a_data_source_defined_by(String dataSourceDefinitionFile) throws Exception {
        viewServerContext.setDataSource(getDataSource(dataSourceDefinitionFile));
        viewServerContext.getMaster().getDataSourceRegistry().register(viewServerContext.getDataSource());
    }

    @Given("^a CSV data source defined by \"([^\"]*)\" and loaded from \"([^\"]*)\"$")
    public void a_csv_data_source_defined_by_and_loaded_from(String dataSourceDefinitionFile, String dataFile) throws Exception {
        DataSource dataSource = getDataSource(dataSourceDefinitionFile);
        if( !(dataSource.getDataLoader().getDataAdapter() instanceof CsvDataAdapter)){
            throw new Exception("Data source must use a CsvDataAdapter to use this step");
        }
        ((CsvDataAdapter) dataSource.getDataLoader().getDataAdapter()).setFileName(dataFile);
        viewServerContext.getMaster().getDataSourceRegistry().register(dataSource);
    }

    private DataSource getDataSource(String dataSourceFile) throws IOException {
        JacksonSerialiser serialiser = new JacksonSerialiser();
        String json = Resources.toString(Resources.getResource(dataSourceFile), Charsets.UTF_8);
        DataSource dataSource = serialiser.deserialise(json, DataSource.class);
        return dataSource;
    }

    @Given("^a report defined by \"([^\"]*)\"$")
    public void a_report_defined_by(String reportDefinitionFile) throws Throwable {
        JacksonSerialiser serialiser = new JacksonSerialiser();
        String json = Resources.toString(Resources.getResource(reportDefinitionFile), Charsets.UTF_8);
        ReportDefinition reportDefinition = serialiser.deserialise(json, ReportDefinition.class);
        viewServerContext.getMaster().getReportRegistry().register(reportDefinition);
    }

    @And("^all slave nodes are initialised$")
    public void all_slave_nodes_are_initialised() throws Throwable {
        viewServerContext.getMaster().nodeAddedSubject().take(viewServerContext.getSlaves().size()).toBlocking().forEach(
                nd -> log.info("Node {} has been registered", nd.toString())
        );
    }

    @Given("^the viewserver has been bootstrapped from \"([^\"]*)\"$")
    public void the_viewserver_has_been_bootstrapped_from(String bootstrapperClassName) throws Throwable {
        viewServerContext.setBootstrapperClass(bootstrapperClassName);
    }
}
