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

import java.util.List;

/**
 * Created by nickc on 25/11/2014.
 */
public class ProtoIndexConfig implements IIndexConfig {
    private Output[] outputs;

    public ProtoIndexConfig(io.viewserver.messages.config.IIndexConfig indexConfigDto) {
        final List<io.viewserver.messages.config.IIndexConfig.IOutput> outputList = indexConfigDto.getOutputs();
        int outputCount = outputList.size();
        this.outputs = new Output[outputCount];
        for (int i = 0; i < outputCount; i++) {
            final io.viewserver.messages.config.IIndexConfig.IOutput output = outputList.get(i);
            final List<io.viewserver.messages.config.IIndexConfig.IQueryHolder> queryHoldersList = output.getQueryHolders();
            final int queryHolderCount = queryHoldersList.size();
            IndexOperator.QueryHolder[] queryHolders = new IndexOperator.QueryHolder[queryHolderCount];
            for (int j = 0; j < queryHolderCount; j++) {
                final io.viewserver.messages.config.IIndexConfig.IQueryHolder queryHolder = queryHoldersList.get(j);
                final List<Integer> valuesList = queryHolder.getValues();
                final int valueCount = valuesList.size();
                int[] values = new int[valueCount];
                for (int k = 0; k < valueCount; k++) {
                    values[k] = valuesList.get(k);
                }

                queryHolders[j] = queryHolder.isExclude()
                        ? IndexOperator.QueryHolder.exclude(queryHolder.getColumnName(), values)
                        : IndexOperator.QueryHolder.include(queryHolder.getColumnName(), values);
                queryHolder.release();
            }

            this.outputs[i] = new Output(output.getName(), queryHolders);
            output.release();
        }
    }

    @Override
    public String[] getIndices() {
        return null;
    }

    @Override
    public Output[] getOutputs() {
        return outputs;
    }
}
