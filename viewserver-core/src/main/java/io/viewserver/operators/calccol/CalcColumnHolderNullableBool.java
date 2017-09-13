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

import io.viewserver.core.NullableBool;
import io.viewserver.expression.tree.IExpressionNullableBool;
import io.viewserver.schema.column.*;

import java.util.BitSet;

/**
 * Created by nickc on 15/10/2014.
 */
public class CalcColumnHolderNullableBool extends ColumnHolderNullableBool implements ICalcColumnHolder, IColumnNullableBool, IWritableColumn {
    private final IExpressionNullableBool expression;
    private BitSet columnsUsed;
    private BitSet calculated = new BitSet();

    public CalcColumnHolderNullableBool(String name, IRowMapper rowMapper, IExpressionNullableBool expression, BitSet columnsUsed) {
        super(name, rowMapper);
        this.expression = expression;
        this.columnsUsed = columnsUsed;
    }

    @Override
    public NullableBool getNullableBool(int row) {
        if (!calculated.get(row)) {
            NullableBool value = expression.getNullableBool(row);
            ((IWritableColumnNullableBool)getColumn()).setNullableBool(row, value);
            calculated.set(row);
            return value;
        }
        return ((IColumnNullableBool)getColumn()).getNullableBool(row);
    }

    @Override
    public boolean getBool(int row) {
        return getNullableBool(row) == NullableBool.True;
    }

    @Override
    public void clearCalculated(int row) {
//        calculated = calculated.andNot(EWAHCompressedBitmap.bitmapOf(row));
        if (calculated.get(row)) {
            calculated.clear(row);
            getNullableBool(row);
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
