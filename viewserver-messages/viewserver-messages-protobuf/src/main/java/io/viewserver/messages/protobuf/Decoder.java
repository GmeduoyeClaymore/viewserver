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

import io.viewserver.messages.IDecoder;
import io.viewserver.messages.IMessage;
import io.viewserver.messages.protobuf.dto.MessageMessage;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistry;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nick on 02/12/15.
 */
public class Decoder implements IDecoder {
    private final ExtensionRegistry extensionRegistry = CommandRegistry.INSTANCE.getExtensionRegistry();

    @Override
    public IMessage decode(byte[] bytes, int offset, int length) {
        return decode(CodedInputStream.newInstance(bytes, offset, length));
    }

    @Override
    public IMessage decode(InputStream inputStream) {
        return decode(CodedInputStream.newInstance(inputStream));
    }

    private IMessage decode(CodedInputStream codedInputStream) {
        final MessageMessage.MessageDto messageDto;
        try {
            messageDto = MessageMessage.MessageDto.parseFrom(codedInputStream, extensionRegistry);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Message.fromDto(messageDto);
    }
}
