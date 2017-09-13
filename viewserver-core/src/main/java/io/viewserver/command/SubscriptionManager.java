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

package io.viewserver.command;

import io.viewserver.core.NumberUtils;
import io.viewserver.operators.serialiser.SerialiserOperator;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created by nickc on 04/11/2014.
 */
public class SubscriptionManager {
    private final TLongObjectHashMap<SerialiserOperator> subscriptions = new TLongObjectHashMap<>();

    public void registerSubscription(int connectionId, int commandId, SerialiserOperator serialiser) {
        subscriptions.put(NumberUtils.packLong(connectionId, commandId), serialiser);
    }

    public SerialiserOperator unregisterSubscription(int connectionId, int commandId) {
        return subscriptions.remove(NumberUtils.packLong(connectionId, commandId));
    }

    public SerialiserOperator getSerialiser(int connectionId, int commandId) {
        return subscriptions.get(NumberUtils.packLong(connectionId, commandId));
    }
}
