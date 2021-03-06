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

import io.viewserver.Constants;
import io.viewserver.changequeue.ChangeQueue;
import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.core.ExecutionContext;
import io.viewserver.execution.TableMetaData;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowFlags;
import io.viewserver.util.ViewServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import java.util.*;

import static io.viewserver.core.Utils.fromArray;
import static io.viewserver.operators.rx.OperatorEvent.getRowDetails;

/**
 * Created by bemm on 26/09/2014.
 */
public abstract class OutputBase implements IOutput, IActiveRowTracker {
    private final List<IInput> inputs = new ArrayList<>();
    private final String name;
    private final IOperator owner;
    private final ActiveRowTracker rowTracker;
    private final ColumnHolderFactory columnHolderFactory;
    private Schema schema;
    private IChangeQueue changeQueue;
    protected TableMetaData metaData;
    private PublishSubject<OperatorEvent> subject;
    private final Logger log;


    protected OutputBase(String name, IOperator owner) {
        this.name = name;
        this.owner = owner;
        log = LoggerFactory.getLogger(owner.getName() + "-" + name);
        this.rowTracker = new ActiveRowTracker(this);
        this.changeQueue = new ChangeQueue(this);
        this.schema = new Schema();
        this.schema.setOwner(this);
        this.columnHolderFactory = new ColumnHolderFactory();
        this.metaData = new TableMetaData();
        owner.getExecutionContext().getMetadataRegistry().registerOutput(this);
        this.subject = PublishSubject.create();
    }
    @Override
    public Observable<OperatorEvent>  observable(String... observedColumns){
        List<String> columns = Arrays.asList(observedColumns);
        return observable(columnId -> {
            Schema schema = getSchema();
            ColumnHolder col = schema.getColumnHolder(columnId);
            return columns.contains(col.getName());
        });
    }

    @Override
    public Observable<OperatorEvent>  observable(){
        return observable((IRowFlags)null);
    }

    @Override
    //be careful when using this it will start spamming alot of objects if you subscribe
    public Observable<OperatorEvent>  observable(IRowFlags flags){
        Observable<OperatorEvent> snapshot =  Observable.create(new Action1<Emitter<OperatorEvent>>() {
            Subscription subscription = null;
            @Override
            public void call(Emitter<OperatorEvent> subscriber) {
                try {
                    IRowSequence rows = (OutputBase.this.getAllRows());
                    this.subscription = subject.subscribe(el -> subscriber.onNext(el), err -> subscriber.onError(err), () ->{
                        subscriber.onCompleted();
                        subscription.unsubscribe();
                    });
                    while (rows.moveNext()) {
                        HashMap<String, Object> rowDetails = getRowDetails(OutputBase.this.getProducer(), rows.getRowId(), flags);
                        subscriber.onNext(new OperatorEvent(EventType.ROW_ADD, rowDetails));
                    }
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }

        }, Emitter.BackpressureMode.BUFFER);

        return snapshot;
    }


    public String getName() {
        return name;
    }

    @Override
    public String getFullName(){
        return owner.getPath() + "-" + name;
    }

    public IOperator getOwner() {
        return owner;
    }

    @Override
    public ActiveRowTracker getRowTracker() {
        return rowTracker;
    }

    @Override
    public boolean isRowActive(int row) {
        return getRowTracker().isActive(row);
    }

    @Override
    public void handleAdd(int row) {
        ExecutionContext.AssertUpdateThread();
        if (getRowTracker().getOwner() == this) {
            getRowTracker().handleAdd(row);
        }
        getCurrentChanges().handleAdd(row);
        if(subject.hasObservers()){
            log.debug("RX Subject Handling Add - " +row);
            subject.onNext(new OperatorEvent(EventType.ROW_ADD,getRowDetails(getProducer(),row,null)));
        }
    }

    protected IOutput getProducer() {
        return this;
    }

    @Override
    public void handleUpdate(int row) {
        ExecutionContext.AssertUpdateThread();
        if (!getRowTracker().isActive(row)) {
            log.warn("Cannot handle update for inactive row " + row + " active rows are " + getRowTracker());
            return;
        }
        getCurrentChanges().handleUpdate(row);
        if(subject.hasObservers()){
            log.debug("RX Subject Handling UPDATE - " +row);
            subject.onNext(new OperatorEvent(EventType.ROW_UPDATE,getRowDetails(getProducer(), row, new IRowFlags() {
                @Override
                public boolean isDirty(int columnId) {
                    return getCurrentChanges().isDirty(row,columnId);
                }
            })));
        }
    }

    @Override
    public void handleRemove(int row) {
        ExecutionContext.AssertUpdateThread();
        if (getRowTracker().getOwner() == this) {
            getRowTracker().handleRemove(row);
        }
        getCurrentChanges().handleRemove(row);
        if(subject.hasObservers()){
            log.debug("RX Subject Handling REMOVE - " +row);
            subject.onNext(new OperatorEvent(EventType.ROW_REMOVE,getRowDetails(getProducer(),row,null)));
        }
    }

    @Override
    public int getRowCount() {
        return getRowTracker().getRowCount();
    }

    @Override
    public TableMetaData getMetaData(){
        return metaData;
    }

    @Override
    public void setMetaData(TableMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public void setMetaDataValue(String key, Object value){
        metaData.put(key, value);
    }

    @Override
    public void plugIn(IInput input) {
        if (input.getProducer() != null) {
            if (input.getProducer() == this) {
                log.debug("Output {} is already plugged in to input {}", this.getName(), input.getName());
                return;
            }
            throw new ViewServerException(String.format("Another output is already plugged in to input %s", input.getName()));
        }

        input.onPluggedIn(this);
        inputs.add(input);
    }

    @Override
    public void commit() {
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            inputs.get(i).ready();
        }

        // disabled this as it's not currently used and is causing issues
//        if (getCurrentChanges().isReferenceCountingEnabled(false)) {
//            IRowSequence allRows = getAllRows();
//            while (allRows.moveNext()) {
//                int rowId = allRows.getRowId();
//                if (getCurrentChanges().getReferenceCount(rowId) == 0) {
//                    handleRemove(rowId);
//                }
//            }
//        }
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public IChangeQueue getCurrentChanges() {
        return changeQueue;
    }

    @Override
    public void tearDown() {
        List<IInput> inputsCopy = new ArrayList<>(inputs);
        int count = inputsCopy.size();
        for (int i = 0; i < count; i++) {
            IInput input = inputsCopy.get(i);
            // with some chains of operators, tearing down one input could lead to another one being torn down, so
            // check if this one is still plugged in
            if (!inputs.contains(input)) {
                continue;
            }
            unplug(input);
            input.onTearDownRequested();
        }
        clearData();

        owner.getExecutionContext().getMetadataRegistry().unregisterOutput(this);
    }

    @Override
    public void unplug(IInput input) {
        if (!inputs.contains(input)) {
            throw new IllegalStateException(input.getName() + " is not plugged in to this output");
        }

        inputs.remove(input);
        input.unplugged(this);
    }

    @Override
    public IRowSequence getAllRows() {
        return getRowTracker().getAllRows();
    }

    @Override
    public IColumnHolderFactory getColumnHolderFactory() {
        return columnHolderFactory;
    }

    @Override
    public void onAfterCommit() {
        getCurrentChanges().clear();
        if (getSchema() != null && getSchema().getChange().hasChanges()) {
            getSchema().getChange().clearChanges();
        }
    }

    @Override
    public void onInitialise() {
    }

    @Override
    public void resetSchema() {

        getCurrentChanges().handleStatus(Status.SchemaReset);
        if(subject.hasObservers()){
            log.debug("RX Subject Handling reset schema");
            subject.onNext(new OperatorEvent(EventType.SCHEMA_RESET,null));
        }
    }

    @Override
    public void clearSchema() {

        getSchema().clear();
    }

    @Override
    public void resetData() {

        getCurrentChanges().handleStatus(Status.DataReset);
        getCurrentChanges().handleStatus(Status.DataReset);
        if(subject.hasObservers()){
            log.debug("RX Subject Handling reset DATA");
            subject.onNext(new OperatorEvent(EventType.DATA_RESET,null));
        }
    }

    @Override
    public void clearData() {
        getRowTracker().clear();
    }

    @Override
    public String toString() {
        return name;
    }
}
