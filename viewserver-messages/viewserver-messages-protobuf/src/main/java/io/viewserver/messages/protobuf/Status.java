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
import io.viewserver.messages.protobuf.dto.StatusMessage;
import io.viewserver.messages.tableevent.IStatus;

/**
 * Created by nick on 07/12/15.
 */
public class Status extends PoolableMessage<Status> implements IStatus<Status> {
    private StatusMessage.StatusDtoOrBuilder statusDto;

    Status() {
        super(IStatus.class);
    }

    @Override
    public void setDto(Object dto) {
        statusDto = (StatusMessage.StatusDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public StatusId getStatusId() {
        final StatusMessage.StatusDto.StatusId status = statusDto.getStatus();
        switch (status) {
            case DATARESET: {
                return StatusId.DataReset;
            }
            case SCHEMARESET: {
                return StatusId.SchemaReset;
            }
            case SCHEMAERROR: {
                return StatusId.SchemaError;
            }
            case SCHEMAERRORCLEARED: {
                return StatusId.SchemaErrorCleared;
            }
            case DATAERROR: {
                return StatusId.DataError;
            }
            case DATAERRORCLEARED: {
                return StatusId.DataErrorCleared;
            }
            case CONFIGERROR:{
                return StatusId.ConfigError;
            }
            case CONFIGERRORCLEARED:{
                return StatusId.ConfigErrorCleared;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unknown status id '%s'", status));
            }
        }
    }

    @Override
    public IStatus setStatusId(StatusId statusId) {
        switch (statusId) {
            case DataReset: {
                getStatusDtoBuilder().setStatus(StatusMessage.StatusDto.StatusId.DATARESET);
                break;
            }
            case SchemaReset: {
                getStatusDtoBuilder().setStatus(StatusMessage.StatusDto.StatusId.SCHEMARESET);
                break;
            }
            case SchemaError: {
                getStatusDtoBuilder().setStatus(StatusMessage.StatusDto.StatusId.SCHEMAERROR);
                break;
            }
            case SchemaErrorCleared: {
                getStatusDtoBuilder().setStatus(StatusMessage.StatusDto.StatusId.SCHEMAERRORCLEARED);
                break;
            }
            case DataError: {
                getStatusDtoBuilder().setStatus(StatusMessage.StatusDto.StatusId.DATAERROR);
                break;
            }
            case DataErrorCleared: {
                getStatusDtoBuilder().setStatus(StatusMessage.StatusDto.StatusId.DATAERRORCLEARED);
                break;
            }
            case ConfigError: {
                getStatusDtoBuilder().setStatus(StatusMessage.StatusDto.StatusId.CONFIGERROR);
                break;
            }
            case ConfigErrorCleared: {
                getStatusDtoBuilder().setStatus(StatusMessage.StatusDto.StatusId.CONFIGERRORCLEARED);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown status id '%s'", statusId));
            }
        }
        return this;
    }

    @Override
    protected void doRelease() {
        statusDto = null;
    }

    StatusMessage.StatusDto.Builder getBuilder() {
        return getStatusDtoBuilder();
    }

    private StatusMessage.StatusDto.Builder getStatusDtoBuilder() {
        if (statusDto == null) {
            statusDto = StatusMessage.StatusDto.newBuilder();
        } else if (statusDto instanceof StatusMessage.StatusDto) {
            statusDto = ((StatusMessage.StatusDto) statusDto).toBuilder();
        }
        return (StatusMessage.StatusDto.Builder) statusDto;
    }
}
