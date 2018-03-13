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

import io.viewserver.collections.*;
import io.viewserver.core.NullableBool;

/**
 * Created by nickc on 13/10/2014.
 */
public class DimensionMapper extends DimensionMapperBase {
    private IDimensionMapperListener dimensionMapperListener;

    public void setDimensionMapperListener(IDimensionMapperListener dimensionMapperListener) {
        this.dimensionMapperListener = dimensionMapperListener;
    }

    @Override
    protected ByteHashSet createByteLookup() {
        return new ByteHashSet(8, 0.75f, (byte)-1);
    }

    @Override
    protected ShortHashSet createShortLookup() {
        return new ShortHashSet(8, 0.75f, Short.MIN_VALUE);
    }

    @Override
    protected IntHashSet createIntLookup() {
        return new IntHashSet(8, 0.75f, Integer.MIN_VALUE);
    }

    @Override
    protected LongHashSet createLongLookup() {
        return new LongHashSet(8, 0.75f, Long.MIN_VALUE);
    }

    @Override
    protected StringHashSet createStringLookup() {
        StringHashSet stringHashSet = new StringHashSet(8, 0.75f, "!!___NULL___!!");
        stringHashSet.addString(null);
        return stringHashSet;
    }

    public int map(IDataSource dataSource, Dimension dimension, Object value){
        switch (dimension.getColumnType()) {
            case Bool: {
                return mapBool(dataSource, dimension, (value instanceof String) ? Boolean.valueOf(value.toString()) : (boolean) value);
            }
            case NullableBool: {
                return mapNullableBool(dataSource, dimension, (NullableBool) value);
            }
            case Byte: {
                return mapByte(dataSource, dimension, value instanceof Integer ? ((Integer) value).byteValue() : ((value instanceof String ? ((Integer) Integer.parseInt((String) value)).byteValue() : (byte) value)));
            }
            case Short: {
                return mapShort(dataSource, dimension, (short) value);
            }
            case Int: {
                int i = mapInt(dataSource, dimension, value instanceof Integer ? ((Integer) value).intValue() : (int) value);
                return i;
            }
            case Long: {
                return mapLong(dataSource, dimension, (long) value);
            }
            case String: {
                return mapString(dataSource, dimension, (String) value);
            }
            default:{
                throw new RuntimeException("Attempting to map unhandled type");
            }
        }
    }

    public int mapString(IDataSource dataSource, Dimension dimension, String value) {
        StringHashSet lookup = (StringHashSet) getLookup(dataSource, dimension);
        int index = lookup.addString(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(dataSource, dimension, index, value);
        }
        return index;
    }

    @Override
    protected String lookupString(Object lookup, int id) {
        return ((StringHashSet)lookup).get(id);
    }

    public int mapByte(IDataSource dataSource, Dimension dimension, byte value) {
        ByteHashSet lookup = (ByteHashSet) getLookup(dataSource, dimension);
        int index = lookup.addByte(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(dataSource, dimension, index, value);
        }
        return index;
    }

    @Override
    protected byte lookupByte(Object lookup, int id) {
        return(((ByteHashSet)lookup).get(id));
    }

    public int mapShort(IDataSource dataSource, Dimension dimension, short value) {
        ShortHashSet lookup = (ShortHashSet) getLookup(dataSource, dimension);
        int index = lookup.addShort(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(dataSource, dimension, index, value);
        }
        return index;
    }

    @Override
    protected short lookupShort(Object lookup, int id) {
        return(((ShortHashSet)lookup).get(id));
    }

    public int mapInt(IDataSource dataSource, Dimension dimension, int value) {
        IntHashSet lookup = (IntHashSet) getLookup(dataSource, dimension);
        int index = lookup.addInt(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(dataSource, dimension, index, value);
        }
        return index;
    }

    @Override
    protected int lookupInt(Object lookup, int id) {
        return(((IntHashSet)lookup).get(id));
    }

    public int mapLong(IDataSource dataSource, Dimension dimension, long value) {
        LongHashSet lookup = (LongHashSet) getLookup(dataSource, dimension);
        int index = lookup.addLong(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(dataSource, dimension, index, value);
        }
        return index;
    }

    @Override
    protected long lookupLong(Object lookup, int id) {
        return(((LongHashSet)lookup).get(id));
    }

    public HashPrimitiveIterator getAllValues(IDataSource dataSource, Dimension dimension) {
        Object lookup = getLookup(dataSource, dimension);
        switch (dimension.getColumnType()) {
            case Byte: {
                return (HashPrimitiveIterator) ((ByteHashSet)lookup).iterator();
            }
            case Short: {
                return (HashPrimitiveIterator) ((ShortHashSet)lookup).iterator();
            }
            case Int: {
                return (HashPrimitiveIterator) ((IntHashSet)lookup).iterator();
            }
            case Long: {
                return (HashPrimitiveIterator) ((LongHashSet)lookup).iterator();
            }
            case String: {
                return (HashPrimitiveIterator) ((StringHashSet)lookup).iterator();
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unhandled dimension type '%s' for dimension '%s' in data source '%s'",
                        dimension.getColumnType(), dimension.getName(), dataSource.getName()));
            }
        }
    }

    private void fireDimensionValueMapped(IDataSource dataSource, Dimension dimension, int id, Object value) {
        if (dimensionMapperListener != null) {
            dimensionMapperListener.onDimensionValueMapped(dataSource, dimension, id, value);
        }
    }
}
