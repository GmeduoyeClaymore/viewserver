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

import io.viewserver.execution.nodes.IGraphNode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by nick on 23/10/15.
 */
public abstract class DataSourceBase implements IDataSource {
    protected List<Dimension> dimensions;
    protected SchemaConfig schema;
    protected String output;
    private String finalOutput;
    protected String name;
    protected List<IGraphNode> nodes;
    protected List<CalculatedColumn> calculatedColumns;
    protected EnumSet<DataSourceOption> options;

    public DataSourceBase() {
        nodes = new ArrayList<>();
        options = EnumSet.noneOf(DataSourceOption.class);
        dimensions = new ArrayList<>();
        calculatedColumns = new ArrayList<>();
    }

    @Override
    public String getOutput() {
        if (output == null) {
            return String.format("#%s", name);
        }
        return output;
    }

    @Override
    public void setOutput(String output) {
        this.finalOutput = this.output = output;
    }

    @Override
    @JsonIgnore
    public String getFinalOutput() {
        return finalOutput;
    }

    @Override
    public void setFinalOutput(String finalOutput) {
        this.finalOutput = finalOutput;
    }

    @Override
    public List<Dimension> getDimensions() {
        return dimensions;
    }

    @Override
    public SchemaConfig getSchema() {
        return schema;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<CalculatedColumn> getCalculatedColumns() {
        return calculatedColumns;
    }

    @Override
    public EnumSet<DataSourceOption> getOptions() {
        return options;
    }

    @Override
    public List<IGraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<IGraphNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Dimension getDimension(String name) {
        int size = dimensions.size();
        for (int i = 0; i < size; i++) {
            Dimension dimension = dimensions.get(i);
            if (dimension.getName().equals(name)) {
                return dimension;
            }
        }
        return null;
    }

    //Setters used by ObjectMapper when loading from json file
    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public void setSchema(SchemaConfig schema) {
        this.schema = schema;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCalculatedColumns(List<CalculatedColumn> calculatedColumns) {
        this.calculatedColumns = calculatedColumns;
    }

    @Override
    public CalculatedColumn getCalculatedColumn(String name) {
        int count = calculatedColumns.size();
        for (int i = 0; i < count; i++) {
            CalculatedColumn calculatedColumn = calculatedColumns.get(i);
            if (calculatedColumn.getName().equals(name)) {
                return calculatedColumn;
            }
        }
        return null;
    }

    public void setOptions(EnumSet<DataSourceOption> options) {
        this.options = options;
    }

    @Override
    @JsonIgnore
    public String getCalcsName() {
        return name + "_calcs";
    }

    @Override
    @JsonIgnore
    public String getIndexName() {
        return name + "_index";
    }

    @Override
    public boolean hasOption(DataSourceOption option) {
        return options.contains(option);
    }
}
