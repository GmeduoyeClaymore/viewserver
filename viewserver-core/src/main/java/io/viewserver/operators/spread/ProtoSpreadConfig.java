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

package io.viewserver.operators.spread;

import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.operators.filter.IFilterConfig;

import java.util.Map;

/**
 * Created by bemm on 20/10/2014.
 */

public class ProtoSpreadConfig implements ISpreadConfig {


    private io.viewserver.messages.config.ISpreadConfig spreadConfig;

    public ProtoSpreadConfig(io.viewserver.messages.config.ISpreadConfig spreadConfig) {
        this.spreadConfig = spreadConfig;
    }

    public String getInputColumnName() {
        return spreadConfig.getInputColumnName();
    }

    public String getOutputColumnName() {
        return spreadConfig.getOutputColumnName();
    }

    public String spreadFunctionName() {
        return spreadConfig.getSpreadFunctionName();
    }

    @Override
    public boolean removeInputColumn() {
        return spreadConfig.removeInputColumn();
    }

    @Override
    public boolean retainSourceRow() {
        return spreadConfig.retainSourceRow();
    }
}

