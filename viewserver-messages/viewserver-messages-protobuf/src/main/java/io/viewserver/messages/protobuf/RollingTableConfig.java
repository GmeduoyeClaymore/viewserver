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
import io.viewserver.messages.config.IRollingTableConfig;
import io.viewserver.messages.protobuf.dto.RollingTableConfigMessage;

/**
 * Created by nick on 08/12/15.
 */
public class RollingTableConfig extends PoolableMessage<RollingTableConfig> implements IRollingTableConfig<RollingTableConfig> {
    private RollingTableConfigMessage.RollingTableConfigDtoOrBuilder rollingTableConfigDto;

    RollingTableConfig() {
        super(IRollingTableConfig.class);
    }

    @Override
    public void setDto(Object dto) {
        rollingTableConfigDto = (RollingTableConfigMessage.RollingTableConfigDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public int getSize() {
        return rollingTableConfigDto.getSize();
    }

    @Override
    public IRollingTableConfig<RollingTableConfig> setSize(int size) {
        getRollingTableConfigDtoBuilder().setSize(size);
        return this;
    }

    @Override
    protected void doRelease() {
        rollingTableConfigDto = null;
    }

    RollingTableConfigMessage.RollingTableConfigDto.Builder getBuilder() {
        return getRollingTableConfigDtoBuilder();
    }

    private RollingTableConfigMessage.RollingTableConfigDto.Builder getRollingTableConfigDtoBuilder() {
        if (rollingTableConfigDto == null) {
            rollingTableConfigDto = RollingTableConfigMessage.RollingTableConfigDto.newBuilder();
        } else if (rollingTableConfigDto instanceof RollingTableConfigMessage.RollingTableConfigDto) {
            rollingTableConfigDto = ((RollingTableConfigMessage.RollingTableConfigDto) rollingTableConfigDto).toBuilder();
        }
        return (RollingTableConfigMessage.RollingTableConfigDto.Builder) rollingTableConfigDto;
    }
}
