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

package io.viewserver.report;

import io.viewserver.execution.nodes.IGraphNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nick on 02/03/2015.
 */
public abstract class GraphDefinitionBase<T> implements IGraphDefinition {
    private final List<IGraphNode> nodes = new ArrayList<>();
    private String output;

    @Override
    public List<IGraphNode> getNodes() {
        return nodes;
    }

    public T withNodes(IGraphNode... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        return (T)this;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public void setOutput(String output) {
        this.output = output;
    }

    public T withOutput(String output) {
        this.output = output;
        return (T)this;
    }
}
