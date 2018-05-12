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
import io.viewserver.operators.index.QueryHolderConfig;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by paulg on 12/11/2014.
 */
public class IndexOutputNode extends GraphNodeBase<IndexOutputNode> {
    private List<QueryHolderConfig> queryHolders = new ArrayList<>();
    private String outputName;
    private String dataSourceName;

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

    public IndexOutputNode withQueryHolders(QueryHolderConfig... queryHolders) {
        this.queryHolders.addAll(Arrays.asList(queryHolders));
        return this;
    }

    public IndexOutputNode withDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
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
            public String getDataSourceName() {
                return dataSourceName;
            }

            @Override
            public String[] getIndices() {
                return null;
            }

            @Override
            public OutputConfig[] getOutputs() {
                return new OutputConfig[]{new OutputConfig(parameterHelper.substituteParameterValues(getConfigForOutputName()), getParameterizedQueryHolders(parameterHelper))};
            }
        };
    }

    public QueryHolderConfig[] getParameterizedQueryHolders(ParameterHelper parameterHelper) {
        QueryHolderConfig[] queryHolderConfigs = new QueryHolderConfig[queryHolders.size()];
        int i=0;
        for(QueryHolderConfig config : queryHolders){
            Object[] newValues = config.getValues();
            int j=0;
            for(Object value : config.getValues()){
                if(value instanceof String){
                    newValues[j] = parameterHelper.substituteParameterValues((String)value);
                }else{
                    newValues[j] = value;
                }
                j++;
            }
            queryHolderConfigs[i] = new QueryHolderConfig(config.getDimension(),config.isExclude(),newValues);
            i++;
        }
        return queryHolderConfigs;
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        throw new NotImplementedException();
    }

    @JsonIgnore
    public String getConfigForOutputName() {
        if (outputName != null) {
            return outputName;
        }

        List<QueryHolderConfig> queryHolders = this.queryHolders;

        return getNameForQueryHolders(queryHolders);
    }

    public static String getNameForQueryHolders(List<QueryHolderConfig> queryHolders) {
        ArrayList<String> configStrings = new ArrayList<>();

        for (QueryHolderConfig queryHolder : queryHolders) {
            configStrings.add(queryHolder.toString());
        }

        return StringUtils.join(configStrings, ',');
    }

    public List<QueryHolderConfig> getQueryHolders() {
        return queryHolders;
    }

    public void setQueryHolders(List<QueryHolderConfig> queryHolders) {
        this.queryHolders = queryHolders;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
}
