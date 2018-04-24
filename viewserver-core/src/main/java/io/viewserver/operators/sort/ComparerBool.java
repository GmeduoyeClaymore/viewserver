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

import io.viewserver.schema.column.IColumnBool;

/**
 * Created by bemm on 15/10/2014.
 */
public class ComparerBool implements IComparer {
    private IColumnBool column;
    private boolean pivotValue;
    private int lessResult;
    private int greaterResult;

    public ComparerBool(IColumnBool column, boolean descending) {
        this.column = column;
        lessResult = descending ? 1 : -1;
        greaterResult = descending ? -1 : 1;
    }

    @Override
    public void setPivotValue(int row) {
        this.pivotValue = column.getBool(row);
    }

    @Override
    public int compare(int row) {
        boolean value = column.getBool(row);
        if (value && !pivotValue) {
            return greaterResult;
        } else if (value == pivotValue) {
            return 0;
        } else {
            return lessResult;
        }
    }

    @Override
    public int compare(int row1, boolean usePreviousValue1, int row2, boolean usePreviousValue2) {
        boolean value1 = usePreviousValue1 ? column.getPreviousBool(row1) : column.getBool(row1);
        boolean value2 = usePreviousValue2 ? column.getPreviousBool(row2) : column.getBool(row2);
        if (value1 && !value2) {
            return greaterResult;
        } else if (value1 == value2) {
            return 0;
        } else {
            return lessResult;
        }
    }
}
