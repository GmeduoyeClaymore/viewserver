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

package io.viewserver.network.netty.inproc;

import io.viewserver.network.netty.INettyEndpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by nick on 27/06/15.
 */
public class NettyInProcEndpoint implements INettyEndpoint {
    private final String name;
    private String url;

    public NettyInProcEndpoint(String url) throws URISyntaxException {
        this.url = url;
        URI uri = new URI(url);
        this.name = uri.getHost();
    }

    @Override
    public ServerBootstrap getServerBootstrap(EventLoopGroup parentGroup, EventLoopGroup childGroup, ChannelHandler handler) {
        ServerBootstrap server = new ServerBootstrap();
        server.group(parentGroup, childGroup)
                .channel(LocalServerChannel.class)
                .childHandler(handler);
        server.bind(new LocalAddress(name));
        return server;
    }

    @Override
    public IClient getClient(EventLoopGroup eventLoopGroup, ChannelHandler handler) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(LocalChannel.class)
                .handler(handler);
        return () -> bootstrap.connect(new LocalAddress(name));
    }


    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public String toString() {
        return "NettyInProcEndpoint{" +
                "name='" + name + '\'' +
                '}';
    }
}
