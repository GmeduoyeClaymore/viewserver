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

package io.viewserver.client;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.viewserver.execution.Options;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.OutputPrinter;
import io.viewserver.operators.deserialiser.DeserialiserEventHandlerBase;
import io.viewserver.operators.deserialiser.DeserialiserOperator;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import com.google.common.util.concurrent.SettableFuture;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by nick on 10/02/2015.
 */
public class ClientSubscription extends ClientSubscriptionBase {
    private Options options;
    private ViewServerClient client;
    private boolean snapshotComplete;

    public ClientSubscription(DeserialiserOperator deserialiserOperator, Options options, ViewServerClient client) {
        super(deserialiserOperator);
        this.options = options;
        this.client = client;
        deserialiserOperator.getOutput().plugIn(new ChangeRecorder(deserialiserOperator.getName() +"_changeRecorder",deserialiserOperator.getExecutionContext(),deserialiserOperator.getCatalog()).getInput());
    }

    public ListenableFuture<io.viewserver.client.CommandResult> update(ISubscriptionOptionsUpdater updater) {
        updater.update(options);
        return client.updateSubscription(this, options);
    }

    int getCommandId() {
        return deserialiserOperator.getSubscribeCommand().getId();
    }

    public Future<List<Map<String, Object>>> getSnapshot() {
        if(this.snapshotComplete){
            return Futures.immediateFuture(getCurrentData());
        }
        if(deserialiserOperator == null){
            throw new RuntimeException("You may be trying to get  a snapshot of a closed subscription");
        }
        SettableFuture<List<Map<String, Object>>> future = SettableFuture.create();
        deserialiserOperator.addEventHandler(new DeserialiserEventHandlerBase() {
            @Override
            public void onSubscriptionError(DeserialiserOperator deserialiserOperator, String msg) {
                ClientSubscription.this.snapshotComplete = true;
                future.setException(new ViewServerClientException(msg));
            }

            @Override
            public void onSnapshotComplete(DeserialiserOperator deserialiserOperator) {
                ArrayList<Map<String, Object>> snapshot = getCurrentData();
                ClientSubscription.this.snapshotComplete = true;
                future.set(snapshot);
            }
        });
        return future;
    }

    public Observable<OperatorEvent> dataChangedObservable(){
        return deserialiserOperator.getOutput().observable();
    }

    public ArrayList<Map<String, Object>> getCurrentData() {
        ArrayList<Map<String, Object>> snapshot = new ArrayList<>();
        Schema schema = deserialiserOperator.getOutput().getSchema();
        IRowSequence allRows = deserialiserOperator.getOutput().getAllRows();
        List<ColumnHolder> columnHolders = schema.getColumnHolders();
        int count = columnHolders.size();
        while (allRows.moveNext()) {
            HashMap<String, Object> row = new HashMap<>();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                row.put(columnHolder.getName(), ColumnHolderUtils.getValue(columnHolder, allRows.getRowId()));
            }
            snapshot.add(row);
        }
        return snapshot;
    }



    public void printSnapshot() {
        OutputPrinter.printOutput(deserialiserOperator.getOutput());
    }

}
