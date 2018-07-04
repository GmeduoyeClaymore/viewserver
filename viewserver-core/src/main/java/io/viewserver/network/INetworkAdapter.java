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

package io.viewserver.network;

import io.viewserver.reactor.INetworkMessageListener;
import io.viewserver.reactor.IReactor;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Created by bemm on 23/06/15.
 */
public interface INetworkAdapter {
    void registerListener(INetworkMessageListener listener);

    void start();

    void setReactor(IReactor reactor);

    void listen(IEndpoint endpoint);

    ListenableFuture<IChannel> connect(IEndpoint endpoint);

    String getCatalogNameForChannel(IChannel channel);

    void reset();

    IMessageManager createMessageManager(IChannel channel);

    void setNetworkMessageWheel(INetworkMessageWheel networkMessageWheel);

    void shutdown();

    INetworkMessageWheel getNetworkMessageWheel();
}
