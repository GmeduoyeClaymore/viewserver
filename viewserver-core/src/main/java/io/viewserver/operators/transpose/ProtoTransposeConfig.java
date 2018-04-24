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

package io.viewserver.operators.transpose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bemm on 25/11/2014.
 */
public class ProtoTransposeConfig implements ITransposeConfig {
    private final List<String> keyColumns;
    private final String pivotColumn;
    private Object[] pivotValues;

    public ProtoTransposeConfig(io.viewserver.messages.config.ITransposeConfig transposeConfigDto) {
        this.keyColumns = new ArrayList<>(transposeConfigDto.getKeyColumns());
        this.pivotColumn = transposeConfigDto.getPivotColumn();

        final List<String> pivotValuesList = transposeConfigDto.getPivotValues();
        int count = pivotValuesList.size();
        this.pivotValues = new Object[count];
        for (int i = 0; i < count; i++) {
            this.pivotValues[i] = pivotValuesList.get(i);
        }
    }

    @Override
    public List<String> getKeyColumns() {
        return keyColumns;
    }

    @Override
    public String getPivotColumn() {
        return pivotColumn;
    }

    @Override
    public Object[] getPivotValues() {
        return pivotValues;
    }
}
