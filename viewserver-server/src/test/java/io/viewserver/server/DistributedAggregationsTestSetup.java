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

package io.viewserver.server;

import io.viewserver.adapters.common.EmptyTableDataLoader;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.*;
import io.viewserver.distribution.RoundRobinStripingStrategy;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.nodes.MeasureGroupByNode;
import io.viewserver.report.CalculationDefinition;
import io.viewserver.report.ReportDefinition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by nick on 14/09/15.
 */
public class DistributedAggregationsTestSetup {
    @Test
    public void setupDataSource() {
        Schema schema = new Schema()
                .withColumns
                        (
                                Arrays.asList
                                        (
                                                new Column("customer", ColumnType.String),
                                                new Column("value", ColumnType.Double),
                                                new Column("include", ColumnType.Bool)
                                        )
                        );

        DataSource dataSource = new DataSource()
                .withName("distributed_aggregations")
                .withDataLoader(new EmptyTableDataLoader("distributed_aggregations"))
                .withSchema(
                        schema
                )
                .withDimensions
                        (
                                Arrays.asList
                                        (
                                                new Dimension("customer", Cardinality.Int, schema.getColumn("customer").getType())
                                        )
                        )
                .withDistributionMode(DistributionMode.Striped)
                .withStripingStrategy(new RoundRobinStripingStrategy())
                .withOptions(DataSourceOption.IsIndexed, DataSourceOption.IsReportSource, DataSourceOption.IsWritable);

        serialise(dataSource);
    }

    @Test
    public void setupReport() {
        ReportDefinition reportDefinition = new ReportDefinition("distributed_aggregations_report", "distributed_aggregations_report")
                .withDataSource("distributed_aggregations")
                .withCalculations(
                        new CalculationDefinition("value_included", "if(include, value, 0)")
                )
                .withMeasure
                        (
                                "sum", "sum",
                                new ArrayList<>(),
                                Arrays.asList(
                                        new GroupByNode.SummaryDefinition("value_sum", "sum", "value")
                                ),
                                new ArrayList<>()
                        )
                .withMeasure
                        (
                                "sum_included", "sum_included",
                                new ArrayList<>(),
                                Arrays.asList(
                                        new GroupByNode.SummaryDefinition("value_included_sum", "sum", "value_included")
                                ),
                                new ArrayList<>()
                        )
                .withMeasure
                        (
                                "average", "average",
                                new ArrayList<>(),
                                Arrays.asList(
                                        new GroupByNode.SummaryDefinition("value_avg", "avg", "value")
                                ),
                                new ArrayList<>()
                        )
                .withMeasure
                        (
                                "average_included", "average_included",
                                Arrays.asList(
                                        new CalculationDefinition("avg_included", "if(include, 1, 0)")
                                ),
                                Arrays.asList(
                                        new GroupByNode.SummaryDefinition("avg_included_count", "sum", "avg_included"),
                                        new GroupByNode.SummaryDefinition("value_avg_included", "avg", "value_included", new Object[] { "avg_included_count" })
                                ),
                                new ArrayList<>()
                        )
                .withNodes
                        (
                                new MeasureGroupByNode("group")
                                        .withGroupByColumns("customer")
                                        .withConnection("#input")
                                        .withDistribution()
                        )
                .withOutput("group");
        serialise(reportDefinition);
    }

    private void serialise(Object object) {
        JacksonSerialiser serialiser = new JacksonSerialiser();
        System.out.println(serialiser.serialise(object, true));
    }
}
