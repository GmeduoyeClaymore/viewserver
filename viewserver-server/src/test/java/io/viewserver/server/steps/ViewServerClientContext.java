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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 10/02/2015.
 */
public class ViewServerClientContext {
    private HashMap<String, ClientConnectionContext> clientConnectionsByName;
    private Map<String,String> contextParams = new HashMap<>();

    public ViewServerClientContext() {
        clientConnectionsByName = new HashMap<>();
    }

    public Map<String, String> replaceParams(Map<String, String> record) {
        HashMap<String,String> result = new HashMap<>();
        for(Map.Entry<String,String> entry : record.entrySet()){
            result.put(entry.getKey(), replaceParams(entry.getValue()));
        }
        return result;
    }

    private String replaceParams(String value) {
        if(value == null || "".equals(value)){
            return value;
        }
        String result = value;
        for(Map.Entry<String,String> entry: contextParams.entrySet()){
            result = result.replace(String.format("{%s}",entry.getKey()),entry.getValue());
        }
        return result;
    }

    public ClientConnectionContext create(String name, String url){
        try {
            TestViewServerClient client = new TestViewServerClient(name, url);
            client.authenticate("open", "cucumber").get();
            ClientConnectionContext result = new ClientConnectionContext(name,client, this);
            this.clientConnectionsByName.put(name,result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ClientConnectionContext get(String name){
        ClientConnectionContext clientConnectionContext = clientConnectionsByName.get(name);
        if(clientConnectionContext == null){
            throw new RuntimeException("Unable to find connected client named " + name);
        }
        return clientConnectionContext;
    }

    public void closeClients() {
        for(Map.Entry<String,ClientConnectionContext> entry : clientConnectionsByName.entrySet()){
            try {
                entry.getValue().getClient().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientConnectionsByName.clear();
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }
}

