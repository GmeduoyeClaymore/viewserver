package com.shotgun.viewserver.setup.report;

import io.viewserver.Constants;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class OperatorAndConnectionReport {
    public static final String ID = "operatorsAndConnections";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, ID)
                .withParameter("operatorPath", "OperatorName", String[].class)
                .withParameter("operatorPathField", "OperatorName", String[].class)
                .withParameter("operatorPathPrefix", "Operator Path Prefic", String[].class)
                .withNodes(
                        new CalcColNode("mainOperatorCalc")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("nodeName", "\"{operatorPathPrefix}\" + {operatorPathField}"))
                                .withConnection("{operatorPath}"),
                            new JoinNode("operatorInputJoin")
                                .withLeftJoinColumns("nodeName")
                                .withRightJoinColumns("inputOperator")
                                .withLeftJoinOuter()
                                .withConnection("mainOperatorCalc", Constants.OUT, "left")
                                .withConnection("connections", Constants.OUT, "right")
                                .withColumnPrefixes("", "input_")
                                .withAlwaysResolveNames(),
                            new JoinNode("operatorOutputJoin")
                                .withLeftJoinColumns("nodeName")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("outputOperator")
                                .withConnection("operatorInputJoin", Constants.OUT, "left")
                                .withConnection("connections", Constants.OUT, "right")
                                .withColumnPrefixes("", "output_")
                                .withAlwaysResolveNames()

                )

                .withOutput("operatorOutputJoin");
    }
}
