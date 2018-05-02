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

package io.viewserver.operators.group.summary;

import io.viewserver.operators.group.summary.avg.AvgFactory;
import io.viewserver.operators.group.summary.json.JsonSummaryFactory;
import io.viewserver.operators.group.summary.minmax.MinMaxFactory;
import io.viewserver.operators.group.summary.stdDev.StdDevFactory;
import io.viewserver.operators.group.summary.sum.SumFactory;
import io.viewserver.operators.group.summary.var.VarFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bemm on 01/11/2014.
 */
public class SummaryRegistry {
    private final Map<String, ISummaryFactory> factories = new HashMap<>();

    public SummaryRegistry() {
        register(new SumFactory());
        register(new AvgFactory());
        register(new VarFactory());
        register(new StdDevFactory());
        register(new JsonSummaryFactory());
        register(new MinMaxFactory(MinMaxFactory.NAME_MAX));
        register(new MinMaxFactory(MinMaxFactory.NAME_MIN));
    }

    public void register(ISummaryFactory factory) {
        factories.put(factory.getName(), factory);
    }

    public ISummaryFactory getSummaryFactory(String function) {
        return factories.get(function);
    }
}
