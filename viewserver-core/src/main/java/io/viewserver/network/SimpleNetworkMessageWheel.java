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

package io.viewserver.network;

import io.viewserver.messages.IDecoder;
import io.viewserver.messages.IEncoder;
import io.viewserver.messages.IMessage;
import io.viewserver.reactor.INetworkMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by nick on 26/06/15.
 */
public class SimpleNetworkMessageWheel implements INetworkMessageWheel {
    private static final Logger log = LoggerFactory.getLogger(SimpleNetworkMessageWheel.class);
    private final List<INetworkMessageListener> listeners = new CopyOnWriteArrayList<>();
    private IEncoder encoder;
    private IDecoder decoder;

    public SimpleNetworkMessageWheel(IEncoder encoder, IDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public void startRotating() {

    }

    @Override
    public void stopRotating() {

    }

    @Override
    public void registerNetworkMessageListener(INetworkMessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void pushToWheel(IChannel channel, byte[] bytes, int offset, int length) {
        pushToWheel(channel, decoder.decode(bytes, offset, length));
    }

    @Override
    public void pushToWheel(IChannel channel, InputStream stream) {
        pushToWheel(channel, decoder.decode(stream));
    }

    @Override
    public byte[] encode(IMessage message) {
        return encoder.encode(message);
    }

    private void pushToWheel(IChannel channel, IMessage message) {
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onNetworkMessage(channel, message);
        }
    }
}
