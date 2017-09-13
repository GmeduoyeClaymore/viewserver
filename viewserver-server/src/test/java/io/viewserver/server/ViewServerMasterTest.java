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

import io.viewserver.command.CommandResult;
import io.viewserver.distribution.INodeMonitor;
import io.viewserver.distribution.ViewServerNode;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ViewServerMasterTest extends ViewServerMaster implements INodeMonitor {
    private CountDownLatch nodeAddedLatch;
    public ViewServerMasterTest(String name, IViewServerMasterConfiguration configuration) {
        super(name, configuration);
    }

    @Override
    protected void initialise() {
        super.initialise();

        distributionManager.addNodeMonitor(this, true);
        distributionManager.setProcessNodeChangesDelay(0);
        distributionManager.setMinimumSlaves(2);
    }

    public void waitForNodes(int count) {
        nodeAddedLatch = new CountDownLatch(count);
        try {
            nodeAddedLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNodeAdded(ViewServerNode node) {
        if (nodeAddedLatch != null) {
            nodeAddedLatch.countDown();
        }
    }

    @Override
    public void onNodeRemoved(ViewServerNode node) {

    }

    @Override
    public void onNodesChanged(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes, CommandResult commandResult) {
        commandResult.setSuccess(true).setComplete(true);
    }
}
