package io.viewserver.execution;

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.plan.IExecutionPlan;

public interface IExecutionPlanRunner {
    <TContext extends IExecutionPlanContext> void executePlan(IExecutionPlan<TContext> executionPlan, TContext context,
                                                              IExecutionContext executionContext, ICatalog catalog, CommandResult commandResult);
}
