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

package io.viewserver.operators.table;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.expression.tree.equals.*;
import io.viewserver.expression.tree.literal.*;
import io.viewserver.operators.IInput;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.schema.column.*;

/**
 * Created by bemm on 12/02/2015.
 */
public class TablePartitionOperator extends FilterOperator implements ITable {
    private String partitionColumnName;
    private Object partitionValue;
    private ColumnHolder partitionColumnHolder;
    private PartitionTableRow tableRow;

    public TablePartitionOperator(String name, IExecutionContext executionContext, ICatalog catalog, String partitionColumnName, Object partitionValue) {
        super(name, executionContext, catalog, null);
        this.partitionColumnName = partitionColumnName;
        this.partitionValue = partitionValue;

        setMode(FilterMode.Filter);
        register();
    }

    @Override
    protected IInput createInput() {
        return new Input(Constants.IN, this);
    }

    private void setFilterExpression() {
        switch (partitionColumnHolder.getType()) {
            case Bool: {
                setFilterExpression(new EqualsBool((ColumnHolderBool) partitionColumnHolder, new LiteralBool((Boolean) partitionValue)));
                break;
            }
            case NullableBool: {
                setFilterExpression(new EqualsNullableBool((ColumnHolderNullableBool)partitionColumnHolder, new LiteralNullableBool((NullableBool) partitionValue)));
                break;
            }
            case Byte: {
                setFilterExpression(new EqualsByte((ColumnHolderByte) partitionColumnHolder, new LiteralByte((Byte) partitionValue)));
                break;
            }
            case Short: {
                setFilterExpression(new EqualsShort((ColumnHolderShort) partitionColumnHolder, new LiteralShort((Short) partitionValue)));
                break;
            }
            case Int: {
                setFilterExpression(new EqualsInt((ColumnHolderInt) partitionColumnHolder, new LiteralInt((Integer) partitionValue)));
                break;
            }
            case Long: {
                setFilterExpression(new EqualsLong((ColumnHolderLong) partitionColumnHolder, new LiteralLong((Long) partitionValue)));
                break;
            }
            case String: {
                setFilterExpression(new EqualsString((ColumnHolderString) partitionColumnHolder, new LiteralString((String) partitionValue)));
                break;
            }
        }
    }

    @Override
    public int addRow(ITableRowUpdater updater) {
        return getTable().addRow(new DelegatingTableRowUpdater(updater) {
            @Override
            public void setValues(ITableRow row) {
                switch (partitionColumnHolder.getType()) {
                    case Bool: {
                        row.setBool(partitionColumnName, (Boolean) partitionValue);
                        break;
                    }
                    case NullableBool: {
                        row.setNullableBool(partitionColumnName, (NullableBool) partitionValue);
                        break;
                    }
                    case Byte: {
                        row.setByte(partitionColumnName, (Byte) partitionValue);
                        break;
                    }
                    case Short: {
                        row.setShort(partitionColumnName, (Short) partitionValue);
                        break;
                    }
                    case Int: {
                        row.setInt(partitionColumnName, (Integer) partitionValue);
                        break;
                    }
                    case Long: {
                        row.setLong(partitionColumnName, (Long) partitionValue);
                        break;
                    }
                    case String: {
                        row.setString(partitionColumnName, (String) partitionValue);
                        break;
                    }
                }
                super.setValues(getTableRowWrapper(row));
            }
        });
    }

    @Override
    public void updateRow(int row, ITableRowUpdater updater) {
        getTable().updateRow(getInputRow(row), new ITableRowUpdater() {
            @Override
            public void setValues(ITableRow row) {
                updater.setValues(getTableRowWrapper(row));
            }
        });
    }

    @Override
    public void removeRow(int row) {
        getTable().removeRow(getInputRow(row));
    }

    @Override
    public void setAllowDataReset(boolean allowDataReset) {
    }

    private ITableRow getTableRowWrapper(ITableRow row) {
        if (tableRow == null) {
            tableRow = new PartitionTableRow(row);
        } else {
            tableRow.tableRow = row;
        }
        return tableRow;
    }

    private ITable getTable() {
        return (ITable) getInput().getProducer().getOwner();
    }

    private class Input extends FilterOperator.Input {
        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void onPluggedIn(IOutput output) {
            if (!(output.getOwner() instanceof ITable)) {
                throw new RuntimeException("Only tables can be plugged in to a TablePartitionOperator");
            }
            super.onPluggedIn(output);
        }


        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            if (columnHolder.getName().equals(partitionColumnName)) {
                switch (columnHolder.getType()) {
                    case Bool:
                    case NullableBool:
                    case Byte:
                    case Short:
                    case Int:
                    case Long:
                    case String: {
                        break;
                    }
                    default: {
                        throw new RuntimeException("Cannot partition on a column of type " + columnHolder.getType());
                    }
                }

                partitionColumnHolder = columnHolder;
                setFilterExpression();
            } else {
                super.onColumnAdd(columnHolder);
            }
        }
    }

    private class PartitionTableRow implements ITableRow {
        private ITableRow tableRow;

        private PartitionTableRow(ITableRow tableRow) {
            this.tableRow = tableRow;
        }

        @Override
        public int getRowId() {
            return tableRow.getRowId();
        }

        @Override
        public boolean getBool(String name) {
            checkPartitionColumn(name);
            return tableRow.getBool(name);
        }

        @Override
        public void setBool(String name, boolean value) {
            checkPartitionColumn(name);
            tableRow.setBool(name, value);
        }

        @Override
        public NullableBool getNullableBool(String name) {
            checkPartitionColumn(name);
            return tableRow.getNullableBool(name);
        }

        @Override
        public void setNullableBool(String name, NullableBool value) {
            checkPartitionColumn(name);
            tableRow.setNullableBool(name, value);
        }

        @Override
        public byte getByte(String name) {
            checkPartitionColumn(name);
            return tableRow.getByte(name);
        }

        @Override
        public void setByte(String name, byte value) {
            checkPartitionColumn(name);
            tableRow.setByte(name, value);
        }

        @Override
        public short getShort(String name) {
            checkPartitionColumn(name);
            return tableRow.getShort(name);
        }

        @Override
        public void setShort(String name, short value) {
            checkPartitionColumn(name);
            tableRow.setShort(name, value);
        }

        @Override
        public int getInt(String name) {
            checkPartitionColumn(name);
            return tableRow.getInt(name);
        }

        @Override
        public void setInt(String name, int value) {
            checkPartitionColumn(name);
            tableRow.setInt(name, value);
        }

        @Override
        public long getLong(String name) {
            checkPartitionColumn(name);
            return tableRow.getLong(name);
        }

        @Override
        public void setLong(String name, long value) {
            checkPartitionColumn(name);
            tableRow.setLong(name, value);
        }

        @Override
        public float getFloat(String name) {
            checkPartitionColumn(name);
            return tableRow.getFloat(name);
        }

        @Override
        public void setFloat(String name, float value) {
            checkPartitionColumn(name);
            tableRow.setFloat(name, value);
        }

        @Override
        public double getDouble(String name) {
            checkPartitionColumn(name);
            return tableRow.getDouble(name);
        }

        @Override
        public void setDouble(String name, double value) {
            checkPartitionColumn(name);
            tableRow.setDouble(name, value);
        }

        @Override
        public String getString(String name) {
            checkPartitionColumn(name);
            return tableRow.getString(name);
        }

        @Override
        public void setString(String name, String value) {
            checkPartitionColumn(name);
            tableRow.setString(name, value);
        }

        @Override
        public Object getValue(String name) {
            checkPartitionColumn(name);
            return tableRow.getValue(name);
        }

        private void checkPartitionColumn(String name) {
            if (name.equals(partitionColumnName)) {
                throw new IllegalArgumentException("Column " + name + " does not exist");
            }
        }
    }
}
