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

package io.viewserver.server;

import io.viewserver.authentication.*;
import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.*;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.controller.ControllerCatalog;
import io.viewserver.controller.ControllerJSONCommandHandler;
import io.viewserver.datasource.*;
import io.viewserver.distribution.CoalescorFactory;
import io.viewserver.distribution.DistributionManager;
import io.viewserver.distribution.DistributionOperatorFactory;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.SystemReportExecutor;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.execution.plan.JoinMultiContextHandler;
import io.viewserver.execution.plan.MultiContextHandlerRegistry;
import io.viewserver.execution.plan.UnionGroupMultiContextHandler;
import io.viewserver.execution.plan.UnionTransposeMultiContextHandler;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.network.IEndpoint;
import io.viewserver.network.Network;
import io.viewserver.network.netty.inproc.NettyInProcEndpoint;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.operators.table.RollingTableFactory;
import io.viewserver.operators.table.TableFactoryRegistry;
import io.viewserver.operators.unenum.UnEnumOperatorFactory;
import io.viewserver.reactor.EventLoopReactor;
import io.viewserver.reactor.IReactor;
import io.viewserver.report.ReportContextRegistry;
import io.viewserver.report.ReportDistributor;
import io.viewserver.report.ReportRegistry;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.server.distribution.RegisterSlaveCommandHandler;
import io.viewserver.sql.ExecuteSqlCommandHandler;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewServerMaster extends ViewServerMasterBase {
    private IViewServerMasterConfiguration configuration;

    public ViewServerMaster(String name, IViewServerMasterConfiguration configuration) {
        super(name);
        this.configuration = configuration;
        localStorageDataAdapterFactory = new H2LocalStorageDataAdapterFactory(configuration.getMasterDatabasePath());
    }



    protected void registerFunction(String name, Class<? extends IUserDefinedFunction> function) {
        getServerExecutionContext().getFunctionRegistry().register(name, function);
    }

    protected IViewServerMasterConfiguration getConfiguration() {
        return configuration;
    }

    protected Iterable<IEndpoint> getMasterEndpoints(){
        return configuration.getMasterEndpoints();
    }

}
