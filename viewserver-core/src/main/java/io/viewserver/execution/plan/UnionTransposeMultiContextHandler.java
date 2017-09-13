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

package io.viewserver.execution.plan;

import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.context.ReportContextExecutionPlanContext;

import java.util.List;

/**
 * Created by nick on 16/03/2015.
 */
public class UnionTransposeMultiContextHandler implements IMultiContextHandler {
    private final IDistributionManager distributionManager;
    private final ExecutionPlanRunner executionPlanRunner;

    public UnionTransposeMultiContextHandler(IDistributionManager distributionManager,
                                             ExecutionPlanRunner executionPlanRunner) {
        this.distributionManager = distributionManager;
        this.executionPlanRunner = executionPlanRunner;
    }

    @Override
    public IExecutionPlan createExecutionPlan() {
        return new MultiContextTransposeExecutionPlan();
    }

    @Override
    public ReportContextExecutionPlanContext createExecutionPlanContext(ReportContext reportContext,
                                                                        List<IExecutionPlanContext> childContexts) {
        MultiContextExecutionPlanContext multiContextExecutionPlanContext = new MultiContextExecutionPlanContext();
        multiContextExecutionPlanContext.setReportContext(reportContext);
        multiContextExecutionPlanContext.setExecutionPlanContexts(childContexts);
        multiContextExecutionPlanContext.setDistributionManager(distributionManager);
        multiContextExecutionPlanContext.setExecutionPlanRunner(executionPlanRunner);

        return multiContextExecutionPlanContext;
    }
}
