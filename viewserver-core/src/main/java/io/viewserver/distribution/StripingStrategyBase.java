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
import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.reactor.ITask;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 09/12/2014.
 */
public abstract class StripingStrategyBase implements IStripingStrategy {
    private static final Logger log = LoggerFactory.getLogger(StripingStrategyBase.class);
    protected List<ViewServerNode> nodes = new ArrayList<>();
//    protected List<ViewServerNode> addedNodes = new ArrayList<>();
//    protected List<ViewServerNode> removedNodes = new ArrayList<>();
    protected Map<ViewServerNode, TIntArrayList> nodeToRows = new HashMap<>();
    protected TIntObjectHashMap<ViewServerNode> rowToNode = new TIntObjectHashMap<>();
    protected IDataDistributor dataDistributor;

    @Override
    public void initialise(IDataDistributor dataDistributor) {
        this.dataDistributor = dataDistributor;
    }

    @Override
    public void onNodeAdded(ViewServerNode node) {
//        if (node.getType().equals(InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.SlaveType.AGGREGATOR)) {
//            if (nodes.size() == 0) {
//                log.debug("Node {} added as first node", node);
//                nodes.add(node);
//                nodeToRows.put(node, new TIntArrayList());
//                dataDistributor.reset();
//            } else {
//                log.debug("Node {} added", node);
//                addedNodes.add(node);
//            }
//        }
    }

    @Override
    public void onNodeRemoved(ViewServerNode node) {
//        if (node.getType().equals(InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.SlaveType.AGGREGATOR)) {
//            log.debug("Node {} removed", node);
//            removedNodes.add(node);
//        }
    }

    @Override
    public void onNodesChanged(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes, CommandResult commandResult) {
//        if (nodes.isEmpty()) {
//            dataDistributor.reset();
//        }
        reallocateRows(addedNodes, removedNodes, commandResult);
    }

    @Override
    public ViewServerNode getNodeForRow(int rowId) {
        if (nodes.size() == 0) {
            return null;
        }

        ViewServerNode node = rowToNode.get(rowId);
        if (node == null) {
            node = getNewNodeForRow(rowId);
            nodeToRows.get(node).add(rowId);
            rowToNode.put(rowId, node);
        }
        return node;
    }

    protected abstract ViewServerNode getNewNodeForRow(int rowId);

    @Override
    public void reallocateRows(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes, CommandResult commandResult) {
        // no changes - do nothing!
//        if (this.removedNodes.size() == 0 && this.addedNodes.size() == 0) {
//            return;
//        }

        // no nodes left...aaaarrrgh!
        if (removedNodes.size() == nodes.size() && addedNodes.size() == 0) {
            nodes.clear();
            nodeToRows.clear();
            rowToNode.clear();
            commandResult.setSuccess(true).setComplete(true);
            return;
        }

        try {
            List<Reallocation> rowsToReallocate = getRowsToReallocate(addedNodes, removedNodes);
            dataDistributor.getOwner().getExecutionContext().getReactor().scheduleTask(new ReallocationBatcher(rowsToReallocate, commandResult), 1, -1);
        } catch (Exception e) {
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }

    protected abstract List<Reallocation> getRowsToReallocate(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes);

    protected class Reallocation {
        protected final ViewServerNode node;
        protected final TIntArrayList rowsToReallocate = new TIntArrayList();

        public Reallocation(ViewServerNode node) {
            this.node = node;
        }
    }

    private class ReallocationBatcher implements ITask {
        private static final int BATCH_SIZE = 10000;
        private final List<Reallocation> reallocations;
        private final CommandResult commandResult;
        private int rowsReallocated = 0;

        public ReallocationBatcher(List<Reallocation> reallocations, CommandResult commandResult) {
            this.reallocations = reallocations;
            this.commandResult = commandResult;
        }

        @Override
        public void execute() {
            CommandResult batchResult = new CommandResult();
            batchResult.setListener(res -> dataDistributor.getOwner().getExecutionContext().getReactor().scheduleTask(this, 1, -1));
            dataDistributor.setTableEventsResult(batchResult);

            int nodeCount = reallocations.size();
            boolean pending = false;
            for (int i = 0; i < nodeCount; i++) {
                Reallocation reallocation = reallocations.get(i);
                if (reallocation.rowsToReallocate.size() < rowsReallocated) {
                    continue;
                }
                pending = true;
                int rowsToReallocateThisTime = Math.min(reallocation.rowsToReallocate.size() - rowsReallocated, BATCH_SIZE);
                for (int j = 0; j < rowsToReallocateThisTime; j++) {
                    int rowId = reallocation.rowsToReallocate.get(rowsReallocated + j);
                    if (rowId < 0) {
                        dataDistributor.addRowEvent(reallocation.node, IRowEvent.Type.Remove, -rowId - 1, null);
                    } else {
                        dataDistributor.addRowEvent(reallocation.node, IRowEvent.Type.Add, rowId, null);
                    }
                }
            }
            rowsReallocated += BATCH_SIZE;

            if (pending) {
                dataDistributor.sendTableEvents();
            } else {
                commandResult.setSuccess(true).setComplete(true);
            }
        }
    }
}
