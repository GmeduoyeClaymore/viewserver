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
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Created by bemm on 26/06/15.
 */
public class NettyPipelineInitialiser extends ChannelInitializer<Channel> {
    private INetworkMessageWheel networkMessageWheel;

    public NettyPipelineInitialiser(INetworkMessageWheel networkMessageWheel) {
        this.networkMessageWheel = networkMessageWheel;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        if (pipeline.get("frameDecoder") == null) {
            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1 << 30, 0, 4, 0, 4));
        }
        pipeline.addLast(new IncomingMessageHandler(networkMessageWheel));

        if (pipeline.get("frameEncoder") == null) {
            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        }
        pipeline.addLast(new OutgoingMessageHandler(networkMessageWheel));
    }
}
