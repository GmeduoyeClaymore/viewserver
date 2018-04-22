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

package io.viewserver.blotter;

import io.viewserver.changequeue.ChangeQueue;
import io.viewserver.operators.IInput;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.IRowSequence;
import io.viewserver.persistence.IPersistentStore;
import io.viewserver.schema.column.IRowFlags;

/**
 * Created by nickc on 27/11/2014.
 */
public class PersistenceInput implements IInput {
    private IOutput producer;
    private boolean isSchemaResetRequested;
    private String name;
    private IPersistentStore persistentStore;
    private boolean isDataResetRequested;
    private ChangeQueue.Cursor cursor;
    private boolean handleRemoves;

    public PersistenceInput(String name, IPersistentStore persistentStore) {
        this.name = name;
        this.persistentStore = persistentStore;
    }

    @Override
    public IOutput getProducer() {
        return producer;
    }

    @Override
    public void onSchema() {
        if (isSchemaResetRequested) {
            isSchemaResetRequested = false;

            persistentStore.createTable(name, producer.getSchema());
        }
    }

    @Override
    public void onPluggedIn(IOutput output) {
        producer = output;
        resetSchema();
    }

    @Override
    public IOperator getOwner() {
        return null;
    }

    @Override
    public void ready() {
        onSchema();
        onData();
    }

    @Override
    public void onData() {
        if (isDataResetRequested) {
            isDataResetRequested = false;

            IRowSequence allRows = producer.getAllRows();
            while (allRows.moveNext()) {
                onRowAdd(allRows.getRowId());
            }
        } else if (producer.getCurrentChanges().hasChanges()) {
            if (cursor == null) {
                cursor = producer.getCurrentChanges().createCursor();
            } else {
                cursor.reset();
            }
            while (producer.getCurrentChanges().getNext(cursor)) {
                int rowId = cursor.getRowId();
                switch (cursor.getOperation()) {
                    case Add: {
                        onRowAdd(rowId);
                        break;
                    }

                    case Update: {
                        onRowUpdate(rowId, cursor);
                        break;
                    }

                    case Remove: {
                        if (handleRemoves) {
                            onRowRemove(rowId);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void onRowAdd(int rowId) {
        persistentStore.addRow(name, rowId);
    }

    private void onRowUpdate(int rowId, IRowFlags rowFlags) {
        persistentStore.updateRow(name, rowId, rowFlags);
    }

    private void onRowRemove(int rowId) {
        persistentStore.removeRow(name, rowId);
    }

    @Override
    public boolean isSchemaResetRequested() {
        return false;
    }

    @Override
    public void onSchemaResetRequested() {

    }

    @Override
    public void resetSchema() {
        isSchemaResetRequested = true;
        resetData();
    }

    @Override
    public boolean isDataResetRequested() {
        return false;
    }

    @Override
    public void resetData() {
        isDataResetRequested = true;
    }

    @Override
    public void onDataResetRequested() {

    }

    @Override
    public boolean isDataRefreshRequested() {
        return false;
    }

    @Override
    public void onDataRefreshRequested() {

    }

    @Override
    public void refreshData() {

    }

    @Override
    public void tearDown() {

    }

    @Override
    public void onTearDownRequested() {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void unplugged(IOutput output) {
        producer = null;
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public void onAfterCommit() {

    }

    @Override
    public void onInitialise() {

    }

    @Override
    public void onDataClear() {

    }

    public void setHandleRemoves(boolean handleRemoves) {
        this.handleRemoves = handleRemoves;
    }
}
