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

package io.viewserver.operators.filter;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.ExecutionContext;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.operators.*;
import io.viewserver.schema.SchemaChange;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowFlags;
import io.viewserver.schema.column.IRowMapper;
import io.viewserver.schema.column.RowMapperBase;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by nickc on 03/10/2014.
 */
public class FilterOperator extends ConfigurableOperatorBase<IFilterConfig> {
    private IInput input;
    private Output output;
    private FilterMode mode = FilterMode.Transparent;
    private IExpressionParser expressionParser;
    private IExpressionBool filterExpression;
    private BitSet filteredColumns;
    private final IntHashSet outputRows;

    public FilterOperator(String name, ExecutionContext executionContext, ICatalog catalog, IExpressionParser expressionParser) {
        super(name, executionContext, catalog);
        this.expressionParser = expressionParser;

        outputRows = new IntHashSet(128, 0.75f, -1);

        input = createInput();
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);
    }

    protected IInput createInput() {
        return new Input(Constants.IN, this);
    }

    public IInput getInput() {
        return input;
    }

    public IOutput getOutput() {
        return output;
    }

    public FilterMode getMode() {
        return mode;
    }

    public void setMode(FilterMode mode) {
        this.mode = mode;
    }

    public IExpressionBool getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(IExpressionBool filterExpression) {
        this.filterExpression = filterExpression;
    }

    @Override
    protected IFilterConfig mergePendingConfig(IFilterConfig pendingConfig, IFilterConfig newConfig) {
        if (!pendingConfig.getMode().equals(newConfig.getMode())) {
            throw new IllegalStateException("Cannot merge configs with conflicting filter modes");
        }
        if (!Objects.equals(pendingConfig.getExpression(), newConfig.getExpression())) {
            throw new IllegalStateException("Cannot merge configs with conflicting filter expressions");
        }
        return new IFilterConfig() {
            @Override
            public FilterMode getMode() {
                return pendingConfig.getMode();
            }

            @Override
            public String getExpression() {
                return pendingConfig.getExpression();
            }

            @Override
            public Map<String, String> getColumnAliases() {
                HashMap<String, String> aliases = new HashMap<>();
                if (pendingConfig.getColumnAliases() != null) {
                    aliases.putAll(pendingConfig.getColumnAliases());
                }
                if (newConfig.getColumnAliases() != null) {
                    aliases.putAll(newConfig.getColumnAliases());
                }

                return aliases;
            }
        };
    }

    @Override
    protected void onDataClear() {
        super.onDataClear();

        outputRows.clear();
    }

    protected int getOutputRow(int row) {
        int mappedRow = outputRows.addInt(row);
        if (mappedRow < 0) {
            mappedRow = -mappedRow - 1;
        }
        return mappedRow;
    }

    protected int getInputRow(int row) {
        return outputRows.get(row);
    }

    protected class Input extends InputBase {
        private final BitSet removedColumns;
        private final TIntArrayList removedRows = new TIntArrayList(8, -1);

        public Input(String name, IOperator owner) {
            super(name, owner);
            removedColumns = new BitSet();
        }

        @Override
        public void onSchema() {
            super.onSchema();

            if (pendingConfig != null) {
                if (!pendingConfig.getMode().equals(mode)) {
                    mode = pendingConfig.getMode();
                    input.resetData();
                }

                if (mode.equals(FilterMode.Filter)) {
                    if (config == null ||
                            !Objects.equals(pendingConfig.getExpression(), config.getExpression())) {
                        String expressionString = pendingConfig.getExpression();
                        if (expressionString != null) {
                            filteredColumns = new BitSet();
                            IExpression expression;
                            try {
                                expression = expressionParser.parse(expressionString, input.getProducer().getSchema(),
                                        pendingConfig.getColumnAliases(), filteredColumns, null);
                            } catch (Throwable e) {
                                mode = FilterMode.Transparent;
                                throw e;
                            }
                            if (!(expression instanceof IExpressionBool)) {
                                throw new IllegalArgumentException("Filter expression must evaluate to a boolean");
                            }
                            filterExpression = (IExpressionBool) expression;
                            input.resetData();
                        }
                    }
                } else {
                    filterExpression = null;
                    filteredColumns = null;
                }
            }
        }

        @Override
        protected void onSchemaChange(SchemaChange schemaChange) {
            removedColumns.clear();

            super.onSchemaChange(schemaChange);

            if (mode == FilterMode.Filter) {
                removedColumns.and(filteredColumns);
                if (!removedColumns.isEmpty()) {
                    mode = FilterMode.Transparent;
                    resetData();
                }
            }
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            ColumnHolder outHolder = output.columnHolderFactory.createColumnHolder(columnHolder.getName(), columnHolder);
            output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            removedColumns.set(output.getOutboundColumnId(columnHolder));

            output.unmapColumn(columnHolder);
        }

        @Override
        protected void onRowAdd(int row) {
            if (matches(row)) {
                int mappedRow = getOutputRow(row);
                removedRows.remove(row);
                output.handleAdd(mappedRow);
            }
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            // TODO: check dirty flags (efficiently!)
            boolean matches = matches(row);
            int outputRow;
            boolean didMatch = (outputRow = outputRows.index(row)) != -1;
            if (matches && !didMatch) {
                outputRow = getOutputRow(row);
                output.handleAdd(outputRow);
            } else if (!matches && didMatch) {
                removedRows.add(row);
                output.handleRemove(outputRow);
            } else if (matches) {
                output.handleUpdate(outputRow);
            }
        }

        @Override
        protected void onRowRemove(int row) {
            int outputRow;
            if ((outputRow = outputRows.index(row)) != -1) {
                removedRows.add(row);
                output.handleRemove(outputRow);
            }
        }

        private boolean matches(int row) {
            return mode.equals(FilterMode.Transparent) || filterExpression == null || (mode == FilterMode.Filter && filterExpression.getBool(row));
        }

        @Override
        public void onAfterCommit() {
            super.onAfterCommit();

            removedRows.forEach(removeRowProc);
            removedRows.resetQuick();
        }

        private final TIntProcedure removeRowProc = (row) -> {
            outputRows.remove(row);
            return true;
        };
    }

    private class Output extends MappedOutputBase {
        private IColumnHolderFactory columnHolderFactory;

        public Output(String name, IOperator owner) {
            super(name, owner);

            columnHolderFactory = new MappedColumnHolderFactory();
        }

        @Override
        public IColumnHolderFactory getColumnHolderFactory() {
            return columnHolderFactory;
        }
    }

    private class MappedColumnHolderFactory extends ColumnHolderFactory {
        @Override
        public IRowMapper getRowMapper(IRowMapper upstreamMapper) {
            return new RowMapper(upstreamMapper);
        }
    }

    private class RowMapper extends RowMapperBase {
        private RowMapper(IRowMapper upstreamMapper) {
            super(upstreamMapper);
        }

        @Override
        public int lookupInboundRow(int row) {
            return outputRows.get(row);
        }
    }

    public enum FilterMode {
        Transparent,
        Filter,
        Opaque
    }
}
