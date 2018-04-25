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

package io.viewserver.server.steps;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.client.ClientSubscription;
import io.viewserver.client.SubscriptionEventHandlerBase;
import io.viewserver.client.ViewServerClient;
import io.viewserver.operators.validator.ValidationOperator;

import java.util.Map;

/**
 * Created by nick on 10/02/2015.
 */
public class TestSubscriptionEventHandler extends SubscriptionEventHandlerBase<ClientSubscription> {

    private ValidationOperator operator;

    public TestSubscriptionEventHandler() {
    }

    @Override
    public void onSubscriptionSuccess(ClientSubscription clientSubscription) {
        super.onSubscriptionSuccess(clientSubscription);
        ICatalog catalog = clientSubscription.getDeserialiserOperator().getCatalog();
        operator = new ValidationOperator(clientSubscription.getDeserialiserOperator().getName() + "_validator", clientSubscription.getDeserialiserOperator().getExecutionContext(), catalog);
        operator.setValidateOnCommit(false);
        clientSubscription.getDeserialiserOperator().getOutput().plugIn(operator.getInput(Constants.IN));
    }

    public ValidationOperator getValidationOperator() {
        return operator;
    }

    @Override
    public void onSnapshotComplete(ClientSubscription subscription) {
        super.onSnapshotComplete(subscription);
    }

    @Override
    public void onRowAdd(ClientSubscription subscription, int rowId, Map<String, Object> values) {
        super.onRowAdd(subscription, rowId, values);
    }

    @Override
    public void onRowUpdate(ClientSubscription subscription, int rowId, Map<String, Object> values) {
        super.onRowUpdate(subscription, rowId, values);
    }

    @Override
    public void onRowRemove(ClientSubscription subscription, int rowId) {
        super.onRowRemove(subscription, rowId);
    }
}
