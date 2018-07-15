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

import gnu.trove.set.hash.TIntHashSet;
import io.viewserver.messages.IMessage;
import io.viewserver.messages.command.IAuthenticateCommand;
import io.viewserver.messages.command.ICommand;
import io.viewserver.messages.protobuf.dto.AuthenticateCommandMessage;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.MessageMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bemm on 02/12/15.
 */
public class Tests {

    @Test
    public void test() {
        final MessageMessage.MessageDto.Builder messageDtoBuilder = MessageMessage.MessageDto.newBuilder();
        final CommandMessage.CommandDto.Builder commandBuilder = messageDtoBuilder.getCommandBuilder()
                .setId(1)
                .setCommand("authenticate");

        final AuthenticateCommandMessage.AuthenticateCommandDto.Builder authenticateCommandDtoBuilder =
                AuthenticateCommandMessage.AuthenticateCommandDto.newBuilder()
                        .setType("blah")
                        .setClientVersion("1.0.0");
        commandBuilder.setExtension(AuthenticateCommandMessage.authenticateCommand, authenticateCommandDtoBuilder.build());

        final MessageMessage.MessageDto messageDto = messageDtoBuilder.buildPartial();

        final Message message = Message.fromDto(messageDto);
        Assert.assertEquals(IMessage.Type.Command, message.getType());
        final ICommand command = message.getCommand();
        Assert.assertEquals(1, command.getId());
        Assert.assertEquals("authenticate", command.getCommand());
        final IAuthenticateCommand authenticateCommand = (IAuthenticateCommand) command.getExtension(IAuthenticateCommand.class);
        Assert.assertEquals("blah", authenticateCommand.getType());
        Assert.assertEquals("1.0.0", authenticateCommand.getClientVersion());
    }
}
