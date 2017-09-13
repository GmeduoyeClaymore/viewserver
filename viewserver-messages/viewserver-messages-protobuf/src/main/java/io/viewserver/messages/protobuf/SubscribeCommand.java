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

package io.viewserver.messages.protobuf;

import io.viewserver.messages.MessagePool;
import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.command.IOptions;
import io.viewserver.messages.command.ISubscribeCommand;
import io.viewserver.messages.config.IProjectionConfig;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.SubscribeCommandMessage;

/**
 * Created by nick on 03/12/15.
 */
public class SubscribeCommand extends PoolableMessage<SubscribeCommand> implements ISubscribeCommand<SubscribeCommand>,
        ICommandExtension<SubscribeCommand> {
    private SubscribeCommandMessage.SubscribeCommandDtoOrBuilder subscribeCommandDto;
    private Options options;

    SubscribeCommand() {
        super(ISubscribeCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        subscribeCommandDto = (SubscribeCommandMessage.SubscribeCommandDto) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        final SubscribeCommandMessage.SubscribeCommandDto.Builder builder = getSubscribeCommandDtoBuilder();
        if (options != null) {
            builder.setOptions(options.getBuilder());
        }
        commandDtoBuilder.setExtension(SubscribeCommandMessage.subscribeCommand, builder.buildPartial());
    }

    @Override
    public String getOperatorName() {
        return subscribeCommandDto.getOperatorName();
    }

    @Override
    public ISubscribeCommand setOperatorName(String operatorName) {
        getSubscribeCommandDtoBuilder().setOperatorName(operatorName);
        return this;
    }

    @Override
    public String getOutputName() {
        return subscribeCommandDto.hasOutputName() ? subscribeCommandDto.getOutputName() : null;
    }

    @Override
    public ISubscribeCommand setOutputName(String outputName) {
        getSubscribeCommandDtoBuilder().setOutputName(outputName);
        return this;
    }

    @Override
    public IOptions getOptions() {
        if (options == null) {
            options = subscribeCommandDto != null ? Options.fromDto(subscribeCommandDto.getOptions())
                    : (Options) MessagePool.getInstance().get(IOptions.class);
        }
        return options;
    }

    @Override
    public ISubscribeCommand setOptions(IOptions options) {
        if (this.options != null) {
            this.options.release();
        }
        this.options = (Options) options.retain();
        return this;
    }

    @Override
    public IProjectionConfig getProjection() {
        return null;
    }

    @Override
    public ISubscribeCommand setProjection(IProjectionConfig projection) {
        return null;
    }

    @Override
    protected void doRelease() {
        if (options != null) {
            options.release();
            options = null;
        }
        subscribeCommandDto = null;
    }

    private SubscribeCommandMessage.SubscribeCommandDto.Builder getSubscribeCommandDtoBuilder() {
        if (subscribeCommandDto == null) {
            subscribeCommandDto = SubscribeCommandMessage.SubscribeCommandDto.newBuilder();
        } else if (subscribeCommandDto instanceof SubscribeCommandMessage.SubscribeCommandDto) {
            subscribeCommandDto = ((SubscribeCommandMessage.SubscribeCommandDto) subscribeCommandDto).toBuilder();
        }
        return (SubscribeCommandMessage.SubscribeCommandDto.Builder) subscribeCommandDto;
    }
}
