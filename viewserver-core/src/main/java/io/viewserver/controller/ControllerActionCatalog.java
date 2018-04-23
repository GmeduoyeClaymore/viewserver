package io.viewserver.controller;

import io.viewserver.Constants;
import io.viewserver.catalog.CatalogHolder;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.InputOperatorBase;
import io.viewserver.operators.OutputBase;
import io.viewserver.operators.table.TableRow;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnType;

public class ControllerActionCatalog extends InputOperatorBase {
    private final Output output;
    private final ICatalog catalogHolder;
    private final ITableStorage storage;
    private final TableRow myTableRow;
    protected boolean initialised;

    public static final String PATH_COLUMN = "path";
    public static final String SYNCHRONOUS = "sync";
    public static final String RETURN_TYPE = "returnType";
    public static final String PARAMETER_JSON = "parameterJSON";
    private int actionRowCount = 0;


    public ControllerActionCatalog(String id, ITableStorage storage, IExecutionContext context, ICatalog catalog) {
        super(id, context, catalog);

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

    public void registerControllerAction(ControllerActionEntry action) {
        myTableRow.setRowId(actionRowCount++);
        myTableRow.setString(PATH_COLUMN, action.path());
        myTableRow.setBool(SYNCHRONOUS, action.isSynchronous());
        myTableRow.setString(RETURN_TYPE, action.returnType() + "");
        myTableRow.setString(PARAMETER_JSON, action.parameterJSON() + "");
        this.output.handleAdd(myTableRow.getRowId());
    }

    private class Output extends OutputBase {


        public Output(String name, IOperator owner) {
            super(name, owner);
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(PATH_COLUMN, io.viewserver.schema.column.ColumnType.String));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(SYNCHRONOUS, ColumnType.Bool));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(RETURN_TYPE, ColumnType.String));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(PARAMETER_JSON, ColumnType.String));
        }


    }
}
