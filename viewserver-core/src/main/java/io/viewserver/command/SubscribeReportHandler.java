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

package io.viewserver.command;

import io.viewserver.catalog.ICatalog;
import io.viewserver.configurator.Configurator;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.SystemReportExecutor;
import io.viewserver.execution.context.ReportContextExecutionPlanContext;
import io.viewserver.messages.command.ISubscribeReportCommand;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.report.ReportContextRegistry;
import io.viewserver.report.ReportRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscribeReportHandler extends ReportContextHandler<ISubscribeReportCommand> {
    private static final Logger log = LoggerFactory.getLogger(SubscribeReportHandler.class);
    private final SystemReportExecutor systemReportExecutor;
    private ReportContextRegistry reportContextRegistry;

    public SubscribeReportHandler(DimensionMapper dimensionMapper, IDataSourceRegistry dataSourceRegistry,
                                  ReportRegistry reportRegistry, SubscriptionManager subscriptionManager,
                                  IDistributionManager distributionManager,
                                  Configurator configurator,
                                  ExecutionPlanRunner executionPlanRunner, ReportContextRegistry reportContextRegistry,
                                  SystemReportExecutor systemReportExecutor) {
        super(ISubscribeReportCommand.class, dimensionMapper, dataSourceRegistry, reportRegistry, subscriptionManager, distributionManager, configurator, executionPlanRunner);
        this.reportContextRegistry = reportContextRegistry;
        this.systemReportExecutor = systemReportExecutor;
    }

    @Override
    protected void handleCommand(Command command, ISubscribeReportCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            ReportContextExecutionPlanContext activeExecutionPlanContext;

            MultiCommandResult multiCommandResult = MultiCommandResult.wrap("SubscribeReportHandler", commandResult);
            CommandResult systemExecutionPlanResult = multiCommandResult.getResultForDependency("System Execution Plan");
            CommandResult userExecutionPlanResult = multiCommandResult.getResultForDependency("User execution plan");

            ReportContext reportContext = ReportContext.fromMessage(data.getReportContext());
            Options options = Options.fromMessage(data.getOptions());
            substituteParamValues(peerSession, reportContext, options);

            log.trace("Subscribe command for context: {}\nOptions: {}", reportContext, options);

            final ICatalog graphNodesCatalog = getGraphNodesCatalog(peerSession);

//            long start = System.nanoTime();
            activeExecutionPlanContext = systemReportExecutor.executeContext(reportContext,
                    peerSession.getExecutionContext(),
                    graphNodesCatalog,
                    systemExecutionPlanResult,
                    "", new CommandResult(), null);

            //converts int values back to strings

            reportContextRegistry.registerContext(activeExecutionPlanContext);

            //for sorting, paging etc
            String inputOperator = activeExecutionPlanContext.getInputOperator();
            if (inputOperator.charAt(0) != '/') {
                inputOperator = graphNodesCatalog.getOperator(inputOperator).getPath();
                activeExecutionPlanContext.setInput(inputOperator, activeExecutionPlanContext.getInputOutputName());
            }
            this.runUserExecutionPlan(activeExecutionPlanContext, options, command.getId(), peerSession, userExecutionPlanResult);

            // subscribe
            this.createSubscription(activeExecutionPlanContext, command.getId(), peerSession, options);
        } catch (Throwable ex) {
            commandResult.setSuccess(false).setMessage(ex.getMessage()).setComplete(true);
            log.error("Failed to subscribe to report", ex);
        }
    }

    private void substituteParamValues(IPeerSession peerSession, ReportContext reportContext, Options options) {
        ValueLists.StringList stringList = new ValueLists.StringList();
        stringList.add(peerSession.getCatalogName());
        reportContext.setParameterValue("@catalogName", stringList);

        ConcurrentHashMap<String, Object> params = ControllerContext.getParams(peerSession);
        if(params != null){
            for(Map.Entry<String,Object> entry : params.entrySet()){
                stringList = new ValueLists.StringList();
                stringList.add(entry.getValue().toString());
                reportContext.setParameterValue("@" + entry.getKey(), stringList);
            }

            for (ReportContext.DimensionValue str : reportContext.getDimensionValues()) {
                int index = 0;
                for(Object val : str.getValues().toArray()){
                    if(val instanceof String && val.toString().startsWith("@")){
                        log.info(String.format("Found controller context param %s in report context for dimension %s. Looking for value in controller context to replace it",val,str.getName()));
                        String parameterName = val.toString().substring(1);
                        Object contextValue = params.get(parameterName);
                        if(contextValue != null){
                            log.info(String.format("Found controller context value %s for %s in controller context",contextValue,val));
                            setValueAtIndex(contextValue, index, str.getValues());
                        }else{
                            log.info(String.format("Unable to find controller context value for %s in controller context",val));
                        }
                    }
                    index++;
                }
            }

        }

        SubscriptionUtils.substituteParamsInFilterExpression(params, options);
    }


    private void setValueAtIndex(Object contextValue, int index, ValueLists.IValueList values) {
        if(!(values instanceof ValueLists.IStringList)){
            throw new RuntimeException(String.format("Unable to apply controller context param to value list of type %s. The value list should be of type StringValueList",contextValue));
        }
        ((ValueLists.IStringList)values).add(index, contextValue.toString());
    }
}
