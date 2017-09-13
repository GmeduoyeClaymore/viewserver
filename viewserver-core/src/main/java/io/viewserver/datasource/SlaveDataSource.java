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

package io.viewserver.datasource;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by nick on 23/10/15.
 */
public class SlaveDataSource extends DataSourceBase {
    @Override
    @JsonIgnore
    public DistributionMode getDistributionMode() {
        return DistributionMode.Local;
    }

    public static SlaveDataSource from(IDataSource dataSource) {
        SlaveDataSource slaveDataSource = new SlaveDataSource();
        slaveDataSource.setName(dataSource.getName());
        slaveDataSource.setSchema(dataSource.getSchema());
        slaveDataSource.setCalculatedColumns(dataSource.getCalculatedColumns());
        slaveDataSource.setDimensions(dataSource.getDimensions());
        slaveDataSource.nodes = dataSource.getNodes();
        slaveDataSource.setOutput(dataSource.getOutput());
        slaveDataSource.setOptions(dataSource.getOptions());
        return slaveDataSource;
    }
}
