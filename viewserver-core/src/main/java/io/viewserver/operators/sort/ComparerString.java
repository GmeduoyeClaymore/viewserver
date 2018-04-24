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

import io.viewserver.schema.column.IColumnString;

/**
 * Created by bemm on 15/10/2014.
 */
public class ComparerString implements IComparer {
    private IColumnString column;
    private String pivotValue;
    private int lessResult;
    private int greaterResult;
    private int multiplier;

    public ComparerString(IColumnString column, boolean descending) {
        this.column = column;
        lessResult = descending ? 1 : -1;
        greaterResult = descending ? -1 : 1;
        multiplier = descending ? -1 : 1;
    }

    @Override
    public void setPivotValue(int row) {
        this.pivotValue = column.getString(row);
    }

    @Override
    public int compare(int row) {
        String value = column.getString(row);
        if (value == null) {
            if (pivotValue == null) {
                return 0;
            } else {
                return lessResult;
            }
        } else if (pivotValue == null) {
            return greaterResult;
        }
        return value.compareTo(pivotValue) * multiplier;
    }

    @Override
    public int compare(int row1, boolean usePreviousValue1, int row2, boolean usePreviousValue2) {
        String value1 = usePreviousValue1 ? column.getPreviousString(row1) : column.getString(row1);
        String value2 = usePreviousValue2 ? column.getPreviousString(row2) : column.getString(row2);
        if (value1 == null) {
            if (value2 == null) {
                return 0;
            } else {
                return lessResult;
            }
        } else if (value2 == null) {
            return greaterResult;
        }
        return value1.compareTo(value2) * multiplier;
    }
}
