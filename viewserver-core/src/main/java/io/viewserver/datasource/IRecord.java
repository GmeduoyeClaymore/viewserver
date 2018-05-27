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

import java.util.Date;

/**
 * Created by bemm on 25/11/2014.
 */
public interface IRecord {
    String[] getColumnNames();

    byte getByte(String columnName);

    String getString(String columnName);

    Boolean getBool(String columnName);

    NullableBool getNullableBool(String columnName);

    Short getShort(String columnName);

    Integer getInt(String columnName);

    Long getLong(String columnName);

    Float getFloat(String columnName);

    Double getDouble(String columnName);

    Date getDate(String columnName);

    Date getDateTime(String columnName);

    Object getValue(String columnName);

    boolean hasValue(String columnName);

}


