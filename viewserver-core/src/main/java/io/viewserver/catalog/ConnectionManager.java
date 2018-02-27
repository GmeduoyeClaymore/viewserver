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
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.operators.table.TableRow;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnStringBase;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.viewserver.schema.column.chunked.ChunkedColumnString;

/**
 * Created by nick on 10/03/2015.
 */
public class ConnectionManager extends InputOperatorBase implements IMetadataListener {
    public static final String OPERATOR_NAME = "connections";
    public static final String OUTPUT_OPERATOR_NAME_COLUMN = "outputOperator";
    private static final String OUTPUT_OPERATOR_TYPE_COLUMN = "outputOperatorType";
    private static final String OUTPUT_NAME_COLUMN = "output";
    public static final String INPUT_OPERATOR_NAME_COLUMN = "inputOperator";
    private static final String INPUT_OPERATOR_TYPE_COLUMN = "inputOperatorType";
    private static final String INPUT_NAME_COLUMN = "input";
    private Output output;
    private TIntObjectHashMap<Link> links = new TIntObjectHashMap<>(8, 0.75f, -1);
    private IntHashSet linkHashes = new IntHashSet(8, 0.75f, -1);
    private boolean initialised ;
    private ITableStorage storage;
    protected TableRow tableRow;

    public ConnectionManager(IExecutionContext executionContext, ICatalog catalog, ITableStorage storage) {
        super(OPERATOR_NAME, executionContext, catalog);
        this.storage = storage;

        executionContext.getMetadataRegistry().addListener(this, true);

        output = new Output(Constants.OUT, this);
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


    @Override
    public void doTearDown() {
        getExecutionContext().getMetadataRegistry().removeListener(this);

        super.doTearDown();
    }

    @Override
    public void onRegisterLink(IOutput output, IInput input) {
        Link link = new Link(output, input);
        int hashCode = link.hashCode();
        links.put(hashCode, link);
        int rowId = linkHashes.addInt(hashCode);
        storage.ensureCapacity(rowId + 1, output.getSchema());
        tableRow.setRowId(rowId);
        tableRow.setString(OUTPUT_OPERATOR_NAME_COLUMN,  link.getOutput().getOwner().getPath() );
        tableRow.setString(OUTPUT_OPERATOR_TYPE_COLUMN,  link.getOutput().getOwner().getClass().getName());
        tableRow.setString(OUTPUT_NAME_COLUMN,  link.getOutput().getName());

        tableRow.setString(INPUT_OPERATOR_NAME_COLUMN,  link.getInput().getOwner().getPath() );
        tableRow.setString(INPUT_OPERATOR_TYPE_COLUMN,  link.getInput().getOwner().getClass().getName());
        tableRow.setString(INPUT_NAME_COLUMN,  link.getInput().getName());
        this.output.handleAdd(rowId);
    }

    @Override
    public void onUnregisterLink(IOutput output, IInput input) {
        Link link = new Link(output, input);
        int hashCode = link.hashCode();
        links.remove(hashCode);
        int rowId = linkHashes.index(hashCode);
        linkHashes.remove(hashCode);
        this.output.handleRemove(rowId);
    }

    @Override
    public void onRegisterInput(IInput input) {

    }

    @Override
    public void onUnregisterInput(IInput input) {

    }

    @Override
    public void onRegisterOutput(IOutput output) {

    }

    @Override
    public void onUnregisterOutput(IOutput output) {

    }

    private class Output extends OutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);
            Schema schema = getSchema();
            schema.addColumn(ColumnHolderUtils.createColumnHolder(OUTPUT_OPERATOR_NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(OUTPUT_OPERATOR_TYPE_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(OUTPUT_NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(INPUT_OPERATOR_NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(INPUT_OPERATOR_TYPE_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(INPUT_NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
        }

    }

}
