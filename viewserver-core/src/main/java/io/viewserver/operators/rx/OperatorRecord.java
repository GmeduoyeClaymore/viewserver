package io.viewserver.operators.rx;

import io.viewserver.core.NullableBool;
import io.viewserver.datasource.IRecord;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class OperatorRecord implements IRecord {

    private Schema schema;
    private int row = -1;

    public OperatorRecord(Schema schema) {
        this.schema = schema;
    }

    public OperatorRecord withRow(int row){
        this.row = row;
        return this;
    }

    @Override
    public String[] getColumnNames() {
        return schema.getColumnHolders().stream().map(c->c.getName()).collect(Collectors.toList()).toArray(new String[schema.getWidth()]);
    }

    @Override
    public byte getByte(String columnName) {
        ColumnHolderByte holderByte = (ColumnHolderByte) schema.getColumnHolder(columnName);
        return holderByte.getByte(row);
    }

    @Override
    public String getString(String columnName) {
        ColumnHolderString holderByte = (ColumnHolderString) schema.getColumnHolder(columnName);
        return holderByte.getString(row);
    }

    @Override
    public Boolean getBool(String columnName) {
        ColumnHolderBool holderByte = (ColumnHolderBool) schema.getColumnHolder(columnName);
        return holderByte.getBool(row);
    }

    @Override
    public NullableBool getNullableBool(String columnName) {
        ColumnHolderNullableBool holderByte = (ColumnHolderNullableBool) schema.getColumnHolder(columnName);
        return holderByte.getNullableBool(row);
    }

    @Override
    public Short getShort(String columnName) {
        ColumnHolderShort holderByte = (ColumnHolderShort) schema.getColumnHolder(columnName);
        return holderByte.getShort(row);
    }

    @Override
    public Integer getInt(String columnName) {
        ColumnHolderInt holderByte = (ColumnHolderInt) schema.getColumnHolder(columnName);
        return holderByte.getInt(row);
    }

    @Override
    public Long getLong(String columnName) {
        ColumnHolderLong holderByte = (ColumnHolderLong) schema.getColumnHolder(columnName);
        return holderByte.getLong(row);
    }

    @Override
    public Float getFloat(String columnName) {
        ColumnHolderFloat holderByte = (ColumnHolderFloat) schema.getColumnHolder(columnName);
        return holderByte.getFloat(row);
    }

    @Override
    public Double getDouble(String columnName) {
        ColumnHolderDouble holderByte = (ColumnHolderDouble) schema.getColumnHolder(columnName);
        return holderByte.getDouble(row);
    }

    @Override
    public Date getDate(String columnName) {
        ColumnHolderLong holderByte = (ColumnHolderLong) schema.getColumnHolder(columnName);
        long aLong = holderByte.getLong(row);
        if(aLong == -1){
            return null;
        }
        return new Date(aLong);
    }

    @Override
    public Date getDateTime(String columnName) {
        ColumnHolderLong holderByte = (ColumnHolderLong) schema.getColumnHolder(columnName);
        long aLong = holderByte.getLong(row);
        if(aLong == -1){
            return null;
        }
        return new Date(aLong);
    }

    @Override
    public Object getValue(String columnName) {
        return ColumnHolderUtils.getValue(schema.getColumnHolder(columnName),row);
    }

    @Override
    public boolean hasValue(String columnName) {
        return getValue(columnName) != null;
    }

    public void withSchema(Schema schema) {
        this.schema = schema;
    }
}
