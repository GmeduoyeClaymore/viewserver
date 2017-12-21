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

package io.viewserver.distribution;

import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.CatalogOutput;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.deserialiser.DeserialiserOperator;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderString;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 11/09/15.
 */
public class SlaveCatalog extends DeserialiserOperator implements ICatalog {
    public static final String SLAVES_CATALOG_NAME = "slaves";
    private final TObjectIntHashMap<String> rowIdsByOperatorName = new TObjectIntHashMap<>(8, 0.75f, -1);
    private final TIntObjectHashMap<String> operatorNamesByRowId = new TIntObjectHashMap<>(8, 0.75f, -1);
    private final Map<String, IOperator> operators = new HashMap<>();

    public SlaveCatalog(IPeerSession peerSession, IExecutionContext executionContext, ICatalog catalog) {
        this(peerSession.getCatalogName(), executionContext, catalog, peerSession, "/");
    }

    private SlaveCatalog(String name, IExecutionContext executionContext, ICatalog catalog, IPeerSession peerSession, String target) {
        super(name, executionContext, catalog, peerSession, target, new ChunkedColumnStorage(1024));
        connect();
    }

    @Override
    protected void writeStringValue(String value, ColumnHolder columnHolder, int rowId) {
        if (columnHolder.getName().equals(CatalogOutput.PATH_COLUMN)) {
            value = String.format("%s%s", getPath(), value);
        } else if (columnHolder.getName().equals(CatalogOutput.NAME_COLUMN)) {
            rowIdsByOperatorName.put(value, rowId);
            operatorNamesByRowId.put(rowId, value);
        }
        super.writeStringValue(value, columnHolder, rowId);
    }

    @Override
    protected void onRowRemove(IRowEvent rowEvent) {
        super.onRowRemove(rowEvent);

        String name = operatorNamesByRowId.remove(rowEvent.getRowId());
        rowIdsByOperatorName.remove(name);
    }

    @Override
    public ICatalog getParent() {
        return getCatalog();
    }

    @Override
    public void registerOperator(IOperator operator) {
        operators.put(operator.getName(), operator);
    }

    @Override
    public IOperator getOperator(String name) {
        // TODO: currently you have to drill down one level at a time into slave catalogs, otherwise it
        // gets complicated...revisit if required
        int slash = name.indexOf('/');
        if (slash > -1) {
            // TODO: implement this as and when required
            throw new UnsupportedOperationException();
        } else {
            IOperator operator = operators.get(name);
            if (operator != null) {
                return operator;
            }
            int rowId = rowIdsByOperatorName.get(name);
            if (rowId == rowIdsByOperatorName.getNoEntryValue()) {
                return null;
            }
            String target = String.format("%s%s", getTarget(), name);
            String operatorType = ((ColumnHolderString)getOutput().getSchema().getColumnHolder(CatalogOutput.TYPE_COLUMN)).getString(rowId);
            if (operatorType.equals(Catalog.class.getName())) {
                operator = new SlaveCatalog(name, getExecutionContext(), this, getPeerSession(), target);
            } else {
                operator = new DeserialiserOperator(name, getExecutionContext(), this, getPeerSession(), target, new ChunkedColumnStorage(1024));
                ((DeserialiserOperator)operator).connect();
            }
            return operator;
        }
    }

    @Override
    public void unregisterOperator(IOperator operator) {
        operators.remove(operator.getName());
    }

    @Override
    public ICatalog createDescendant(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addChild(ICatalog childCatalog) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChild(ICatalog childCatalog) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<IOperator> getAllOperators() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICatalog getChild(String name) {
        throw new UnsupportedOperationException();
    }
}
