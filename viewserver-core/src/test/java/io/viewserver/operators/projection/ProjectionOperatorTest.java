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

package io.viewserver.operators.projection;

import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.ChangeRecorder;
import io.viewserver.operators.TestReactor;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by bemm on 31/10/2014.
 */
public class ProjectionOperatorTest {
    @Test
    public void canAddRows() throws Exception {
        IExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);
        schema.addColumn("remove", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
                row.setInt("remove", 3);
            }
        });
        executionContext.commit();

        ProjectionOperator projection = new ProjectionOperator("projection", executionContext, catalog);
        projection.configure(new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return ProjectionMode.Inclusionary;
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                return Arrays.asList(
                        new ProjectionColumn("market", "test"),
                        new ProjectionColumn("product")
                );
            }
        }, new CommandResult());
        table.getOutput().plugIn(projection.getInput());

        projection.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();
    }

    @Test
    public void canUpdateRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);
        schema.addColumn("remove", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
                row.setInt("remove", 3);
            }
        });
        executionContext.commit();

        ProjectionOperator projection = new ProjectionOperator("projection", executionContext, catalog);
        projection.configure(new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return ProjectionMode.Inclusionary;
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                return Arrays.asList(
                        new ProjectionColumn("market", "test"),
                        new ProjectionColumn("product")
                );
            }
        }, new CommandResult());
        table.getOutput().plugIn(projection.getInput());

        projection.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.updateRow(0, new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 9);
            }
        });

        executionContext.commit();
    }

    @Test
    public void canRemoveRows() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);
        schema.addColumn("remove", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market", 1);
                row.setInt("product", 2);
                row.setInt("remove", 3);
            }
        });
        executionContext.commit();

        ProjectionOperator projection = new ProjectionOperator("projection", executionContext, catalog);
        projection.configure(new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return ProjectionMode.Inclusionary;
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                return Arrays.asList(
                        new ProjectionColumn("market", "test"),
                        new ProjectionColumn("product")
                );
            }
        }, new CommandResult());
        table.getOutput().plugIn(projection.getInput());

        projection.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        table.removeRow(0);

        executionContext.commit();
    }

    @Test
    public void canUseRegexes() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        Catalog catalog = new Catalog(executionContext);

        Schema schema = new Schema();
        schema.addColumn("market1", ColumnType.Int);
        schema.addColumn("market2", ColumnType.Int);
        schema.addColumn("product", ColumnType.Int);

        Table table = new Table("table", executionContext, catalog, schema, new ChunkedColumnStorage(1024));
        table.initialise(8);

        table.addRow(new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                row.setInt("market1", 1);
                row.setInt("market2", 1);
                row.setInt("product", 1);
            }
        });
        executionContext.commit();

        ProjectionOperator projection = new ProjectionOperator("projection", executionContext, catalog);
        projection.configure(new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return ProjectionMode.Projection;
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                return Arrays.asList(
                        new ProjectionColumn("market([0-9]+)", true, "projected$1")
                );
            }
        }, new CommandResult());
        table.getOutput().plugIn(projection.getInput());

        projection.getOutput().plugIn(new ChangeRecorder("rec", executionContext, catalog).getInput());

        executionContext.commit();

        projection.configure(new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return ProjectionMode.Inclusionary;
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                return Arrays.asList(
                        new ProjectionColumn("market([0-9]+)", true)
                );
            }
        }, new CommandResult());
        executionContext.commit();

        projection.configure(new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return ProjectionMode.Exclusionary;
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                return Arrays.asList(
                        new ProjectionColumn("market([0-9]+)", true)
                );
            }
        }, new CommandResult());
        executionContext.commit();
    }
}
