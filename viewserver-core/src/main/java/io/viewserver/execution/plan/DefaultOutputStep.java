package io.viewserver.execution.plan;

import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.filter.FilterOperator;

import java.util.List;

public class DefaultOutputStep implements IExecutionPlanStep<DataSourceExecutionPlanContext> {
    @Override
    public void execute(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        IDataSource dataSource = dataSourceExecutionPlanContext.getDataSource();
        List<IGraphNode> nodes = dataSourceExecutionPlanContext.getGraphNodes();
        nodes.add(
                new FilterNode(DataSource.DEFAUT_NAME).
                        withMode(FilterOperator.FilterMode.Transparent).
                        withExpression("1==1").withConnection(dataSourceExecutionPlanContext.getDataSource().getFinalOutput()));
    }
}

