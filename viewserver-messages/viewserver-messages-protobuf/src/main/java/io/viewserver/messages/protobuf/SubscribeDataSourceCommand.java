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
import io.viewserver.messages.command.ISubscribeDataSourceCommand;
import io.viewserver.messages.config.IProjectionConfig;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.SubscribeDataSourceCommandMessage;

/**
 * Created by nick on 03/12/15.
 */
public class SubscribeDataSourceCommand extends PoolableMessage<SubscribeDataSourceCommand> implements ISubscribeDataSourceCommand<SubscribeDataSourceCommand>,
        ICommandExtension<SubscribeDataSourceCommand> {
    private SubscribeDataSourceCommandMessage.SubscribeDataSourceCommandDtoOrBuilder subscribeDataSourceCommandDto;
    private Options options;

    SubscribeDataSourceCommand() {
        super(ISubscribeDataSourceCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        subscribeDataSourceCommandDto = (SubscribeDataSourceCommandMessage.SubscribeDataSourceCommandDto) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        final SubscribeDataSourceCommandMessage.SubscribeDataSourceCommandDto.Builder builder = getSubscribeCommandDtoBuilder();
        if (options != null) {
            builder.setOptions(options.getBuilder());
        }
        commandDtoBuilder.setExtension(SubscribeDataSourceCommandMessage.subscribeDataSourceCommand, builder.buildPartial());
    }

    @Override
    public String getDataSourceName() {
        return subscribeDataSourceCommandDto.getDataSourceName();
    }

    @Override
    public ISubscribeDataSourceCommand setDataSourceName(String dataSourceName) {
        getSubscribeCommandDtoBuilder().setDataSourceName(dataSourceName);
        return this;
    }

    @Override
    public String getOutputName() {
        return subscribeDataSourceCommandDto.hasOutputName() ? subscribeDataSourceCommandDto.getOutputName() : null;
    }

    @Override
    public ISubscribeDataSourceCommand setOutputName(String outputName) {
        getSubscribeCommandDtoBuilder().setOutputName(outputName);
        return this;
    }

    @Override
    public IOptions getOptions() {
        if (options == null) {
            options = subscribeDataSourceCommandDto != null ? Options.fromDto(subscribeDataSourceCommandDto.getOptions())
                    : (Options) MessagePool.getInstance().get(IOptions.class);
        }
        return options;
    }

    @Override
    public ISubscribeDataSourceCommand setOptions(IOptions options) {
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
    public ISubscribeDataSourceCommand setProjection(IProjectionConfig projection) {
        return null;
    }

    @Override
    protected void doRelease() {
        if (options != null) {
            options.release();
            options = null;
        }
        subscribeDataSourceCommandDto = null;
    }

    private SubscribeDataSourceCommandMessage.SubscribeDataSourceCommandDto.Builder getSubscribeCommandDtoBuilder() {
        if (subscribeDataSourceCommandDto == null) {
            subscribeDataSourceCommandDto = SubscribeDataSourceCommandMessage.SubscribeDataSourceCommandDto.newBuilder();
        } else if (subscribeDataSourceCommandDto instanceof SubscribeDataSourceCommandMessage.SubscribeDataSourceCommandDto) {
            subscribeDataSourceCommandDto = ((SubscribeDataSourceCommandMessage.SubscribeDataSourceCommandDto) subscribeDataSourceCommandDto).toBuilder();
        }
        return (SubscribeDataSourceCommandMessage.SubscribeDataSourceCommandDto.Builder) subscribeDataSourceCommandDto;
    }
}
