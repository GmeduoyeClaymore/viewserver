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

package io.viewserver.catalog;

import io.viewserver.Constants;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.InputOperatorBase;
import io.viewserver.operators.table.TableRow;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import rx.Observable;

import java.util.Collection;

/**
 * Created by nickc on 07/10/2014.
 */
public class Catalog extends InputOperatorBase implements ICatalog {
    private final CatalogOutput output;
    private final CatalogHolder catalogHolder;
    private boolean initialised ;
    public Catalog(IExecutionContext executionContext) {
        this("/", executionContext, null);
    }

    public Catalog(String name, ICatalog parent) {
        this(name, parent.getExecutionContext(), parent);
    }

    private ITableStorage storage;
    protected TableRow tableRow;

    private Catalog(String name, IExecutionContext executionContext, ICatalog parent) {
        super(name, executionContext, parent);

        catalogHolder = new CatalogHolder(this);

        storage = new ChunkedColumnStorage(1024);

        output = new CatalogOutput(Constants.OUT, this, catalogHolder);
        addOutput(output);

        setSystemOperator(true);

        initialise(1024);

        tableRow = new TableRow(0, output.getSchema());
    }

    public void initialise(int capacity) {
        if (initialised) {
            throw new RuntimeException("Table already initialised");
        }

        storage.initialise(capacity, output.getSchema(), output.getCurrentChanges());

        initialised = true;
    }


    public IOutput getOutput() { return output; }

    @Override
    public ICatalog getParent() {
        return catalogHolder.getParent();
    }

    @Override
    public int registerOperator(IOperator operator) {
        catalogHolder.registerOperator(operator);
        int rowId = catalogHolder.getRowIdForOperator(operator);
        storage.ensureCapacity(rowId + 1, output.getSchema());

        tableRow.setRowId(rowId);
        tableRow.setString(CatalogOutput.NAME_COLUMN,  operator.getName() );
        tableRow.setString(CatalogOutput.TYPE_COLUMN,  operator.getClass().getName() );
        tableRow.setString(CatalogOutput.PATH_COLUMN,  operator.getPath() );
        output.handleAdd(rowId);
        return rowId;
    }

    @Override
    public IOperator getOperator(String name) {
        return catalogHolder.getOperator(name);
    }

    public Observable<IOperator> getOperatorObservable(String name) {
        return catalogHolder.getOperatorObservable(name);
    }

    @Override
    public IOperator getOperatorByPath(String name) {
        return catalogHolder.getOperatorByPath(name);
    }

    @Override
    public void unregisterOperator(IOperator operator) {
        output.handleRemove(catalogHolder.getRowIdForOperator(operator));
        catalogHolder.unregisterOperator(operator);
    }

    @Override
    public ICatalog createDescendant(String path) {
        return catalogHolder.createDescendant(path);
    }

    @Override
    public ICatalog getDescendant(String path) {
        return catalogHolder.getDescendant(path);
    }

    @Override
    public void addChild(ICatalog childCatalog) {
        catalogHolder.addChild(childCatalog);
    }

    @Override
    public void removeChild(ICatalog childCatalog) {
        catalogHolder.removeChild(childCatalog);
    }

    @Override
    public Observable<IOperator> waitForOperator(String name) {
        return catalogHolder.waitForOperator(name);
    }

    @Override
    public Observable<ICatalog> waitForChild(String name) {
        return catalogHolder.waitForChild(name);
    }

    @Override
    public Collection<IOperator> getAllOperators() {
        return catalogHolder.getAllOperators();
    }

    @Override
    public ICatalog getChild(String name) {
        return catalogHolder.getChild(name);
    }
}
