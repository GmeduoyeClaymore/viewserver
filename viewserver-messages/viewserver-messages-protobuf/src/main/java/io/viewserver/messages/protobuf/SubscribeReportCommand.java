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
import io.viewserver.messages.command.ISubscribeReportCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.SubscribeReportCommandMessage;

/**
 * Created by nick on 03/12/15.
 */
public class SubscribeReportCommand extends PoolableMessage<SubscribeReportCommand>
        implements ISubscribeReportCommand<SubscribeReportCommand>,
        ICommandExtension<SubscribeReportCommand> {
    private SubscribeReportCommandMessage.SubscribeReportCommandDtoOrBuilder subscribeReportCommandDto;
    private ReportContext reportContext;
    private Options options;

    SubscribeReportCommand() {
        super(ISubscribeReportCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        subscribeReportCommandDto = (SubscribeReportCommandMessage.SubscribeReportCommandDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(SubscribeReportCommandMessage.subscribeReportCommand, getBuilder().buildPartial());
    }

    @Override
    public IReportContext getReportContext() {
        if (reportContext == null) {
            reportContext = subscribeReportCommandDto != null ? ReportContext.fromDto(subscribeReportCommandDto.getContext())
                    : (ReportContext)MessagePool.getInstance().get(IReportContext.class);
        }
        return reportContext;
    }

    @Override
    public ISubscribeReportCommand<SubscribeReportCommand> setReportContext(IReportContext reportContext) {
        if (this.reportContext != null) {
            this.reportContext.release();
        }
        this.reportContext = (ReportContext) reportContext.retain();
        return this;
    }

    @Override
    public IOptions getOptions() {
        if (options == null) {
            options = subscribeReportCommandDto != null ? Options.fromDto(subscribeReportCommandDto.getOptions())
                    : (Options) MessagePool.getInstance().get(IOptions.class);
        }
        return options;
    }

    @Override
    public ISubscribeReportCommand<SubscribeReportCommand> setOptions(IOptions options) {
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
        subscribeReportCommandDto = null;
    }

    SubscribeReportCommandMessage.SubscribeReportCommandDto.Builder getBuilder() {
        final SubscribeReportCommandMessage.SubscribeReportCommandDto.Builder builder = getSubscribeReportCommandDtoBuilder();
        if (reportContext != null) {
            builder.setContext(reportContext.getBuilder());
        }
        if (options != null) {
            builder.setOptions(options.getBuilder());
        }
        return builder;
    }

    private SubscribeReportCommandMessage.SubscribeReportCommandDto.Builder getSubscribeReportCommandDtoBuilder() {
        if (subscribeReportCommandDto == null) {
            subscribeReportCommandDto = SubscribeReportCommandMessage.SubscribeReportCommandDto.newBuilder();
        } else if (subscribeReportCommandDto instanceof SubscribeReportCommandMessage.SubscribeReportCommandDto) {
            subscribeReportCommandDto = ((SubscribeReportCommandMessage.SubscribeReportCommandDto) subscribeReportCommandDto).toBuilder();
        }
        return (SubscribeReportCommandMessage.SubscribeReportCommandDto.Builder) subscribeReportCommandDto;
    }
}
