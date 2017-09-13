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

import io.viewserver.execution.Options;
import io.viewserver.operators.deserialiser.DeserialiserOperator;

/**
 * Created by nick on 14/07/15.
 */
public class ClientSubscriptionBase implements AutoCloseable {
    protected DeserialiserOperator deserialiserOperator;

    public ClientSubscriptionBase(DeserialiserOperator deserialiserOperator) {
        this.deserialiserOperator = deserialiserOperator;
    }

    @Override
    public void close() throws Exception {
        final DeserialiserOperator tempDeserialiserOperator = deserialiserOperator;
        deserialiserOperator = null;
        if (tempDeserialiserOperator != null) {
            tempDeserialiserOperator.getExecutionContext().getReactor().scheduleTask(tempDeserialiserOperator::tearDown, 0, -1);
        }
    }

    public interface ISubscriptionOptionsUpdater {
        void update(Options options);
    }
}
