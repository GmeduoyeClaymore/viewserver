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

package io.viewserver.logging.log4j.log4j;

import io.viewserver.client.CommandResult;
import io.viewserver.client.ViewServerClient;
import io.viewserver.network.Command;
import io.viewserver.network.Network;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.MultiThreadedEventLoopReactor;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.logging.log4j.ThreadContext;

import java.net.URISyntaxException;

/**
 * Created by nick on 30/09/15.
 */
public class LoggerClient extends ViewServerClient {
    public LoggerClient(String name, String url) throws URISyntaxException {
        super(name, url);
    }

    @Override
    protected IReactor initReactor(Network serverNetwork) {
        IReactor reactor = super.initReactor(serverNetwork);
        reactor.scheduleTask(() -> {
            ThreadContext.put("loggerReactor", "true");
        }, 0, -1);
        return reactor;
    }

}
