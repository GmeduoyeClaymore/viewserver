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

package io.viewserver.network.netty.websocket;

import io.viewserver.network.netty.INettyEndpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Created by nick on 27/06/15.
 */
public class NettyWebSocketEndpoint implements INettyEndpoint {
    private static final Logger log = LoggerFactory.getLogger(NettyWebSocketEndpoint.class);
    private final String url;
    private URI uri;
    private SslContext serverSslContext;
    private boolean usingSelfSignedCertificate;
    private File keyCertChainFile;
    private File keyFile;
    private String keyPassword;
    private boolean bypassCertificateChecks;
    private final int MAX_MESSAGE_SIZE_BYTES = 5242880; //set the maximum message size to 5Mb

    public NettyWebSocketEndpoint(String url) {
        this(url, null, null);
    }

    public NettyWebSocketEndpoint(String url, File keyCertChainFile, File keyFile) {
        this(url, keyCertChainFile, keyFile, null);
    }

    public NettyWebSocketEndpoint(String url, File keyCertChainFile, File keyFile, String keyPassword) {
        this.url = url;
        this.keyCertChainFile = keyCertChainFile;
        this.keyFile = keyFile;
        this.keyPassword = keyPassword;
        initialise(url);
    }

    public NettyWebSocketEndpoint(String url, boolean bypassCertificateChecks) {
        this.url = url;
        this.bypassCertificateChecks = bypassCertificateChecks;
        initialise(url);
    }

    private void initialise(String url) {
        try {
            this.uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(url, e);
        }
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "NettyWebSocketEndpoint{" +
                "uri=" + uri +
                '}';
    }

    @Override
    public ServerBootstrap getServerBootstrap(EventLoopGroup parentGroup, EventLoopGroup childGroup, ChannelHandler handler) {
        if (this.uri.getScheme().equals("wss")) {
            if (keyCertChainFile == null) {
                log.warn("No certificate provided for WSS endpoint - will use self-signed");
                try {
                    SelfSignedCertificate certificate = new SelfSignedCertificate();
                    keyCertChainFile = certificate.certificate();
                    keyFile = certificate.privateKey();
                    usingSelfSignedCertificate = true;
                } catch (CertificateException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                serverSslContext = SslContextBuilder.forServer(keyCertChainFile, keyFile, keyPassword).build();
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }
        } else if (!this.uri.getScheme().equals("ws")) {
            throw new IllegalArgumentException("Invalid scheme '" + uri.getScheme() + "' for web socket endpoint");
        }

        ServerBootstrap server = new ServerBootstrap();
        server.group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (serverSslContext != null) {
                            pipeline.addLast(serverSslContext.newHandler(ch.alloc()));
                        }
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(1 << 30));
//                        pipeline.addLast(new WebSocketServerCompressionHandler());
                        pipeline.addLast("websocket", new WebSocketServerProtocolHandler("/", null, false, MAX_MESSAGE_SIZE_BYTES));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                                    ChannelPipeline pipeline = ctx.channel().pipeline();
                                    pipeline.addAfter("websocket", "ws-decoder-xx", new MessageToMessageDecoder<BinaryWebSocketFrame>() {
                                        @Override
                                        protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) throws Exception {
                                            out.add(msg.content().retain());
                                        }
                                    });

                                    pipeline.addAfter("websocket", "ws-encoder-xx", new MessageToMessageEncoder<ByteBuf>() {
                                        @Override
                                        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
                                            out.add(new BinaryWebSocketFrame(msg).retain());
                                        }
                                    });
                                }

                                super.userEventTriggered(ctx, evt);
                            }
                        });

                        pipeline.addLast("frameDecoder", new ChannelInboundHandlerAdapter());
                        pipeline.addLast("frameEncoder", new ChannelOutboundHandlerAdapter());
                        pipeline.addLast(handler);
                    }
                });

        server.bind(uri.getPort());
        return server;
    }

    @Override
    public IClient getClient(EventLoopGroup eventLoopGroup, ChannelHandler handler) {
        SslContext sslContext;
        if (this.uri.getScheme().equals("wss")) {
            try {
                SslContextBuilder builder = SslContextBuilder.forClient();
                if (bypassCertificateChecks || usingSelfSignedCertificate) {
                    builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                }
                sslContext = builder.build();
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }
        } else {
            sslContext = null;
        }

        Bootstrap bootstrap = new Bootstrap();
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (sslContext != null) {
                            pipeline.addLast(sslContext.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
                        }
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(1 << 30));
                        pipeline.addLast("websocket", new WebSocketClientProtocolHandler(handshaker));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                                    ChannelPipeline pipeline = ctx.channel().pipeline();
                                    pipeline.addAfter("websocket", "ws-decoder-xx", new MessageToMessageDecoder<BinaryWebSocketFrame>() {
                                        @Override
                                        protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) throws Exception {
                                            out.add(msg.content().retain());
                                        }
                                    });

                                    pipeline.addAfter("websocket", "ws-encoder-xx", new MessageToMessageEncoder<ByteBuf>() {
                                        @Override
                                        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
                                            out.add(new BinaryWebSocketFrame(msg).retain());
                                        }
                                    });
                                }

                                super.userEventTriggered(ctx, evt);
                            }
                        });

                        pipeline.addLast("frameDecoder", new ChannelInboundHandlerAdapter());
                        pipeline.addLast("frameEncoder", new ChannelOutboundHandlerAdapter());
                        pipeline.addLast(handler);
                    }
                });
        return () -> bootstrap.connect(uri.getHost(), uri.getPort());
    }

    @Override
    public String getUrl() {
        return this.url;
    }
}
