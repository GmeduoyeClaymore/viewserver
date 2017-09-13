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

package io.viewserver.operators.join;

import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Test;

/**
 * Created by nickc on 27/10/2014.
 */
public class JoinOperatorTest {
    @Test
    public void canAddRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema leftSchema = new Schema();
        leftSchema.addColumn("market", ColumnType.Int);
        leftSchema.addColumn("notional", ColumnType.Int);

        Table leftTable = new Table("leftTable", executionContext, catalog, leftSchema, new ChunkedColumnStorage(1024));
        leftTable.initialise(8);

        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });
        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("notional", 200);
            }
        });
        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 300);
            }
        });

        Schema rightSchema = new Schema();
        rightSchema.addColumn("market2", ColumnType.Int);
        rightSchema.addColumn("notional2", ColumnType.Int);

        Table rightTable = new Table("rightTable", executionContext, catalog, rightSchema, new ChunkedColumnStorage(1024));
        rightTable.initialise(8);

        JoinOperator join = new JoinOperator("join", executionContext, catalog);
        join.configure(new IJoinConfig() {
            @Override
            public String[] getLeftJoinColumns() {
                return new String[] { "market" };
            }

            @Override
            public boolean isLeftJoinOuter() {
                return false;
            }

            @Override
            public String[] getRightJoinColumns() {
                return new String[] { "market2" };
            }

            @Override
            public boolean isRightJoinOuter() {
                return false;
            }

            @Override
            public IColumnNameResolver getColumnNameResolver() {
                return null;
            }

            @Override
            public boolean getAlwaysResolveNames() {
                return false;
            }
        }, new CommandResult());
        leftTable.getOutput().plugIn(join.getInput("left"));
        rightTable.getOutput().plugIn(join.getInput("right"));

        join.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();

        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 2);
                row.setInt("notional2", 2000);
            }
        });

        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 1);
                row.setInt("notional2", 1000);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canUpdateRows_JoinColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema leftSchema = new Schema();
        leftSchema.addColumn("market", ColumnType.Int);
        leftSchema.addColumn("notional", ColumnType.Int);

        Table leftTable = new Table("leftTable", executionContext, catalog, leftSchema, new ChunkedColumnStorage(1024));
        leftTable.initialise(8);

        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        Schema rightSchema = new Schema();
        rightSchema.addColumn("market2", ColumnType.Int);
        rightSchema.addColumn("notional2", ColumnType.Int);

        Table rightTable = new Table("rightTable", executionContext, catalog, rightSchema, new ChunkedColumnStorage(1024));
        rightTable.initialise(8);

        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 1);
                row.setInt("notional2", 1000);
            }
        });
        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 2);
                row.setInt("notional2", 2000);
            }
        });

        JoinOperator join = new JoinOperator("join", executionContext, catalog);
        join.configure(new IJoinConfig() {
            @Override
            public String[] getLeftJoinColumns() {
                return new String[] { "market" };
            }

            @Override
            public boolean isLeftJoinOuter() {
                return false;
            }

            @Override
            public String[] getRightJoinColumns() {
                return new String[] { "market2" };
            }

            @Override
            public boolean isRightJoinOuter() {
                return false;
            }

            @Override
            public IColumnNameResolver getColumnNameResolver() {
                return null;
            }

            @Override
            public boolean getAlwaysResolveNames() {
                return false;
            }
        }, new CommandResult());
        leftTable.getOutput().plugIn(join.getInput("left"));
        rightTable.getOutput().plugIn(join.getInput("right"));

        join.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();

        leftTable.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canUpdateRows_OtherColumns() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema leftSchema = new Schema();
        leftSchema.addColumn("market", ColumnType.Int);
        leftSchema.addColumn("notional", ColumnType.Int);

        Table leftTable = new Table("leftTable", executionContext, catalog, leftSchema, new ChunkedColumnStorage(1024));
        leftTable.initialise(8);

        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        Schema rightSchema = new Schema();
        rightSchema.addColumn("market2", ColumnType.Int);
        rightSchema.addColumn("notional2", ColumnType.Int);

        Table rightTable = new Table("rightTable", executionContext, catalog, rightSchema, new ChunkedColumnStorage(1024));
        rightTable.initialise(8);

        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 1);
                row.setInt("notional2", 1000);
            }
        });

        JoinOperator join = new JoinOperator("join", executionContext, catalog);
        join.configure(new IJoinConfig() {
            @Override
            public String[] getLeftJoinColumns() {
                return new String[] { "market" };
            }

            @Override
            public boolean isLeftJoinOuter() {
                return false;
            }

            @Override
            public String[] getRightJoinColumns() {
                return new String[] { "market2" };
            }

            @Override
            public boolean isRightJoinOuter() {
                return false;
            }

            @Override
            public IColumnNameResolver getColumnNameResolver() {
                return null;
            }

            @Override
            public boolean getAlwaysResolveNames() {
                return false;
            }
        }, new CommandResult());
        leftTable.getOutput().plugIn(join.getInput("left"));
        rightTable.getOutput().plugIn(join.getInput("right"));

        join.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();

        leftTable.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("notional", 200);
            }
        });

        executionContext.commit();

        rightTable.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("notional2", 2000);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canRemoveRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema leftSchema = new Schema();
        leftSchema.addColumn("market", ColumnType.Int);
        leftSchema.addColumn("notional", ColumnType.Int);

        Table leftTable = new Table("leftTable", executionContext, catalog, leftSchema, new ChunkedColumnStorage(1024));
        leftTable.initialise(8);

        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });
        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("notional", 200);
            }
        });

        Schema rightSchema = new Schema();
        rightSchema.addColumn("market2", ColumnType.Int);
        rightSchema.addColumn("notional2", ColumnType.Int);

        Table rightTable = new Table("rightTable", executionContext, catalog, rightSchema, new ChunkedColumnStorage(1024));
        rightTable.initialise(8);

        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 1);
                row.setInt("notional2", 1000);
            }
        });
        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 2);
                row.setInt("notional2", 2000);
            }
        });

        JoinOperator join = new JoinOperator("join", executionContext, catalog);
        join.configure(new IJoinConfig() {
            @Override
            public String[] getLeftJoinColumns() {
                return new String[] { "market" };
            }

            @Override
            public boolean isLeftJoinOuter() {
                return false;
            }

            @Override
            public String[] getRightJoinColumns() {
                return new String[] { "market2" };
            }

            @Override
            public boolean isRightJoinOuter() {
                return false;
            }

            @Override
            public IColumnNameResolver getColumnNameResolver() {
                return null;
            }

            @Override
            public boolean getAlwaysResolveNames() {
                return false;
            }
        }, new CommandResult());
        leftTable.getOutput().plugIn(join.getInput("left"));
        rightTable.getOutput().plugIn(join.getInput("right"));

        join.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();

        leftTable.removeRow(0);

        executionContext.commit();

        rightTable.removeRow(1);

        executionContext.commit();
    }

    @Test
    public void crossJoin() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema leftSchema = new Schema();
        leftSchema.addColumn("market", ColumnType.Int);

        Table leftTable = new Table("leftTable", executionContext, catalog, leftSchema, new ChunkedColumnStorage(1024));
        leftTable.initialise(8);

        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
            }
        });
        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
            }
        });

        Schema rightSchema = new Schema();
        rightSchema.addColumn("market2", ColumnType.Int);

        Table rightTable = new Table("rightTable", executionContext, catalog, rightSchema, new ChunkedColumnStorage(1024));
        rightTable.initialise(8);

        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 3);
            }
        });
        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 4);
            }
        });

        JoinOperator join = new JoinOperator("join", executionContext, catalog);
        join.configure(new IJoinConfig() {
            @Override
            public String[] getLeftJoinColumns() {
                return new String[0];
            }

            @Override
            public boolean isLeftJoinOuter() {
                return false;
            }

            @Override
            public String[] getRightJoinColumns() {
                return new String[0];
            }

            @Override
            public boolean isRightJoinOuter() {
                return false;
            }

            @Override
            public IColumnNameResolver getColumnNameResolver() {
                return null;
            }

            @Override
            public boolean getAlwaysResolveNames() {
                return false;
            }
        }, new CommandResult());
        leftTable.getOutput().plugIn(join.getInput("left"));
        rightTable.getOutput().plugIn(join.getInput("right"));

        join.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();
    }

    @Test
    public void outerJoinTest() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog catalog = new Catalog(executionContext);

        Schema leftSchema = new Schema();
        leftSchema.addColumn("market", ColumnType.Int);
        leftSchema.addColumn("notional", ColumnType.Int);

        Table leftTable = new Table("leftTable", executionContext, catalog, leftSchema, new ChunkedColumnStorage(1024));
        leftTable.initialise(8);

        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("notional", 100);
            }
        });

        Schema rightSchema = new Schema();
        rightSchema.addColumn("market2", ColumnType.Int);
        rightSchema.addColumn("notional2", ColumnType.Int);

        Table rightTable = new Table("rightTable", executionContext, catalog, rightSchema, new ChunkedColumnStorage(1024));
        rightTable.initialise(8);

        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 2);
                row.setInt("notional2", 2000);
            }
        });

        JoinOperator join = new JoinOperator("join", executionContext, catalog);
        join.configure(new IJoinConfig() {
            @Override
            public String[] getLeftJoinColumns() {
                return new String[] { "market" };
            }

            @Override
            public boolean isLeftJoinOuter() {
                return true;
            }

            @Override
            public String[] getRightJoinColumns() {
                return new String[] { "market2" };
            }

            @Override
            public boolean isRightJoinOuter() {
                return true;
            }

            @Override
            public IColumnNameResolver getColumnNameResolver() {
                return null;
            }

            @Override
            public boolean getAlwaysResolveNames() {
                return false;
            }
        }, new CommandResult());
        leftTable.getOutput().plugIn(join.getInput("left"));
        rightTable.getOutput().plugIn(join.getInput("right"));

        join.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());
        executionContext.commit();

        leftTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 2);
                row.setInt("notional", 200);
            }
        });
        rightTable.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market2", 1);
                row.setInt("notional2", 1000);
            }
        });

        executionContext.commit();

        leftTable.removeRow(1);
        rightTable.removeRow(1);

        executionContext.commit();
    }
}
