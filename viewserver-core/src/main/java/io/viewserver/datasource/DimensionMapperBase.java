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

package io.viewserver.datasource;

import io.viewserver.core.NullableBool;
import io.viewserver.core._KeyType_;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 02/07/15.
 */
public abstract class DimensionMapperBase implements IDimensionMapper {
    private static final NullableBool[] nullableBoolValues = { NullableBool.Null, NullableBool.False, NullableBool.True };
    private Map<LookupKey, Object> lookups = new HashMap<>();
    private final LookupKey lookupKey = new LookupKey();

    @Override
    public void registerDimension(IDataSource dataSource, Dimension dimension) {
        final boolean global = dimension.isGlobal();
        LookupKey key = getLookupKey(dataSource, dimension, global);
        Object existingLookup = lookups.get(key);
        if (existingLookup != null) {
            if(global){
                return;
            }
            throw new IllegalStateException(String.format("%s has already been registered as a dimension", key));
        }

        switch (dimension.getColumnType()) {
            case Bool:
            case NullableBool: {
                break;
            }
            case Byte: {
                lookups.put(key, createByteLookup());
                break;
            }
            case Short: {
                lookups.put(key, createShortLookup());
                break;
            }
            case Int: {
                lookups.put(key, createIntLookup());
                break;
            }
            case Long: {
                lookups.put(key, createLongLookup());
                break;
            }
            case String: {
                lookups.put(key, createStringLookup());
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Invalid dimension type '%s'", dimension.getColumnType()));
            }
        }
    }

    protected abstract Object createByteLookup();

    protected abstract Object createShortLookup();

    protected abstract Object createIntLookup();

    protected abstract Object createLongLookup();

    protected abstract Object createStringLookup();

    @Override
    public String lookupString(IDataSource dataSource, Dimension dimension, int id) {
        return lookupString(getLookup(dataSource, dimension), id);
    }

    protected abstract String lookupString(Object lookup, int id);

    @Override
    public byte lookupByte(IDataSource dataSource, Dimension dimension, int id) {
        return lookupByte(getLookup(dataSource, dimension), id);
    }

    protected abstract byte lookupByte(Object lookup, int id);

    public int mapBool(IDataSource dataSource, Dimension dimension, boolean value) {
        return NullableBool.fromBoolean(value).getNumericValue();
    }

    @Override
    public boolean lookupBool(IDataSource dataSource, Dimension dimension, int id) {
        return id == NullableBool.True.getNumericValue();
    }

    public int mapNullableBool(IDataSource dataSource, Dimension dimension, NullableBool value) {
        return value.getNumericValue();
    }

    @Override
    public NullableBool lookupNullableBool(IDataSource dataSource, Dimension dimension, int id) {
        return nullableBoolValues[id];
    }

    @Override
    public short lookupShort(IDataSource dataSource, Dimension dimension, int id) {
        return lookupShort(getLookup(dataSource, dimension), id);
    }

    protected abstract short lookupShort(Object lookup, int id);

    @Override
    public int lookupInt(IDataSource dataSource, Dimension dimension, int id) {
        return lookupInt(getLookup(dataSource, dimension), id);
    }

    protected abstract int lookupInt(Object lookup, int id);

    @Override
    public long lookupLong(IDataSource dataSource, Dimension dimension, int id) {
        return lookupLong(getLookup(dataSource, dimension), id);
    }

    protected abstract long lookupLong(Object lookup, int id);

    @Override
    public _KeyType_ lookup_KeyName_(IDataSource dataSource, Dimension dimension, int id) {
        throw new UnsupportedOperationException("Tidy this stuff up man!");
    }

    protected Object getLookup(IDataSource dataSource, Dimension dimension) {
        LookupKey key = getLookupKey(dataSource, dimension, dimension.isGlobal());
        Object lookup = lookups.get(key);
        if (lookup == null) {
            throw new IllegalArgumentException("There is no dimension registered as " + key);
        }
        return lookup;
    }

    private LookupKey getLookupKey(IDataSource dataSource, Dimension dimension, boolean global) {
        lookupKey.dataSource = global ? null : dataSource;
        lookupKey.dimension = dimension;
        return lookupKey;
    }

    @Override
    public void clear() {
        lookups.clear();
    }

    private class LookupKey {
        private IDataSource dataSource;
        private Dimension dimension;

        @Override
        public String toString() {
            return String.format("%s/%s", dataSource != null ? dataSource.getName() : "global", dimension.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LookupKey lookupKey = (LookupKey) o;

            if (dataSource != null ? !dataSource.getName().equals(lookupKey.dataSource.getName()) : lookupKey.dataSource != null)
                return false;
            return dimension.getName().equals(lookupKey.dimension.getName());
        }

        @Override
        public int hashCode() {
            int result = dataSource != null ? dataSource.getName().hashCode() : 0;
            result = 31 * result + dimension.getName().hashCode();
            return result;
        }
    }
}
