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

package io.viewserver.operators.group.summary;

import io.viewserver.operators.group.ISummary;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.IRowFlags;

/**
 * Created by nick on 19/03/2015.
 */
public abstract class MultiTypeSummaryBase implements ISummary {
    protected final String name;
    protected String target;
    protected ISummary internalSummary;

    protected MultiTypeSummaryBase(String name, String target) {
        this.name = name;
        this.target = target;
    }

    @Override
    public abstract void initialise(ISummaryContext context);

    @Override
    public void onGroupAdd(int groupId) {
        internalSummary.onGroupAdd(groupId);
    }

    @Override
    public void onGroupRemove(int groupId) {
        internalSummary.onGroupRemove(groupId);
    }

    @Override
    public void onGroupEnter(int groupId, int rowId) {
        internalSummary.onGroupEnter(groupId, rowId);
    }

    @Override
    public void onGroupLeave(int groupId, int rowId) {
        internalSummary.onGroupLeave(groupId, rowId);
    }

    @Override
    public void onRowUpdate(int groupId, int rowId) {
        internalSummary.onRowUpdate(groupId, rowId);
    }

    @Override
    public void onAfterCommit() {
        internalSummary.onAfterCommit();
    }

    @Override
    public boolean hasChanges(IRowFlags rowFlags) {
        return internalSummary.hasChanges(rowFlags);
    }

    @Override
    public void reset() {
        internalSummary.reset();
    }

    @Override
    public String getName() {
        return internalSummary.getName();
    }

    @Override
    public boolean supportsPreviousValues() {
        return internalSummary.supportsPreviousValues();
    }

    @Override
    public ColumnType getType() {
        return internalSummary.getType();
    }
}
