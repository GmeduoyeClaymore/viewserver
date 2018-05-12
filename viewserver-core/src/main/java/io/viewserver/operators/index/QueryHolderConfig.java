package io.viewserver.operators.index;

import io.viewserver.datasource.Dimension;

import java.util.Arrays;

public class QueryHolderConfig {
    private Dimension dimension;
    private Object[] values;
    private boolean exclude;

    public QueryHolderConfig(Dimension dimension,boolean exclude,Object... values) {
        this.dimension = dimension;
        this.exclude = exclude;
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }

    public Dimension getDimension(){
        return dimension;
    }

    public boolean isExclude() {
        return exclude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryHolderConfig that = (QueryHolderConfig) o;

        if (!dimension.equals(that.dimension)) return false;
        if (!Arrays.equals(values, that.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dimension.hashCode();
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values[i]);
        }
        return String.format("%s:%s:[%s]", (this.dimension.isGlobal() ? "global_" : "") +  this.dimension.getName(), this.exclude, builder.toString());
    }

    public static QueryHolderConfig include(Dimension dimension, Object... values) {
        return new QueryHolderConfig(dimension,false, values);
    }

    public static QueryHolderConfig exclude(Dimension dimension, Object... values) {
        return new QueryHolderConfig(dimension,true, values);
    }

    // for deserialisation...yuck
    public void setDimension(Dimension columnName) {
        this.dimension = columnName;
    }

    // for deserialisation...yuck
    public void setValues(Object[] values) {
        this.values = values;
    }

    // for deserialisation...yuck
    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }
}