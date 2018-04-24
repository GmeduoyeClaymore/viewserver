package io.viewserver.operators.spread;

import io.viewserver.schema.column.*;

public class SpreadColumn extends ColumnHolderString {

    public SpreadColumn(String name) {
        super(name);
    }

    public SpreadColumn(String name, IRowMapper rowMapper) {
        super(name, rowMapper);
    }

    public SpreadColumn(IColumn column, IRowMapper rowMapper) {
        super(column, rowMapper);
    }

    @Override
    public boolean supportsPreviousValues() {
        return super.supportsPreviousValues();
    }

    @Override
    public ColumnType getType() {
        return super.getType();
    }

    @Override
    public String getString(int row) {
        return super.getString(row);
    }

    @Override
    public String getPreviousString(int row) {
        return super.getPreviousString(row);
    }

    @Override
    public ColumnMetadataString getMetadata() {
        return super.getMetadata();
    }

    @Override
    public IColumn getColumn() {
        return super.getColumn();
    }

    @Override
    public void setColumn(IColumn column) {
        super.setColumn(column);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setColumnId(int columnId) {
        super.setColumnId(columnId);
    }

    @Override
    public int getColumnId() {
        return super.getColumnId();
    }

    @Override
    public int getSourceRow(int row) {
        return super.getSourceRow(row);
    }

    @Override
    public IRowMapper getRowMapper() {
        return super.getRowMapper();
    }

    @Override
    public void setMetadata(ColumnMetadata metadata) {
        super.setMetadata(metadata);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
