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

import io.viewserver.client.ClientSubscription;
import io.viewserver.client.ViewServerClient;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 10/02/2015.
 */
public class ViewServerClientContext {
    public ViewServerClient client;
    public ReportContext reportContext = new ReportContext();
    public Options options = new Options();
    public Map<String, ClientSubscription> subscriptions = new HashMap<>();

    public ViewServerClientContext() {
        options.setOffset(-1);
        options.setLimit(-1);
    }

    public ClientSubscription addSubscription(String name, ClientSubscription clientSubscription) {
        subscriptions.put(name, clientSubscription);
        return clientSubscription;
    }

    public void removeSubscription(String name){
        subscriptions.remove(name);
    }

    public ClientSubscription getSubscription(String name) {
        return subscriptions.get(name);
    }
}
