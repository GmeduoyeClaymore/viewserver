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

package io.viewserver.operators.union;

import io.viewserver.collections.IntHashSet;
import io.viewserver.collections.LongHashSet;
import io.viewserver.core.NullableBool;
import io.viewserver.core.NumberUtils;
import io.viewserver.schema.column.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by bemm on 03/12/2014.
 */
public class UnionColumnHolderNullableBool extends ColumnHolderNullableBool implements IUnionColumnHolder, IRowMapper {
    private static final Logger log = LoggerFactory.getLogger(UnionColumnHolderNullableBool.class);
    private final UnionColumnNullableBool column;
    private IntHashSet sources = new IntHashSet(8, 0.75f, -1);
    private IColumnNullableBool[] columns = new IColumnNullableBool[8];
    private IRowMapper[] upstreamMappers = new IRowMapper[8];
    private LongHashSet rows;
    private IColumnNullableBool sourceColumn;
    private int cachedSource;

    public UnionColumnHolderNullableBool(String name, LongHashSet rows) {
        super(name);
        this.rows = rows;
        column = new UnionColumnNullableBool(name);
    }

    @Override
    public void mapSource(int source, IColumn column, IRowMapper upstreamMapper) {
        int index = sources.addInt(source);
        if (index < 0) {
            index = -index - 1;
        }
        ensureCapacity(index + 1);
        columns[index] = (IColumnNullableBool) column;
        upstreamMappers[index] = upstreamMapper;
        if (log.isTraceEnabled()) {
            log.trace("Mapping column {} for source {}", column.getName(), source);
        }
        if (column instanceof IWritableColumn) {
            ((IWritableColumn)column).storePreviousValues();
        }
    }

    @Override
    public void unmapSource(int source) {
        int index = sources.index(source);
        sources.remove(source);
        columns[index] = null;
        upstreamMappers[index] = null;
    }

    @Override
    public int getSourceRow(int row) {
        int inboundRow = lookupInboundRow(row);

        int index = sources.index(cachedSource);
        if (index < 0) {
            log.warn("Union column {} looking up source of row {} - inboundRow = {}, source = {} with index {}", getName(), row, inboundRow, cachedSource, index);
            return -1;
        }
        IRowMapper upstreamMapper = upstreamMappers[index];
        if (upstreamMapper != null) {
            inboundRow = upstreamMapper.getSourceRow(inboundRow);
        }
        this.sourceColumn = columns[index];
        return inboundRow;
    }

    @Override
    public int lookupInboundRow(int row) {
        long key = rows.get(row);
        cachedSource = NumberUtils.unpackLongHi(key);
        int inboundRow = NumberUtils.unpackLongLo(key);
        return inboundRow;
    }

    @Override
    public IColumn getColumn() {
        return column;
    }

    @Override
    public IRowMapper getRowMapper() {
        return this;
    }

    private void ensureCapacity(int capacity) {
        if (columns.length < capacity) {
            columns = Arrays.copyOf(columns, capacity);
            upstreamMappers = Arrays.copyOf(upstreamMappers, capacity);
        }
    }

    private class UnionColumnNullableBool implements IColumnNullableBool, IWritableColumn {
        private String name;

        private UnionColumnNullableBool(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean supportsPreviousValues() {
            return sourceColumn.supportsPreviousValues();
        }

        @Override
        public ColumnType getType() {
            return ColumnType.NullableBool;
        }

        @Override
        public NullableBool getNullableBool(int row) {
            return sourceColumn.getNullableBool(row);
        }

        @Override
        public boolean getBool(int row) {
            return getNullableBool(row) == NullableBool.True;
        }

        @Override
        public NullableBool getPreviousNullableBool(int row) {
            return sourceColumn.getPreviousNullableBool(row);
        }

        @Override
        public void storePreviousValues() {
            log.trace("Enable storing of previous values for column {}", name);
            for (IColumnNullableBool column : columns) {
                if (column != null) {
                    ((IWritableColumn) column).storePreviousValues();
                }
            }
        }

        @Override
        public void resetAll() {
            throw new UnsupportedOperationException();
        }
    }
}
