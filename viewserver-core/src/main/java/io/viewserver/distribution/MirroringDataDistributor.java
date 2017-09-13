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
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.messages.tableevent.ITableEvent;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.IRowSequence;
import io.viewserver.schema.column.IRowFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickc on 09/12/2014.
 */
public class MirroringDataDistributor extends DataDistributorBase implements INodeMonitor {
    private static final Logger log = LoggerFactory.getLogger(MirroringDataDistributor.class);
    private List<ViewServerNode> nodes = new ArrayList<>();
    private List<ViewServerNode> addedNodes = new ArrayList<>();
    private IDistributionManager distributionManager;

    public MirroringDataDistributor(DistributionOperator owner, IDistributionManager distributionManager) {
        super(owner, distributionManager);
        this.distributionManager = distributionManager;

        distributionManager.addNodeMonitor(this, true);
    }

    @Override
    public void onNodeAdded(ViewServerNode node) {
    }

    @Override
    public void onNodeRemoved(ViewServerNode node) {
    }

    @Override
    public void onNodesChanged(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes, CommandResult commandResult) {
        setTableEventsResult(commandResult);

        if (addedNodes.size() > 0) {

            ITableEvent tableEvent = getTableEvent();

            IOutput producer = owner.getInput().getProducer();
            log.debug("Mirroring {} rows in '{}' to {} added nodes", producer.getRowCount(), producer.getOwner().getName(),
                    addedNodes.size());
            IRowSequence allRows = producer.getAllRows();
            int addedNodeCount = addedNodes.size();
            while (allRows.moveNext()) {
                addRowEvent(tableEvent, IRowEvent.Type.Add, allRows.getRowId(), null);
                if (tableEvent.getRowEvents().size() >= 10000) {
                    for (int i = 0; i < addedNodeCount; i++) {
                        sendTableEvent(addedNodes.get(i), tableEvent);
                    }
                    tableEvent.release();
                    tableEvent = getTableEvent();
                }
            }

            for (int i = 0; i < addedNodeCount; i++) {
                ViewServerNode node = addedNodes.get(i);
                sendTableEvent(node, tableEvent);
                nodes.add(node);
            }
            tableEvent.release();
        }

        int removedNodeCount = removedNodes.size();
        for (int i = 0; i < removedNodeCount; i++) {
            nodes.remove(removedNodes.get(i));
        }

        onSendTableEventsComplete();
    }

    private ITableEvent getTableEvent() {
        return MessagePool.getInstance().get(ITableEvent.class)
                .setId(0)
                .setInitSize(0)
                .setExecutionCycle(0);
    }

    @Override
    public void onRowAdd(int rowId) {
        int count = nodes.size();
        if (log.isTraceEnabled()) {
            log.trace("Adding Add row event to {} nodes", count);
        }
        for (int i = 0; i < count; i++) {
            addRowEvent(nodes.get(i), IRowEvent.Type.Add, rowId, null);
        }
    }

    @Override
    public void onRowUpdate(int rowId, IRowFlags rowFlags) {
        int count = nodes.size();
        for (int i = 0; i < count; i++) {
            addRowEvent(nodes.get(i), IRowEvent.Type.Update, rowId, rowFlags);
        }
    }

    @Override
    public void onRowRemove(int rowId) {
        int count = nodes.size();
        for (int i = 0; i < count; i++) {
            addRowEvent(nodes.get(i), IRowEvent.Type.Remove, rowId, null);
        }
    }

    @Override
    public void onAfterCommit() {
        super.onAfterCommit();

        addedNodes.clear();
    }

    @Override
    public void tearDown() {
        distributionManager.removeNodeMonitor(this);

        super.tearDown();
    }
}
