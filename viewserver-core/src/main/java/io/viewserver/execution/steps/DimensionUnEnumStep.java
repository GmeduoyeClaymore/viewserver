package io.viewserver.execution.steps;

import io.viewserver.Constants;
import io.viewserver.execution.context.DimensionExecutionPlanContext;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.nodes.UnEnumNode;

public class DimensionUnEnumStep implements IExecutionPlanStep<DimensionExecutionPlanContext>{
    @Override
    public void execute(DimensionExecutionPlanContext dimensionExecutionPlanContext) {

        UnEnumNode userUnEnum = new UnEnumNode("dimensionsUnEnum" , dimensionExecutionPlanContext.getDataSource())
                .withMetadata("isUserExecutionPlanNode", true)
                .withConnection(dimensionExecutionPlanContext.getInputOperator(), dimensionExecutionPlanContext.getInputOutputName(), Constants.IN);


        dimensionExecutionPlanContext.addNodes(userUnEnum);
        dimensionExecutionPlanContext.setInput(userUnEnum.getName());
    }
}
