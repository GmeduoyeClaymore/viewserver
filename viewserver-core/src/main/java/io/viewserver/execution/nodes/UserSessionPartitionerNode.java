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

package io.viewserver.execution.nodes;

import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.network.SessionManager;
import io.viewserver.operators.table.IUserSessionPartitionerConfig;
import io.viewserver.operators.table.UserSessionPartitioner;

/**
 * Created by bemm on 19/02/2015.
 */
public class UserSessionPartitionerNode extends GraphNodeBase<UserSessionPartitionerNode> implements IUserSessionPartitionerConfig {
    private String sourceTableName;
    private String userIdColumnName;

    public UserSessionPartitionerNode() {
        super();
    }

    public UserSessionPartitionerNode(String name) {
        super(name, UserSessionPartitioner.class.getName());
        withConnection(String.format("/%s", SessionManager.OPERATOR_NAME));
    }

    public UserSessionPartitionerNode withSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
        return this;
    }

    public UserSessionPartitionerNode withUserIdColumnName(String userIdColumnName) {
        this.userIdColumnName = userIdColumnName;
        return this;
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return this;
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSourceTableName() {
        return sourceTableName;
    }

    @Override
    public String getUserIdColumnName() {
        return userIdColumnName;
    }
}
