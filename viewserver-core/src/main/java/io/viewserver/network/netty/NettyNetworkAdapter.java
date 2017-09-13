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

import io.viewserver.messages.IMessage;
import io.viewserver.network.*;
import io.viewserver.reactor.INetworkMessageListener;
import io.viewserver.reactor.IReactor;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by nick on 26/06/15.
 */
public class NettyNetworkAdapter implements INetworkAdapter {
    private static final Logger log = LoggerFactory.getLogger(NettyNetworkAdapter.class);
    private NioEventLoopGroup clientWorkerGroup;
    private final CopyOnWriteArrayList<INetworkMessageListener> listeners;
    private INetworkMessageWheel networkMessageWheel;
    private boolean listening;
    private NioEventLoopGroup parentGroup;
    private NioEventLoopGroup handlers;
    private CopyOnWriteArrayList<ServerBootstrap> servers;
    private IReactor reactor;

    public NettyNetworkAdapter() {
        listeners = new CopyOnWriteArrayList<>();
        servers = new CopyOnWriteArrayList<>();
    }

    @Override
    public void registerListener(INetworkMessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void start() {
    }

    @Override
    public void setReactor(IReactor reactor) {
        this.reactor = reactor;
    }

    public NioEventLoopGroup getClientWorkerGroup() {
        if (clientWorkerGroup == null) {
            clientWorkerGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(String.format("io-%s-client", reactor.getName())));
        }
        return clientWorkerGroup;
    }

    @Override
    public void listen(IEndpoint endpoint) {
        if (parentGroup == null) {
            parentGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(String.format("io-%s-server-boss", reactor.getName())));
            handlers = new NioEventLoopGroup(1, new DefaultThreadFactory(String.format("io-%s-server-worker", reactor.getName())));
        }

        ServerBootstrap serverBootstrap = ((INettyEndpoint) endpoint).getServerBootstrap(parentGroup, handlers, new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        log.debug("New connection on channel {}", ctx.channel());
                        NettyChannel channel = new NettyChannel(ctx.channel());
                        for (INetworkMessageListener listener : listeners) {
                            listener.onConnection(channel);
                        }

                        super.channelActive(ctx);
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        log.debug("Disconnection on channel {}", ctx.channel());
                        NettyChannel channel = new NettyChannel(ctx.channel());
                        for (INetworkMessageListener listener : listeners) {
                            listener.onDisconnection(channel);
                        }

                        super.channelInactive(ctx);
                    }
                });
                new NettyPipelineInitialiser(networkMessageWheel).initChannel(ch);
            }
        });
        servers.add(serverBootstrap);
    }

    @Override
    public ListenableFuture<IChannel> connect(IEndpoint endpoint) {
        SettableFuture<IChannel> promise = SettableFuture.create();
        final INettyEndpoint.IClient client = ((INettyEndpoint) endpoint).getClient(getClientWorkerGroup(), new NettyPipelineInitialiser(networkMessageWheel));
        ChannelFuture channelFuture = client.connect();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    NettyChannel channel = new NettyChannel(future.channel());
                    promise.set(channel);
                } else {
                    promise.setException(future.cause());
                }
            }
        });
        return promise;
    }

    @Override
    public String getCatalogNameForChannel(IChannel channel) {
        return String.format("%08x", channel.hashCode());
    }

    @Override
    public void reset() {
//        Set<Channel> parents = new HashSet<>();
//        for (Channel activeChannel : activeChannels) {
//            try {
////                if (activeChannel.parent() != null) {
////                    parents.add(activeChannel.parent());
////                }
//                activeChannel.close().sync();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        for (Channel parent : parents) {
//            try {
//                parent.close().sync();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        try {
            NioEventLoopGroup handlers = this.handlers;
            this.handlers = null;
            if (handlers != null) {
                log.debug("Shutting down Netty handlers");
                handlers.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
            }
            NioEventLoopGroup parentGroup = this.parentGroup;
            this.parentGroup = null;
            if (parentGroup != null) {
                log.debug("Shutting down Netty parent group");
                parentGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
            }
            NioEventLoopGroup clientWorkerGroup = this.clientWorkerGroup;
            this.clientWorkerGroup = null;
            if (clientWorkerGroup != null) {
                log.debug("Shutting down Netty client group");
                clientWorkerGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("Shut down Netty");

        listeners.clear();
    }

    @Override
    public IMessageManager createMessageManager(IChannel channel) {
        return new IMessageManager() {
            private IPeerSession peerSession;

            @Override
            public void setPeerSession(IPeerSession peerSession) {
                this.peerSession = peerSession;
            }

            @Override
            public void sendMessage(IMessage message) {
                message.retain();
                ((NettyChannel) channel).getChannel().writeAndFlush(message);
            }
        };
    }

    @Override
    public void setNetworkMessageWheel(INetworkMessageWheel networkMessageWheel) {
        this.networkMessageWheel = networkMessageWheel;
    }

    @Override
    public void shutdown() {
        reset();
    }

    @Override
    public INetworkMessageWheel getNetworkMessageWheel() {
        return networkMessageWheel;
    }
}
