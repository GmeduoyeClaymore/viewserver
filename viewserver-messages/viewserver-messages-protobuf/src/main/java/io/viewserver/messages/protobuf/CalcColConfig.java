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

package io.viewserver.messages.protobuf;

import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.config.ICalcColConfig;
import io.viewserver.messages.protobuf.dto.CalcColConfigMessage;

import java.util.List;

/**
 * Created by nick on 08/12/15.
 */
public class CalcColConfig extends PoolableMessage<CalcColConfig> implements ICalcColConfig<CalcColConfig> {
    private CalcColConfigMessage.CalcColConfigDtoOrBuilder calcColConfigDto;
    private RecyclingList<ICalculatedColumn, CalcColConfigMessage.CalcColConfigDto.CalculatedColumn> calculationsList;
    private RecyclingList<ICalculationAlias, CalcColConfigMessage.CalcColConfigDto.CalculationAlias> aliasesList;

    CalcColConfig() {
        super(ICalcColConfig.class);
    }

    @Override
    public void setDto(Object dto) {
        calcColConfigDto = (CalcColConfigMessage.CalcColConfigDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public List<ICalculatedColumn> getCalculations() {
        if (calculationsList == null) {
            calculationsList = new RecyclingList<ICalculatedColumn, CalcColConfigMessage.CalcColConfigDto.CalculatedColumn>(ICalculatedColumn.class) {
                @Override
                protected void doAdd(Object dto) {
                    final CalcColConfigMessage.CalcColConfigDto.Builder builder = getCalcColConfigDtoBuilder();
                    dtoList = builder.getCalculationsList();
                    if (dto instanceof CalcColConfigMessage.CalcColConfigDto.CalculatedColumn) {
                        builder.addCalculations((CalcColConfigMessage.CalcColConfigDto.CalculatedColumn) dto);
                    } else {
                        builder.addCalculations((CalcColConfigMessage.CalcColConfigDto.CalculatedColumn.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getCalcColConfigDtoBuilder().clearCalculations();
                }
            };
        }
        calculationsList.setDtoList(calcColConfigDto != null ? calcColConfigDto.getCalculationsList() : null);
        return calculationsList;
    }

    @Override
    public List<ICalculationAlias> getAliases() {
        if (aliasesList == null) {
            aliasesList = new RecyclingList<ICalculationAlias, CalcColConfigMessage.CalcColConfigDto.CalculationAlias>(ICalculationAlias.class) {
                @Override
                protected void doAdd(Object dto) {
                    final CalcColConfigMessage.CalcColConfigDto.Builder builder = getCalcColConfigDtoBuilder();
                    dtoList = builder.getAliasesList();
                    if (dto instanceof CalcColConfigMessage.CalcColConfigDto.CalculationAlias) {
                        builder.addAliases((CalcColConfigMessage.CalcColConfigDto.CalculationAlias) dto);
                    } else {
                        builder.addAliases((CalcColConfigMessage.CalcColConfigDto.CalculationAlias.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getCalcColConfigDtoBuilder().clearAliases();
                }
            };
        }
        aliasesList.setDtoList(calcColConfigDto != null ? calcColConfigDto.getAliasesList() : null);
        return aliasesList;
    }

    @Override
    public boolean doesDataRefreshOnColumnAdd() {
        return calcColConfigDto.hasDoesDataRefreshOnColumnAdd() && calcColConfigDto.getDoesDataRefreshOnColumnAdd();
    }

    @Override
    public ICalcColConfig<CalcColConfig> setDataRefreshOnColumnAdd(boolean dataRefreshOnColumnAdd) {
        getCalcColConfigDtoBuilder().setDoesDataRefreshOnColumnAdd(dataRefreshOnColumnAdd);
        return this;
    }

    @Override
    protected void doRelease() {
        if (calculationsList != null) {
            calculationsList.release();
        }
        if (aliasesList != null) {
            aliasesList.release();
        }
        calcColConfigDto = null;
    }

    CalcColConfigMessage.CalcColConfigDto.Builder getBuilder() {
        return getCalcColConfigDtoBuilder();
    }

    private CalcColConfigMessage.CalcColConfigDto.Builder getCalcColConfigDtoBuilder() {
        if (calcColConfigDto == null) {
            calcColConfigDto = CalcColConfigMessage.CalcColConfigDto.newBuilder();
        } else if (calcColConfigDto instanceof CalcColConfigMessage.CalcColConfigDto) {
            calcColConfigDto = ((CalcColConfigMessage.CalcColConfigDto) calcColConfigDto).toBuilder();
        }
        return (CalcColConfigMessage.CalcColConfigDto.Builder) calcColConfigDto;
    }

    public static class CalculatedColumn extends PoolableMessage<CalculatedColumn> implements ICalculatedColumn<CalculatedColumn> {
        private CalcColConfigMessage.CalcColConfigDto.CalculatedColumnOrBuilder calculatedColumnDto;

        CalculatedColumn() {
            super(ICalculatedColumn.class);
        }

        @Override
        public void setDto(Object dto) {
            calculatedColumnDto = (CalcColConfigMessage.CalcColConfigDto.CalculatedColumnOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getName() {
            return calculatedColumnDto.getName();
        }

        @Override
        public ICalculatedColumn<CalculatedColumn> setName(String name) {
            getCalculatedColumnDtoBuilder().setName(name);
            return this;
        }

        @Override
        public String getExpression() {
            return calculatedColumnDto.getExpression();
        }

        @Override
        public ICalculatedColumn<CalculatedColumn> setExpression(String expression) {
            getCalculatedColumnDtoBuilder().setExpression(expression);
            return this;
        }

        @Override
        protected void doRelease() {
            calculatedColumnDto = null;
        }

        CalcColConfigMessage.CalcColConfigDto.CalculatedColumn.Builder getBuilder() {
            return getCalculatedColumnDtoBuilder();
        }

        private CalcColConfigMessage.CalcColConfigDto.CalculatedColumn.Builder getCalculatedColumnDtoBuilder() {
            if (calculatedColumnDto == null) {
                calculatedColumnDto = CalcColConfigMessage.CalcColConfigDto.CalculatedColumn.newBuilder();
            } else if (calculatedColumnDto instanceof CalcColConfigMessage.CalcColConfigDto.CalculatedColumn) {
                calculatedColumnDto = ((CalcColConfigMessage.CalcColConfigDto.CalculatedColumn) calculatedColumnDto).toBuilder();
            }
            return (CalcColConfigMessage.CalcColConfigDto.CalculatedColumn.Builder) calculatedColumnDto;
        }
    }

    public static class CalculationAlias extends PoolableMessage<CalculationAlias> implements ICalculationAlias<CalculationAlias> {
        private CalcColConfigMessage.CalcColConfigDto.CalculationAliasOrBuilder calculationAliasDto;

        CalculationAlias() {
            super(ICalculationAlias.class);
        }

        @Override
        public void setDto(Object dto) {
            calculationAliasDto = (CalcColConfigMessage.CalcColConfigDto.CalculationAliasOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getAlias() {
            return calculationAliasDto.getAlias();
        }

        @Override
        public ICalculationAlias<CalculationAlias> setAlias(String alias) {
            getCalculationAliasDtoBuilder().setAlias(alias);
            return this;
        }

        @Override
        public String getName() {
            return calculationAliasDto.getName();
        }

        @Override
        public ICalculationAlias<CalculationAlias> setName(String name) {
            getCalculationAliasDtoBuilder().setName(name);
            return this;
        }

        @Override
        protected void doRelease() {
            calculationAliasDto = null;
        }

        CalcColConfigMessage.CalcColConfigDto.CalculationAlias.Builder getBuilder() {
            return getCalculationAliasDtoBuilder();
        }

        private CalcColConfigMessage.CalcColConfigDto.CalculationAlias.Builder getCalculationAliasDtoBuilder() {
            if (calculationAliasDto == null) {
                calculationAliasDto = CalcColConfigMessage.CalcColConfigDto.CalculationAlias.newBuilder();
            } else if (calculationAliasDto instanceof CalcColConfigMessage.CalcColConfigDto.CalculationAlias) {
                calculationAliasDto = ((CalcColConfigMessage.CalcColConfigDto.CalculationAlias) calculationAliasDto).toBuilder();
            }
            return (CalcColConfigMessage.CalcColConfigDto.CalculationAlias.Builder) calculationAliasDto;
        }
    }
}
