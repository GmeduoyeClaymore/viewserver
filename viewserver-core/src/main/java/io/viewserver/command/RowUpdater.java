package io.viewserver.command;

import io.viewserver.datasource.ColumnType;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnMetadata;

import java.util.Date;
import java.util.List;

/**
 * Created by Paul on 15/02/2018.
 */
public class RowUpdater implements ITableRowUpdater {
    protected IRecord record;
    private ITable table;

    public RowUpdater() {
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

    @Override
    public void setValues(ITableRow row) {
        List<ColumnHolder> columnHolders = table.getOutput().getSchema().getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);
            if (columnHolder == null) {
                continue;
            }

            String columnName = columnHolder.getName();
            if (!record.hasValue(columnName)) {
                continue;
            }

            ColumnMetadata metadata = columnHolder.getMetadata();
            ColumnType dataType = metadata.getDataType();

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

}