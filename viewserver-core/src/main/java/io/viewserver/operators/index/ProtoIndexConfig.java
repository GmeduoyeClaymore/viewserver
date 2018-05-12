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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

import static io.viewserver.core.Utils.toArray;

/**
 * Created by bemm on 25/11/2014.
 */
public class ProtoIndexConfig implements IIndexConfig {
    private OutputConfig[] outputs;

    public ProtoIndexConfig(io.viewserver.messages.config.IIndexConfig indexConfigDto) {
        throw new NotImplementedException();
    }

    @Override
    public String getDataSourceName() {
        throw new NotImplementedException();
    }

    @Override
    public String[] getIndices() {
        return null;
    }

    @Override
    public OutputConfig[] getOutputs() {
        return outputs;
    }
}
