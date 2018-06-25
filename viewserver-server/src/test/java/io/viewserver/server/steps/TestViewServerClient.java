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

import io.viewserver.client.ViewServerClient;
import io.viewserver.network.Command;
import io.viewserver.network.IEndpoint;
import com.google.common.util.concurrent.SettableFuture;
import io.viewserver.network.ReconnectionSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.security.auth.Subject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by nick on 13/02/2015.
 */
public class TestViewServerClient extends ViewServerClient {
    private static final Logger log = LoggerFactory.getLogger(TestViewServerClient.class);
    private PublishSubject<Boolean> onClientClose = PublishSubject.create();

    public TestViewServerClient(String name, List<IEndpoint> endpoint) {
        super(name, endpoint, ReconnectionSettings.Times(20));
    }

    public TestViewServerClient(String name, IEndpoint endpoint) {
        super(name, Arrays.asList(endpoint), ReconnectionSettings.Times(20));
    }


    public Observable<Boolean> onClientClose() {
        return onClientClose;
    }

    public TestViewServerClient(String name, String url) throws URISyntaxException {
        super(name, url, ReconnectionSettings.Times(20));
    }

    public Future<Boolean> resetServer() {
        SettableFuture<Boolean> future = SettableFuture.create();
        Command reset = new Command("reset");
        reset.setCommandResultListener(commandResult -> {
            if (commandResult.isSuccess()) {
                log.info("Server reset!");
                future.set(true);
            } else {
                log.error("Failed to reset server");
                future.setException(new Exception("Failed to reset server - " + commandResult.getMessage()));
            }
        });

        sendCommand(reset);

        return future;
    }

    @Override
    public void close() {
        try{
            log.info("MILESTONE: Client closed");
            super.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        onClientClose.onNext(true);
        onClientClose.onCompleted();
        log.info("MILESTONE: Client closed fired");
    }
}
