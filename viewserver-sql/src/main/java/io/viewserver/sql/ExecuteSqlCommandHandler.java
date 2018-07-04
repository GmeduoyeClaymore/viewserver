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

package io.viewserver.sql;

import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.parser.SqlParserOptions;
import com.facebook.presto.sql.tree.Query;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.command.MultiCommandResult;
import io.viewserver.command.SubscriptionHandlerBase;
import io.viewserver.command.SubscriptionManager;
import io.viewserver.configurator.Configurator;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.group.summary.SummaryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bemm on 19/11/15.
 */
public class ExecuteSqlCommandHandler extends SubscriptionHandlerBase<IExecuteSqlCommand> {
    private static final Logger log = LoggerFactory.getLogger(ExecuteSqlCommandHandler.class);
    private final SqlParser parser;
    private SummaryRegistry summaryRegistry;

    public ExecuteSqlCommandHandler(SubscriptionManager subscriptionManager,
                                    Configurator configurator, IExecutionPlanRunner executionPlanRunner,
                                    SummaryRegistry summaryRegistry) {
        super(IExecuteSqlCommand.class, subscriptionManager, configurator, executionPlanRunner);
        this.summaryRegistry = summaryRegistry;
        SqlParserOptions options = new SqlParserOptions();
        parser = new SqlParser(options);
    }

    @Override
    protected void handleCommand(Command command, IExecuteSqlCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            final Query query = (Query) parser.createStatement(data.getQuery());

            final MultiCommandResult multiCommandResult = MultiCommandResult.wrap("SubscribeReportHandler", commandResult);
            final CommandResult sqlExecutionPlanResult = multiCommandResult.getResultForDependency("SQL Execution Plan");

            final ICatalog catalog = data.isPermanent() ? peerSession.getSystemCatalog()
                    : peerSession.getSessionCatalog();

            final SqlExecutionPlanContext context = new SqlExecutionPlanContext(query, data.isPermanent());
            context.setCatalog(catalog);
            context.setExecutionContext(peerSession.getExecutionContext());

            final SqlExecutionPlan executionPlan = new SqlExecutionPlan(summaryRegistry, executionPlanRunner);
            executionPlanRunner.executePlan(executionPlan, context, peerSession.getExecutionContext(),
                    catalog, sqlExecutionPlanResult);

            // subscribe
            this.createSubscription(context, command.getId(), peerSession, null);
        } catch (Throwable t) {
            commandResult.setSuccess(false).setMessage(t.getMessage()).setComplete(true);
            log.error("Failed to execute SQL query", t);
        }
    }
}
