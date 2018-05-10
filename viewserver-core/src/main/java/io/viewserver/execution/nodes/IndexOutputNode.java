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

package io.viewserver.execution.nodes;

import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.index.IIndexConfig;
import io.viewserver.operators.index.IndexOperator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by paulg on 12/11/2014.
 */
public class IndexOutputNode extends GraphNodeBase<IndexOutputNode> {
    private List<IndexOperator.QueryHolder> queryHolders = new ArrayList<>();
    private String outputName;

    public IndexOutputNode() {
        super();
    }

    public IndexOutputNode(String name) {
        this(name, null);
    }

    public IndexOutputNode(String name, String outputName) {
        super(name, "Index");
        this.outputName = outputName;
    }

    public IndexOutputNode withQueryHolders(IndexOperator.QueryHolder... queryHolders) {
        this.queryHolders.addAll(Arrays.asList(queryHolders));
        return this;
    }

    @JsonIgnore
    @Override
    public List<String> getDependencies() {
        return Arrays.asList(getName());
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return null;
            }

            @Override
            public Output[] getOutputs() {
                return new Output[]{new Output(getConfigForOutputName(), queryHolders.toArray(new IndexOperator.QueryHolder[queryHolders.size()]))};
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        io.viewserver.messages.config.IIndexConfig indexConfigDto = MessagePool.getInstance().get(io.viewserver.messages.config.IIndexConfig.class);
        io.viewserver.messages.config.IIndexConfig.IOutput outputDto = MessagePool.getInstance().get(io.viewserver.messages.config.IIndexConfig.IOutput.class)
                .setName(getConfigForOutputName());
        final List<io.viewserver.messages.config.IIndexConfig.IQueryHolder> queryHoldersList = outputDto.getQueryHolders();
        int queryHolderCount = this.queryHolders.size();
        for (int i = 0; i < queryHolderCount; i++) {
            final IndexOperator.QueryHolder queryHolder = queryHolders.get(i);
            io.viewserver.messages.config.IIndexConfig.IQueryHolder queryHolderDto = MessagePool.getInstance().get(io.viewserver.messages.config.IIndexConfig.IQueryHolder.class)
                    .setColumnName(queryHolder.getColumnName())
                    .setExclude(queryHolder.isExclude());
            final List<Integer> valuesList = queryHolderDto.getValues();
            final int[] values = queryHolder.getValues();
            int valueCount = values.length;
            for (int j = 0; j < valueCount; j++) {
                valuesList.add(values[j]);
            }
            queryHoldersList.add(queryHolderDto);
            queryHolderDto.release();
        }
        indexConfigDto.getOutputs().add(outputDto);
        outputDto.release();
        return indexConfigDto;
    }

    @JsonIgnore
    public String getConfigForOutputName() {
        if (outputName != null) {
            return outputName;
        }

        List<IndexOperator.QueryHolder> queryHolders = this.queryHolders;

        return getNameForQueryHolders(queryHolders);
    }

    public static String getNameForQueryHolders(List<IndexOperator.QueryHolder> queryHolders) {
        ArrayList<String> configStrings = new ArrayList<>();

        for (IndexOperator.QueryHolder queryHolder : queryHolders) {
            configStrings.add(queryHolder.toString());
        }

        return StringUtils.join(configStrings, ',');
    }

    public List<IndexOperator.QueryHolder> getQueryHolders() {
        return queryHolders;
    }

    public void setQueryHolders(List<IndexOperator.QueryHolder> queryHolders) {
        this.queryHolders = queryHolders;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }
}
