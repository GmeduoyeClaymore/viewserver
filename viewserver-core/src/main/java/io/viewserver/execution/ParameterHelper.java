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

import io.viewserver.datasource.DimensionMapper;
import io.viewserver.messages.command.IReportContext;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.report.DefaultDimensionValues;
import io.viewserver.report.ParameterDefinition;
import io.viewserver.report.ReportDefinition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bemm on 31/10/2014.
 */
public class ParameterHelper {
    public static ParameterHelper NO_PARAM_HELPER = new ParameterHelper();
    private final Pattern parameterPattern = Pattern.compile("\\{([^,\\}]+)((,([^,\\}]+))(,([^,\\}]+))?)?\\}");
    private ReportDefinition definition;
    private ReportContext reportContext;
    private IParameterHolder parameterHolder;
    private DimensionMapper dimensionMapper;

    public ParameterHelper() {
    }

    public ParameterHelper(ReportDefinition definition, IParameterHolder parameterHolder, DimensionMapper dimensionMapper) {
        this.definition = definition;
        this.parameterHolder = parameterHolder;
        this.dimensionMapper = dimensionMapper;
        if(parameterHolder instanceof ReportContext){
            this.reportContext = (ReportContext) parameterHolder;
        }
        validate(definition,parameterHolder);
    }

    public ParameterHelper(ReportDefinition definition, ReportContext reportContext, IParameterHolder parameterHolder, DimensionMapper dimensionMapper) {
        this(definition, parameterHolder, dimensionMapper);
        this.definition = definition;
        this.reportContext = reportContext;

        validate(definition, reportContext);
    }

    private void validate(ReportDefinition definition, IParameterHolder reportContext) {
        StringBuilder errors = new StringBuilder();
        for(ParameterDefinition par : definition.getParameters().values()){
            ValueLists.IValueList parameterValue = reportContext.getParameterValue(par.getName());
            if(par.isRequired() && (parameterValue == null || parameterValue.isEmpty())){
                if(errors.length() > 0){
                    errors.append("\n");
                }
                errors.append(String.format("Parameter \"%s\" is required and has not been specified",par.getName()));
            }
        }
        if(errors.length() > 0){
            throw new RuntimeException(errors.toString());
        }
    }



    public ValueLists.IValueList getParameterValues(String parameterName) {
        return parameterHolder.getParameterValue(parameterName);
    }

    public DimensionMapper getDimensionMapper() {
        return dimensionMapper;
    }

    public ReportContext getReportContext() {
        return reportContext;
    }

    public String substituteParameterValues(String expression) {
        return substituteParameterValues(expression, parameterHolder);
    }

    public String substituteParameterValues(String expression, IParameterHolder parameterHolder) {
        if (parameterHolder == null || expression == null) {
            return expression;
        }

        String result = expression;
        Matcher matcher = parameterPattern.matcher(expression);
        while (matcher.find()) {
            String parameterName = matcher.group(1);
            ValueLists.IValueList value = parameterHolder.getParameterValue(parameterName);

            //TODO - support optional parameters here
          /*  if (value == null) {
                throw new IllegalArgumentException("Invalid parameter '" + parameterName + "' in expression");
            }*/

            if(value != null && value.size() > 0) {
                String separator = ",";
                String prepend = "";

                if (matcher.group(4) != null) {
                    separator = matcher.group(4);

                    if (matcher.group(6) != null) {
                        prepend = matcher.group(6);
                    }
                }
                result = result.replace(matcher.group(0), prepend + value.getValuesString(separator));
            }else{
                result = result.replace(matcher.group(0), "");
            }
        }
        return result.length() == 0 ? null : result;
    }
}
