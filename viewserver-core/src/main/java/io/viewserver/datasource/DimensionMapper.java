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
import org.apache.commons.beanutils.ConvertUtils;


public class DimensionMapper extends DimensionMapperBase {

    public DimensionMapper() {
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

    public int map(String namespace, String  dimensionName, ContentType dimensionContentType, Object value){
        switch (dimensionContentType) {
            case Bool: {
                return mapBool(namespace, dimensionName, (value instanceof String) ? Boolean.valueOf(value.toString()) : (boolean) value);
            }
            case NullableBool: {
                return mapNullableBool(namespace, dimensionName, (NullableBool) value);
            }
            case Byte: {
                return mapByte(namespace, dimensionName, value instanceof Integer ? ((Integer) value).byteValue() : ((value instanceof String ? ((Integer) Integer.parseInt((String) value)).byteValue() : (byte) value)));
            }
            case Short: {
                return mapShort(namespace, dimensionName, (short) value);
            }
            case Int: {
                int i = mapInt(namespace, dimensionName, value instanceof Integer ? ((Integer) value).intValue() :  (int)ConvertUtils.convert(value, Integer.class));
                return i;
            }
            case Long: {
                return mapLong(namespace, dimensionName, (long) value);
            }
            case String: {
                return mapString(namespace, dimensionName, (String) value);
            }
            default:{
                throw new RuntimeException("Attempting to map unhandled type "  + dimensionContentType);
            }
        }
    }

    public int mapString(String namespace, String dimensionName, String value) {
        if(value == null || "".equals(value)){
            return -1;
        }
        StringHashSet lookup = (StringHashSet) getLookup(namespace, dimensionName, ContentType.String, Cardinality.Int);
        int index = lookup.addString(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(namespace, dimensionName, index, value);
        }
        return index;
    }

    @Override
    protected String lookupString(Object lookup, int id) {
        if(id == -1){
            return null;
        }
        return ((StringHashSet)lookup).get(id);
    }

    public int mapByte(String namespace, String dimensionName, byte value) {
        ByteHashSet lookup = (ByteHashSet) getLookup(namespace, dimensionName, ContentType.Byte, Cardinality.Byte);
        int index = lookup.addByte(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(namespace, dimensionName, index, value);
        }
        return index;
    }

    @Override
    protected byte lookupByte(Object lookup, int id) {
        return(((ByteHashSet)lookup).get(id));
    }

    public int mapShort(String namespace, String dimensionName, short value) {
        ShortHashSet lookup = (ShortHashSet) getLookup(namespace, dimensionName, ContentType.Short, Cardinality.Short);
        int index = lookup.addShort(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(namespace, dimensionName, index, value);
        }
        return index;
    }

    @Override
    protected short lookupShort(Object lookup, int id) {
        return(((ShortHashSet)lookup).get(id));
    }

    public int mapInt(String namespace, String dimensionName, int value) {
        IntHashSet lookup = (IntHashSet) getLookup(namespace, dimensionName, ContentType.Int, Cardinality.Int);
        int index = lookup.addInt(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(namespace, dimensionName, index, value);
        }
        return index;
    }

    @Override
    protected int lookupInt(Object lookup, int id) {
        return(((IntHashSet)lookup).get(id));
    }

    public int mapLong(String namespace, String dimensionName, long value) {
        LongHashSet lookup = (LongHashSet) getLookup(namespace, dimensionName, ContentType.Long, Cardinality.Int);
        int index = lookup.addLong(value);
        if (index < 0) {
            index = -index - 1;
        } else {
            fireDimensionValueMapped(namespace, dimensionName, index, value);
        }
        return index;
    }

    @Override
    protected long lookupLong(Object lookup, int id) {
        return(((LongHashSet)lookup).get(id));
    }

    public HashPrimitiveIterator getAllValues(String namespace, String dimensionName, ContentType contentType, Cardinality cardinality) {
        Object lookup = getLookup(namespace, dimensionName, contentType, cardinality);
        switch (contentType) {
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
                        contentType, dimensionName, namespace));
            }
        }
    }

    private void fireDimensionValueMapped(String namespace, String dimensionName, int id, Object value) {
    }


}
