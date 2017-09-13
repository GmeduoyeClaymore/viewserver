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

import io.viewserver.network.IChannel;
import io.netty.channel.Channel;

/**
 * Created by nick on 26/06/15.
 */
public class NettyChannel implements IChannel {
    private Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NettyChannel that = (NettyChannel) o;

        if (channel.hashCode() != that.channel.hashCode()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return channel.hashCode();
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public String toString() {
        return String.format("NettyChannel{channel=%s}",  channel);
    }
}
