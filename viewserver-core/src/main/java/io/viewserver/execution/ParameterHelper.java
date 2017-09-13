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

import io.viewserver.messages.common.ValueLists;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickc on 31/10/2014.
 */
public class ParameterHelper {
    public static ParameterHelper NO_PARAM_HELPER = new ParameterHelper(null);
    private final Pattern parameterPattern = Pattern.compile("\\{([^,\\}]+)((,([^,\\}]+))(,([^,\\}]+))?)?\\}");
    private ReportContext reportContext;
    private IParameterHolder parameterHolder;

    public ParameterHelper() {
    }

    public ParameterHelper(IParameterHolder parameterHolder) {
        this.parameterHolder = parameterHolder;
    }

    public ParameterHelper(ReportContext reportContext, IParameterHolder parameterHolder) {
        this(parameterHolder);
        this.reportContext = reportContext;
    }

    public ValueLists.IValueList getParameterValues(String parameterName) {
        return parameterHolder.getParameterValue(parameterName);
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
