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
import io.viewserver.messages.command.IRegisterDataSourceCommand;
import io.viewserver.messages.command.IUpdateDimensionMapCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.RegisterDataSourceCommandMessage;

/**
 * Created by nick on 07/12/15.
 */
public class RegisterDataSourceCommand extends PoolableMessage<RegisterDataSourceCommand>
        implements IRegisterDataSourceCommand<RegisterDataSourceCommand>, ICommandExtension<RegisterDataSourceCommand> {
    private RegisterDataSourceCommandMessage.RegisterDataSourceCommandDtoOrBuilder registerDataSourceCommandDto;
    private UpdateDimensionMapCommand updateDimensionMapCommand;

    RegisterDataSourceCommand() {
        super(IRegisterDataSourceCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        registerDataSourceCommandDto = (RegisterDataSourceCommandMessage.RegisterDataSourceCommandDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(RegisterDataSourceCommandMessage.registerDataSourceCommand, getBuilder().buildPartial());
    }

    @Override
    public String getDataSource() {
        return registerDataSourceCommandDto.getDataSource();
    }

    @Override
    public IRegisterDataSourceCommand<RegisterDataSourceCommand> setDataSource(String dataSource) {
        getRegisterDataSourceCommandDtoBuilder().setDataSource(dataSource);
        return this;
    }

    @Override
    public boolean hasDimensionMapUpdate() {
        return updateDimensionMapCommand != null || (registerDataSourceCommandDto != null && registerDataSourceCommandDto.hasDimensionMapUpdate());
    }

    @Override
    public IUpdateDimensionMapCommand getDimensionMapUpdate() {
        if (updateDimensionMapCommand == null) {
            updateDimensionMapCommand = registerDataSourceCommandDto != null && registerDataSourceCommandDto.hasDimensionMapUpdate() ? UpdateDimensionMapCommand.fromDto(registerDataSourceCommandDto.getDimensionMapUpdate())
                    : (UpdateDimensionMapCommand) MessagePool.getInstance().get(IUpdateDimensionMapCommand.class);
        }
        return updateDimensionMapCommand;
    }

    @Override
    public IRegisterDataSourceCommand<RegisterDataSourceCommand> setDimensionMapUpdate(IUpdateDimensionMapCommand dimensionMapUpdate) {
        if (this.updateDimensionMapCommand != null) {
            this.updateDimensionMapCommand.release();
        }
        this.updateDimensionMapCommand = (UpdateDimensionMapCommand)dimensionMapUpdate.retain();
        return this;
    }

    @Override
    protected void doRelease() {
        if (updateDimensionMapCommand != null) {
            updateDimensionMapCommand.release();
            updateDimensionMapCommand = null;
        }
        registerDataSourceCommandDto = null;
    }

    RegisterDataSourceCommandMessage.RegisterDataSourceCommandDto.Builder getBuilder() {
        final RegisterDataSourceCommandMessage.RegisterDataSourceCommandDto.Builder builder = getRegisterDataSourceCommandDtoBuilder();
        if (updateDimensionMapCommand != null) {
            builder.setDimensionMapUpdate(updateDimensionMapCommand.getBuilder());
        }
        return builder;
    }

    private RegisterDataSourceCommandMessage.RegisterDataSourceCommandDto.Builder getRegisterDataSourceCommandDtoBuilder() {
        if (registerDataSourceCommandDto == null) {
            registerDataSourceCommandDto = RegisterDataSourceCommandMessage.RegisterDataSourceCommandDto.newBuilder();
        } else if (registerDataSourceCommandDto instanceof RegisterDataSourceCommandMessage.RegisterDataSourceCommandDto) {
            registerDataSourceCommandDto = ((RegisterDataSourceCommandMessage.RegisterDataSourceCommandDto) registerDataSourceCommandDto).toBuilder();
        }
        return (RegisterDataSourceCommandMessage.RegisterDataSourceCommandDto.Builder) registerDataSourceCommandDto;
    }
}
