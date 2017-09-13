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
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.InputOperatorBase;

import java.util.Collection;

/**
 * Created by nickc on 07/10/2014.
 */
public class Catalog extends InputOperatorBase implements ICatalog {
    private final CatalogOutput output;
    private final CatalogHolder catalogHolder;

    public Catalog(ExecutionContext executionContext) {
        this("/", executionContext, null);
    }

    public Catalog(String name, ICatalog parent) {
        this(name, parent.getExecutionContext(), parent);
    }

    private Catalog(String name, ExecutionContext executionContext, ICatalog parent) {
        super(name, executionContext, parent);

        catalogHolder = new CatalogHolder(this);

        output = new CatalogOutput(Constants.OUT, this, catalogHolder);
        addOutput(output);

        setSystemOperator(true);
    }

    public IOutput getOutput() { return output; }

    @Override
    public ICatalog getParent() {
        return catalogHolder.getParent();
    }

    @Override
    public void registerOperator(IOperator operator) {
        catalogHolder.registerOperator(operator);
        output.handleAdd(catalogHolder.getRowIdForOperator(operator));
    }

    @Override
    public IOperator getOperator(String name) {
        return catalogHolder.getOperator(name);
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
    public void addChild(ICatalog childCatalog) {
        catalogHolder.addChild(childCatalog);
    }

    @Override
    public void removeChild(ICatalog childCatalog) {
        catalogHolder.removeChild(childCatalog);
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
