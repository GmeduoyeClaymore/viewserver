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

package io.viewserver.network.netty;

import io.viewserver.messages.IMessage;
import io.viewserver.network.INetworkMessageWheel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * Created by bemm on 02/12/15.
 */
public class OutgoingMessageHandler extends MessageToMessageEncoder<IMessage> {
    private INetworkMessageWheel networkMessageWheel;

    public OutgoingMessageHandler(INetworkMessageWheel networkMessageWheel) {
        this.networkMessageWheel = networkMessageWheel;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IMessage msg, List<Object> out) throws Exception {
        out.add(wrappedBuffer(networkMessageWheel.encode(msg)));
        msg.release();
    }
}
