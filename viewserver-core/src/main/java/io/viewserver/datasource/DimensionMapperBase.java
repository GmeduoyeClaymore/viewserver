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
import io.viewserver.operators.dimension.DimensionMapperOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bemm on 02/07/15.
 */
public abstract class DimensionMapperBase implements IDimensionMapper {
    private static final Logger log = LoggerFactory.getLogger(DimensionMapperBase.class);
    private static final NullableBool[] nullableBoolValues = { NullableBool.Null, NullableBool.False, NullableBool.True };
    private Map<LookupKey, Object> lookups = new HashMap<>();

    @Override
    public LookupKey registerDimension(String dimensionNamespace, Dimension dimension) {
        return registerDimension(dimensionNamespace, dimension.getName(),dimension.getContentType());
    }
    public LookupKey registerDimension(String dimensionNamespace, String dimensionName, ContentType dimensionContentType) {
        LookupKey key = getLookupKey(dimensionNamespace, dimensionName);
        Object existingLookup = lookups.get(key);
        if (existingLookup != null) {
            log.warn("Already found a dimension named " + key + " not registering it again");
            return (LookupKey) key;
        }

        switch (dimensionContentType) {
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
                throw new IllegalArgumentException(String.format("Invalid dimension type '%s'", dimensionContentType));
            }
        }
        return key;
    }

    protected abstract Object createByteLookup();

    protected abstract Object createShortLookup();

    protected abstract Object createIntLookup();

    protected abstract Object createLongLookup();

    protected abstract Object createStringLookup();

    @Override
    public String lookupString(String dimensionNamespace, String dimensionName, int id) {
        return lookupString(getLookup(dimensionNamespace, dimensionName, ContentType.String), id);
    }

    protected abstract String lookupString(Object lookup, int id);

    @Override
    public byte lookupByte(String dimensionNamespace, String dimensionName, int id) {
        return lookupByte(getLookup(dimensionNamespace, dimensionName, ContentType.Byte), id);
    }

    protected abstract byte lookupByte(Object lookup, int id);

    public int mapBool(String dimensionNamespace, String dimensionName, boolean value) {
        return NullableBool.fromBoolean(value).getNumericValue();
    }

    @Override
    public boolean lookupBool(String dimensionNamespace, String dimensionName, int id) {
        return id == NullableBool.True.getNumericValue();
    }

    public int mapNullableBool(String dimensionNamespace, String dimensionName, NullableBool value) {
        return value.getNumericValue();
    }

    @Override
    public NullableBool lookupNullableBool(String dimensionNamespace, String dimensionName,int id) {
        return nullableBoolValues[id];
    }

    @Override
    public short lookupShort(String dimensionNamespace, String dimensionName, int id) {
        return lookupShort(getLookup(dimensionNamespace, dimensionName, ContentType.Short), id);
    }

    protected abstract short lookupShort(Object lookup, int id);

    @Override
    public int lookupInt(String dimensionNamespace, String dimensionName, int id) {
        return lookupInt(getLookup(dimensionNamespace, dimensionName, ContentType.Int), id);
    }

    protected abstract int lookupInt(Object lookup, int id);

    @Override
    public long lookupLong(String dimensionNamespace, String dimensionName,int id) {
        return lookupLong(getLookup(dimensionNamespace, dimensionName, ContentType.Long), id);
    }

    protected abstract long lookupLong(Object lookup, int id);

    @Override
    public _KeyType_ lookup_KeyName_(String dimensionNamespace, String dimensionName, ContentType dimensionContentType,int id) {
        throw new UnsupportedOperationException("Tidy this stuff up man!");
    }

    protected Object getLookup(String dimensionNamespace, String dimensionName, ContentType contentType) {
        LookupKey key = getLookupKey(dimensionNamespace, dimensionName);
        LookupKey key2 = getLookupKey("global", dimensionName);
        Object lookup = lookups.get(key);
        if (lookup == null) {
            lookup = lookups.get(key2);
        }
        if (lookup == null) {
            LookupKey newKey  = this.registerDimension(dimensionNamespace, dimensionName, contentType);
            return lookups.get(newKey);
        }
        return lookup;
    }

    private LookupKey getLookupKey(String dimensionNamespace, String dimensionName) {
        return new LookupKey(dimensionNamespace, dimensionName);
    }

    @Override
    public void clear() {
        lookups.clear();
    }


}
