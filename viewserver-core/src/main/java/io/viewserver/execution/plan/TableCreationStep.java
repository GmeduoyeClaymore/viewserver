package io.viewserver.execution.plan;

import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.execution.nodes.TableNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.table.ISchemaConfig;

import java.util.List;

public class TableCreationStep implements IExecutionPlanStep<DataSourceExecutionPlanContext> {
    @Override
    public void execute(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        IDataSource dataSource = dataSourceExecutionPlanContext.getDataSource();
        ISchemaConfig schemaConfig = dataSource.getSchema();
        List<IGraphNode> nodes = dataSourceExecutionPlanContext.getGraphNodes();
        if (schemaConfig !=null) {
            nodes.add(new TableNode(DataSource.TABLE_NAME)
                    .withSchemaConfig(schemaConfig));
            // set the output of the data source to be the calculations
            // if a report context specifies no index filters, then this will be the report source
            // otherwise, the output from the index filter will be the report source
            dataSource.setFinalOutput(IDataSourceRegistry.getOperatorPath(dataSource, DataSource.TABLE_NAME));
        }
    }
}





