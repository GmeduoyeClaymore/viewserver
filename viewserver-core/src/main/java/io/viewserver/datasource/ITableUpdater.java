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

import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.schema.Schema;

/**
 * Created by bemm on 25/11/2014.
 */
public interface ITableUpdater {
    ITable createTable(String name, Schema schema);

    ITable createTable(String name, SchemaConfig schema);

    int addRow(ITableRowUpdater rowUpdater);

    void updateRow(ITableRowUpdater rowUpdater);

    int addOrUpdateRow(ITableRowUpdater rowUpdater);
}
