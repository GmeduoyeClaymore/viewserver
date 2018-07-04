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

package io.viewserver.network.netty.ipc;

import io.viewserver.network.netty.INettyEndpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by bemm on 27/06/15.
 */
public class NettyIpcEndpoint implements INettyEndpoint {
    private static final Logger log = LoggerFactory.getLogger(NettyIpcEndpoint.class);
    private final String name;
    private String url;

    public NettyIpcEndpoint(String url) throws URISyntaxException {
        this.url = url;
        URI uri = new URI(url);
        this.name = uri.getHost();
    }

    @Override
    public ServerBootstrap getServerBootstrap(EventLoopGroup parentGroup, EventLoopGroup childGroup, ChannelHandler handler) {
        ServerBootstrap server = new ServerBootstrap() {
            @Override
            public ServerBootstrap channelFactory(ChannelFactory<? extends ServerChannel> channelFactory) {
                return super.channelFactory(new ChannelFactory<ServerChannel>() {
                    @Override
                    public ServerChannel newChannel() {
                        try {
                            return channelFactory.newChannel();
                        } catch (Throwable ex) {
                            return null;
                        }
                    }
                });
            }
        };
        server.group(parentGroup, childGroup)
                .channel(EpollServerDomainSocketChannel.class)
                .childHandler(handler);
        try {
            server.bind(new DomainSocketAddress(name));
        } catch (Throwable ex) {
            log.warn("Could not listen on IPC socket '" + name + "'");
            return null;
        }
        return server;
    }

    @Override
    public IClient getClient(EventLoopGroup eventLoopGroup, ChannelHandler handler) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(EpollDomainSocketChannel.class)
                .handler(handler);
        return () -> {
            try {
                return bootstrap.connect(new DomainSocketAddress(name));
            } catch (Throwable ex) {
                // TODO: work out why this kicks into some weird netty handler
                log.error("Could not connect to IPC socket '" + name + "'", ex);
                return null;
            }
        };
    }

    @Override
    public String toString() {
        return "NettyIpcEndpoint{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public String getUrl() {
        return this.url;
    }
}
