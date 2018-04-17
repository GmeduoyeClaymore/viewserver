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

package io.viewserver.server.steps;

import io.viewserver.datasource.DataSource;
import io.viewserver.network.EndpointFactoryRegistry;
import io.viewserver.network.IEndpoint;
import io.viewserver.server.*;
import io.viewserver.server.setup.DefaultBootstrapper;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by nick on 10/02/2015.
 */
public class InProcessViewServerContext implements IViewServerContext {
    public ViewServerMasterBase master;
    public final List<ViewServerSlave> slaves = new ArrayList<>();
    public DataSource dataSource;
    public String bootstrapperClass = DefaultBootstrapper.class.getName();
    public IViewServerMasterConfiguration masterConfiguration = new IViewServerMasterConfiguration() {
        @Override
        public String getBootstrapperClass() {
            return bootstrapperClass;
        }

        @Override
        public Iterable<IEndpoint> getMasterEndpoints() {
            try {
                return Collections.singleton(
                        EndpointFactoryRegistry.createEndpoint("ws://127.0.0.1:8080")
                );
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getMasterDatabasePath() {
            return System.getProperty("user.home") + "/viewserver_test";
        }
    };

    @Override
    public String getUrl() {
        return "inproc://master";
    }

    @Override
    public ViewServerMasterBase getMaster() {
        return master;
    }

    public List<ViewServerSlave> getSlaves() {
        return slaves;
    }

    @Override
    public void setMaster(ViewServerMasterBase mast) {
        master = (ViewServerMasterBase) mast;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getBootstrapperClass() {
        return bootstrapperClass;
    }

    @Override
    public void setBootstrapperClass(String bootstrapperClass) {
        this.bootstrapperClass = bootstrapperClass;
    }

    public IViewServerMasterConfiguration getMasterConfiguration() {
        return masterConfiguration;
    }
}
