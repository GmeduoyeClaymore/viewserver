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

package io.viewserver.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

/**
 * Created by bemm on 29/09/2014.
 */
public class ActiveRowTracker implements IActiveRowTracker {
    private final Logger log;
    private BitSet activeRows;
    private int cardinality;
    private IOutput owner;
    public ActiveRowTracker(IOutput owner) {
        this.owner = owner;
        activeRows = new BitSet();
        log = LoggerFactory.getLogger(owner.getOwner().getName() + "-ActiveRowTracker");
        log.trace("Creating row tracker");
    }

    public void handleAdd(int row) {
        if (isActive(row)) {
            throw new RuntimeException("Row " + row + " already active");
        }
        activeRows.set(row);
        cardinality++;
        log.trace("Row {} has been marked active",row);
    }

    public void handleRemove(int row) {
        if (!isActive(row)) {
            throw new RuntimeException("Row " + row + " is not active");
        }
        activeRows.clear(row);
        cardinality--;
        log.trace("Row {} has been removed",row);
    }

    public boolean isActive(int row) {
        return activeRows.get(row);
    }

    @Override
    public int getRowCount() {
        return cardinality;
    }

    @Override
    public IRowSequence getAllRows() {
        return new RowSequence();
    }

    public void clear() {
        log.trace("Clearing row tracker");
        activeRows.clear();
        cardinality = 0;
    }

    public IOutput getOwner() {
        return owner;
    }

    private class RowSequence implements IRowSequence {
        private int rowId = -1;

        @Override
        public int getRowId() {
            if (rowId < 0) {
                throw new RuntimeException("No current row");
            }
            return rowId;
        }

        @Override
        public boolean moveNext() {
            if (rowId == -2) {
                return false;
            }
            rowId = activeRows.nextSetBit(rowId + 1);
            if (rowId == -1) {
                rowId = -2;
            }
            return rowId >= 0;
        }

        @Override
        public void reset() {
            rowId = -1;
        }
    }

    @Override
    public String toString() {
        return "ActiveRowTracker{" +
                "activeRows=" + activeRows +
                ", cardinality=" + cardinality +
                '}';
    }
}
