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

package io.viewserver.operators.index;

import io.viewserver.configurator.IOutputConfig;

/**
 * Created by bemm on 31/10/2014.
 */
public interface IIndexConfig extends IOutputConfig {

    String getDataSourceName();

    String[] getIndices();

    OutputConfig[] getOutputs();

    public static class OutputConfig {
        private final String name;
        private final QueryHolderConfig[] queryHolders;

        public OutputConfig(String name, QueryHolderConfig[] queryHolders) {
            this.name = name;
            this.queryHolders = queryHolders;
        }

        public String getName() {
            return name;
        }

        public QueryHolderConfig[] getQueryHolders() {
            return queryHolders;
        }
    }
}
