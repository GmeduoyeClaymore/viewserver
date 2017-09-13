// :_KeyName_=Bool,_KeyType_=boolean;_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double;_KeyName_=String,_KeyType_=String

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
import io.viewserver.core.NumberUtils;
import io.viewserver.core._KeyType_;
import io.viewserver.schema.column.*;
import io.viewserver.util.ViewServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by nickc on 03/12/2014.
 */
public class UnionColumnHolder_KeyName_ extends ColumnHolder_KeyName_ implements IUnionColumnHolder, IRowMapper {
    private static final Logger log = LoggerFactory.getLogger(UnionColumnHolder_KeyName_.class);
    private final UnionColumn_KeyName_ column;
    private IntHashSet sources = new IntHashSet(8, 0.75f, -1);
    private IColumn_KeyName_[] columns = new IColumn_KeyName_[8];
    private IRowMapper[] upstreamMappers = new IRowMapper[8];
    private LongHashSet rows;
    private IColumn_KeyName_ sourceColumn;
    private int cachedSource;

    public UnionColumnHolder_KeyName_(String name, LongHashSet rows) {
        super(name);
        this.rows = rows;
        column = new UnionColumn_KeyName_(name);
    }

    @Override
    public void mapSource(int source, IColumn column, IRowMapper upstreamMapper) {
        int index = sources.addInt(source);
        if (index < 0) {
            index = -index - 1;
        }
        ensureCapacity(index + 1);
        columns[index] = (IColumn_KeyName_) column;
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
            throw new ViewServerException(String.format(
                    "Union column %s looking up source of row %d - inboundRow = %d, source = %d with index %d",
                    getName(), row, inboundRow, cachedSource, index));
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

    private class UnionColumn_KeyName_ implements IColumn_KeyName_, IWritableColumn {
        private String name;

        private UnionColumn_KeyName_(String name) {
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
            return ColumnType._KeyName_;
        }

        @Override
        public _KeyType_ get_KeyName_(int row) {
            return sourceColumn.get_KeyName_(row);
        }

        @Override
        public _KeyType_ getPrevious_KeyName_(int row) {
            return sourceColumn.getPrevious_KeyName_(row);
        }

        @Override
        public void storePreviousValues() {
            int count = columns.length;
            for (int i = 0; i < count; i++) {
                IColumn_KeyName_ column = columns[i];
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
