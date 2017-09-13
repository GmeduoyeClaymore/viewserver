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

package io.viewserver.execution.context;

import io.viewserver.datasource.IDataSource;

import java.util.Map;

/**
 * Created by paulg on 31/10/2014.
 */

public class DataSourceExecutionPlanContext extends ExecutionPlanContextBase{
    private IDataSource dataSource;

    public DataSourceExecutionPlanContext(IDataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public IDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public boolean shouldHashNames() {
        return false;
    }

    @Override
    public Map<String, String> getDefaultNodeNames() {
        final Map<String, String> defaultNodeNames = super.getDefaultNodeNames();
        defaultNodeNames.put(dataSource.getName(), dataSource.getName());
        return defaultNodeNames;
    }
}
