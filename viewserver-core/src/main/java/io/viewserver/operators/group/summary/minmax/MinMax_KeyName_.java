// :_KeyName_=Byte,_KeyType_=byte,_LongKeyName_=Byte;_KeyName_=Short,_KeyType_=short,_LongKeyName_=Short;_KeyName_=Int,_KeyType_=int,_LongKeyName_=Integer;_KeyName_=Long,_KeyType_=long,_LongKeyName_=Long;_KeyName_=Float,_KeyType_=float,_LongKeyName_=Float;_KeyName_=Double,_KeyType_=double,_LongKeyName_=Double

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

package io.viewserver.operators.group.summary.minmax;

import io.viewserver.operators.group.ISummary;
import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.schema.column.*;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;

/**
 * Created by nick on 13/03/2015.
 */
public class MinMax_KeyName_ implements ISummary {
    private final String name;
    private final String column;
    private boolean isMinimum;
    private int heapSize;
    private ColumnHolder summaryColumn;
    private ColumnHolder_KeyName_ columnHolder;
    private ISummaryContext context;
    private ArrayList<GroupHeap> groupHeaps = new ArrayList<>();
    private BitSet dirtyGroups = new BitSet();

    public MinMax_KeyName_(String name, String column, boolean isMinimum, int heapSize) {
        this.name = name;
        this.column = column;
        this.isMinimum = isMinimum;
        this.heapSize = heapSize;
    }

    @Override
    public void initialise(ISummaryContext context) {
        this.context = context;
        this.summaryColumn = ColumnHolderUtils.createColumnHolder(name, getType());
        context.getTableStorage().initialiseColumn(summaryColumn);
        context.setResultColumn(summaryColumn);
        columnHolder = (ColumnHolder_KeyName_) context.getInboundSchema().getColumnHolder(column);
        ((IWritableColumn)columnHolder.getColumn()).storePreviousValues();
    }

    @Override
    public void onGroupAdd(int groupId) {
        GroupHeap heap = new GroupHeap(groupId);
        if (groupHeaps.size() > groupId) {
            groupHeaps.set(groupId, heap);
        } else {
            groupHeaps.add(heap);
        }
    }

    @Override
    public void onGroupRemove(int groupId) {
        groupHeaps.set(groupId, null);
        clearHeapDirty(groupId);
    }

    @Override
    public void onGroupEnter(int groupId, int rowId) {
        GroupHeap heap = groupHeaps.get(groupId);
        _KeyType_ value = columnHolder.get_KeyName_(rowId);
        heap.addValue(value);
        markHeapDirty(groupId);
    }

    @Override
    public void onGroupLeave(int groupId, int rowId) {
        GroupHeap heap = groupHeaps.get(groupId);
        _KeyType_ value = columnHolder.getPrevious_KeyName_(rowId);
        heap.removeValue(value);
        markHeapDirty(groupId);
    }

    @Override
    public void onRowUpdate(int groupId, int rowId) {
        onGroupLeave(groupId, rowId);
        onGroupEnter(groupId, rowId);
    }

    @Override
    public void onAfterCommit() {
        for (int groupId = dirtyGroups.nextSetBit(0); groupId >= 0; groupId = dirtyGroups.nextSetBit(groupId + 1)) {
            GroupHeap heap = groupHeaps.get(groupId);
            heap.onAfterCommit();
        }
        dirtyGroups.clear();
    }

    @Override
    public boolean hasChanges(IRowFlags rowFlags) {
        return false;
    }

    @Override
    public void reset() {
        ((IWritableColumn_KeyName_)summaryColumn.getColumn()).resetAll();
        groupHeaps.clear();
        dirtyGroups.clear();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean supportsPreviousValues() {
        return columnHolder.supportsPreviousValues();
    }

    @Override
    public ColumnType getType() {
        return ColumnType._KeyName_;
    }

    private void markHeapDirty(int groupId) {
        dirtyGroups.set(groupId);
    }

    private void clearHeapDirty(int groupId) {
        dirtyGroups.clear(groupId);
    }

    private class GroupHeap {
        private MinMaxPriorityQueue<_LongKeyName_> queue = MinMaxPriorityQueue.orderedBy(isMinimum ? Comparator.<_LongKeyName_>naturalOrder() : Comparator.<_LongKeyName_>reverseOrder()).create();
        private int groupId;

        private GroupHeap(int groupId) {
            this.groupId = groupId;
        }

        public void addValue(_KeyType_ value) {
            queue.add(value);
        }

        public void removeValue(_KeyType_ value) {
            queue.remove(value);
        }

        public void onAfterCommit() {
            if (queue.isEmpty()) {
                rescan();
            }

            if (!queue.isEmpty()) {
                ((IWritableColumn_KeyName_) summaryColumn.getColumn()).set_KeyName_(groupId, queue.peek());
                context.markDirty(groupId);
            }
        }

        private void rescan() {
        }
    }
}
