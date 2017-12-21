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

package io.viewserver.datasource;

import io.viewserver.core.IExecutionContext;
import io.viewserver.distribution.IStripingStrategy;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties({ "calcsName", "indexName" })
public class DataSource extends DataSourceBase {
    public static final String CALCS_NAME = "calcs";
    public static final String INDEX_NAME = "index";
    private static final Logger log = LoggerFactory.getLogger(DataSource.class);
    private IDataLoader dataLoader;
    private IStripingStrategy stripingStrategy;
    private List<ReportContext> startupReports;
    private PartitionConfig partitionConfig;
    private DistributionMode distributionMode = DistributionMode.Local;

    public DataSource() {
        super();
    }

    public DataSource withName(String name){
        this.name = name;
        return this;
    }

    public DataSource withDimensions(List<Dimension> dimensions){
        this.dimensions.clear();
        this.dimensions.addAll(dimensions);
        return this;
    }

    public DataSource withSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public DataSource withNodes(IGraphNode... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        return this;
    }

    public DataSource withDataLoader(IDataLoader dataAdapter){
        this.dataLoader = dataAdapter;
        return this;
    }

    public DataSource withOptions(DataSourceOption... options) {
        if (options.length > 0) {
            this.options.addAll(Arrays.asList(options));
        }
        return this;
    }

    public DataSource withOutput(String output){
        this.output = output;
        return this;
    }

    public DataSource withCalculatedColumns(CalculatedColumn... calculatedColumns) {
        this.calculatedColumns.addAll(Arrays.asList(calculatedColumns));
        return this;
    }

    public DataSource withStartupReports(ReportContext... reportContexts) {
        if (startupReports == null) {
            startupReports = new ArrayList<>();
        }
        startupReports.addAll(Arrays.asList(reportContexts));
        return this;
    }

    public DataSource withPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
        return this;
    }

    public void initialise(DimensionMapper dimensionMapper, ITableUpdater tableUpdater, FunctionRegistry functionRegistry, IExpressionParser expressionParser, IExecutionContext executionContext) {
        this.getDataLoader().configure(tableUpdater, dimensionMapper, this, functionRegistry, expressionParser, executionContext);
        createTable();
    }

    private void createTable() {
        this.getDataLoader().createTable();
    }

    public ListenableFuture loadData() {
        return this.getDataLoader().load();
    }


    public IDataLoader getDataLoader() {
        return dataLoader;
    }

    public void setDataLoader(IDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public List<ReportContext> getStartupReports() {
        return startupReports;
    }

    public void setStartupReports(List<ReportContext> startupReports) {
        this.startupReports = startupReports;
    }

    public DataSource withDistributionMode(DistributionMode distributionMode) {
        this.distributionMode = distributionMode;
        return this;
    }

    public DataSource withStripingStrategy(IStripingStrategy stripingStrategy) {
        this.stripingStrategy = stripingStrategy;
        return this;
    }

    public IStripingStrategy getStripingStrategy() {
        return stripingStrategy;
    }

    public void setStripingStrategy(IStripingStrategy stripingStrategy) {
        this.stripingStrategy = stripingStrategy;
    }

    @Override
    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    public void setDistributionMode(DistributionMode distributionMode) {
        this.distributionMode = distributionMode;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
