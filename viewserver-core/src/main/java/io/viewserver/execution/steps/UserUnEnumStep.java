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

package io.viewserver.execution.steps;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.IExecutionContext;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.execution.nodes.*;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.report.IGraphDefinition;
import io.viewserver.report.IMeasuringGraphDefinition;

import java.util.Collections;
import java.util.List;

/**
 * Created by nickc on 31/10/2014.
 */

public class UserUnEnumStep implements IExecutionPlanStep<OptionsExecutionPlanContext> {

    private int commandId;

    public UserUnEnumStep(int commandId) {
        this.commandId = commandId;
    }
    @Override
    public void execute(OptionsExecutionPlanContext optionsExecutionPlanContext) {

        UnEnumNode userUnEnum = new UserUnEnumNode("userUnEnum:" + commandId, optionsExecutionPlanContext.getDataSource())
                .withMetadata("isUserExecutionPlanNode", true)
                .withConnection(optionsExecutionPlanContext.getInputOperator(), optionsExecutionPlanContext.getInputOutputName(), Constants.IN);


        optionsExecutionPlanContext.addNodes(userUnEnum);
        optionsExecutionPlanContext.setInput(userUnEnum.getName());
    }

}
