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

package io.viewserver.operators.rx;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.schema.SchemaChange;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.IRowFlags;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.HashMap;
import java.util.List;

import static io.viewserver.operators.rx.OperatorEvent.getRowDetails;

public class RxOperator extends OperatorBase {
    private Input input;
    private PublishSubject<OperatorEvent> subject;
    public RxOperator(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);

        input = new Input(Constants.IN, this);
        addInput(input);
        this.subject =  PublishSubject.create();
    }

    public Observable<OperatorEvent> getObservable(){
        return subject;
    }

    public IInput getInput() {
        return input;
    }

    private class Input extends InputBase {
        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void resetSchema() {
            log(getOwner().getName() + " - STATUS: SchemaReset");
            super.resetData();
            subject.onNext(new OperatorEvent(EventType.SCHEMA_RESET_REQUESTED,null));
        }

        @Override
        protected void onSchemaReset() {
            super.onSchemaReset();
            subject.onNext(new OperatorEvent(EventType.SCHEMA_RESET,null));
        }

        @Override
        protected void onSchemaChange(SchemaChange schemaChange) {
            if (!schemaChange.getAddedColumns().isEmpty()) {
                for (ColumnHolder addedColumn : schemaChange.getAddedColumns()) {
                    subject.onNext(new OperatorEvent(EventType.COLUMN_ADD,addedColumn));
                }
            }
            if (!schemaChange.getRemovedColumns().isEmpty()) {
                for (ColumnHolder removedColumn : schemaChange.getRemovedColumns()) {
                    subject.onNext(new OperatorEvent(EventType.COLUMN_REMOVE,removedColumn));
                }
            }
        }


        @Override
        public void resetData() {
            log(getOwner().getName() + " - STATUS: DataReset");
            super.resetData();
            subject.onNext(new OperatorEvent(EventType.DATA_RESET,null));
        }

        @Override
        protected void onRowAdd(int row)
        {
            subject.onNext(new OperatorEvent(EventType.ROW_ADD,null));
            log(getOwner().getName() + " - ADD: { " + getRowDetails(input.getProducer(),row, null) + " }");
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            log(getOwner().getName() + " - UPD: { " + row + " }");
            subject.onNext(new OperatorEvent(EventType.ROW_UPDATE,getRowDetails(input.getProducer(),row, rowFlags)));
        }

        @Override
        protected void onRowRemove(int row) {
            log(getOwner().getName() + " - REM," + row);
            subject.onNext(new OperatorEvent(EventType.ROW_REMOVE,getRowDetails(input.getProducer(), row, null)));
        }



        private void log(String message) {
            System.out.println(message);
        }
    }
}
