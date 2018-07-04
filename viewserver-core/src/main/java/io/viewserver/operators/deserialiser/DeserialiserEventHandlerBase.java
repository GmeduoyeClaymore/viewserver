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

package io.viewserver.operators.deserialiser;

import io.viewserver.schema.column.IRowFlags;

/**
 * Created by bemm on 10/02/2015.
 */
public abstract class DeserialiserEventHandlerBase implements IDeserialiserEventHandler {
    @Override
    public void onSubscriptionSuccess(DeserialiserOperator deserialiserOperator) {

    }

    @Override
    public void onSubscriptionError(DeserialiserOperator deserialiserOperator, String msg) {

    }

    @Override
    public void onDataReset(DeserialiserOperator deserialiserOperator) {

    }

    @Override
    public void onSnapshotComplete(DeserialiserOperator deserialiserOperator) {

    }

    @Override
    public void onBatchComplete(DeserialiserOperator deserialiserOperator, int rowEventsCount) {

    }

    @Override
    public void onRowAdd(DeserialiserOperator deserialiserOperator, int rowId) {

    }

    @Override
    public void onRowUpdate(DeserialiserOperator deserialiserOperator, int rowId, IRowFlags rowFlags) {

    }

    @Override
    public void onRowRemove(DeserialiserOperator deserialiserOperator, int rowId) {

    }
}
