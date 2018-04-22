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

import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.execution.TableMetaData;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.schema.Schema;
import rx.Observable;

/**
 * Created by nickc on 26/09/2014.
 */
public interface IOutput extends IActiveRowTracker {
    //be careful when using this it will start spamming alot of objects if you subscribe
    Observable<OperatorEvent> observable();

    String getName();

    String getFullName();

    IOperator getOwner();

    void handleAdd(int row);

    void handleUpdate(int row);

    void handleRemove(int row);

    void commit();

    Schema getSchema();

    void plugIn(IInput input);

    void tearDown();

    void unplug(IInput input);

    ActiveRowTracker getRowTracker();

    boolean isRowActive(int row);

    IColumnHolderFactory getColumnHolderFactory();

    void onAfterCommit();

    void onInitialise();

    IChangeQueue getCurrentChanges();

    void clearSchema();

    void resetSchema();

    void clearData();

    void resetData();

    public TableMetaData getMetaData();

    public void setMetaData(TableMetaData metaData);

    public void setMetaDataValue(String key, Object value);
}
