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

package io.viewserver.schema.column;

import io.viewserver.datasource.*;
import io.viewserver.datasource.ColumnType;

import java.util.BitSet;

/**
 * Created by nickc on 16/01/2015.
 */
public abstract class ColumnMetadata {
    private ColumnType dataType;
    private final BitSet flags = new BitSet();
    private String dimensionName;
    private String dimensionNameSpace;
    private Cardinality cardinality;

    public ColumnType getDataType() {
        return dataType;
    }

    public void setDataType(ColumnType dataType) {
        this.dataType = dataType;
    }

    public boolean isFlagged(int flag) {
        return flags.get(flag);
    }

    public void setFlag(int flag) {
        flags.set(flag);
    }

    public void clearFlag(int flag) {
        flags.clear(flag);
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    public Cardinality getCardinality(){
        return cardinality;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public String getDimensionNameSpace() {
        return dimensionNameSpace;
    }

    public void setDimensionNameSpace(String dimensionNameSpace) {
        this.dimensionNameSpace = dimensionNameSpace;
    }
}
