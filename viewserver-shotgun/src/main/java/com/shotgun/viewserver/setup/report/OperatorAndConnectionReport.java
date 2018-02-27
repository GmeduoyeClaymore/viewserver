package com.shotgun.viewserver.setup.report;

import io.viewserver.Constants;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class OperatorAndConnectionReport {
    public static final String ID = "operatorsAndConnections";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, ID)
                .withParameter("operatorPath", "OperatorName", String[].class)
                .withParameter("operatorPathField", "OperatorName", String[].class)
                .withNodes(
                        new JoinNode("operatorInputJoin")
                                .withLeftJoinColumns("{operatorPathField}")
                                .withRightJoinColumns("inputOperator")
                                .withConnection("{operatorPath}", Constants.OUT, "left")
                                .withConnection("connections", Constants.OUT, "right")
                                .withColumnPrefixes("", "input_")
                                .withAlwaysResolveNames(),
                        new JoinNode("operatorOutputJoin")
                                .withLeftJoinColumns("{operatorPathField}")
                                .withRightJoinColumns("outputOperator")
                                .withConnection("operatorInputJoin", Constants.OUT, "left")
                                .withConnection("connections", Constants.OUT, "right")
                                .withColumnPrefixes("", "output_")
                                .withAlwaysResolveNames(),
                        new ProjectionNode("projectionNode")
                                .withMode(IProjectionConfig.ProjectionMode.Projection)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("{operatorPathField}", "nodeName")
                                )
                                .withConnection("operatorOutputJoin")

                )

                .withOutput("projectionNode");
    }
}
