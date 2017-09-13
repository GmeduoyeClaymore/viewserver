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

import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.command.IUnsubscribeCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.UnsubscribeCommandMessage;

/**
 * Created by nick on 07/12/15.
 */
public class UnsubscribeCommand extends PoolableMessage<UnsubscribeCommand> implements IUnsubscribeCommand<UnsubscribeCommand>,
    ICommandExtension<UnsubscribeCommand> {
    private UnsubscribeCommandMessage.UnsubscribeCommandDtoOrBuilder unsubscribeCommandDto;

    UnsubscribeCommand() {
        super(IUnsubscribeCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        unsubscribeCommandDto = (UnsubscribeCommandMessage.UnsubscribeCommandDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(UnsubscribeCommandMessage.unsubscribeCommand, getUnsubscribeCommandDtoBuilder().buildPartial());
    }

    @Override
    public int getSubscriptionId() {
        return unsubscribeCommandDto.getSubscriptionId();
    }

    @Override
    public IUnsubscribeCommand setSubscriptionId(int subscriptionId) {
        getUnsubscribeCommandDtoBuilder().setSubscriptionId(subscriptionId);
        return this;
    }

    @Override
    protected void doRelease() {
        unsubscribeCommandDto = null;
    }

    private UnsubscribeCommandMessage.UnsubscribeCommandDto.Builder getUnsubscribeCommandDtoBuilder() {
        if (unsubscribeCommandDto == null) {
            unsubscribeCommandDto = UnsubscribeCommandMessage.UnsubscribeCommandDto.newBuilder();
        } else if (unsubscribeCommandDto instanceof UnsubscribeCommandMessage.UnsubscribeCommandDto) {
            unsubscribeCommandDto = ((UnsubscribeCommandMessage.UnsubscribeCommandDto) unsubscribeCommandDto).toBuilder();
        }
        return (UnsubscribeCommandMessage.UnsubscribeCommandDto.Builder) unsubscribeCommandDto;
    }
}
