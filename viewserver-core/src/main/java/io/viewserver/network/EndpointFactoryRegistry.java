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

import io.viewserver.network.netty.inproc.NettyInProcEndpoint;
import io.viewserver.network.netty.ipc.NettyIpcEndpoint;
import io.viewserver.network.netty.tcp.NettyTcpEndpoint;
import io.viewserver.network.netty.websocket.NettyWebSocketEndpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nick on 11/07/15.
 */
public class EndpointFactoryRegistry {
    private static final ConcurrentHashMap<String, IEndpointFactory> factories = new ConcurrentHashMap<>();

    static {
        registerEndpointFactory("tcp", NettyTcpEndpoint::new);
        registerEndpointFactory("tcps", NettyTcpEndpoint::new);
        registerEndpointFactory("ws", NettyWebSocketEndpoint::new);
        registerEndpointFactory("wss", NettyWebSocketEndpoint::new);
        registerEndpointFactory("inproc", NettyInProcEndpoint::new);
        registerEndpointFactory("ipc", NettyIpcEndpoint::new);
    }

    private static void registerEndpointFactory(String scheme, IEndpointFactory factory) {
        factories.put(scheme, factory);
    }

    public static IEndpoint createEndpoint(String url) {
        try {
            URI uri = new URI(url);
            IEndpointFactory factory = factories.get(uri.getScheme());
            if (factory == null) {
                throw new IllegalArgumentException("No endpoint factory registered for scheme '" + uri.getScheme() + "'");
            }
            return factory.create(url);
        }catch (URISyntaxException uri){
            throw new RuntimeException(uri);
        }
    }

    public interface IEndpointFactory {
        IEndpoint create(String url) throws URISyntaxException;
    }
}
