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
import io.viewserver.schema.column.*;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * Created by nick on 08/10/15.
 */
public class KeyHashingStripingStrategy extends StripingStrategyBase {
    private String[] stripingColumnNames;

    public KeyHashingStripingStrategy withStripingColumns(String... stripingColumns){
        this.stripingColumnNames = stripingColumns;
        return this;
    }

    public String[] getStripingColumnNames() {
        return stripingColumnNames;
    }

    @Override
    protected ViewServerNode getNewNodeForRow(int rowId) {
        return nodes.get(getHashForRow(rowId) % nodes.size());
    }

    private int getHashForRow(int row) {
        int groupKey;
        if (stripingColumnNames.length == 1) {
            groupKey = getHashComponent(stripingColumnNames[0], row);
        } else {
            int[] components = new int[stripingColumnNames.length];
            for (int i = 0; i < stripingColumnNames.length; i++) {
                components[i] = getHashComponent(stripingColumnNames[i], row);
            }
            groupKey = Arrays.hashCode(components);
        }
        return groupKey;
    }

    private int getHashComponent(String columnName, int row) {
        ColumnHolder columnHolder = dataDistributor.getOwner().getInput().getProducer().getSchema().getColumnHolder(columnName);
        switch (columnHolder.getType()) {
            case Bool: {
                return (((IColumnBool) columnHolder).getBool(row)) ? 1 : 0;
            }
            case NullableBool: {
                return ((IColumnNullableBool)columnHolder).getNullableBool(row).getNumericValue();
            }
            case Byte: {
                return ((IColumnByte)columnHolder).getByte(row);
            }
            case Short: {
                return ((IColumnShort)columnHolder).getShort(row);
            }
            case Int: {
                return ((IColumnInt)columnHolder).getInt(row);
            }
            case Long: {
                return (int) (((IColumnLong) columnHolder).getLong(row));
            }
            case Float: {
                return ((Float)((IColumnFloat)columnHolder).getFloat(row)).hashCode();
            }
            case Double: {
                return ((Double)((IColumnDouble)columnHolder).getDouble(row)).hashCode();
            }
            case String: {
                return ((IColumnString)columnHolder).getString(row).hashCode();
            }
            default: {
                throw new IllegalArgumentException("Cannot group on a column of type " + columnHolder.getType());
            }
        }
    }

    @Override
    protected List<Reallocation> getRowsToReallocate(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes) {
        List<ViewServerNode> addedNodesCopy = new ArrayList<>(addedNodes);
        List<ViewServerNode> removedNodesCopy = new ArrayList<>(removedNodes);

        int oldNodeCount = nodes.size();
        int newNodeCount = nodes.size() - removedNodesCopy.size() + addedNodesCopy.size();

        // same number of nodes -> just add rows to the new nodes
        if (newNodeCount == oldNodeCount) {
            List<Reallocation> reallocations = new ArrayList<>();
            for (int i = 0; i < oldNodeCount; i++) {
                ViewServerNode oldNode = nodes.get(i);
                if (removedNodesCopy.contains(oldNode)) {
                    ViewServerNode newNode = addedNodesCopy.remove(addedNodesCopy.size() - 1);
                    nodes.set(i, newNode);

                    TIntArrayList rows = nodeToRows.remove(oldNode);
                    nodeToRows.put(newNode, rows);

                    Reallocation reallocation = new Reallocation(newNode);
                    reallocations.add(reallocation);
                    rows.forEach(row -> {
                        rowToNode.put(row, newNode);
                        reallocation.rowsToReallocate.add(row);
                        return true;
                    });
                }
            }
            return reallocations;
        }

        nodes.removeAll(removedNodes);
        nodes.addAll(addedNodes);

        int removedNodeCount = removedNodesCopy.size();
        for (int i = 0; i < removedNodeCount; i++) {
            nodeToRows.remove(removedNodesCopy.get(i));
        }

        int addedNodeCount = addedNodesCopy.size();
        for (int i = 0; i < addedNodeCount; i++) {
            nodeToRows.put(addedNodesCopy.get(i), new TIntArrayList());
        }

        Map<ViewServerNode, Reallocation> reallocations = new HashMap<>();
        IRowSequence allRows = dataDistributor.getOwner().getInput().getProducer().getAllRows();
        while (allRows.moveNext()) {
            int rowId = allRows.getRowId();
            ViewServerNode oldNode = rowToNode.get(rowId);
            // TODO: review whether we should cache the hash for each row (particularly for strings)
            ViewServerNode newNode = getNewNodeForRow(rowId);
            if (oldNode != newNode) {
                if (oldNode != null && !removedNodesCopy.contains(oldNode)) {
                    Reallocation reallocation = reallocations.get(oldNode);
                    if (reallocation == null) {
                        reallocation = new Reallocation(oldNode);
                        reallocations.put(oldNode, reallocation);
                    }
                    reallocation.rowsToReallocate.add(-rowId - 1);
                }
                nodeToRows.get(newNode).add(rowId);
                rowToNode.put(rowId, newNode);
                Reallocation reallocation = reallocations.get(newNode);
                if (reallocation == null) {
                    reallocation = new Reallocation(newNode);
                    reallocations.put(newNode, reallocation);
                }
                reallocation.rowsToReallocate.add(rowId);
            }
        }

        return new ArrayList<>(reallocations.values());
    }
}
