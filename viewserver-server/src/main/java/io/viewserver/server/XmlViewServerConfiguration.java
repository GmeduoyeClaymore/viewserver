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

import io.viewserver.core.Utils;
import io.viewserver.network.EndpointFactoryRegistry;
import io.viewserver.network.IEndpoint;
import io.viewserver.server.setup.DefaultBootstrapper;
import org.apache.commons.configuration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.stream.Collectors;

/**
 * Created by nick on 12/08/15.
 */
public class XmlViewServerConfiguration implements IViewServerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(XmlViewServerConfiguration.class);
    public static final String VIEWSERVER_SERVERS_MASTER_BOOTSTRAPPER = "viewserver.servers.master.bootstrapper";
    public static final String VIEWSERVER_SERVERS_MASTER_ENDPOINTS_ENDPOINT = "viewserver.servers.master.endpoints.endpoint";
    public static final String VIEWSERVER_SERVERS_MASTER_DATABASE = "viewserver.servers.master.database";
    public static final String VIEWSERVER_SERVERS_MASTER = "viewserver.servers.master";
    public static final String VIEWSERVER_SERVERS_SLAVES = "viewserver.servers.slaves";
    public static final String VIEWSERVER_LICENSEFILE = "viewserver.licensefile";
    public static final String VIEWSERVER_SERVERS_SLAVES_MASTER_URL = "viewserver.servers.slaves[@masterUrl]";
    protected final CompositeConfiguration configuration;

    public XmlViewServerConfiguration(String configurationFile) {
        configuration = new CompositeConfiguration();
        try {
            configuration.addConfiguration(new XMLConfiguration(configurationFile));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        configuration.addConfiguration(new SystemConfiguration());

        // TODO: get rid of the static variable
        Utils.Configuration = configuration;

        log.info("Using configuration file: {}", configurationFile);
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public String getBootstrapperClass() {
        return configuration.getString(VIEWSERVER_SERVERS_MASTER_BOOTSTRAPPER, DefaultBootstrapper.class.getName());
    }

    @Override
    public Iterable<IEndpoint> getMasterEndpoints() {
        return configuration.getList(VIEWSERVER_SERVERS_MASTER_ENDPOINTS_ENDPOINT).stream().map(x -> {
            try {
                return EndpointFactoryRegistry.createEndpoint((String) x);
            } catch (URISyntaxException e) {
                log.error("Could not create endpoint from configuration", e);
                return null;
            }
        }).filter(x -> x != null).collect(Collectors.toList());
    }



    @Override
    public String getMasterDatabasePath() {
        return configuration.getString(VIEWSERVER_SERVERS_MASTER_DATABASE, "%user.home%/viewserver");
    }

    @Override
    public boolean isMaster() {
        return configuration.getKeys(VIEWSERVER_SERVERS_MASTER).hasNext();
    }



    @Override
    public int getNumberOfSlaves() {
        return configuration.getInt(VIEWSERVER_SERVERS_SLAVES, 0);
    }

    @Override
    public IEndpoint getMasterEndpoint() {
        try {
            return EndpointFactoryRegistry.createEndpoint(configuration.getString(VIEWSERVER_SERVERS_SLAVES_MASTER_URL));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
