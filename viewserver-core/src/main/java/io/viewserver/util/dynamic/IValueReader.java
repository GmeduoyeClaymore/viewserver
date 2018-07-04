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

package io.viewserver.util.dynamic;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by bemm on 11/02/2015.
 */
public interface IValueReader {
    Date readDate(String field);

    Timestamp readTimestamp(String field);

    Integer readInteger(String field);

    Boolean readBoolean(String field);

    Long readLong(String field);

    Float readFloat(String field);

    Double readDouble(String field);

    String readString(String field);

    String[] readStringArray(String field);

    double[] readDoubleArray(String field);

    int[] readIntegerArray(String field);

}