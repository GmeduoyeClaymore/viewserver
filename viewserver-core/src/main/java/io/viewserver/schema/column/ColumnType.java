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

package io.viewserver.schema.column;

/**
 * Created by nickc on 23/09/2014.
 */
public enum ColumnType {
    Bool(false, false, 0, false, 1, false),
    Byte((byte)0, false, 1, true, java.lang.Byte.SIZE, java.lang.Byte.MIN_VALUE),
    Short((short)0, false, 2, true, java.lang.Short.SIZE, java.lang.Short.MIN_VALUE),
    Int(0, false, 3, true, Integer.SIZE, Integer.MIN_VALUE),
    Long(0l, false, 4, true, java.lang.Long.SIZE, java.lang.Long.MIN_VALUE),
    Float(0f, false, 5, true, java.lang.Float.SIZE, java.lang.Float.MIN_VALUE),
    Double(0d, false, 6, true, java.lang.Double.SIZE, java.lang.Double.MIN_VALUE),
    String(null, true, -1, false, -1, null),
    NullableBool(io.viewserver.core.NullableBool.Null, false, 0, false, -1, io.viewserver.core.NullableBool.Null),
    _KeyName_(null),
    Unknown(null);

    private Object defaultValue;
    private boolean isObject;
    private int castOrder;
    private boolean isNumber;
    private int size;
    private Object nullValue;

    ColumnType(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    ColumnType(Object defaultValue, boolean isObject) {
        this.defaultValue = defaultValue;
        this.isObject = isObject;
    }

    ColumnType(Object defaultValue, boolean isObject, int castOrder, boolean isNumber, int size, Object nullValue) {
        this.defaultValue = defaultValue;
        this.isObject = isObject;
        this.castOrder = castOrder;
        this.isNumber = isNumber;
        this.size = size;
        this.nullValue = nullValue;
    }

    public io.viewserver.messages.common.ColumnType serialise() {
        switch (this) {
            case Bool: {
                return io.viewserver.messages.common.ColumnType.Boolean;
            }
            case NullableBool: {
                return io.viewserver.messages.common.ColumnType.NullableBoolean;
            }
            case Byte: {
                return io.viewserver.messages.common.ColumnType.Byte;
            }
            case Short: {
                return io.viewserver.messages.common.ColumnType.Short;
            }
            case Int: {
                return io.viewserver.messages.common.ColumnType.Integer;
            }
            case Long: {
                return io.viewserver.messages.common.ColumnType.Long;
            }
            case Float: {
                return io.viewserver.messages.common.ColumnType.Float;
            }
            case Double: {
                return io.viewserver.messages.common.ColumnType.Double;
            }
            case String: {
                return io.viewserver.messages.common.ColumnType.String;
            }
            default: {
                throw new IllegalStateException("Unknown column type");
            }
        }
    }

    public static ColumnType deserialise(io.viewserver.messages.common.ColumnType columnType) {
        switch (columnType) {
            case Boolean: {
                return Bool;
            }
            case NullableBoolean: {
                return NullableBool;
            }
            case Byte: {
                return Byte;
            }
            case Short: {
                return Short;
            }
            case Integer: {
                return Int;
            }
            case Long: {
                return Long;
            }
            case Float: {
                return Float;
            }
            case Double: {
                return Double;
            }
            case String: {
                return String;
            }
            default: {
                throw new IllegalArgumentException(java.lang.String.format("Unknown column type '%s'", columnType));
            }
        }
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isObject() {
        return isObject;
    }

    public void setObject(boolean isObject) {
        this.isObject = isObject;
    }

    public boolean areEqual(Object currentValue, Object value) {
        return currentValue != null && currentValue.equals(value);
    }

    public int getCastOrder() {
        return castOrder;
    }

    public boolean isNumber() {
        return isNumber;
    }

    public void setNumber(boolean isNumber) {
        this.isNumber = isNumber;
    }

    public int getSize() {
        return size;
    }

    public Object getNullValue() {
        return nullValue;
    }

    public static ColumnType fromString(String string) {
        try {
            return ColumnType.valueOf(string);
        } catch (IllegalArgumentException e) {
            for (ColumnType columnType : ColumnType.values()) {
                if (string.equalsIgnoreCase(columnType.toString())) {
                    return columnType;
                }
            }
        }
        throw new IllegalArgumentException("Unknown column type '" + string + "'");
    }
}
