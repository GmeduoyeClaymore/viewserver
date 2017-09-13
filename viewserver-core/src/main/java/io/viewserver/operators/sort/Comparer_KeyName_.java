// :_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double

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

import io.viewserver.core._KeyType_;
import io.viewserver.schema.column.IColumn_KeyName_;

/**
 * Created by nickc on 15/10/2014.
 */
public class Comparer_KeyName_ implements IComparer {
    private IColumn_KeyName_ column;
    private _KeyType_ pivotValue;
    private int lessResult;
    private int greaterResult;

    public Comparer_KeyName_(IColumn_KeyName_ column, boolean descending) {
        this.column = column;
        lessResult = descending ? 1 : -1;
        greaterResult = descending ? -1 : 1;
    }

    @Override
    public void setPivotValue(int row) {
        this.pivotValue = column.get_KeyName_(row);
    }

    @Override
    public int compare(int row) {
        _KeyType_ value = column.get_KeyName_(row);
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
        _KeyType_ value1 = usePreviousValue1 ? column.getPrevious_KeyName_(row1) : column.get_KeyName_(row1);
        _KeyType_ value2 = usePreviousValue2 ? column.getPrevious_KeyName_(row2) : column.get_KeyName_(row2);
        if (value1 < value2) {
            return lessResult;
        } else if (value1 > value2) {
            return greaterResult;
        } else {
            return 0;
        }
    }
}
