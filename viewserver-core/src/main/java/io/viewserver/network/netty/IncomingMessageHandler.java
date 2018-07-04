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

import io.viewserver.network.INetworkMessageWheel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Created by bemm on 26/06/15.
 */
public class IncomingMessageHandler extends MessageToMessageDecoder<ByteBuf> {
    private INetworkMessageWheel networkMessageWheel;

    public IncomingMessageHandler(INetworkMessageWheel networkMessageWheel) {
        this.networkMessageWheel = networkMessageWheel;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> objects) throws Exception {
        final byte[] array;
        final int offset;
        final int length = byteBuf.readableBytes();
//        try {
            if (byteBuf.hasArray()) {
                array = byteBuf.array();
                offset = byteBuf.arrayOffset() + byteBuf.readerIndex();
                networkMessageWheel.pushToWheel(new NettyChannel(channelHandlerContext.channel()), array, offset, length);
            } else {
//                array = new byte[length];
//                byteBuf.getBytes(byteBuf.readerIndex(), array, 0, length);
//                offset = 0;
                networkMessageWheel.pushToWheel(new NettyChannel(channelHandlerContext.channel()), new ByteBufInputStream(byteBuf));
            }
//        } finally {
//            byteBuf.release();
//        }
    }
}
