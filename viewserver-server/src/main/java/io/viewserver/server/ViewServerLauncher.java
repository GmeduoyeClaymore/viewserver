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

package io.viewserver.server;

import io.viewserver.network.EndpointFactoryRegistry;
import io.viewserver.network.IEndpoint;
import io.viewserver.server.setup.BootstrapperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by paulg on 23/09/2014.
 */

public class ViewServerLauncher<TConfiguration extends IViewServerConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(ViewServerLauncher.class);
    private IViewServerConfiguration configuration;
    private IViewServerMasterFactory masterFactory;

    public ViewServerLauncher() {
        this(ViewServerMaster::new);
    }

    public ViewServerLauncher(TConfiguration configuration) {
        this(configuration, ViewServerMaster::new);
    }

    public ViewServerLauncher(IViewServerMasterFactory<TConfiguration> masterFactory) {
        this((TConfiguration)new XmlViewServerConfiguration(System.getProperty("viewserver.configurationFile", "config.xml")), masterFactory);
    }

    public ViewServerLauncher(TConfiguration configuration, IViewServerMasterFactory<TConfiguration> masterFactory) {
        this.configuration = configuration;
        this.masterFactory = masterFactory;
    }

    public void launch(String[] args) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error("An uncaught exception occurred on thread " + t.getId() + " [" + t.getName() + "]", e);
            }
        });

        logger.info("Working directory: {}", new File(".").getAbsolutePath());

        if (args.length > 0) {
            if ("bootstrap".equals(args[0])) {
                BootstrapperBase.bootstrap(configuration);
                return;
            }
        }

        boolean isMaster = configuration.isMaster();
        int noSlaves = configuration.getNumberOfSlaves();

        if (isMaster) {
            ViewServerMaster viewServerMaster = masterFactory.createMaster("master", configuration);
            viewServerMaster.run();
        }

        if (noSlaves > 0) {
            logger.info("Loading {} slaves", noSlaves);

            IEndpoint endpoint = null;
            if (isMaster) {
                try {
                    endpoint = EndpointFactoryRegistry.createEndpoint("inproc://master");
                } catch (URISyntaxException e) {
                }
            } else {
                endpoint = configuration.getMasterEndpoint();
            }

            for (int i = 0; i < noSlaves; i++) {
                ViewServerSlave slave = new ViewServerSlave(String.format("slave%d", i), endpoint);
                slave.run();
            }
        }

        if (!isMaster && noSlaves == 0) {
            logger.info("Not master and not starting any slaves - exiting");
            return;
        }
    }

    public interface IViewServerMasterFactory<TConfiguration extends IViewServerConfiguration> {
        ViewServerMaster createMaster(String name, TConfiguration viewServerConfiguration);
    }
}
