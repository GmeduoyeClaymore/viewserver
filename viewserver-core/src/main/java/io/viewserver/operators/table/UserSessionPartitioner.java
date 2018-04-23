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

package io.viewserver.operators.table;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.network.IPeerSession;
import io.viewserver.network.SessionManager;
import io.viewserver.operators.*;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowFlags;
import io.viewserver.util.ViewServerException;

/**
 * Created by nick on 19/02/2015.
 */
public class UserSessionPartitioner extends ConfigurableOperatorBase<IUserSessionPartitionerConfig> {
    private final Input input;
    private SessionManager sessionManager;
    private Table sourceTable;
    private String userIdColumnName;

    public UserSessionPartitioner(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);

        input = new Input(Constants.IN, this);
        addInput(input);
        register();
    }

    @Override
    protected void processConfig(IUserSessionPartitionerConfig config) {
        IOperator operator = getCatalog().getOperatorByPath(config.getSourceTableName());
        if (operator == null || !(operator instanceof Table)) {
            throw new OperatorConfigurationException(this,
                    String.format("Operator '%s' does not exist, or is not a table.", config.getSourceTableName()));
        }
        sourceTable = (Table)operator;

        userIdColumnName = config.getUserIdColumnName();
    }

    private class Input extends InputBase {
        private ColumnHolder userIdColumnHolder;

        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void onPluggedIn(IOutput output) {
            if (!(output.getOwner() instanceof SessionManager)) {
                throw new ViewServerException("Only the SessionManager can be plugged in to a UserSessionPartitioner!");
            }
            sessionManager = (SessionManager) output.getOwner();
            super.onPluggedIn(output);
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            if (SessionManager.USERID_COLUMN.equals(columnHolder.getName())) {
                this.userIdColumnHolder = columnHolder;
            }
        }

        @Override
        protected void onRowAdd(int row) {
            IPeerSession peerSession = sessionManager.getSessionById(row);
            if (peerSession.isAuthenticated()) {
                TablePartitionOperator tablePartitionOperator = new TablePartitionOperator(sourceTable.getName(),
                        getExecutionContext(), peerSession.getSessionCatalog(), userIdColumnName, peerSession.getAuthenticationToken().getId());
                sourceTable.getOutput().plugIn(tablePartitionOperator.getInput());
            }
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            if (rowFlags.isDirty(userIdColumnHolder.getColumnId())) {
                IPeerSession peerSession = sessionManager.getSessionById(row);
                TablePartitionOperator tablePartitionOperator = new TablePartitionOperator(sourceTable.getName(),
                        getExecutionContext(), peerSession.getSessionCatalog(), userIdColumnName, peerSession.getAuthenticationToken().getId());
                sourceTable.getOutput().plugIn(tablePartitionOperator.getInput());
            }
        }
    }
}
