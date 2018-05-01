package io.viewserver.execution.plan;

import io.viewserver.Constants;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.nodes.DimensionMapperNode;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.execution.steps.IExecutionPlanStep;

import java.util.List;

public class DataSourceDimensionMappingStep implements IExecutionPlanStep<DataSourceExecutionPlanContext> {
    @Override
    public void execute(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        IDataSource dataSource = dataSourceExecutionPlanContext.getDataSource();
        List<Dimension> dimensions = dataSource.getDimensions();
        List<IGraphNode> nodes = dataSourceExecutionPlanContext.getGraphNodes();


        if (!dimensions.isEmpty()) {
            nodes.add(new DimensionMapperNode(DataSource.DIMENSION_MAP_NAME)
                    .withDimensions(dimensions)
                    .withDataSourceName(dataSource.getName())
                    .withRemoveInputColumns(false)
                    .withConnection(dataSource.getFinalOutput(), Constants.OUT, Constants.IN));
            dataSource.setFinalOutput(IDataSourceRegistry.getOperatorPath(dataSource, DataSource.DIMENSION_MAP_NAME));
        }
    }
}
