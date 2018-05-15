package io.viewserver.report;

import io.viewserver.execution.ReportContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultDimensionValues {
    private String name;
    private List<ReportContext.DimensionValue> values;


    public DefaultDimensionValues(String name,  ReportContext.DimensionValue... values) {
        this.name = name;
        this.values = Arrays.asList(values);
    }

    public DefaultDimensionValues(String name) {
        this.name = name;
        values = new ArrayList<ReportContext.DimensionValue>();
    }

    public DefaultDimensionValues withDimValue(Object... vals){
        values.add(new ReportContext.DimensionValue(name,false,vals));
        return this;
    }

    public DefaultDimensionValues withValue(ReportContext.DimensionValue value){
        values.add(value);
        return this;
    }
    public DefaultDimensionValues withValue(boolean exclude, Object... vals){
        values.add(new ReportContext.DimensionValue(name,exclude,vals));
        return this;
    }

    public String getName() {
        return name;
    }

    public List<ReportContext.DimensionValue> getValues() {
        return values;
    }
}
