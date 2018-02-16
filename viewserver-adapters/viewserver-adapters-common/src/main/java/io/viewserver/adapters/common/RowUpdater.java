package io.viewserver.adapters.common;

import io.viewserver.core.NullableBool;
import io.viewserver.datasource.*;
import io.viewserver.datasource.ColumnType;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.expression.parser.ExpressionVisitorImpl;
import io.viewserver.expression.tree.*;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.schema.column.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Paul on 15/02/2018.
 */
public class RowUpdater implements ITableRowUpdater {
    protected IRecord record;
    private DimensionMapper dimensionMapper;
    private ITable table;
    private IDataSource dataSource;
    private Map<String, IExpression> expressions = new HashMap<>();
    private IExpressionParser expressionParser;
    private FunctionRegistry functionRegistry;

    public RowUpdater(DimensionMapper dimensionMapper, FunctionRegistry functionRegistry, IExpressionParser expressionParser) {
        this.dimensionMapper = dimensionMapper;
        this.functionRegistry = functionRegistry;
        this.expressionParser = expressionParser;
    }

    @Override
    public Object getValue(String columnName) {
        return record.getValue(columnName);
    }

    public void setRecord(IRecord record) {
        this.record = record;
    }

    public void setTable(ITable table) {
        this.table = table;
    }

    public void setDataSource(IDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void setValues(ITableRow row) {
        List<ColumnHolder> columnHolders = table.getOutput().getSchema().getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);
            if (columnHolder == null) {
                continue;
            }

            Dimension dimension = null;
            String columnName = columnHolder.getName();

            if (!record.hasValue(columnName)) {
                continue;
            }

            if (dataSource != null) {
                dimension = dataSource.getDimension(columnName);
            }
            ColumnMetadata metadata = columnHolder.getMetadata();
            io.viewserver.datasource.ColumnType dataType = null;
            if (metadata != null) {
                if (metadata.isFlagged(ColumnFlags.DATASOURCE_CALCULATION)) {
                    continue;
                }
                dataType = metadata.getDataType();
            }
            if (dataType == null) {
                dataType = mapToDataType(columnHolder.getType());
            }
            if (dimension != null) {
                int id = -1;
                switch (dataType) {
                    case Bool: {
                        id = dimensionMapper.mapBool(dataSource, dimension, record.getBool(columnName));
                        break;
                    }
                    case NullableBool: {
                        id = dimensionMapper.mapNullableBool(dataSource, dimension, record.getNullableBool(columnName));
                        break;
                    }
                    case Byte: {
                        id = dimensionMapper.mapByte(dataSource, dimension, record.getByte(columnName));
                        break;
                    }
                    case Short: {
                        id = dimensionMapper.mapShort(dataSource, dimension, record.getShort(columnName));
                        break;
                    }
                    case Int: {
                        id = dimensionMapper.mapInt(dataSource, dimension, record.getInt(columnName));
                        break;
                    }
                    case Long: {
                        id = dimensionMapper.mapLong(dataSource, dimension, record.getLong(columnName));
                        break;
                    }
                    case String: {
                        id = dimensionMapper.mapString(dataSource, dimension, record.getString(columnName));
                        break;
                    }
                }
                setDimensionValue(row, columnName, dimension, id);
            } else {
                switch (dataType) {
                    case Bool: {
                        row.setBool(columnName, record.getBool(columnName));
                        break;
                    }
                    case NullableBool: {
                        row.setNullableBool(columnName, record.getNullableBool(columnName));
                        break;
                    }
                    case Byte: {
                        row.setByte(columnName, record.getByte(columnName));
                        break;
                    }
                    case Short: {
                        row.setShort(columnName, record.getShort(columnName));
                        break;
                    }
                    case Int: {
                        row.setInt(columnName, record.getInt(columnName));
                        break;
                    }
                    case Long: {
                        row.setLong(columnName, record.getLong(columnName));
                        break;
                    }
                    case Float: {
                        row.setFloat(columnName, record.getFloat(columnName));
                        break;
                    }
                    case Double: {
                        row.setDouble(columnName, record.getDouble(columnName));
                        break;
                    }
                    case String: {
                        String value = record.getString(columnName);
                        if (value != null) {
                            row.setString(columnName, value.intern());
                        }
                        break;
                    }
                    case Date: {
                        Date date = record.getDate(columnName);
                        if (date != null) {
                            row.setLong(columnName, date.getTime());
                        }
                        break;
                    }
                    case DateTime: {
                        Date dateTime = record.getDateTime(columnName);
                        if (dateTime != null) {
                            row.setLong(columnName, dateTime.getTime());
                        }
                        break;
                    }
                }
            }
        }

        if (dataSource != null) {
            List<CalculatedColumn> calculatedColumns = dataSource.getCalculatedColumns();
            int calculatedColumnsCount = calculatedColumns.size();
            for (int i = 0; i < calculatedColumnsCount; i++) {
                CalculatedColumn calculatedColumn = calculatedColumns.get(i);
                IExpression expression = getExpression(calculatedColumn, this);
                Dimension dimension = null;
                String columnName = calculatedColumn.getName();
                if (dataSource != null) {
                    dimension = dataSource.getDimension(columnName);
                }

                if(dimension != null & expression instanceof DataSourceColumnExpression && !((DataSourceColumnExpression) expression).hasValue()){
                    continue;
                }

                int id = -1;
                switch (expression.getType()) {
                    case Bool: {
                        boolean value = ((IExpressionBool) expression).getBool(row.getRowId());
                        if (dimension != null) {
                            id = dimensionMapper.mapBool(dataSource, dimension, value);
                        } else {
                            row.setBool(columnName, value);
                        }
                        break;
                    }
                    case NullableBool: {
                        NullableBool value = ((IExpressionNullableBool) expression).getNullableBool(row.getRowId());
                        if (dimension != null) {
                            id = dimensionMapper.mapNullableBool(dataSource, dimension, value);
                        } else {
                            row.setNullableBool(columnName, value);
                        }
                        break;
                    }
                    case Byte: {
                        byte value = ((IExpressionByte) expression).getByte(row.getRowId());
                        if (dimension != null) {
                            id = dimensionMapper.mapByte(dataSource, dimension, value);
                        } else {
                            row.setByte(columnName, value);
                        }
                        break;
                    }
                    case Short: {
                        short value = ((IExpressionShort) expression).getShort(row.getRowId());
                        if (dimension != null) {
                            id = dimensionMapper.mapShort(dataSource, dimension, value);
                        } else {
                            row.setShort(columnName, value);
                        }
                        break;
                    }
                    case Int: {
                        int value = ((IExpressionInt) expression).getInt(row.getRowId());
                        if (dimension != null) {
                            id = dimensionMapper.mapInt(dataSource, dimension, value);
                        } else {
                            row.setInt(columnName, value);
                        }
                        break;
                    }
                    case Long: {
                        long value = ((IExpressionLong) expression).getLong(row.getRowId());
                        if (dimension != null) {
                            id = dimensionMapper.mapLong(dataSource, dimension, value);
                        } else {
                            row.setLong(columnName, value);
                        }
                        break;
                    }
                    case Float: {
                        row.setFloat(columnName, ((IExpressionFloat) expression).getFloat(row.getRowId()));
                        break;
                    }
                    case Double: {
                        row.setDouble(columnName, ((IExpressionDouble) expression).getDouble(row.getRowId()));
                        break;
                    }
                    case String: {
                        String value = ((IExpressionString) expression).getString(row.getRowId());
                        if (dimension != null) {
                            id = dimensionMapper.mapString(dataSource, dimension, value);
                        } else {
                            row.setString(columnName, value);
                        }
                        break;
                    }
                }
                if (dimension != null) {
                    setDimensionValue(row, columnName, dimension, id);
                }
            }
        }
    }

    protected IExpression getExpression(CalculatedColumn column, RowUpdater rowUpdater) {
        IExpression expression = expressions.get(column.getName());
        if (expression == null) {
            expression = expressionParser.parse(column.getExpression(), new DataSourceExpressionVisitor(functionRegistry, rowUpdater, table.getOutput().getSchema()));
            expressions.put(column.getName(), expression);
        }
        return expression;
    }

    protected io.viewserver.datasource.ColumnType mapToDataType(io.viewserver.schema.column.ColumnType columnType) {
        switch (columnType) {
            case Bool: return io.viewserver.datasource.ColumnType.Bool;
            case NullableBool: return io.viewserver.datasource.ColumnType.NullableBool;
            case Byte: return io.viewserver.datasource.ColumnType.Byte;
            case Short: return io.viewserver.datasource.ColumnType.Short;
            case Int: return io.viewserver.datasource.ColumnType.Int;
            case Long: return io.viewserver.datasource.ColumnType.Long;
            case Float: return io.viewserver.datasource.ColumnType.Float;
            case Double: return io.viewserver.datasource.ColumnType.Double;
            case String: return io.viewserver.datasource.ColumnType.String;
            default: throw new IllegalArgumentException(String.format("Unknown column type '%s'", columnType));
        }
    }

    protected void setDimensionValue(ITableRow row, String columnName, Dimension dimension, int id) {
        switch (dimension.getCardinality()) {
            case Boolean: {
                row.setBool(columnName, id == NullableBool.True.getNumericValue());
                break;
            }
            case Byte: {
                row.setByte(columnName, (byte) (id & 0xff));
                break;
            }
            case Short: {
                row.setShort(columnName, (short) (id & 0xffff));
                break;
            }
            case Int: {
                row.setInt(columnName, id);
                break;
            }
        }
    }

    private class DataSourceExpressionVisitor extends ExpressionVisitorImpl {
        private RowUpdater rowUpdater;

        public DataSourceExpressionVisitor(FunctionRegistry functionRegistry, RowUpdater rowUpdater, io.viewserver.schema.Schema schema) {
            super(schema, functionRegistry, null, dimensionMapper, null);
            this.rowUpdater = rowUpdater;
        }

        @Override
        protected IExpression getColumnExpression(String columnName) {
            Column column = dataSource.getSchema().getColumn(columnName);
            if (column == null) {
                throw new IllegalArgumentException("No such column '" + columnName + "'");
            }
            return new DataSourceColumnExpression(column, rowUpdater);
        }
    }

    private class DataSourceColumnExpression implements IExpression, IExpressionBool, IExpressionNullableBool, IExpressionByte, IExpressionShort,
            IExpressionInt, IExpressionLong, IExpressionFloat, IExpressionDouble, IExpressionString {
        private Column column;
        private RowUpdater rowUpdater;

        public DataSourceColumnExpression(Column column, RowUpdater rowUpdater) {
            this.column = column;
            this.rowUpdater = rowUpdater;
        }

        public boolean hasValue() {
            return rowUpdater.record.hasValue(column.getName());
        }

        public io.viewserver.schema.column.ColumnType getType() {
            return column.getType().getColumnType();
        }

        @Override
        public boolean getBool(int row) {
            return rowUpdater.record.getBool(column.getName());
        }

        @Override
        public NullableBool getNullableBool(int row) {
            return rowUpdater.record.getNullableBool(column.getName());
        }

        @Override
        public byte getByte(int row) {
            return rowUpdater.record.getByte(column.getName());
        }

        @Override
        public double getDouble(int row) {
            return rowUpdater.record.getDouble(column.getName());
        }

        @Override
        public float getFloat(int row) {
            return rowUpdater.record.getFloat(column.getName());
        }

        @Override
        public int getInt(int row) {
            return rowUpdater.record.getInt(column.getName());
        }

        @Override
        public long getLong(int row) {
            switch (column.getType()) {
                case Date: {
                    return rowUpdater.record.getDate(column.getName()).getTime();
                }
                case DateTime: {
                    return rowUpdater.record.getDateTime(column.getName()).getTime();
                }
                default: {
                    return rowUpdater.record.getLong(column.getName());
                }
            }
        }

        @Override
        public short getShort(int row) {
            return rowUpdater.record.getShort(column.getName());
        }

        @Override
        public String getString(int row) {
            return rowUpdater.record.getString(column.getName());
        }
    }
}