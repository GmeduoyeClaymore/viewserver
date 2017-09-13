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

/**
 * Created by nick on 27/02/2015.
 */
public class ChildContextPivotValueProvider implements IPivotValueProvider {
    @Override
    public Object[] getPivotValues(ParameterHelper parameterHelper) {
        int size = parameterHelper.getReportContext().getChildContexts().size();
        Object[] values = new Object[size];
        for (int i = 0; i < size; i++) {
            values[i] = i;
        }
        return values;
    }
}
