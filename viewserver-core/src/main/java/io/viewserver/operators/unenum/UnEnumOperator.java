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

package io.viewserver.operators.unenum;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDimensionMapper;
import io.viewserver.operators.*;
import io.viewserver.schema.column.*;

import java.util.List;

/**
 * Created by nickc on 22/10/2014.
 */
public class UnEnumOperator extends ConfigurableOperatorBase<IUnEnumConfig> {
    private IDataSource dataSource;
    private final IDimensionMapper dimensionMapper;
    private Input input;
    private Output output;
    private List<String> dimensions;

    public UnEnumOperator(String name, IExecutionContext executionContext, ICatalog catalog, IDimensionMapper dimensionMapper) {
        super(name, executionContext, catalog);
        this.dimensionMapper = dimensionMapper;

        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);
        register();
    }

    public IInput getInput() {
        return input;
    }

    public IOutput getOutput() {
        return output;
    }

    @Override
    protected IUnEnumConfig mergePendingConfig(IUnEnumConfig pendingConfig, IUnEnumConfig newConfig) {
        if (pendingConfig.getDataSource() != null && !pendingConfig.getDataSource().getName().equals(newConfig.getDataSource().getName())) {
            throw new IllegalStateException("Cannot merge configs with conflicting data sources");
        }
        return pendingConfig;
    }

    @Override
    protected void processConfig(IUnEnumConfig config) {
        this.dataSource = config.getDataSource();
        this.dimensions = config.getDimensions();
    }

    private class Input extends InputBase {
        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            Dimension dimension = dataSource == null ? null : dataSource.getDimension(columnHolder.getName());
            if (dimension != null && !(columnHolder.getColumn() instanceof IUnEnumColumn)
                    && (dimensions == null || dimensions.isEmpty() || dimensions.contains(dimension.getName()))) {
                ColumnType type = dimension.getContentType().getColumnType();
                if (type == ColumnType.Bool) {
                    type = ColumnType.NullableBool;
                }
                ColumnHolder outHolder = ColumnHolderUtils.createColumnHolder(columnHolder.getName(), type);

                createMetaData(outHolder, type);

                switch (type) {
                    case NullableBool: {
                        outHolder.setColumn(new UnEnumColumnNullableBool(columnHolder, outHolder, dataSource.getName(), dimension, dimensionMapper));
                        break;
                    }
                    case Byte: {
                        outHolder.setColumn(new UnEnumColumnByte(columnHolder, outHolder, dataSource.getName(), dimension, dimensionMapper));
                        break;
                    }
                    case Short: {
                        outHolder.setColumn(new UnEnumColumnShort(columnHolder, outHolder, dataSource.getName(), dimension, dimensionMapper));
                        break;
                    }
                    case Int: {
                        outHolder.setColumn(new UnEnumColumnInt(columnHolder, outHolder, dataSource.getName(), dimension, dimensionMapper));
                        break;
                    }
                    case Long: {
                        outHolder.setColumn(new UnEnumColumnLong(columnHolder, outHolder, dataSource.getName(), dimension, dimensionMapper));
                        break;
                    }
                    case String: {
                        outHolder.setColumn(new UnEnumColumnString(columnHolder, outHolder, dataSource.getName(), dimension, dimensionMapper));
                        break;
                    }
                }


                output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
                return;
            } else {
                ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(columnHolder.getName(), columnHolder);
                output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
            }
        }

        private void createMetaData(ColumnHolder columnHolder, ColumnType type) {
            ColumnMetadata metadata = ColumnHolderUtils.createColumnMetadata(type);
            switch (type) {
                case Bool: {
                    ((ColumnMetadataBool) metadata).setNullValue(false);
                    break;
                }
                case NullableBool: {
                    ((ColumnMetadataNullableBool) metadata).setNullValue(NullableBool.Null);
                    break;
                }
                case Byte: {
                    ((ColumnMetadataByte) metadata).setNullValue((byte) -1);
                    break;
                }
                case Short: {
                    ((ColumnMetadataShort) metadata).setNullValue(Short.MIN_VALUE);
                    break;
                }
                case Int: {
                    ((ColumnMetadataInt) metadata).setNullValue(Integer.MIN_VALUE);
                    break;
                }
                case Long: {
                    ((ColumnMetadataLong) metadata).setNullValue(Long.MIN_VALUE);
                    break;
                }
                case String: {
                    ((ColumnMetadataString) metadata).setNullValue(null);
                    break;
                }
                case Float: {
                    ((ColumnMetadataFloat) metadata).setNullValue(Float.MIN_VALUE);
                    break;
                }
                case Double: {
                    ((ColumnMetadataDouble) metadata).setNullValue(Double.MIN_VALUE);
                    break;
                }
            }
            columnHolder.setMetadata(metadata);
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            output.unmapColumn(columnHolder);
        }

        @Override
        protected void onRowAdd(int row) {
            output.handleAdd(row);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            output.handleUpdate(row);
        }

        @Override
        protected void onRowRemove(int row) {
            output.handleRemove(row);
        }
    }

    private class Output extends MappedOutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public IColumnHolderFactory getColumnHolderFactory() {
            return new PassThroughColumnHolderFactory();
        }

        @Override
        public ActiveRowTracker getRowTracker() {
            return input.getProducer().getRowTracker();
        }

        @Override
        public void clearData() {
            // don't clear the row tracker as it isn't ours
        }
    }

}
