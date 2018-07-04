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

package io.viewserver.network.netty;

import io.viewserver.network.IEndpoint;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;

/**
 * Created by bemm on 27/06/15.
 */
public interface INettyEndpoint extends IEndpoint {
    ServerBootstrap getServerBootstrap(EventLoopGroup parentGroup, EventLoopGroup childGroup, ChannelHandler handler);

    IClient getClient(EventLoopGroup eventLoopGroup, ChannelHandler handler);

    interface IClient {
        ChannelFuture connect();
    }
}
