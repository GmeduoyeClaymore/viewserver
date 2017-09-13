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

import io.viewserver.operators.IRowSequence;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 09/12/2014.
 */
public class RoundRobinStripingStrategy extends StripingStrategyBase {
    private int lastNode = -1;

    @Override
    protected ViewServerNode getNewNodeForRow(int rowId) {
        lastNode++;
        if (lastNode == nodes.size()) {
            lastNode = 0;
        }
        return nodes.get(lastNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    protected List<Reallocation> getRowsToReallocate(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes) {
        Map<ViewServerNode, Reallocation> reallocations = new HashMap<>();

        // special case where we're removing and adding the same number of nodes
        if (removedNodes.size() == addedNodes.size()) {
            for (int i = 0; i < removedNodes.size(); i++) {
                ViewServerNode removedNode = removedNodes.get(i);
                TIntArrayList rowsRemoved = nodeToRows.remove(removedNode);
                if (rowsRemoved == null) {
                    continue;
                }

                ViewServerNode addedNode = addedNodes.get(i);
                Reallocation reallocation = new Reallocation(addedNode);
                reallocations.put(addedNode, reallocation);
                nodeToRows.put(addedNode, rowsRemoved);
                rowsRemoved.forEach(new TIntProcedure() {
                    @Override
                    public boolean execute(int rowId) {
                        rowToNode.put(rowId, addedNode);

                        // now process the row add on the distributor
                        reallocation.rowsToReallocate.add(rowId);
                        return true;
                    }
                });
            }

            nodes.removeAll(removedNodes);
            nodes.addAll(addedNodes);
            return new ArrayList<>(reallocations.values());
        }

        int totalRows = nodes.size() != 0 ? rowToNode.size() : dataDistributor.getOwner().getInput().getProducer().getRowCount();
        int oldRowsPerNode = nodes.size() != 0 ? totalRows / nodes.size() : -1;
        int targetRowsPerNode = totalRows / (nodes.size() - removedNodes.size() + addedNodes.size());

        int addedNodeCount = addedNodes.size();
        for (int i = 0; i < addedNodeCount; i++) {
            nodeToRows.put(addedNodes.get(i), new TIntArrayList(targetRowsPerNode));
        }

        TIntArrayList rowsToReallocate = new TIntArrayList();
        int removedNodeCount = removedNodes.size();
        for (int i = 0; i < removedNodeCount; i++) {
            rowsToReallocate.addAll(nodeToRows.remove(removedNodes.get(i)));
        }

        List<ViewServerNode> nodesToAddRowsTo;
        int rowsToAddToEachNode;
        if (addedNodeCount > removedNodeCount) {
            // remove rows from the old nodes to bring them down to the target number of rows
            int rowsToTakeFromEachNode = oldRowsPerNode - targetRowsPerNode;
            int count = nodes.size();
            if (count > 0) {
                for (int j = 0; j < count; j++) {
                    ViewServerNode node = nodes.get(j);
                    if (!removedNodes.contains(node)) {
                        Reallocation reallocation = new Reallocation(node);
                        reallocations.put(node, reallocation);
                        TIntArrayList rowsInNode = nodeToRows.get(node);
                        for (int i = 0; i < rowsToTakeFromEachNode; i++) {
                            int rowId = rowsInNode.removeAt(rowsInNode.size() - 1);
                            rowsToReallocate.add(rowId);
                            reallocation.rowsToReallocate.add(-rowId - 1);
                        }
                    }
                }
            } else {
                IRowSequence allRows = dataDistributor.getOwner().getInput().getProducer().getAllRows();
                while (allRows.moveNext()) {
                    rowsToReallocate.add(allRows.getRowId());
                }
            }

            // now round-robin the rows between the new nodes
            rowsToAddToEachNode = rowsToReallocate.size() / addedNodes.size();
            nodesToAddRowsTo = addedNodes;
        } else {
            // allocate the target number of rows to each of the new nodes
            for (int j = 0; j < addedNodeCount; j++) {
                ViewServerNode node = addedNodes.get(j);
                Reallocation reallocation = new Reallocation(node);
                reallocations.put(node, reallocation);
                TIntArrayList rowsInNode = nodeToRows.get(node);
                for (int i = 0; i < targetRowsPerNode; i++) {
                    int rowId = rowsToReallocate.removeAt(rowsToReallocate.size() - 1);
                    rowsInNode.add(rowId);
                    rowToNode.put(rowId, node);
                    reallocation.rowsToReallocate.add(rowId);
                }
            }

            // now round-robin the remaining rows between the old nodes
            nodesToAddRowsTo = new ArrayList<>(nodes);
            nodesToAddRowsTo.removeAll(removedNodes);
            rowsToAddToEachNode = rowsToReallocate.size() / nodesToAddRowsTo.size();
        }

        for (int nodeId = 0; nodeId < nodesToAddRowsTo.size(); nodeId++) {
            ViewServerNode node = nodesToAddRowsTo.get(nodeId);
            Reallocation reallocation = reallocations.get(node);
            if (reallocation == null) {
                reallocation = new Reallocation(node);
                reallocations.put(node, reallocation);
            }
            TIntArrayList rowsInNode = nodeToRows.get(node);
            int rowsToAddToThisNode = (nodeId == nodesToAddRowsTo.size() - 1) ? rowsToReallocate.size() : rowsToAddToEachNode;
            for (int i = 0; i < rowsToAddToThisNode; i++) {
                int rowId = rowsToReallocate.removeAt(rowsToReallocate.size() - 1);
                rowsInNode.add(rowId);
                rowToNode.put(rowId, node);
                reallocation.rowsToReallocate.add(rowId);
            }
        }

        nodes.removeAll(removedNodes);
        nodes.addAll(addedNodes);
        return new ArrayList<>(reallocations.values());
    }
}
