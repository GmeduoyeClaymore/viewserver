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

package io.viewserver.changequeue;

import io.viewserver.collections.IntHashSet;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.Status;
import io.viewserver.schema.column.IRowFlags;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by nickc on 30/09/2014.
 */
public class ChangeQueue implements IChangeQueue {
    private IntHashSet rows;

    private static final int Add = 0;
    private static final int Update = 1;
    private static final int Remove = 2;
    private static final int OpCount = 3;
    private BitSet operations;
    private final TIntObjectHashMap<BitSet> dirtyFlags;
    private final BitSet dirtyColumns;
    private final BitSet columnsWithDirty = new BitSet();
    private final TIntHashSet addedRows;
    private final TIntArrayList queue;
    private IOutput owner;
    private List<Status> statuses;
    private boolean hasUpdates;
    private boolean referenceCountingEnabled;
    private TIntIntHashMap referenceCounts;
    private int isRowAddedCacheRow = -1;
    private boolean isRowAddedCacheResponse;

    public ChangeQueue(IOutput owner) {
        this.owner = owner;
        rows = new IntHashSet(128, 0.75f, -1);
        operations = new BitSet();
        queue = new TIntArrayList(128, -1);
        dirtyFlags = new TIntObjectHashMap<>(8);
        dirtyColumns = new BitSet();
        statuses = new ArrayList<>();
        addedRows = new TIntHashSet();
    }

    public IOutput getOwner() {
        return owner;
    }

    @Override
    public List<Status> getStatuses() {
        return statuses;
    }

    @Override
    public void handleStatus(Status status) {
        queue.add(-status.getId());
        statuses.add(status);
    }

    @Override
    public void ensureCapacity(int capacity) {
        rows = new IntHashSet(capacity, 0.75f, -1);
    }

    @Override
    public void handleAdd(int row) {
        boolean isNew = rows.add(row);
        int operationsOffset = row * OpCount;
        if (isNew) {
            queue.add(row);
            addedRows.add(row);
        } else {
            if (operations.get(operationsOffset + Update)) {
                throw new IllegalStateException("Received add for row " + row + " when already flagged for update");
            }
        }
        operations.set(operationsOffset + Add);
    }

    @Override
    public void handleUpdate(int row) {
        boolean isNew = rows.add(row);
        if (isNew) {
            queue.add(row);
            int operationsOffset = row * OpCount;
            operations.set(operationsOffset + Update);
            hasUpdates = true;
        }
    }

    @Override
    public void handleRemove(int row) {
        boolean isNew = rows.add(row);
        if (isNew) {
            queue.add(row);
        }

        int operationsOffset = row * OpCount;
//        operations = operations.andNot(EWAHCompressedBitmap.bitmapOf(operationsOffset + Update));
        operations.clear(operationsOffset + Update);

        boolean added = operations.get(operationsOffset + Add);
        if (added) {
//            operations = operations.andNot(EWAHCompressedBitmap.bitmapOf(operationsOffset + Add));
            operations.clear(operationsOffset + Add);
        } else {
            operations.set(operationsOffset + Remove);
        }
    }

    @Override
    public Cursor createCursor() {
        return new Cursor(this);
    }

    @Override
    public boolean getNext(Cursor cursor) {
        while (cursor.advance) {
            if (cursor.queuePtr >= queue.size()) {
                return false;
            }

            int statusOrRowId = queue.get(cursor.queuePtr++);
            if (statusOrRowId < 0) {
                cursor.rowId = -1;
                cursor.operation = Operation.Status;
                cursor.status = Status.getStatus(-statusOrRowId);
                cursor.advance = true;
                return true;
            } else {
                cursor.rowId = statusOrRowId;
                int operationsOffset = cursor.rowId * OpCount;
                cursor.doAdd = operations.get(operationsOffset + Add);
                cursor.doUpdate = operations.get(operationsOffset + Update);
                cursor.doRemove = operations.get(operationsOffset + Remove);
                cursor.advance = !(cursor.doAdd || cursor.doUpdate || cursor.doRemove);
            }
        }

        if (cursor.doRemove) {
            cursor.doRemove = false;
            cursor.operation = Operation.Remove;
            cursor.advance = !(cursor.doAdd || cursor.doUpdate);
            return true;
        }

        if (cursor.doAdd) {
            cursor.doAdd = false;
            cursor.operation = Operation.Add;
            cursor.advance = !cursor.doUpdate;
            return true;
        }

        if (cursor.doUpdate) {
            cursor.doUpdate = false;
            cursor.operation = Operation.Update;
            cursor.advance = true;
            return true;
        }

        return false;
    }

    @Override
    public void clear() {
        if (hasChanges()) {
            rows.clear();
            operations.clear();
            queue.resetQuick();
            addedRows.clear();
            hasUpdates = false;
            isRowAddedCacheRow = -1;
            statuses.clear();
        }
        dirtyFlags.clear();
        dirtyColumns.clear();
        columnsWithDirty.clear();
    }

    @Override
    public void markColumnDirty(int columnId) {
        dirtyColumns.set(columnId);
        columnsWithDirty.set(columnId);
    }

    @Override
    public void markDirty(int rowId, int columnId) {
        if (dirtyColumns.get(columnId) || isRowAdded(rowId)) {
            // whole column or row is dirty - nothing to do!
            return;
        }

        BitSet dirtyFlags = this.dirtyFlags.get(columnId);
        if (dirtyFlags == null) {
            dirtyFlags = new BitSet();
            dirtyFlags.set(rowId);
            this.dirtyFlags.put(columnId, dirtyFlags);
        } else {
            dirtyFlags.set(rowId);
        }

        columnsWithDirty.set(columnId);
    }

    @Override
    public boolean isDirty(int rowId, int columnId) {
        if (rowId == -1) {
            return false;
        }

        if (isColumnDirty(columnId) || isRowAdded(rowId)) {
            return true;
        }

        if (!columnHasDirty(columnId)) {
            return false;
        }

        BitSet dirtyFlags = this.dirtyFlags.get(columnId);
        if (dirtyFlags != null && dirtyFlags.get(rowId)) {
            return true;
        }

        return false;
    }

    private boolean isRowAdded(int rowId) {
        if (isRowAddedCacheRow != rowId) {
            isRowAddedCacheRow = rowId;
            isRowAddedCacheResponse = addedRows.contains(rowId);
        }
        return isRowAddedCacheResponse;
    }

    @Override
    public boolean isColumnDirty(int columnId) {
        return dirtyColumns.get(columnId);
    }

    @Override
    public boolean columnHasDirty(int columnId) {
        return columnsWithDirty.get(columnId);
    }

    @Override
    public boolean hasChanges() {
        return !queue.isEmpty();
    }

    @Override
    public boolean hasUpdates() {
        return hasUpdates;
    }

    @Override
    public boolean isReferenceCountingEnabled(boolean checkUpstream) {
        return referenceCountingEnabled;
    }

    @Override
    public void setReferenceCountingEnabled(boolean referenceCountingEnabled) {
        this.referenceCountingEnabled = referenceCountingEnabled;
        if (referenceCountingEnabled) {
            referenceCounts = new TIntIntHashMap();
        } else {
            referenceCounts = null;
        }
    }

    @Override
    public void incrementReferenceCount(int rowId) {
        if (referenceCountingEnabled) {
            referenceCounts.adjustOrPutValue(rowId, 1, 0);
        }
    }

    @Override
    public void decrementReferenceCount(int rowId) {
        if (referenceCountingEnabled) {
            referenceCounts.adjustValue(rowId, -1);
        }
    }

    @Override
    public int getReferenceCount(int rowId) {
        return referenceCounts.get(rowId);
    }

    public static class Cursor implements IRowFlags {
        private IChangeQueue changeQueue;
        private int queuePtr;
        private int rowId;
        private boolean advance;
        private boolean doAdd;
        private boolean doUpdate;
        private boolean doRemove;
        private Operation operation;
        private Status status;

        protected Cursor(IChangeQueue changeQueue) {
            this.changeQueue = changeQueue;
            reset();
        }

        public void reset() {
            rowId = -1;
            queuePtr = 0;
            doAdd = doUpdate = doRemove = false;
            advance = true;
        }

        public int getRowId() {
            return rowId;
        }

        public Operation getOperation() {
            return operation;
        }

        @Override
        public boolean isDirty(int columnId) {
            return changeQueue.isDirty(rowId, columnId);
        }

        public Status getStatus() {
            return status;
        }
    }
}
