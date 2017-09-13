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

package io.viewserver.operators;

import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.changequeue.IMappedChangeQueue;
import io.viewserver.changequeue.MappedChangeQueue;
import io.viewserver.schema.column.ColumnHolder;

import java.util.HashMap;

/**
 * Created by nickc on 18/10/2014.
 */
public abstract class MappedOutputBase extends OutputBase {
    private IMappedChangeQueue mappedChangeQueue;
    protected HashMap<String, Object> metaData;

    protected MappedOutputBase(String name, IOperator owner) {
        super(name, owner);
        mappedChangeQueue = new MappedChangeQueue(this);
    }

    @Override
    public IMappedChangeQueue getCurrentChanges() {
        return mappedChangeQueue;
    }

    public void mapColumn(ColumnHolder inboundColumn, ColumnHolder outboundColumn, IChangeQueue sourceChangeQueue) {
        getSchema().addColumn(outboundColumn);
        getCurrentChanges().mapColumn(inboundColumn, outboundColumn, sourceChangeQueue);
    }

    public void unmapColumn(ColumnHolder inboundColumn) {
        int outboundColumnId = getCurrentChanges().getOutboundColumnId(inboundColumn);
        getSchema().removeColumn(outboundColumnId);
    }

    public int getOutboundColumnId(ColumnHolder inboundColumn) {
        return getCurrentChanges().getOutboundColumnId(inboundColumn);
    }
}
