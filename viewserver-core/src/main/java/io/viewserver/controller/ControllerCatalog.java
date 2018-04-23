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

package io.viewserver.controller;

import io.viewserver.Constants;
import io.viewserver.catalog.CatalogHolder;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.operators.table.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import rx.Observable;

import java.util.Collection;
import java.util.HashMap;


/**
 * Created by nick on 18/02/2015.
 */
public class ControllerCatalog extends InputOperatorBase implements ICatalog{
    private final Output output;
    private final ICatalog catalogHolder;
    private final ITableStorage storage;
    private final TableRow myTableRow;
    protected boolean initialised;

    public static final String NAME_COLUMN = "name";
    public static final String PATH = "path";
    private HashMap<String,ControllerRegistration> registrationHashMap = new HashMap<>();
    private int controllerRowCount = 0;


    public ControllerCatalog(ITableStorage storage, IExecutionContext context,ICatalog catalog) {
        super("controllers", context, catalog);

        this.storage = storage;
        output = new Output(Constants.OUT, this);
        addOutput(output);

        catalogHolder = new CatalogHolder(this);

        setSystemOperator(true);

        initialise(1024);

        myTableRow = new TableRow(0, output.getSchema());

        register();
    }



    public void initialise(int capacity) {
        if (initialised) {
            throw new RuntimeException("Table already initialised");
        }

        storage.initialise(capacity, output.getSchema(), output.getCurrentChanges());

        initialised = true;
    }


    @Override
    public void doTearDown() {
        catalogHolder.tearDown();
        super.doTearDown();
    }

    public ControllerRegistration getController(String controllerName) {
        return registrationHashMap.get(controllerName);
    }

    public void registerController(Object controller) {
        ControllerRegistration reg = new ControllerRegistration(controller);
        if(registrationHashMap.containsKey(reg.getName())){
            throw new RuntimeException("Already have a controller registered for name \"" + reg.getName() + "\"");
        }

        ControllerActionCatalog controllerActionCatalog = new ControllerActionCatalog(reg.getName(), new ChunkedColumnStorage(1024), this.getExecutionContext(), this);
        registrationHashMap.put(reg.getName(), reg);
        myTableRow.setRowId(controllerRowCount++);
        myTableRow.setString(NAME_COLUMN, reg.getName());
        myTableRow.setString(PATH, controllerActionCatalog.getPath());
        this.output.handleAdd(myTableRow.getRowId());

        for(ControllerActionEntry entry : reg.getActions().values()){
            controllerActionCatalog.registerControllerAction(entry);
        }

    }

    private class Output extends OutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(PATH, io.viewserver.schema.column.ColumnType.String));
        }
    }

    @Override
    public IOutput getOutput() {
        return output;
    }

    @Override
    public ICatalog getParent() {
        return catalogHolder.getParent();
    }

    @Override
    public int registerOperator(IOperator operator) {
        return catalogHolder.registerOperator(operator);
    }

    @Override
    public IOperator getOperator(String name) {
        return catalogHolder.getOperator(name);
    }

    @Override
    public IOperator getOperatorByPath(String name) {
        return catalogHolder.getOperatorByPath(name);
    }

    @Override
    public void unregisterOperator(IOperator operator) {
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
    public Collection<IOperator> getAllOperators() {
        return catalogHolder.getAllOperators();
    }

    @Override
    public ICatalog getChild(String name) {
        return catalogHolder.getChild(name);
    }

    @Override
    public Observable<ICatalog> waitForChild(String name) {
        return catalogHolder.waitForChild(name);
    }

}


