package com.shotgun.viewserver.setup.report;

import io.viewserver.Constants;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.report.ReportDefinition;

public class OperatorAndConnectionReport {
    public static final String ID = "operatorsAndConnections";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, ID)
                .withRequiredParameter("operatorPath", "OperatorName", String[].class)
                .withRequiredParameter("@catalogName", "@catalogName", String[].class)
                .withRequiredParameter("operatorPathField", "OperatorName", String[].class)
                .withRequiredParameter("operatorPathPrefix", "Operator Path Prefic", String[].class)
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
                                .withAlwaysResolveNames(),
                            new FilterNode("filterOutThisSession")
                                    .withExpression("!(input_outputOperator like \"*{@catalogName}*\") && !(input_inputOperator like \"*{@catalogName}*\") && !(output_outputOperator like \"*{@catalogName}*\") && !(output_inputOperator like \"*{@catalogName}*\")")
                                    .withMode(FilterOperator.FilterMode.Filter)
                        .withConnection("operatorOutputJoin")


                )

                .withOutput("filterOutThisSession");
    }
}
