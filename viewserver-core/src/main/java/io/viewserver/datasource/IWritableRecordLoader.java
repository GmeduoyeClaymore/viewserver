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

import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.IRowFlags;

/**
 * Created by nick on 19/02/2015.
 */
public interface IWritableDataAdapter extends IDataAdapter {
    void setSchema(io.viewserver.schema.Schema schema);

    TableKeyDefinition getDerivedTableKeyDefinition();

    void setTableKeyDefinition(TableKeyDefinition tableKeyDefinition);

    void insertRecord(ITableRow tableRow);

    void updateRecord(ITableRow tableRow, IRowFlags rowFlags);

    void deleteRecord(ITableRow tableRow);

    void clearData();
}
