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
 * Created by nick on 10/02/2015.
 */
public interface IDeserialiserEventHandler {
    void onSubscriptionSuccess(DeserialiserOperator deserialiserOperator);

    void onSubscriptionError(DeserialiserOperator deserialiserOperator, String msg);

    void onSnapshotComplete(DeserialiserOperator deserialiserOperator);

    void onRowAdd(DeserialiserOperator deserialiserOperator, int rowId);

    void onRowUpdate(DeserialiserOperator deserialiserOperator, int rowId, IRowFlags rowFlags);

    void onRowRemove(DeserialiserOperator deserialiserOperator, int rowId);

    void onBatchComplete(DeserialiserOperator deserialiserOperator, int rowEventsCount);

    void onDataReset(DeserialiserOperator deserialiserOperator);
}
