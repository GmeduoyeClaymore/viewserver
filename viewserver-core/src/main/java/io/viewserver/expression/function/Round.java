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

package io.viewserver.expression.function;

import io.viewserver.expression.tree.*;
import io.viewserver.expression.tree.literal.LiteralInt;
import io.viewserver.schema.column.ColumnType;

/**
 * Created by bemm on 21/10/2014.
 */
public class Round implements IUserDefinedFunction, IExpressionInt, IExpressionLong, IExpressionFloat, IExpressionDouble {
    private IExpression roundExpression;
    private int precision = 0;
    private ColumnType type;
    private int multiplier;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length < 1 || !(parameters[0] instanceof IExpressionFloat || parameters[0] instanceof IExpressionDouble)) {
            throw new IllegalArgumentException("round expression requires a double or long column");
        }
        roundExpression =  parameters[0];
        if (parameters.length > 1) {
            if (!(parameters[1] instanceof LiteralInt)) {
                throw new IllegalArgumentException("round expression takes precision as the second parameter, which must be an integer");
            }
            precision = ((IExpressionInt) parameters[1]).getInt(0);
        }
        if (precision == 0) {
            if (roundExpression instanceof IExpressionFloat) {
                type = ColumnType.Int;
            } else {
                type = ColumnType.Long;
            }
        } else {
            type = roundExpression.getType();
        }
        multiplier = (int) Math.pow(10, precision);
    }

    @Override
    public ColumnType getType() {
        return type;
    }

    @Override
    public int getInt(int row) {
        return (int)getRoundedValue(row);
    }

    @Override
    public double getDouble(int row) {
        return getRoundedValue(row);
    }

    @Override
    public float getFloat(int row) {
        return (float)getRoundedValue(row);
    }

    @Override
    public long getLong(int row) {
        return (long)getRoundedValue(row);
    }

    private double getRoundedValue(int row) {
        if(roundExpression instanceof IExpressionDouble){
            double lVal = ((IExpressionDouble)roundExpression).getDouble(row);
            return Math.round(lVal * multiplier) / (double)multiplier;
        } else {
            float lVal = ((IExpressionFloat)roundExpression).getFloat(row);
            return Math.round(lVal * multiplier) / (float)multiplier;
        }
    }
}
