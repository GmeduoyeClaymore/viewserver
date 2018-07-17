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
import rx.Observable;
import rx.observable.ListenableFutureObservable;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


public class NettyBasicServerComponent extends  BasicServerComponents {

    private static final Logger log = LoggerFactory.getLogger(NettyBasicServerComponent.class);

    private String serverName;
    protected List<IEndpoint> endpointList;
    protected Network serverNetwork;
    protected IReactor serverReactor;

    public NettyBasicServerComponent(String serverName,List<IEndpoint> endpointList) {
        this.serverName = serverName;
        this.endpointList = endpointList;
    }

    @Override
    public Observable<Object> start() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final NettyNetworkAdapter networkAdapter = new NettyNetworkAdapter();
            final SimpleNetworkMessageWheel networkMessageWheel = new SimpleNetworkMessageWheel(new Encoder(), new Decoder());
            networkAdapter.setNetworkMessageWheel(networkMessageWheel);
            serverNetwork = new Network(this.getCommandHandlerRegistry(), this.getExecutionContext(), this.getServerCatalog(), networkAdapter);
            serverReactor = this.initReactor(serverNetwork);
            serverReactor.start();
            serverReactor.scheduleTask(() -> {
                printMemoryUsage();
            }, 1, 3 * 60 * 1000);
            return ListenableFutureObservable.from(this.getExecutionContext().submit(() -> latch.countDown(), 5), Runnable::run) ;
        } catch (Throwable e) {
            log.error("Fatal error happened during startup", e);
            throw new RuntimeException(e);
        }
    }

    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        log.info("Memory used: {}; Free memory: {}; Max memory: {}", humanReadableByteCount(runtime.totalMemory() - runtime.freeMemory(),true),
                humanReadableByteCount(runtime.freeMemory(),true), humanReadableByteCount(runtime.maxMemory(),true));
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public void listen() {
        log.info("MILESTONE: Calling server listen");
        endpointList.forEach(serverNetwork::listen);
    }

    @Override
    public void stop() {
        if(serverReactor != null){
            serverReactor.shutDown();
        }
        if(serverNetwork != null){
            serverNetwork.shutdown();
        }
    }

    private IReactor initReactor(Network serverNetwork) {
        IReactor serverReactor = new EventLoopReactor(serverName,serverNetwork);
        this.getExecutionContext().setReactor(serverReactor);
        return serverReactor;
    }
}