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

package io.viewserver.report;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.execution.context.ReportContextExecutionPlanContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.InputOperatorBase;
import io.viewserver.operators.OutputBase;
import io.viewserver.operators.table.TableRow;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnType;

/**
 * Created by nick on 31/03/2015.
 */
public class ReportCatalog extends InputOperatorBase{
    public static final String NAME_COLUMN = "name";
    public static final String TYPE_COLUMN = "type";
    public static final String DISTRIBUTED_COLUMN = "distributed";
    public static final String OPNAME_COLUMN = "opName";
    public static final String PATH_COLUMN = "path";

    private final Output output;
    private ITableStorage storage;
    protected TableRow tableRow;
    private boolean initialised;

    public ReportCatalog(String name, ReportContextExecutionPlanContext executionPlanContext, ICatalog parent, ITableStorage storage) {
        super(name, parent.getExecutionContext(), parent);
        this.storage = storage;
        output = new Output(Constants.OUT, this);
        addOutput(output);
        tableRow = new TableRow(0, output.getSchema());
        initialise(1024);
        setSystemOperator(true);
    }

    public void initialise(int capacity) {
        if (initialised) {
            throw new RuntimeException("Table already initialised");
        }

        storage.initialise(capacity, output.getSchema(), output.getCurrentChanges());

        initialised = true;
    }



    private class Output extends OutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);

            Schema schema = getSchema();
            schema.addColumn(ColumnHolderUtils.createColumnHolder(NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(TYPE_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(DISTRIBUTED_COLUMN, ColumnType.Bool));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(OPNAME_COLUMN, ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(PATH_COLUMN, ColumnType.String));
        }
    }


}
