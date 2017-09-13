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

package io.viewserver.distribution;

import io.viewserver.command.CommandResult;
import io.viewserver.messages.command.IConfigurateCommand;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.network.IPeerSession;

import java.util.List;

/**
 * Created by nickc on 25/11/2014.
 */
public interface IDistributionManager {
    List<ViewServerNode> getAggregatorNodes();

    void configureNodes(IConfigurateCommand configuratorSpec, CommandResult commandResult, List<ViewServerNode> nodesToConfigure);

    void resetAllNodes(CommandResult commandResult);

    IInitialiseSlaveCommand.Type getNodeType(IPeerSession node);

    IInitialiseSlaveCommand.Type getNodeType();

    int getNumberOfSlaves();

    void addNodeMonitor(INodeMonitor nodeMonitor, boolean catchUp);

    void removeNodeMonitor(INodeMonitor nodeMonitor);

    void setProcessNodeChangesDelay(int processNodeChangesDelay);

    void setMinimumSlaves(int minimumSlaves);

    void initialise();

    void addNode(IPeerSession peerSession, CommandResult commandResult);
}
