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

package io.viewserver.execution.nodes;

import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.common.ValueLists;

/**
* Created by bemm on 27/02/2015.
*/ //TODO - this isn't right but we need to be able to serlialize the node
public class RangePivotValueProvider implements IPivotValueProvider {
    private String startParam;
    private String rangeParam;

    public RangePivotValueProvider(){}

    public RangePivotValueProvider(String startParam, String rangeParam){
        this.startParam = startParam;
        this.rangeParam = rangeParam;
    }

    public String getStartParam() {
        return startParam;
    }

    public void setStartParam(String startParam) {
        this.startParam = startParam;
    }

    public String getRangeParam() {
        return rangeParam;
    }

    public void setRangeParam(String rangeParam) {
        this.rangeParam = rangeParam;
    }

    @Override
    public Object[] getPivotValues(ParameterHelper parameterHelper){
        int startBucket = ((ValueLists.IIntegerList)parameterHelper.getParameterValues(startParam)).get(0);
        int buckets = ((ValueLists.IIntegerList)parameterHelper.getParameterValues(rangeParam)).get(0);
        Object[] values = new Object[buckets];
        for (int i = 0; i < buckets; i++) {
            values[i] = startBucket + i;
        }
        return values;
    }
}
