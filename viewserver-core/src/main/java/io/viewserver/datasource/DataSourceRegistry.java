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

package io.viewserver.datasource;

import io.viewserver.Constants;
import io.viewserver.catalog.CatalogHolder;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.core.Utils;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.operators.*;
import io.viewserver.operators.table.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import gnu.trove.map.hash.TIntObjectHashMap;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;



/**
 * Created by nick on 18/02/2015.
 */
public class DataSourceRegistry<T extends IDataSource> extends KeyedTable implements IDataSourceRegistry<T>, ICatalog {
    protected final ICatalog systemCatalog;
    protected final IExecutionContext executionContext;
    private final IJsonSerialiser serialiser = new JacksonSerialiser();
    private Class<T> clazz;
    private final List<IDataSourceListener> listeners = new ArrayList<>();
    private final TIntObjectHashMap<T> dataSourcesById = new TIntObjectHashMap<>();
    private final CatalogHolder catalogHolder;

    public DataSourceRegistry(ICatalog systemCatalog, IExecutionContext executionContext, Class<T> clazz) {
        super(TABLE_NAME, executionContext, systemCatalog, getSchema(), new ChunkedColumnStorage(32), getTableKeyDefinitions());
        this.systemCatalog = systemCatalog;
        this.executionContext = executionContext;
        this.clazz = clazz;

        this.catalogHolder = new CatalogHolder(this);

        setSystemOperator(true);
        setAllowDataReset(true);
        initialise(8);
    }

    private static Schema getSchema() {
        Schema schema = new Schema();
        schema.addColumn(ColumnHolderUtils.createColumnHolder(ID_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(JSON_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(STATUS_COL, io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder(PATH_COL, io.viewserver.schema.column.ColumnType.String));
        return schema;
    }

    protected static TableKeyDefinition getTableKeyDefinitions() {
        return new TableKeyDefinition(ID_COL);
    }

    @Override
    public void register(T dataSource) {
        executionContext.getReactor().scheduleTask(() -> {
            addRow(new ITableRowUpdater() {
                @Override
                public Object getValue(String columnName) {
                    switch (columnName) {
                        case ID_COL: {
                            return dataSource.getName();
                        }
                        default: {
                            throw new UnsupportedOperationException();
                        }
                    }
                }

                @Override
                public void setValues(ITableRow row) {
                    row.setString(ID_COL, dataSource.getName());
                    row.setString(JSON_COL, serialiser.serialise(dataSource));
                    dataSourcesById.put(row.getRowId(), dataSource);
                }
            });
        }, 0, -1);
    }

    @Override
    public Observable<IOperator> getOperatorObservable(String name) {
        return catalogHolder.getOperatorObservable(name);
    }

    @Override
    public int addRow(ITableRowUpdater updater) {
        final String[] values = new String[1];
        final int row = super.addRow(new DelegatingTableRowUpdater(updater) {
            @Override
            public void setValues(ITableRow row) {
                super.setValues(row);
                values[0] = (String) super.getValue(ID_COL);
                row.setString(STATUS_COL, DataSourceStatus.UNINITIALIZED.toString());
                row.setString(PATH_COL, String.format("%s/%s", getPath(), values[0]));
            }
        });
        new DataSourceCatalog(values[0], new ChunkedColumnStorage(1024));
        fireListeners(l -> l.onDataSourceRegistered(getDataSourceForRow(row)));
        return row;
    }

    @Override
    public void setStatus(String name, DataSourceStatus status) {
        int rowId = getRow(new TableKey(name));
        updateRow(rowId, row -> row.setString(STATUS_COL, status.toString()));
        fireListeners(l -> l.onDataSourceStatusChanged(get(name), status));
    }

    private void fireListeners(Consumer<IDataSourceListener> consumer) {
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            consumer.accept(listeners.get(i));
        }
    }

    @Override
    public DataSourceStatus getStatus(String name) {
        int rowId = getRow(new TableKey(name));
        if (rowId == -1) {
            return DataSourceStatus.UNKNOWN;
        }
        ColumnHolder statusColHolder = getOutput().getSchema().getColumnHolder(STATUS_COL);
        final String statusName = (String) ColumnHolderUtils.getValue(statusColHolder, rowId);
        return DataSourceStatus.valueOf(statusName);
    }

    @Override
    public T get(String name) {
        int rowId = getRow(new TableKey(name));
        if (rowId == -1) {
            return null;
        }
        return getDataSourceForRow(rowId);
    }

    private T getDataSourceForRow(int rowId) {
        T dataSource = dataSourcesById.get(rowId);
        if (dataSource != null) {
            return dataSource;
        }
        ColumnHolder jsonColHolder = getOutput().getSchema().getColumnHolder(JSON_COL);
        String dataSourceJson = (String)ColumnHolderUtils.getValue(jsonColHolder, rowId);
        dataSource = deserialise(dataSourceJson);
        dataSourcesById.put(rowId, dataSource);
        return dataSource;
    }

    @Override
    public Collection<T> getAll() {
        List<T> dataSources = new ArrayList<>();
        IRowSequence allRows =  getOutput().getAllRows();
        while (allRows.moveNext()) {
            dataSources.add(getDataSourceForRow(allRows.getRowId()));
        }
        return dataSources;
    }

    @Override
    public void clear() {
        resetData();

        List<IOperator> operators = new ArrayList<>(catalogHolder.getAllOperators());
        operators.forEach(this::removeDataSourceCatalog);
    }

    private void removeDataSourceCatalog(IOperator dataSourceCatalog) {
        dataSourceCatalog.tearDown();
    }

    @Override
    public void onDataSourceBuilt(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        final String name = dataSourceExecutionPlanContext.getDataSource().getName();
        final DataSourceCatalog dataSourceCatalog = (DataSourceCatalog) catalogHolder.getOperator(name);
        dataSourceCatalog.registerNodes(dataSourceExecutionPlanContext);
    }

    @Override
    public void addListener(IDataSourceListener listener) {
        listeners.add(listener);

        ArrayList<T> dataSources = new ArrayList<>(getAll());
        int count = dataSources.size();
        for (int i = 0; i < count; i++) {
            IDataSource dataSource = dataSources.get(i);
            listener.onDataSourceRegistered(dataSource);
            listener.onDataSourceStatusChanged(dataSource, getStatus(dataSource.getName()));
        }
    }

    private T deserialise(String json) {
        json = Utils.replaceSystemTokens(json);
        return serialiser.deserialise(json, clazz);
    }

    @Override
    public ICatalog getParent() {
        return catalogHolder.getParent();
    }

    @Override
    public void registerOperator(IOperator operator) {
        catalogHolder.registerOperator(operator);
    }

    @Override
    public IOperator getOperator(String name) {
        return catalogHolder.getOperator(name);
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

    @Override
    public void doTearDown() {
        catalogHolder.tearDown();
        super.doTearDown();
    }

    public class DataSourceCatalog extends InputOperatorBase implements ICatalog {
        private final TIntObjectHashMap<NodeSpec> nodes = new TIntObjectHashMap<>(8, 0.75f, -1);
        private final IntHashSet nodeIds = new IntHashSet(8, 0.75f, -1);
        private final Output output;
        private final ICatalog catalogHolder;
        private final ITableStorage storage;
        private final TableRow myTableRow;
        private DataSourceExecutionPlanContext executionPlanContext;
        protected boolean initialised;

        public static final String NAME_COLUMN = "name";
        public static final String TYPE_COLUMN = "type";
        public static final String OPNAME_COLUMN = "opName";
        public static final String PATH_COLUMN = "path";

        public DataSourceCatalog(String id, ITableStorage storage) {
            super(id, DataSourceRegistry.this.executionContext, DataSourceRegistry.this);

            this.storage = storage;
            output = new Output(Constants.OUT, this);
            addOutput(output);

            catalogHolder = new CatalogHolder(this);

            setSystemOperator(true);

            initialise(1024);

            myTableRow = new TableRow(0, output.getSchema());
        }

        public void initialise(int capacity) {
            if (initialised) {
                throw new RuntimeException("Table already initialised");
            }

            storage.initialise(capacity, output.getSchema(), output.getCurrentChanges());

            initialised = true;
        }


        public void registerNodes(DataSourceExecutionPlanContext executionPlanContext) {
            this.executionPlanContext = executionPlanContext;
            // TODO: do something better to get the table in there
            final String tableName = executionPlanContext.getDataSource().getName();
            registerNode(new NodeSpec(tableName, "Table"));
            final List<IGraphNode> graphNodes = executionPlanContext.getGraphNodes();
            int count = graphNodes.size();
            for (int i = 0; i < count; i++) {
                final IGraphNode node = graphNodes.get(i);
                registerNode(new NodeSpec(node.getName(), node.getType()));
            }
        }

        private void registerNode(NodeSpec node) {
            final int hashCode = node.hashCode();
            nodes.put(hashCode, node);
            final int rowId = nodeIds.addInt(hashCode);
            myTableRow.setRowId(rowId);
            myTableRow.setString(NAME_COLUMN,  node.name );
            myTableRow.setString(TYPE_COLUMN,  node.type);
            myTableRow.setString(OPNAME_COLUMN,  getOperatorName(node));
            myTableRow.setString(PATH_COLUMN,  getOperator(getOperatorName(node)).getPath());
            output.handleAdd(rowId);
        }


        private String getOperatorName(NodeSpec node) {
            return "Table".equals(node.type) ? node.name : executionPlanContext.getOperatorName(node.name);
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
        public void registerOperator(IOperator operator) {
            catalogHolder.registerOperator(operator);
        }

        @Override
        public IOperator getOperator(String name) {
            return catalogHolder.getOperator(name);
        }

        @Override
        public Observable<IOperator> getOperatorObservable(String name) {
            return catalogHolder.getOperatorObservable(name);
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

        @Override
        public void doTearDown() {
            catalogHolder.tearDown();
            super.doTearDown();
        }

        private class NodeSpec {
            private String name;
            private String type;

            public NodeSpec(String name, String type) {
                this.name = name;
                this.type = type;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                NodeSpec nodeSpec = (NodeSpec) o;

                if (!name.equals(nodeSpec.name)) return false;
                return type.equals(nodeSpec.type);

            }

            @Override
            public int hashCode() {
                int result = name.hashCode();
                result = 31 * result + type.hashCode();
                return result;
            }
        }

        private class Output extends OutputBase {
            public Output(String name, IOperator owner) {
                super(name, owner);
                getSchema().addColumn(ColumnHolderUtils.createColumnHolder(NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
                getSchema().addColumn(ColumnHolderUtils.createColumnHolder(TYPE_COLUMN, io.viewserver.schema.column.ColumnType.String));
                getSchema().addColumn(ColumnHolderUtils.createColumnHolder(OPNAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
                getSchema().addColumn(ColumnHolderUtils.createColumnHolder(PATH_COLUMN, io.viewserver.schema.column.ColumnType.String));

            }
        }
    }
}
