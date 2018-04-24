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

package io.viewserver.operators.table;

import io.viewserver.core.NullableBool;

/**
 * Created by bemm on 25/11/2014.
 */
public interface ITableRow {
    int getRowId();

    boolean getBool(String name);

    void setBool(String name, boolean value);

    NullableBool getNullableBool(String name);

    void setNullableBool(String name, NullableBool value);

    byte getByte(String name);

    void setByte(String name, byte value);

    short getShort(String name);

    void setShort(String name, short value);

    int getInt(String name);

    void setInt(String name, int value);

    long getLong(String name);

    void setLong(String name, long value);

    float getFloat(String name);

    void setFloat(String name, float value);

    double getDouble(String name);

    void setDouble(String name, double value);

    String getString(String name);

    void setString(String name, String value);

    Object getValue(String name);
}
