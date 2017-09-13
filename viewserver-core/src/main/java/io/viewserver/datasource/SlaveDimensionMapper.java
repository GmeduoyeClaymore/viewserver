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

import gnu.trove.map.hash.*;

/**
 * Created by nickc on 13/10/2014.
 */
public class SlaveDimensionMapper extends DimensionMapperBase {

    @Override
    protected Object createByteLookup() {
        return new TByteByteHashMap(8, 0.75f, (byte)-1, (byte)-1);
    }

    @Override
    protected Object createShortLookup() {
        return new TShortShortHashMap(8, 0.75f, Short.MIN_VALUE, Short.MIN_VALUE);
    }

    @Override
    protected Object createIntLookup() {
        return new TIntIntHashMap(8, 0.75f, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Override
    protected Object createLongLookup() {
        return new TIntLongHashMap(8, 0.75f, Integer.MIN_VALUE, Long.MIN_VALUE);
    }

    @Override
    protected Object createStringLookup() {
        return new TIntObjectHashMap<String>(8, 0.75f, Integer.MIN_VALUE);
    }

    public void mapString(IDataSource dataSource, Dimension dimension, String value, int id) {
        TIntObjectHashMap<String> lookup = (TIntObjectHashMap<String>) getLookup(dataSource, dimension);
        lookup.put(id, value);
    }

    @Override
    protected String lookupString(Object lookup, int id) {
        return ((TIntObjectHashMap<String>)lookup).get(id);
    }

    public void mapByte(IDataSource dataSource, Dimension dimension, byte value, int id) {
        TByteByteHashMap lookup = (TByteByteHashMap) getLookup(dataSource, dimension);
        lookup.put((byte) id, value);
    }

    @Override
    protected byte lookupByte(Object lookup, int id) {
        return ((TByteByteHashMap)lookup).get((byte)id);
    }

    public void mapShort(IDataSource dataSource, Dimension dimension, short value, int id) {
        TShortShortHashMap lookup = (TShortShortHashMap) getLookup(dataSource, dimension);
        lookup.put((short) id, value);
    }

    @Override
    protected short lookupShort(Object lookup, int id) {
        return ((TShortShortHashMap)lookup).get((short)id);
    }

    public void mapInt(IDataSource dataSource, Dimension dimension, int value, int id) {
        TIntIntHashMap lookup = (TIntIntHashMap) getLookup(dataSource, dimension);
        lookup.put(id, value);
    }

    @Override
    protected int lookupInt(Object lookup, int id) {
        return ((TIntIntHashMap)lookup).get(id);
    }

    public void mapLong(IDataSource dataSource, Dimension dimension, long value, int id) {
        TIntLongHashMap lookup = (TIntLongHashMap) getLookup(dataSource, dimension);
        lookup.put(id, value);
    }

    @Override
    protected long lookupLong(Object lookup, int id) {
        return ((TIntLongHashMap)lookup).get(id);
    }
}
