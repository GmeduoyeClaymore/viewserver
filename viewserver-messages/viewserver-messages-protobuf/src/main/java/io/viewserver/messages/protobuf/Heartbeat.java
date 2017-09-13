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
import io.viewserver.messages.heartbeat.IHeartbeat;
import io.viewserver.messages.protobuf.dto.HeartbeatMessage;

/**
 * Created by nick on 02/12/15.
 */
public class Heartbeat extends PoolableMessage<Heartbeat> implements IHeartbeat<Heartbeat> {
    private HeartbeatMessage.HeartbeatDtoOrBuilder heartbeatDto;

    public static Heartbeat fromDto(HeartbeatMessage.HeartbeatDto heartbeatDto) {
        final Heartbeat heartbeat = (Heartbeat) MessagePool.getInstance().get(IHeartbeat.class);
        heartbeat.heartbeatDto = heartbeatDto;
        return heartbeat;
    }

    Heartbeat() {
        super(IHeartbeat.class);
    }

    public Type getType() {
        switch (heartbeatDto.getType()) {
            case PING: {
                return Type.Ping;
            }
            case PONG: {
                return Type.Pong;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unknown type '%s' in heartbeat", heartbeatDto.getType()));
            }
        }
    }

    @Override
    public IHeartbeat setType(IHeartbeat.Type type) {
        switch (type) {
            case Ping: {
                getHeartbeatBuilder().setType(HeartbeatMessage.HeartbeatDto.Type.PING);
                break;
            }
            case Pong: {
                getHeartbeatBuilder().setType(HeartbeatMessage.HeartbeatDto.Type.PONG);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown heartbeat type '%s'", type));
            }
        }
        return this;
    }

    @Override
    protected void doRelease() {
        heartbeatDto = null;
    }

    private HeartbeatMessage.HeartbeatDto.Builder getHeartbeatBuilder() {
        if (heartbeatDto == null) {
            heartbeatDto = HeartbeatMessage.HeartbeatDto.newBuilder();
        } else if (heartbeatDto instanceof HeartbeatMessage.HeartbeatDto) {
            heartbeatDto = ((HeartbeatMessage.HeartbeatDto) heartbeatDto).toBuilder();
        }
        return (HeartbeatMessage.HeartbeatDto.Builder) heartbeatDto;
    }

    HeartbeatMessage.HeartbeatDto.Builder getBuilder() {
        return getHeartbeatBuilder();
    }
}
