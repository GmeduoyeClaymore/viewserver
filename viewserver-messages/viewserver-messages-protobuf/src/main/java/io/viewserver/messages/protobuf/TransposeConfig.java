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
import io.viewserver.messages.config.ITransposeConfig;
import io.viewserver.messages.protobuf.dto.TransposeConfigMessage;

import java.util.List;

/**
 * Created by nick on 08/12/15.
 */
public class TransposeConfig extends PoolableMessage<TransposeConfig> implements ITransposeConfig<TransposeConfig> {
    private TransposeConfigMessage.TransposeConfigDtoOrBuilder transposeConfigDto;
    private ListWrapper<String> keyColumns;
    private ListWrapper<String> pivotValues;

    TransposeConfig() {
        super(ITransposeConfig.class);
    }

    @Override
    public void setDto(Object dto) {
        transposeConfigDto = (TransposeConfigMessage.TransposeConfigDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public List<String> getKeyColumns() {
        if (keyColumns == null) {
            keyColumns = new ListWrapper<>(x -> {
                final TransposeConfigMessage.TransposeConfigDto.Builder builder = getTransposeConfigDtoBuilder();
                keyColumns.setInnerList(builder.getKeyColumnsList());
                builder.addKeyColumns(x);
            });
        }
        keyColumns.setInnerList(transposeConfigDto != null ? transposeConfigDto.getKeyColumnsList() : null);
        return keyColumns;
    }

    @Override
    public String getPivotColumn() {
        return transposeConfigDto.getPivotColumn();
    }

    @Override
    public ITransposeConfig<TransposeConfig> setPivotColumn(String pivotColumn) {
        getTransposeConfigDtoBuilder().setPivotColumn(pivotColumn);
        return this;
    }

    @Override
    public List<String> getPivotValues() {
        if (pivotValues == null) {
            pivotValues = new ListWrapper<>(x -> {
                final TransposeConfigMessage.TransposeConfigDto.Builder builder = getTransposeConfigDtoBuilder();
                pivotValues.setInnerList(builder.getPivotValuesList());
                builder.addPivotValues(x);
            });
        }
        pivotValues.setInnerList(transposeConfigDto != null ? transposeConfigDto.getPivotValuesList() : null);
        return pivotValues;
    }

    @Override
    protected void doRelease() {
        if (keyColumns != null) {
            keyColumns.setInnerList(null);
        }
        if (pivotValues != null) {
            pivotValues.setInnerList(null);
        }
        transposeConfigDto = null;
    }

    TransposeConfigMessage.TransposeConfigDto.Builder getBuilder() {
        return getTransposeConfigDtoBuilder();
    }

    private TransposeConfigMessage.TransposeConfigDto.Builder getTransposeConfigDtoBuilder() {
        if (transposeConfigDto == null) {
            transposeConfigDto = TransposeConfigMessage.TransposeConfigDto.newBuilder();
        } else if (transposeConfigDto instanceof TransposeConfigMessage.TransposeConfigDto) {
            transposeConfigDto = ((TransposeConfigMessage.TransposeConfigDto) transposeConfigDto).toBuilder();
        }
        return (TransposeConfigMessage.TransposeConfigDto.Builder) transposeConfigDto;
    }
}
