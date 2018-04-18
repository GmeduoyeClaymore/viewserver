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

package io.viewserver.server.components;

import io.viewserver.messages.protobuf.Decoder;
import io.viewserver.messages.protobuf.Encoder;
import io.viewserver.network.IEndpoint;
import io.viewserver.network.Network;
import io.viewserver.network.SimpleNetworkMessageWheel;
import io.viewserver.network.netty.NettyNetworkAdapter;
import io.viewserver.reactor.EventLoopReactor;
import io.viewserver.reactor.IReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class NettyBasicServerComponent extends  BasicServerComponents {

    private static final Logger log = LoggerFactory.getLogger(NettyBasicServerComponent.class);

    private List<IEndpoint> endpointList;

    public NettyBasicServerComponent(List<IEndpoint> endpointList) {
        this.endpointList = endpointList;
    }

    @Override
    public void start() {
        try {
            final NettyNetworkAdapter networkAdapter = new NettyNetworkAdapter();
            final SimpleNetworkMessageWheel networkMessageWheel = new SimpleNetworkMessageWheel(new Encoder(), new Decoder());
            networkAdapter.setNetworkMessageWheel(networkMessageWheel);
            Network serverNetwork = new Network(this.getCommandHandlerRegistry(), this.getExecutionContext(), this.getServerCatalog(), networkAdapter);
            IReactor serverReactor = this.initReactor(serverNetwork);
            serverReactor.start();
            serverReactor.scheduleTask(() -> {
                Runtime runtime = Runtime.getRuntime();
                log.info("Memory used: {}; Free memory: {}; Max memory: {}", runtime.totalMemory() - runtime.freeMemory(),
                        runtime.freeMemory(), runtime.maxMemory());
            }, 1, 3 * 60 * 1000);

            endpointList.forEach(serverNetwork::listen);
        } catch (Throwable e) {
            log.error("Fatal error happened during startup", e);
        }
    }

    private IReactor initReactor(Network serverNetwork) {
        IReactor serverReactor = new EventLoopReactor("main",serverNetwork);
        this.getExecutionContext().setReactor(serverReactor);
        return serverReactor;
    }
}