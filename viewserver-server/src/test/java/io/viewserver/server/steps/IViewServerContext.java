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
import io.viewserver.server.IViewServerMasterConfiguration;
import io.viewserver.server.ViewServerMaster;
import io.viewserver.server.ViewServerMasterBase;
import io.viewserver.server.ViewServerSlave;

import java.util.List;

/**
 * Created by nick on 10/02/2015.
 */
public interface IViewServerContext {
    String getUrl();
    ViewServerMasterBase getMaster();
    List<ViewServerSlave> getSlaves();
    void setMaster(ViewServerMasterBase master);

    DataSource getDataSource();

    void setDataSource(DataSource dataSource);

    String getBootstrapperClass();

    void setBootstrapperClass(String bootstrapperClass);

    IViewServerMasterConfiguration getMasterConfiguration();
}
