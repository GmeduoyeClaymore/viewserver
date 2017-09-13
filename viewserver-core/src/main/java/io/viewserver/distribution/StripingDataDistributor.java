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

import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.schema.column.IRowFlags;

/**
 * Created by nickc on 09/12/2014.
 */
public class StripingDataDistributor extends DataDistributorBase {
    private IDistributionManager distributionManager;
    private final IStripingStrategy stripingStrategy;

    public StripingDataDistributor(DistributionOperator owner, IDistributionManager distributionManager, IStripingStrategy stripingStrategy) {
        super(owner, distributionManager);
        this.distributionManager = distributionManager;
        this.stripingStrategy = stripingStrategy;

        stripingStrategy.initialise(this);
        distributionManager.addNodeMonitor(stripingStrategy, true);
    }

//    @Override
//    public void onBeforeCommit() {
//        // rejig everything according to added/removed nodes
//        stripingStrategy.reallocateRows(addedNodes, removedNodes, commandResult);
//
//        super.onBeforeCommit();
//    }

    @Override
    public void onRowAdd(int rowId) {
        ViewServerNode node = stripingStrategy.getNodeForRow(rowId);
        if (node != null) {
            addRowEvent(node, IRowEvent.Type.Add, rowId, null);
        }
    }

    @Override
    public void onRowUpdate(int rowId, IRowFlags rowFlags) {
        ViewServerNode node = stripingStrategy.getNodeForRow(rowId);
        if (node != null) {
            addRowEvent(node, IRowEvent.Type.Update, rowId, rowFlags);
        }
    }

    @Override
    public void onRowRemove(int rowId) {
        ViewServerNode node = stripingStrategy.getNodeForRow(rowId);
        if (node != null) {
            addRowEvent(node, IRowEvent.Type.Remove, rowId, null);
        }
    }

    @Override
    public void tearDown() {
        distributionManager.removeNodeMonitor(stripingStrategy);

        super.tearDown();
    }
}
