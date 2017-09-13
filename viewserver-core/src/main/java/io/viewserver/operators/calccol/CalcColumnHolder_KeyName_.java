// :_KeyName_=Bool,_KeyType_=boolean;_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double;_KeyName_=String,_KeyType_=String

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

package io.viewserver.operators.calccol;

import io.viewserver.core._KeyType_;
import io.viewserver.expression.tree.IExpression_KeyName_;
import io.viewserver.schema.column.*;

import java.util.BitSet;

/**
 * Created by nickc on 15/10/2014.
 */
public class CalcColumnHolder_KeyName_ extends ColumnHolder_KeyName_ implements ICalcColumnHolder, IColumn_KeyName_, IWritableColumn {
    private final IExpression_KeyName_ expression;
    private BitSet columnsUsed;
    private BitSet calculated = new BitSet();

    public CalcColumnHolder_KeyName_(String name, IRowMapper rowMapper, IExpression_KeyName_ expression, BitSet columnsUsed) {
        super(name, rowMapper);
        this.expression = expression;
        this.columnsUsed = columnsUsed;
    }

    @Override
    public _KeyType_ get_KeyName_(int row) {
        if (!calculated.get(row)) {
            _KeyType_ value = expression.get_KeyName_(row);
            ((IWritableColumn_KeyName_)getColumn()).set_KeyName_(row, value);
            calculated.set(row);
            return value;
        }
        return ((IColumn_KeyName_)getColumn()).get_KeyName_(row);
    }

    @Override
    public void clearCalculated(int row) {
//        calculated = calculated.andNot(EWAHCompressedBitmap.bitmapOf(row));
        if (calculated.get(row)) {
            calculated.clear(row);
            get_KeyName_(row);
        }
    }

    @Override
    public boolean isAffected(IRowFlags rowflags) {
        for (int columnId = columnsUsed.nextSetBit(0); columnId >= 0; columnId = columnsUsed.nextSetBit(columnId + 1)) {
            if (rowflags.isDirty(columnId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearAllCalculated() {
        calculated.clear();
    }

    @Override
    public void storePreviousValues() {
        ((IWritableColumn)getColumn()).storePreviousValues();
    }

    @Override
    public void resetAll() {
        ((IWritableColumn)getColumn()).resetAll();
    }
}
