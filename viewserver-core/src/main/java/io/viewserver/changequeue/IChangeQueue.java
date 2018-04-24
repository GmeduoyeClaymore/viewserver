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

import io.viewserver.operators.Status;
import io.viewserver.schema.column.IColumnWatcher;

import java.util.List;

/**
 * Created by bemm on 29/09/2014.
 */
public interface IChangeQueue extends IColumnWatcher {
    void handleAdd(int row);

    void handleUpdate(int row);

    void handleRemove(int row);

    ChangeQueue.Cursor createCursor();

    boolean getNext(ChangeQueue.Cursor cursor);

    void clear();

    boolean isColumnDirty(int columnId);

    boolean columnHasDirty(int columnId);

    boolean hasChanges();

    List<Status> getStatuses();

    void handleStatus(Status reset);

    void ensureCapacity(int capacity);

    boolean hasUpdates();

    boolean isReferenceCountingEnabled(boolean checkUpstream);

    void setReferenceCountingEnabled(boolean referenceCountingEnabled);

    void incrementReferenceCount(int rowId);

    void decrementReferenceCount(int rowId);

    int getReferenceCount(int rowId);

    public enum Operation {
        Add,
        Update,
        Status, Remove
    }
}
