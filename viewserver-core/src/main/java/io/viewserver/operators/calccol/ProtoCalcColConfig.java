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

package io.viewserver.operators.calccol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 25/11/2014.
 */
public class ProtoCalcColConfig implements ICalcColConfig {
    private final boolean doesDataRefreshOnColumnAdd;
    private final List<CalcColOperator.CalculatedColumn> calculatedColumns = new ArrayList<>();
    private final Map<String, String> columnAliases = new HashMap<>();

    public ProtoCalcColConfig(io.viewserver.messages.config.ICalcColConfig calcColConfigDto) {
        final List<io.viewserver.messages.config.ICalcColConfig.ICalculatedColumn> calculationsList = calcColConfigDto.getCalculations();
        int count = calculationsList.size();
        for (int i = 0; i < count; i++) {
            final io.viewserver.messages.config.ICalcColConfig.ICalculatedColumn calculatedColumn = calculationsList.get(i);
            calculatedColumns.add(new CalcColOperator.CalculatedColumn(calculatedColumn.getName(), calculatedColumn.getExpression()));
            calculatedColumn.release();
        }

        final List<io.viewserver.messages.config.ICalcColConfig.ICalculationAlias> aliasList = calcColConfigDto.getAliases();
        count = aliasList.size();
        for (int i = 0; i < count; i++) {
            final io.viewserver.messages.config.ICalcColConfig.ICalculationAlias calculationAlias = aliasList.get(i);
            columnAliases.put(calculationAlias.getAlias(), calculationAlias.getName());
            calculationAlias.release();
        }

        doesDataRefreshOnColumnAdd = calcColConfigDto.doesDataRefreshOnColumnAdd();
    }

    @Override
    public List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
        return calculatedColumns;
    }

    @Override
    public Map<String, String> getColumnAliases() {
        return columnAliases;
    }

    @Override
    public boolean isDataRefreshedOnColumnAdd() {
        return doesDataRefreshOnColumnAdd;
    }
}
