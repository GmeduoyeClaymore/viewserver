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

package io.viewserver.operators;

import io.viewserver.changequeue.ChangeQueue;
import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.schema.SchemaChange;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowFlags;

import java.util.List;

/**
 * Created by nickc on 29/09/2014.
 */
public abstract class InputBase implements IInput {
    private String name;
    private IOperator owner;
    private IOutput producer;
    protected boolean isDataResetRequested;
    private ChangeQueue.Cursor changeCursor;
    protected boolean isSchemaResetRequested;
    private boolean isDataRefreshRequested;

    protected InputBase(String name, IOperator owner) {
        this.name = name;
        this.owner = owner;

        owner.getExecutionContext().getMetadataRegistry().registerInput(this);
    }

    @Override
    public IOutput getProducer() {
        return producer;
    }

    @Override
    public void onSchema() {
        if (isSchemaResetRequested()) {
            isSchemaResetRequested = false;
            onSchemaReset();
        } else {
            SchemaChange schemaChange = getProducer().getSchema().getChange();
            if (schemaChange.hasChanges()) {
                onSchemaChange(schemaChange);
            }
        }
    }

    protected void onSchemaReset() {
        List<ColumnHolder> columnHolders = producer.getSchema().getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);
            if (columnHolder != null) {
                onColumnAdd(columnHolder);
            }
        }
    }

    protected void onSchemaChange(SchemaChange schemaChange) {
        List<ColumnHolder> removedColumns = schemaChange.getRemovedColumns();
        int count = removedColumns.size();
        for (int i = 0; i < count; i++) {
            onColumnRemove(removedColumns.get(i));
        }

        List<ColumnHolder> addedColumns = schemaChange.getAddedColumns();
        count = addedColumns.size();
        for (int i = 0; i < count; i++) {
            onColumnAdd(addedColumns.get(i));
        }
    }

    @Override
    public void onData() {
        if (isDataResetRequested()) {
            isDataResetRequested = false;
            isDataRefreshRequested = false;
            onDataReset();
        } else {
            IChangeQueue changeQueue = getProducer().getCurrentChanges();
            if (isDataRefreshRequested) {
                isDataRefreshRequested = false;
                onDataRefresh(changeQueue);
            } else if (changeQueue.hasChanges()) {
                onDataChange(changeQueue);
            }
        }
    }

    protected void onDataReset() {
        IRowSequence rows = getProducer().getAllRows();
        while (rows.moveNext()) {
            onRowAdd(rows.getRowId());
        }
    }

    // used when e.g. a new column is added, and therefore every row must be updated
    protected void onDataRefresh(IChangeQueue changeQueue) {
        onDataChange(changeQueue);

        ColumnFlagWrapper wrapper = new ColumnFlagWrapper(changeQueue);
        IRowSequence allRows = getProducer().getAllRows();
        while (allRows.moveNext()) {
            int rowId = allRows.getRowId();
            onRowUpdate(rowId, wrapper);
        }
    }

    protected void onDataChange(IChangeQueue changeQueue) {
        if (changeCursor == null) {
            changeCursor = changeQueue.createCursor();
        } else {
            changeCursor.reset();
        }
        while (changeQueue.getNext(changeCursor)) {
            int rowId = changeCursor.getRowId();
            switch (changeCursor.getOperation()) {
                case Add: {
                    onRowAdd(rowId);
                    break;
                }
                case Update: {
                    onRowUpdate(rowId, changeCursor);
                    break;
                }
                case Remove: {
                    onRowRemove(rowId);
                    break;
                }
            }
        }
    }

    @Override
    public void onDataClear() {
    }

    protected void onRowAdd(int row) {
    }

    protected void onRowUpdate(int row, IRowFlags rowFlags) {
    }

    protected void onRowRemove(int row) {
    }

    @Override
    public void onPluggedIn(IOutput output) {
        producer = output;
        resetSchema();

        owner.getExecutionContext().getMetadataRegistry().registerLink(output, this);
    }

    @Override
    public void unplugged(IOutput output) {
        producer = null;

        owner.getExecutionContext().getMetadataRegistry().unregisterLink(output, this);
    }

    protected void onColumnAdd(ColumnHolder columnHolder) {

    }

    protected void onColumnRemove(ColumnHolder columnHolder) {

    }

    @Override
    public IOperator getOwner() {
        return owner;
    }

    @Override
    public void ready() {
        owner.inputReady(this);
    }

    @Override
    public boolean isSchemaResetRequested() {
        return isSchemaResetRequested;
    }

    @Override
    public void onSchemaResetRequested() {
        owner.resetSchema();
    }

    @Override
    public void resetSchema() {
        isSchemaResetRequested = true;
        resetData();
    }

    @Override
    public boolean isDataResetRequested() {
        return isDataResetRequested;
    }

    @Override
    public void onDataResetRequested() {
        owner.resetData();
    }

    @Override
    public void resetData() {
        isDataResetRequested = true;
    }

    @Override
    public boolean isDataRefreshRequested() {
        return isDataRefreshRequested;
    }

    @Override
    public void onDataRefreshRequested() {
        owner.refreshData();
    }

    @Override
    public void refreshData() {
        isDataRefreshRequested = true;
    }

    @Override
    public void tearDown() {
        if (producer != null) {
            producer.unplug(this);
        }

        owner.getExecutionContext().getMetadataRegistry().unregisterInput(this);
    }

    @Override
    public void onTearDownRequested() {
        owner.tearDown();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onAfterCommit() {
    }

    @Override
    public void onInitialise() {
    }
}
