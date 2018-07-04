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

package io.viewserver.network.netty.tcp;

import io.viewserver.network.netty.INettyEndpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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

/**
 * Created by bemm on 27/06/15.
 */
public class NettyTcpEndpoint implements INettyEndpoint {
    private static final Logger log = LoggerFactory.getLogger(NettyTcpEndpoint.class);
    private boolean usingSelfSignedCertificate;
    private String url;
    private File keyCertChainFile;
    private File keyFile;
    private String keyPassword;
    private boolean bypassCertificateChecks;
    private URI uri;

    public NettyTcpEndpoint(String url) {
        this(url, null, null);
    }

    public NettyTcpEndpoint(String url, File keyCertChainFile, File keyFile) {
        this(url, keyCertChainFile, keyFile, null);
    }

    public NettyTcpEndpoint(String url, File keyCertChainFile, File keyFile, String keyPassword) {
        this.url = url;
        this.keyCertChainFile = keyCertChainFile;
        this.keyFile = keyFile;
        this.keyPassword = keyPassword;
        initialise(url);
    }

    public NettyTcpEndpoint(String url, boolean bypassCertificateChecks) {
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

    @Override
    public String toString() {
        return "NettyTcpEndpoint{" +
                "uri=" + uri +
                '}';
    }

    @Override
    public ServerBootstrap getServerBootstrap(EventLoopGroup parentGroup, EventLoopGroup childGroup, ChannelHandler handler) {
        SslContext sslContext;
        if (this.uri.getScheme().equals("tcps")) {
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
                sslContext = SslContextBuilder.forServer(keyCertChainFile, keyFile, keyPassword).build();
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }
        } else if (this.uri.getScheme().equals("tcp")) {
            sslContext = null;
        } else {
            throw new IllegalArgumentException("Invalid scheme '" + uri.getScheme() + "' for web socket endpoint");
        }

        ServerBootstrap server = new ServerBootstrap();
        server.group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (sslContext != null) {
                            pipeline.addLast(sslContext.newHandler(ch.alloc()));
                        }
                        pipeline.addLast(handler);
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true);
        server.bind(uri.getHost(), uri.getPort());
        return server;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public IClient getClient(EventLoopGroup eventLoopGroup, ChannelHandler handler) {
        SslContext sslContext;
        if (this.uri.getScheme().equals("tcps")) {
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
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (sslContext != null) {
                            pipeline.addLast(sslContext.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
                        }
                        pipeline.addLast(handler);
                    }
                });
        return () -> bootstrap.connect(uri.getHost(), uri.getPort());
    }
}
