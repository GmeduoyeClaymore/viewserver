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

package io.viewserver.execution;

import io.viewserver.core.JacksonSerialiser;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.IReportContext;
import io.viewserver.messages.common.ColumnType;
import io.viewserver.messages.common.ValueLists;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by bemm on 31/10/2014.
 */
public class ReportContext implements IParameterHolder {
    private String reportName;
    private final Map<String, ValueLists.IValueList> parameterValues = new HashMap<>();
    private final List<DimensionValue> dimensionValues = new ArrayList<>();
    private List<ReportContext> childContexts = new ArrayList<>();
    private String output;
    private String multiContextMode;

    public ReportContext() {
    }

    public ReportContext(ReportContext original) {
        reportName = original.reportName;
        original.parameterValues.entrySet().forEach(addParameterValueProc);
        int count = original.dimensionValues.size();
        for (int i = 0; i < count; i++) {
            DimensionValue dimensionValue = original.dimensionValues.get(i);
            this.dimensionValues.add(new DimensionValue(dimensionValue.name, dimensionValue.exclude, dimensionValue.values.copy()));
        }
        count = original.childContexts.size();
        for (int i = 0; i < count; i++) {
            childContexts.add(new ReportContext(childContexts.get(i)));
        }
        output = original.output;
    }

    private final Consumer<Map.Entry<String, ValueLists.IValueList>> addParameterValueProc = parameterValue -> {
        String key = parameterValue.getKey();
        ValueLists.IValueList copy = parameterValue.getValue().copy();
        setParameterValue(key, copy);
    };

    public void setParameterValue(String key, ValueLists.IValueList copy) {
        parameterValues.put(key, copy);
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Map<String, ValueLists.IValueList> getParameterValues() {
        return parameterValues;
    }

    public List<DimensionValue> getDimensionValues() {
        return dimensionValues;
    }

    public List<ReportContext> getChildContexts() {
        return childContexts;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getMultiContextMode() {
        return multiContextMode;
    }

    public void setMultiContextMode(String multiContextMode) {
        this.multiContextMode = multiContextMode;
    }

    @Override
    public ValueLists.IValueList getParameterValue(String name) {
        ValueLists.IValueList values = parameterValues.get(name);
        if (values != null) {
            return values;
        }
        if (!childContexts.isEmpty()) {
            return childContexts.get(0).getParameterValue(name);
        }
        return null;
    }

    @Override
    public ReportContext clone() throws CloneNotSupportedException {
        return (ReportContext) super.clone();
    }

    public static ReportContext fromMessage(IReportContext reportContextMessage) {
        ReportContext reportContext = new ReportContext();
        reportContext.reportName = reportContextMessage.getReportId();

        List<IReportContext.IParameterValue> parameterValues = reportContextMessage.getParameterValues();
        int count = parameterValues.size();
        for (int i = 0; i < count; i++) {
            IReportContext.IParameterValue parameterValue = parameterValues.get(i);
            reportContext.parameterValues.put(parameterValue.getName(), getValues(parameterValue.getValue()).copy());
        }

        List<IReportContext.IDimensionValue> dimensions = reportContextMessage.getDimensionValues();
        count = dimensions.size();
        for (int i = 0; i < count; i++) {
            IReportContext.IDimensionValue filter = dimensions.get(i);
            reportContext.dimensionValues.add(new DimensionValue(filter.getName(), false,
                    getValues(filter.getValue()).copy()));
        }

        List<IReportContext.IDimensionValue> excludedFilters = reportContextMessage.getExcludedFilters();
        count = excludedFilters.size();
        for (int i = 0; i < count; i++) {
            IReportContext.IDimensionValue filter = excludedFilters.get(i);
            reportContext.dimensionValues.add(new DimensionValue(filter.getName(), true,
                    getValues(filter.getValue()).copy()));
        }

        List<IReportContext> childContexts = reportContextMessage.getChildContexts();
        count = childContexts.size();
        for (int i = 0; i < count; i++) {
            reportContext.childContexts.add(ReportContext.fromMessage(childContexts.get(i)));
        }

        reportContext.output = reportContextMessage.getOutput();

        if (reportContext.childContexts.size() > 0) {
            final String multiContextMode = reportContextMessage.getMultiContextMode();
            if (multiContextMode != null) {
                reportContext.setMultiContextMode(multiContextMode);
            } else if ("comparisonTranspose".equals(reportContext.output)) {
                // handle legacy contexts
                reportContext.setMultiContextMode("uniontranspose");
            } else {
                // handle legacy contexts
                reportContext.setMultiContextMode("uniongroup");
            }
        }

        return reportContext;
    }

    public IReportContext toMessage() {
        final IReportContext message = MessagePool.getInstance().get(IReportContext.class);

        if (reportName != null) {
            message.setReportId(reportName);
        }

        parameterValues.entrySet().forEach(parameterValue -> {
            final IReportContext.IParameterValue parameterValueMessage = MessagePool.getInstance().get(IReportContext.IParameterValue.class);
            final IReportContext.IValue valueMessage = getValueMessage(parameterValue.getValue());
            message.getParameterValues().add(parameterValueMessage
                    .setName(parameterValue.getKey())
                    .setValue(valueMessage));
            valueMessage.release();
            parameterValueMessage.release();
        });

        int count = dimensionValues.size();
        for (int i = 0; i < count; i++) {
            DimensionValue dimensionValue = dimensionValues.get(i);
            final IReportContext.IValue valueMessage = getValueMessage(dimensionValue.getValues());
            IReportContext.IDimensionValue dimensionValueMessage = MessagePool.getInstance().get(IReportContext.IDimensionValue.class)
                    .setName(dimensionValue.getName())
                    .setValue(valueMessage);
            if (dimensionValue.isExclude()) {
                message.getExcludedFilters().add(dimensionValueMessage);
            } else {
                message.getDimensionValues().add(dimensionValueMessage);
            }
            valueMessage.release();
            dimensionValueMessage.release();
        }

        count = childContexts.size();
        for (int i = 0; i < count; i++) {
            message.getChildContexts().add(childContexts.get(i).toMessage());
        }

        if (output != null) {
            message.setOutput(output);
        }

        if (multiContextMode != null) {
            message.setMultiContextMode(multiContextMode);
        }

        return message;
    }

    private static ValueLists.IValueList getValues(IReportContext.IValue value) {
        final ColumnType type = value.getType();
        if (type == null) {
            return ValueLists.EMPTY_LIST;
        }
        switch (type) {
            case Boolean: {
                return value.getBooleanValues();
            }
            case Integer: {
                return value.getIntegerValues();
            }
            case Long: {
                return value.getLongValues();
            }
            case Float: {
                return value.getFloatValues();
            }
            case Double: {
                return value.getDoubleValues();
            }
            case String: {
                return value.getStringValues();
            }
            default: {
                throw new IllegalArgumentException("Invalid value type " + type);
            }
        }
    }

    private IReportContext.IValue getValueMessage(ValueLists.IValueList values) {
        if (values instanceof ValueLists.IBooleanList) {
            final IReportContext.IValue value = MessagePool.getInstance().get(IReportContext.IValue.class);
            final ValueLists.IBooleanList messageValues = value.getBooleanValues();
            int size = values.size();
            for (int i = 0; i < size; i++) {
                messageValues.add(i, ((ValueLists.IBooleanList) values).get(i));
            }
            return value;
        }
        if (values instanceof ValueLists.IIntegerList) {
            final IReportContext.IValue value = MessagePool.getInstance().get(IReportContext.IValue.class);
            final ValueLists.IIntegerList messageValues = value.getIntegerValues();
            int size = values.size();
            for (int i = 0; i < size; i++) {
                messageValues.add(((ValueLists.IIntegerList) values).get(i));
            }
            return value;
        }
        if (values instanceof ValueLists.ILongList) {
            final IReportContext.IValue value = MessagePool.getInstance().get(IReportContext.IValue.class);
            final ValueLists.ILongList messageValues = value.getLongValues();
            int size = values.size();
            for (int i = 0; i < size; i++) {
                messageValues.add(((ValueLists.ILongList) values).get(i));
            }
            return value;
        }
        if (values instanceof ValueLists.IFloatList) {
            final IReportContext.IValue value = MessagePool.getInstance().get(IReportContext.IValue.class);
            final ValueLists.IFloatList messageValues = value.getFloatValues();
            int size = values.size();
            for (int i = 0; i < size; i++) {
                messageValues.add(((ValueLists.IFloatList) values).get(i));
            }
            return value;
        }
        if (values instanceof ValueLists.IDoubleList) {
            final IReportContext.IValue value = MessagePool.getInstance().get(IReportContext.IValue.class);
            final ValueLists.IDoubleList messageValues = value.getDoubleValues();
            int size = values.size();
            for (int i = 0; i < size; i++) {
                messageValues.add(((ValueLists.IDoubleList) values).get(i));
            }
            return value;
        }
        if (values instanceof ValueLists.IStringList) {
            final IReportContext.IValue value = MessagePool.getInstance().get(IReportContext.IValue.class);
            final ValueLists.IStringList messageValues = value.getStringValues();
            int size = values.size();
            for (int i = 0; i < size; i++) {
                messageValues.add(((ValueLists.IStringList) values).get(i));
            }
            return value;
        }
        throw new IllegalArgumentException("Cannot set values of type " + values.getClass().getName());
    }

    @Override
    public String toString() {
        JacksonSerialiser serialiser = new JacksonSerialiser();
        return serialiser.serialise(this, true);
    }

    public static class DimensionValue {
        private String name;
        private ValueLists.IValueList values;
        private boolean exclude;

        public DimensionValue(String name, ValueLists.IValueList values) {
            this.name = name;
            this.values = values;
        }

        @JsonCreator
        public DimensionValue(String name, boolean exclude, ValueLists.IValueList values) {
            this.name = name;
            this.values = values;
            this.exclude = exclude;
        }

        public DimensionValue(String name, boolean exclude, Object... values) {
            this.name = name;
            this.exclude = exclude;
            this.values = ValueLists.valueListOf(values);
        }

        public String getName() {
            return name;
        }

        public ValueLists.IValueList getValues() {
            return values;
        }

        public boolean isExclude() {
            return exclude;
        }
    }
}

