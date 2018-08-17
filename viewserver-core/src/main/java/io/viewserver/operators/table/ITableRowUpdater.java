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

import io.viewserver.operators.IOutput;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;

/**
 * Created by bemm on 29/09/2014.
 */
public interface ITableRowUpdater {
    default Object getValue(String columnName) {
        throw new UnsupportedOperationException();
    }

    void setValues(ITableRow row);

    default void processAdd(IOutput output, int row){
        output.handleAdd(row);
    }
    default void processUpdate(IOutput output, int row){
        output.handleUpdate(row);
    }

    default void ensureCapacity(ITableStorage storage, int row, Schema schema){
        storage.ensureCapacity(row+1,schema);
    }
}
