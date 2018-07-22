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
import io.viewserver.messages.command.IOptions;
import io.viewserver.messages.command.IReportContext;
import io.viewserver.messages.command.ISubscribeDimensionCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.SubscribeDimensionCommandMessage;

/**
 * Created by bemm on 07/12/15.
 */
public class SubscribeDimensionCommand extends PoolableMessage<SubscribeDimensionCommand>
    implements ISubscribeDimensionCommand<SubscribeDimensionCommand>, ICommandExtension<SubscribeDimensionCommand> {
    private SubscribeDimensionCommandMessage.SubscribeDimensionCommandDtoOrBuilder subscribeDimensionDto;
    private Options options;
    private ReportContext reportContext;
    SubscribeDimensionCommand() {
        super(ISubscribeDimensionCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        subscribeDimensionDto = (SubscribeDimensionCommandMessage.SubscribeDimensionCommandDtoOrBuilder) dto;
    }


    @Override
    public IReportContext getReportContext() {
        if (reportContext == null) {
            reportContext = subscribeDimensionDto != null ? ReportContext.fromDto(subscribeDimensionDto.getContext())
                    : (ReportContext) MessagePool.getInstance().get(IReportContext.class);
        }
        return reportContext;
    }

    @Override
    public ISubscribeDimensionCommand<SubscribeDimensionCommand> setReportContext(IReportContext reportContext) {
        if (this.reportContext != null) {
            this.reportContext.release();
        }
        this.reportContext = (ReportContext) reportContext.retain();
        return this;
    }


    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(SubscribeDimensionCommandMessage.subscribeDimensionCommand, getBuilder().buildPartial());
    }
    
    @Override
    public String getDimension() {
        return subscribeDimensionDto.getDimension();
    }

    @Override
    public ISubscribeDimensionCommand<SubscribeDimensionCommand> setDimension(String dimension) {
        getSubscribeDimensionDtoBuilder().setDimension(dimension);
        return this;
    }
    @Override
    public String getDataSourceName() {
        return subscribeDimensionDto.getDataSourceName();
    }

    @Override
    public ISubscribeDimensionCommand<SubscribeDimensionCommand> setDataSourceName(String dataSourceName) {
        getSubscribeDimensionDtoBuilder().setDataSourceName(dataSourceName);
        return this;
    }

    @Override
    public IOptions getOptions() {
        if (options == null) {
            options = subscribeDimensionDto != null ? Options.fromDto(subscribeDimensionDto.getOptions())
                    : (Options) MessagePool.getInstance().get(IOptions.class);
        }
        return options;
    }

    @Override
    public ISubscribeDimensionCommand<SubscribeDimensionCommand> setOptions(IOptions options) {
        if (this.options != null) {
            this.options.release();
        }
        this.options = (Options) options.retain();
        return this;
    }

    @Override
    protected void doRelease() {
        if (reportContext != null) {
            reportContext.release();
            reportContext = null;
        }
        if (options != null) {
            options.release();
            options = null;
        }
        subscribeDimensionDto = null;
    }

    SubscribeDimensionCommandMessage.SubscribeDimensionCommandDto.Builder getBuilder() {
        final SubscribeDimensionCommandMessage.SubscribeDimensionCommandDto.Builder builder = getSubscribeDimensionDtoBuilder();
        if (reportContext != null) {
            builder.setContext(reportContext.getBuilder());
        }
        if (options != null) {
            builder.setOptions(options.getBuilder());
        }
        return builder;
    }

    private SubscribeDimensionCommandMessage.SubscribeDimensionCommandDto.Builder getSubscribeDimensionDtoBuilder() {
        if (subscribeDimensionDto == null) {
            subscribeDimensionDto = SubscribeDimensionCommandMessage.SubscribeDimensionCommandDto.newBuilder();
        } else if (subscribeDimensionDto instanceof SubscribeDimensionCommandMessage.SubscribeDimensionCommandDto) {
            subscribeDimensionDto = ((SubscribeDimensionCommandMessage.SubscribeDimensionCommandDto) subscribeDimensionDto).toBuilder();
        }
        return (SubscribeDimensionCommandMessage.SubscribeDimensionCommandDto.Builder) subscribeDimensionDto;
    }
}
