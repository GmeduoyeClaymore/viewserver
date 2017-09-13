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

package io.viewserver.operators.sort;

import io.viewserver.schema.column.IColumnNullableBool;

/**
 * Created by nickc on 15/10/2014.
 */
public class ComparerNullableBool implements IComparer {
    private IColumnNullableBool column;
    private byte pivotValue;
    private int lessResult;
    private int greaterResult;

    public ComparerNullableBool(IColumnNullableBool column, boolean descending) {
        this.column = column;
        lessResult = descending ? 1 : -1;
        greaterResult = descending ? -1 : 1;
    }

    @Override
    public void setPivotValue(int row) {
        this.pivotValue = column.getNullableBool(row).getNumericValue();
    }

    @Override
    public int compare(int row) {
        byte value = column.getNullableBool(row).getNumericValue();
        if (value < pivotValue) {
            return lessResult;
        } else if (value > pivotValue) {
            return greaterResult;
        } else {
            return 0;
        }
    }

    @Override
    public int compare(int row1, boolean usePreviousValue1, int row2, boolean usePreviousValue2) {
        byte value1 = (usePreviousValue1 ? column.getPreviousNullableBool(row1) : column.getNullableBool(row1)).getNumericValue();
        byte value2 = (usePreviousValue2 ? column.getPreviousNullableBool(row2) : column.getNullableBool(row2)).getNumericValue();
        if (value1 < value2) {
            return lessResult;
        } else if (value1 > value2) {
            return greaterResult;
        } else {
            return 0;
        }
    }
}
