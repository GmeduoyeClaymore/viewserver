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

import gherkin.lexer.Ru;
import io.viewserver.client.ClientSubscription;
import io.viewserver.client.ViewServerClient;
import io.viewserver.collections.BoolHashSet;
import io.viewserver.controller.ControllerUtils;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by bemm on 10/02/2015.
 */
public class ViewServerClientContext {
    private HashMap<String, ClientConnectionContext> clientConnectionsByName;
    private Map<String,String> contextParams = new HashMap<>();
    private static DateTime nowDate = new DateTime();
    private static final Logger log = LoggerFactory.getLogger(ViewServerClientContext.class);
    private List<TestViewServerClient> clientReferences;

    public ViewServerClientContext() {
        clientConnectionsByName = new HashMap<>();
        clientReferences = new ArrayList<>();
    }

    public Map<String, String> replaceParams(Map<String, String> record) {
        HashMap<String,String> result = new HashMap<>();
        for(Map.Entry<String,String> entry : record.entrySet()){
            result.put(entry.getKey(), replaceParams(entry.getValue()));
        }
        return result;
    }

    public String replaceParams(String value) {
        if(value == null || "".equals(value)){
            return value;
        }
        String result = value;
        Map<String, String> contextParams = this.contextParams;
        result = replaceParams(result, contextParams);

        for(Map.Entry<Object, Object> prop : System.getProperties().entrySet()){
            result = result.replace(String.format("{%s}",prop.getKey() + ""),prop.getValue() + "");
        }
        return (String) TestUtils.replaceReference(result);
    }

    public static String replaceParams(String result, Map<String, String> contextParams) {
        for(Map.Entry<String,String> entry: contextParams.entrySet()){
            result = result.replace(String.format("{%s}",entry.getKey()),entry.getValue());
        }
        result = result.replace("{now_date}",nowDate.toString());
        result = result.replace("{now_date+1}",nowDate.plusDays(1).toString());
        result = result.replace("{now_date+2}",nowDate.plusDays(2).toString());
        return result;
    }

    public Observable<ClientConnectionContext> create(String name, String url, String authName, String token) throws AuthenticationException{
        TestViewServerClient client = null;
        try {
            client = new TestViewServerClient(name, replaceParams(url)){
                @Override
                protected int getTimeoutInterval() {
                    return 120000;
                }
            };
            log.info("MILESTONE - Creating client {} connecting to URL {}",name, url);
            synchronized (this.clientReferences) {
                this.clientReferences.add(client);
            }
            TestViewServerClient finalClient = client;
            return client.withAuthentication(authName, token).flatMap(
                    success -> {
                        log.info("Client successfully authenticated");
                        ClientConnectionContext result = new ClientConnectionContext(name,finalClient, this);
                        this.clientConnectionsByName.put(name,result);
                        return Observable.just(result);
                    },
                    err -> {
                        try {
                            log.info("Authentication failed " + err);
                            if(!finalClient.isClosed) {
                                finalClient.close();
                                err = unwrap(err);
                                HashMap<String, Object> result = (HashMap<String, Object>) ControllerUtils.mapDefault(err.getMessage());
                                String alternativeUrl = (String) result.get("alternative");
                                if (alternativeUrl != null) {
                                    log.info("Client not authenticated but found alternative " + alternativeUrl);
                                    return create(name, alternativeUrl, authName, token);
                                } else {
                                    log.info("Client not authenticated no alternative found aborting");
                                }
                            }
                        }catch (Throwable ex2){
                           log.error("Problem with authentication",err);
                        }
                        return Observable.error(new AuthenticationException(err.getMessage()));
                    },
                    () -> {
                        finalClient.close();
                        return Observable.empty();
                    }
            );
        } catch (Exception e) {
            if(client != null){
                client.close();
            }
            throw new RuntimeException(e);
        }
    }

    private void removeClientReference(TestViewServerClient client) {
        synchronized (this.clientReferences) {
            this.clientReferences.remove(client);
        }
    }

    private Throwable unwrap(Throwable ex) {
        if(ex instanceof ExecutionException){
            return ex.getCause();
        }
        return ex;
    }

    public ClientConnectionContext get(String name){
        ClientConnectionContext clientConnectionContext = clientConnectionsByName.get(name);
        if(clientConnectionContext == null){
            throw new RuntimeException("Unable to find connected client named " + name);
        }
        return clientConnectionContext;
    }

    public void closeClients() {
        synchronized (this.clientReferences) {
            for (TestViewServerClient testViewServerClient : clientReferences) {
                try {
                    log.info("MILESTONE - Closing test client {}", testViewServerClient.getName());
                    testViewServerClient.close();
                } catch (Exception ex) {
                    log.error("Problem closing client", ex);
                }
            }
            clientConnectionsByName.clear();
            contextParams.clear();
            clientReferences.clear();
        }
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }

    public void addAllParams(Map<String, String> params) {
        contextParams.putAll(params);
    }

}

