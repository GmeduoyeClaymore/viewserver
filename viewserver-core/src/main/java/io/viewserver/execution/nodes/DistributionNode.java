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

import io.viewserver.datasource.DistributionMode;
import io.viewserver.distribution.IDistributionConfig;
import io.viewserver.distribution.IStripingStrategy;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.config.IOperatorConfig;

/**
 * Created by nickc on 09/12/2014.
 */
public class DistributionNode extends GraphNodeBase<DistributionNode> {
    private DistributionMode mode;
    private IStripingStrategy stripingStrategy;

    public DistributionNode(String name) {
        super(name, "Distribution");
    }

    public DistributionNode withMode(DistributionMode mode) {
        this.mode = mode;
        return this;
    }

    public DistributionNode withStripingStrategy(IStripingStrategy stripingStrategy) {
        this.stripingStrategy = stripingStrategy;
        return this;
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new IDistributionConfig() {
            @Override
            public DistributionMode getDistributionMode() {
                return mode;
            }

            @Override
            public IStripingStrategy getStripingStrategy() {
                return stripingStrategy;
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        throw new UnsupportedOperationException("Distribution node is never distributed!");
    }

    public DistributionMode getMode() {
        return mode;
    }

    public void setMode(DistributionMode mode) {
        this.mode = mode;
    }

    public IStripingStrategy getStripingStrategy() {
        return stripingStrategy;
    }

    public void setStripingStrategy(IStripingStrategy stripingStrategy) {
        this.stripingStrategy = stripingStrategy;
    }
}
