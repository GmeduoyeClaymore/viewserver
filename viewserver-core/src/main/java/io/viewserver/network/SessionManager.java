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

import io.viewserver.Constants;
import io.viewserver.authentication.AuthenticationToken;
import io.viewserver.catalog.ICatalog;
import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.InputOperatorBase;
import io.viewserver.operators.OutputBase;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnStringBase;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nick on 19/02/2015.
 */
public class SessionManager extends InputOperatorBase implements IPeerSessionAuthenticationHandler {
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    public static final String OPERATOR_NAME = "sessions";
    public static final String SESSIONID_COLUMN = "sessionId";
    public static final String USERID_COLUMN = "userId";
    public static final String USERTYPE_COLUMN = "userType";
    public static final String PATH_COLUMN = "path";
    private TIntObjectHashMap<IPeerSession> sessionsById = new TIntObjectHashMap<>();
    private Output output;

    public SessionManager(ExecutionContext executionContext, ICatalog catalog) {
        super(OPERATOR_NAME, executionContext, catalog);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        setSystemOperator(true);
    }

    public IPeerSession getSessionById(int id) {
        return sessionsById.get(id);
    }

    public void addSession(IPeerSession peerSession) {
        int id = peerSession.getConnectionId();
        log.debug("Adding session id {}", id);
        sessionsById.put(id, peerSession);

        peerSession.addAuthenticationEventHandler(this);

        output.handleAdd(id);
    }

    public void removeSession(IPeerSession peerSession) {
        int id = peerSession.getConnectionId();
        log.debug("Removing session id {}", id);
        sessionsById.remove(id);

        peerSession.removeAuthenticationEventHandler(this);

        output.handleRemove(id);
    }

    @Override
    public void handle(IPeerSession peerSession, AuthenticationToken authenticationToken) {
        int id = peerSession.getConnectionId();
        IChangeQueue currentChanges = output.getCurrentChanges();
        currentChanges.markDirty(id, 0);
        currentChanges.markDirty(id, 1);
        output.handleUpdate(id);
    }

    private class Output extends OutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);

            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new SessionIdColumn()));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new UserIdColumn()));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new UserTypeColumn()));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new PathColumn()));
        }
    }

    private class SessionIdColumn extends ColumnStringBase {
        public SessionIdColumn() {
            super(SESSIONID_COLUMN);
        }

        @Override
        public String getString(int row) {
            IPeerSession peerSession = getSessionById(row);
            return peerSession.getSessionCatalog().getName();
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }

        @Override
        public String getPreviousString(int row) {
            throw new UnsupportedOperationException();
        }
    }

    private class UserIdColumn extends ColumnStringBase {
        public UserIdColumn() {
            super(USERID_COLUMN);
        }

        @Override
        public String getString(int row) {
            IPeerSession peerSession = getSessionById(row);
            AuthenticationToken authenticationToken = peerSession.getAuthenticationToken();
            if (authenticationToken != null) {
                return authenticationToken.getId();
            }
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }

        @Override
        public String getPreviousString(int row) {
            throw new UnsupportedOperationException();
        }
    }

    private class UserTypeColumn extends ColumnStringBase {
        public UserTypeColumn() {
            super(USERTYPE_COLUMN);
        }

        @Override
        public String getString(int row) {
            IPeerSession peerSession = getSessionById(row);
            AuthenticationToken authenticationToken = peerSession.getAuthenticationToken();
            if (authenticationToken != null) {
                return authenticationToken.getType();
            }
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }

        @Override
        public String getPreviousString(int row) {
            throw new UnsupportedOperationException();
        }
    }

    private class PathColumn extends ColumnStringBase {
        public PathColumn() {
            super(PATH_COLUMN);
        }

        @Override
        public String getString(int row) {
            IPeerSession peerSession = getSessionById(row);
            IOperator sessionCatalog = (IOperator) peerSession.getSessionCatalog();
            return sessionCatalog.getPath();
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }

        @Override
        public String getPreviousString(int row) {
            throw new UnsupportedOperationException();
        }
    }
}
