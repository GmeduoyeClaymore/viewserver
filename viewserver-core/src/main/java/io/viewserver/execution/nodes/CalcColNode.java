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
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.calccol.ICalcColConfig;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by paulg on 12/11/2014.
 */
public class CalcColNode extends GraphNodeBase<CalcColNode> {
    private final List<CalcColOperator.CalculatedColumn> calculations = new ArrayList<>();
    Map<String, String> columnAliases = new HashMap<>();
    private boolean isDataRefreshedOnColumnAdd = true;
    private final List<String> configStrings = new ArrayList<>();

    public CalcColNode(){
        super();
    }

    public CalcColNode(String name) {
        super(name, "CalcCol");
    }

    public CalcColNode withCalculations(CalcColOperator.CalculatedColumn... calculations) {
        this.calculations.addAll(Arrays.asList(calculations));
        return this;
    }

    public CalcColNode withCalculations(List<CalcColOperator.CalculatedColumn> calculations) {
        this.calculations.addAll(calculations);
        return this;
    }

    public CalcColNode withColumnAliases(Map<String, String> columnAliases) {
        this.columnAliases.putAll(columnAliases);
        return this;
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new ICalcColConfig() {
            @Override
            public List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                ArrayList<CalcColOperator.CalculatedColumn> calculations = new ArrayList<>();
                int count = CalcColNode.this.calculations.size();
                for (int i = 0; i < count; i++) {
                    CalcColOperator.CalculatedColumn calculation = CalcColNode.this.calculations.get(i);
                    calculations.add(new CalcColOperator.CalculatedColumn(
                            parameterHelper.substituteParameterValues(calculation.getName()),
                            parameterHelper.substituteParameterValues(calculation.getExpression()),
                            calculation.getSourceColumnRegex() != null ? calculation.getSourceColumnRegex().pattern() : null));
                }
                return calculations;
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return columnAliases;
            }

            @Override
            public boolean isDataRefreshedOnColumnAdd() {
                return isDataRefreshedOnColumnAdd;
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        final io.viewserver.messages.config.ICalcColConfig configDto = MessagePool.getInstance().get(io.viewserver.messages.config.ICalcColConfig.class);

        final List<io.viewserver.messages.config.ICalcColConfig.ICalculatedColumn> calculationsList = configDto.getCalculations();
        int count = this.calculations.size();
        for (int i = 0; i < count; i++) {
            CalcColOperator.CalculatedColumn calculation = this.calculations.get(i);
            final io.viewserver.messages.config.ICalcColConfig.ICalculatedColumn calculatedColumn = MessagePool.getInstance().get(io.viewserver.messages.config.ICalcColConfig.ICalculatedColumn.class)
                    .setName(parameterHelper.substituteParameterValues(calculation.getName()))
                    .setExpression(parameterHelper.substituteParameterValues(calculation.getExpression()));
            calculationsList.add(calculatedColumn);
            calculatedColumn.release();
        }

        final List<io.viewserver.messages.config.ICalcColConfig.ICalculationAlias> aliasList = configDto.getAliases();
        columnAliases.entrySet().forEach(columnAlias -> {
            final io.viewserver.messages.config.ICalcColConfig.ICalculationAlias calculationAlias = MessagePool.getInstance().get(io.viewserver.messages.config.ICalcColConfig.ICalculationAlias.class)
                    .setAlias(columnAlias.getKey())
                    .setName(columnAlias.getValue());
            aliasList.add(calculationAlias);
            calculationAlias.release();
        });

        configDto.setDataRefreshOnColumnAdd(isDataRefreshedOnColumnAdd);

        return configDto;
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        configStrings.clear();

        int count = calculations.size();
        for (int i = 0; i < count; i++) {
            CalcColOperator.CalculatedColumn calculation = calculations.get(i);
            configStrings.add(String.format("%s:%s:%s", parameterHelper.substituteParameterValues(calculation.getName()),
                    parameterHelper.substituteParameterValues(calculation.getExpression()),
                    calculation.getSourceColumnRegex()));
        }

        columnAliases.entrySet().forEach(addColumnAliasToOperatorName);

        return String.format("calcCol:%s", StringUtils.join(configStrings, ','));
    }
    private final Consumer<Map.Entry<String, String>> addColumnAliasToOperatorName = columnAlias -> {
        configStrings.add(String.format("%s:%s", columnAlias.getKey(), columnAlias.getValue()));
    };

    public CalcColNode withDataRefreshedOnColumnAdd(boolean isDataRefreshedOnColumnAdd) {
        this.isDataRefreshedOnColumnAdd = isDataRefreshedOnColumnAdd;
        return this;
    }

    public List<CalcColOperator.CalculatedColumn> getCalculations() {
        return calculations;
    }

    public Map<String, String> getColumnAliases() {
        return columnAliases;
    }

    public void setColumnAliases(Map<String, String> columnAliases) {
        this.columnAliases = columnAliases;
    }
}
