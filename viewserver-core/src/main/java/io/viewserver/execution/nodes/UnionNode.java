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
import io.viewserver.operators.union.IUnionConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paulg on 10/12/2014.
 */
public class UnionNode extends GraphNodeBase<UnionNode> {

    List<IUnionConfig.Input> inputs = new ArrayList<>();

    public UnionNode() {
        super();
    }

    public UnionNode(String name) {
        super(name, "Union");
    }

    public UnionNode withInput(IUnionConfig.Input input) {
        inputs.add(input);
        return this;
    }

    public UnionNode withInput(String name, int sourceId) {
        inputs.add(new IUnionConfig.Input(name, sourceId));
        return this;
    }

    @Override
    public Object getConfig(final ParameterHelper parameterHelper) {
        return new IUnionConfig() {
            @Override
            public Input[] getInputs() {
                return inputs.toArray(new Input[inputs.size()]);
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        io.viewserver.messages.config.IUnionConfig builder = MessagePool.getInstance().get(io.viewserver.messages.config.IUnionConfig.class);

        final List<io.viewserver.messages.config.IUnionConfig.IInput> inputsList = builder.getInputs();
        int count = this.inputs.size();
        for (int i = 0; i < count; i++) {
            final IUnionConfig.Input input = this.inputs.get(i);
            final io.viewserver.messages.config.IUnionConfig.IInput inputDto = MessagePool.getInstance().get(io.viewserver.messages.config.IUnionConfig.IInput.class)
                    .setName(input.getName())
                    .setSourceId(input.getSourceId());
            inputsList.add(inputDto);
            inputDto.release();
        }

        return builder;
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        return String.format("union:%s", inputs.toString());
    }

    public List<IUnionConfig.Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<IUnionConfig.Input> inputs) {
        this.inputs = inputs;
    }
}
