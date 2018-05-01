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

import io.viewserver.controller.ControllerUtils;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import org.apache.commons.beanutils.ConvertUtils;

public enum ContentType {



    Bool(io.viewserver.schema.column.ColumnType.Bool, c -> ConvertUtils.lookup(boolean.class).convert(boolean.class,c)),
    NullableBool(io.viewserver.schema.column.ColumnType.NullableBool, c -> ConvertUtils.lookup(Boolean.class).convert(Boolean.class,c)),
    Byte(io.viewserver.schema.column.ColumnType.Byte, c -> ConvertUtils.lookup(byte.class).convert(byte.class,c)),
    Short(io.viewserver.schema.column.ColumnType.Short, c -> ConvertUtils.lookup(short.class).convert(short.class,c)),
    Int(io.viewserver.schema.column.ColumnType.Int),
    Long(io.viewserver.schema.column.ColumnType.Long),
    Float(io.viewserver.schema.column.ColumnType.Float),
    Double(io.viewserver.schema.column.ColumnType.Double),
    String(io.viewserver.schema.column.ColumnType.String),
    Json(io.viewserver.schema.column.ColumnType.String, c -> convertToString(c), c-> convertFromString(c)),
    Date(io.viewserver.schema.column.ColumnType.Long,  c -> ((java.util.Date) convertToDate(c)).getTime(), c -> convertToDate(c)),
    DateTime(io.viewserver.schema.column.ColumnType.Long,  c -> ((java.util.Date) convertToDate(c)).getTime(), c -> convertToDate(c));

    private static Object convertFromString(Object c) {
        if(c instanceof String){
            return ControllerUtils.mapDefault((java.lang.String) c);
        }
        return c;
    }

    private static String convertToString(Object c) {
        if(c instanceof  String){
            return (java.lang.String) c;
        }
        if(c instanceof DynamicJsonBackedObject){
            return ((DynamicJsonBackedObject) c).serialize();
        }
        return JacksonSerialiser.getInstance().serialise(c);
    }

    private static Object convertToDate(Object c) {
        if(c instanceof String){
            return JacksonSerialiser.getInstance().deserialise((java.lang.String) c, java.util.Date.class);
        }
        return ConvertUtils.lookup(java.util.Date.class).convert(java.util.Date.class,c);
    }

    private io.viewserver.schema.column.ColumnType columnType;
    private Converter converterTo;
    private Converter converterFrom;

    ContentType(ColumnType columnType) {
        this(columnType,null,null);
    }
    ContentType(ColumnType columnType, Converter converterToAndFrom) {
        this(columnType,converterToAndFrom,converterToAndFrom);
    }
    ContentType(ColumnType columnType, Converter converterTo, Converter converterFrom) {
        this.columnType = columnType;
        this.converterTo = converterTo;
        this.converterFrom = converterFrom;
    }

    public Object convertToDataType(Object val){
        if(converterTo == null){
            return val;
        }
        return converterTo.convert(val);
    }
    public Object convertToContentType(Object val){
        if(converterFrom == null){
            return val;
        }
        return converterFrom.convert(val);
    }

    public io.viewserver.schema.column.ColumnType getColumnType() {
        return columnType;
    }

    public interface Converter{
        public Object convert(Object input);
    }
}
