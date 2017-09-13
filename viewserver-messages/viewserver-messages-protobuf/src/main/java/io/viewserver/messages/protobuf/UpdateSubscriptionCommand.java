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
import io.viewserver.messages.command.IUpdateSubscriptionCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.UpdateSubscriptionCommandMessage;

/**
 * Created by nick on 07/12/15.
 */
public class UpdateSubscriptionCommand extends PoolableMessage<UpdateSubscriptionCommand>
    implements IUpdateSubscriptionCommand<UpdateSubscriptionCommand>, ICommandExtension<UpdateSubscriptionCommand> {
    private UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDtoOrBuilder updateSubscriptionCommandDto;
    private Options options;

    UpdateSubscriptionCommand() {
        super(IUpdateSubscriptionCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        updateSubscriptionCommandDto = (UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(UpdateSubscriptionCommandMessage.updateSubscriptionCommand, getBuilder().buildPartial());
    }

    @Override
    public int getCommandId() {
        return updateSubscriptionCommandDto.getCommandId();
    }

    @Override
    public IUpdateSubscriptionCommand setCommandId(int commandId) {
        getUpdateSubscriptionCommandDtoBuilder().setCommandId(commandId);
        return this;
    }

    @Override
    public IOptions getOptions() {
        if (options == null) {
            options = updateSubscriptionCommandDto != null ? Options.fromDto(updateSubscriptionCommandDto.getOptions())
                    : (Options) MessagePool.getInstance().get(IOptions.class);
        }
        return options;
    }

    @Override
    public IUpdateSubscriptionCommand setOptions(IOptions options) {
        if (this.options != null) {
            this.options.release();
        }
        this.options = (Options) options.retain();
        return this;
    }

    @Override
    protected void doRelease() {
        if (options != null) {
            options.release();
            options = null;
        }
        updateSubscriptionCommandDto = null;
    }

    UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDto.Builder getBuilder() {
        final UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDto.Builder builder = getUpdateSubscriptionCommandDtoBuilder();
        if (options != null) {
            builder.setOptions(options.getBuilder());
        }
        return builder;
    }

    private UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDto.Builder getUpdateSubscriptionCommandDtoBuilder() {
        if (updateSubscriptionCommandDto == null) {
            updateSubscriptionCommandDto = UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDto.newBuilder();
        } else if (updateSubscriptionCommandDto instanceof UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDto) {
            updateSubscriptionCommandDto = ((UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDto) updateSubscriptionCommandDto).toBuilder();
        }
        return (UpdateSubscriptionCommandMessage.UpdateSubscriptionCommandDto.Builder) updateSubscriptionCommandDto;
    }
}
