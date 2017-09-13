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

package io.viewserver.messages.tableevent;

import io.viewserver.messages.IPoolableMessage;

import java.util.List;

/**
 * Created by nick on 02/12/15.
 */
public interface ITableEvent<T> extends IPoolableMessage<T> {
    int getId();

    ITableEvent setId(int id);

    int getInitSize();

    ITableEvent setInitSize(int initSize);

    int getExecutionCycle();

    ITableEvent setExecutionCycle(int executionCycle);

    boolean hasSchemaChange();

    ISchemaChange getSchemaChange();

    ITableEvent setSchemaChange(ISchemaChange schemaChange);

    List<IRowEvent> getRowEvents();

    List<IStatus> getStatuses();

    int getFlags();

    ITableEvent setFlags(int flags);

    ITableMetadata getMetadata();

    ITableEvent setMetadata(ITableMetadata metadata);

    enum Flags {
        SnapshotComplete(1);

        private int number;

        Flags(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }
    }
}
