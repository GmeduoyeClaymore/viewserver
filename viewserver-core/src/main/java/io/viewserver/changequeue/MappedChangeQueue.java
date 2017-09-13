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

import io.viewserver.operators.IOutput;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nickc on 18/10/2014.
 */
public class MappedChangeQueue extends ChangeQueue implements IMappedChangeQueue {
    private static final Logger log = LoggerFactory.getLogger(MappedChangeQueue.class);
    private int[] columnMapping;
    private IRowMapper[] upstreamRowMappersByColumn;
    private IChangeQueue[] upstreamChangeQueuesByColumn;
    private Set<Mapper> distinctMappers = new HashSet<>();
    private boolean affectsReferenceCounts;

    public MappedChangeQueue(IOutput owner) {
        super(owner);
        columnMapping = new int[32];
        upstreamRowMappersByColumn = new IRowMapper[32];
        upstreamChangeQueuesByColumn = new IChangeQueue[32];
    }

    @Override
    public void handleAdd(int row) {
        super.handleAdd(row);

        if (affectsReferenceCounts) {
            for (Mapper mapper : distinctMappers) {
                if (mapper.changeQueue.isReferenceCountingEnabled(true)) {
                    int sourceRow = mapper.rowMapper != null ? mapper.rowMapper.getSourceRow(row) : row;
                    mapper.changeQueue.incrementReferenceCount(sourceRow);
                }
            }
        }
    }

    @Override
    public void handleRemove(int row) {
        super.handleRemove(row);

        if (affectsReferenceCounts) {
            for (Mapper mapper : distinctMappers) {
                if (mapper.changeQueue.isReferenceCountingEnabled(true)) {
                    int sourceRow = mapper.rowMapper != null ? mapper.rowMapper.getSourceRow(row) : row;
                    mapper.changeQueue.decrementReferenceCount(sourceRow);
                }
            }
        }
    }

    @Override
    public boolean isReferenceCountingEnabled(boolean checkUpstream) {
        if (super.isReferenceCountingEnabled(checkUpstream)) {
            return true;
        }

        if (checkUpstream) {
            for (Mapper mapper : distinctMappers) {
                if (mapper.changeQueue.isReferenceCountingEnabled(true)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void incrementReferenceCount(int rowId) {
        super.incrementReferenceCount(rowId);

        for (Mapper mapper : distinctMappers) {
            if (mapper.changeQueue.isReferenceCountingEnabled(true)) {
                int sourceRow = mapper.rowMapper != null ? mapper.rowMapper.getSourceRow(rowId) : rowId;
                mapper.changeQueue.incrementReferenceCount(sourceRow);
            }
        }
    }

    @Override
    public void decrementReferenceCount(int rowId) {
        super.decrementReferenceCount(rowId);

        for (Mapper mapper : distinctMappers) {
            if (mapper.changeQueue.isReferenceCountingEnabled(true)) {
                int sourceRow = mapper.rowMapper != null ? mapper.rowMapper.getSourceRow(rowId) : rowId;
                mapper.changeQueue.decrementReferenceCount(sourceRow);
            }
        }
    }

    @Override
    public boolean isDirty(int rowId, int columnId) {
        if (rowId == -1) {
            return false;
        }

        if (super.isDirty(rowId, columnId)) {
            return true;
        }

        if (columnId >= columnMapping.length) {
            // the column is ours, so no upstream
            return false;
        }

        try {
            int inboundColumnId = columnMapping[columnId];
            if (inboundColumnId == -1) {
                return false;
            }

            final IChangeQueue upstreamChangeQueue = upstreamChangeQueuesByColumn[columnId];
            if (upstreamChangeQueue != null) {
                IRowMapper upstreamRowMapper = this.upstreamRowMappersByColumn[columnId];
                int inboundRow = upstreamRowMapper != null ? upstreamRowMapper.lookupInboundRow(rowId) : rowId;
                return upstreamChangeQueue.isDirty(inboundRow, inboundColumnId);
            }
        } catch (Throwable ex) {
            log.error(String.format("Problem checking dirty flags in %s.%s", getOwner().getOwner(), getOwner()), ex);
            return false;
        }

        return false;
    }

    @Override
    public void mapColumn(ColumnHolder inboundColumn, ColumnHolder outboundColumn, IChangeQueue sourceChangeQueue) {
        int outboundId = outboundColumn.getColumnId();
        if (outboundId == -1) {
            throw new IllegalStateException("Column '" + outboundColumn.getName() + "' must be added to the output schema before mapping");
        }
        ensureColumnMappingCapacity(outboundId + 1);
        columnMapping[outboundId] = inboundColumn.getColumnId();
        upstreamRowMappersByColumn[outboundId] = outboundColumn.getRowMapper();
        upstreamChangeQueuesByColumn[outboundId] = sourceChangeQueue;
        distinctMappers.add(new Mapper(sourceChangeQueue, outboundColumn.getRowMapper()));
    }

    @Override
    public int getOutboundColumnId(ColumnHolder inboundColumn) {
        return columnMapping[inboundColumn.getColumnId()];
    }

    private void ensureColumnMappingCapacity(int capacity) {
        int oldLength = columnMapping.length;
        if (oldLength < capacity) {
            columnMapping = Arrays.copyOf(columnMapping, capacity);
            for (int i = oldLength; i < capacity; i++) {
                columnMapping[i] = -1;
            }

            upstreamRowMappersByColumn = Arrays.copyOf(upstreamRowMappersByColumn, capacity);
            upstreamChangeQueuesByColumn = Arrays.copyOf(upstreamChangeQueuesByColumn, capacity);
        }
    }

    @Override
    public boolean columnHasDirty(int columnId) {
        if (super.columnHasDirty(columnId)) {
            // we have a dirty column
            return true;
        }

        if (columnId >= columnMapping.length) {
            // the column is ours, so no upstream
            return false;
        }

        int inboundColumnId = columnMapping[columnId];
        if (inboundColumnId == -1) {
            return false;
        }
        if (upstreamChangeQueuesByColumn[columnId] != null && upstreamChangeQueuesByColumn[columnId].columnHasDirty(inboundColumnId)) {
            return true;
        }

        return false;
    }

    @Override
    public void setAffectsReferenceCounts(boolean affectsReferenceCounts) {
        this.affectsReferenceCounts = affectsReferenceCounts;
    }

    private class Mapper {
        private IChangeQueue changeQueue;
        private IRowMapper rowMapper;

        private Mapper(IChangeQueue changeQueue, IRowMapper rowMapper) {
            this.changeQueue = changeQueue;
            this.rowMapper = rowMapper;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Mapper mapper = (Mapper) o;

            if (changeQueue != null ? !changeQueue.equals(mapper.changeQueue) : mapper.changeQueue != null)
                return false;
            if (rowMapper != null ? !rowMapper.equals(mapper.rowMapper) : mapper.rowMapper != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = changeQueue != null ? changeQueue.hashCode() : 0;
            result = 31 * result + (rowMapper != null ? rowMapper.hashCode() : 0);
            return result;
        }
    }
}
