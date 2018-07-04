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
import io.viewserver.messages.config.IFilterConfig;
import io.viewserver.messages.protobuf.dto.FilterConfigMessage;

/**
 * Created by bemm on 08/12/15.
 */
public class FilterConfig extends PoolableMessage<FilterConfig> implements IFilterConfig<FilterConfig> {
    private FilterConfigMessage.FilterConfigDtoOrBuilder filterConfigDto;

    FilterConfig() {
        super(IFilterConfig.class);
    }

    @Override
    public void setDto(Object dto) {
        filterConfigDto = (FilterConfigMessage.FilterConfigDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public FilterMode getMode() {
        final FilterConfigMessage.FilterConfigDto.Mode mode = filterConfigDto.getMode();
        switch (mode) {
            case TRANSPARENT: {
                return FilterMode.Transparent;
            }
            case FILTER: {
                return FilterMode.Filter;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unknown filter mode '%s'", mode));
            }
        }
    }

    @Override
    public IFilterConfig<FilterConfig> setMode(FilterMode mode) {
        switch (mode) {
            case Transparent: {
                getFilterConfigDtoBuilder().setMode(FilterConfigMessage.FilterConfigDto.Mode.TRANSPARENT);
                break;
            }
            case Filter: {
                getFilterConfigDtoBuilder().setMode(FilterConfigMessage.FilterConfigDto.Mode.FILTER);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown filter mode '%s'", mode));
            }
        }
        return this;
    }

    @Override
    public String getExpression() {
        return filterConfigDto.getExpression();
    }

    @Override
    public IFilterConfig<FilterConfig> setExpression(String expression) {
        getFilterConfigDtoBuilder().setExpression(expression);
        return this;
    }

    @Override
    protected void doRelease() {
        filterConfigDto = null;
    }

    FilterConfigMessage.FilterConfigDto.Builder getBuilder() {
        return getFilterConfigDtoBuilder();
    }

    private FilterConfigMessage.FilterConfigDto.Builder getFilterConfigDtoBuilder() {
        if (filterConfigDto == null) {
            filterConfigDto = FilterConfigMessage.FilterConfigDto.newBuilder();
        } else if (filterConfigDto instanceof FilterConfigMessage.FilterConfigDto) {
            filterConfigDto = ((FilterConfigMessage.FilterConfigDto) filterConfigDto).toBuilder();
        }
        return (FilterConfigMessage.FilterConfigDto.Builder) filterConfigDto;
    }
}
