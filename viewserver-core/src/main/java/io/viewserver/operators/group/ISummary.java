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

package io.viewserver.operators.group;

import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.schema.column.IColumn;
import io.viewserver.schema.column.IRowFlags;

/**
 * Created by bemm on 02/10/2014.
 */
public interface ISummary extends IColumn {
    void initialise(ISummaryContext context);

    void onGroupAdd(int groupId);

    void onGroupRemove(int groupId);

    void onGroupEnter(int groupId, int rowId);

    void onGroupLeave(int groupId, int rowId);

    void onRowUpdate(int groupId, int rowId);

    void onAfterCommit();

    boolean hasChanges(IRowFlags rowFlags);

    void reset();
}
