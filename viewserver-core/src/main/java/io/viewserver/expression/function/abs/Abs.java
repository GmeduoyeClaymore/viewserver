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

package io.viewserver.expression.function.abs;

import io.viewserver.expression.function.IRetypeableUserDefinedFunction;
import io.viewserver.expression.tree.*;
import io.viewserver.schema.column.ColumnType;

/**
 * Created by bemm on 14/10/2014.
 */
public class Abs implements IRetypeableUserDefinedFunction {
    private IExpression parameter;

    @Override
    public ColumnType getType() {
        return ColumnType.Unknown;
    }

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 1) {
            throw new IllegalArgumentException("abs() takes 1 parameters");
        }
        if (!parameters[0].getType().isNumber()) {
            throw new IllegalArgumentException("abs() takes a number as the 1st parameter");
        }
        this.parameter = parameters[0];
    }

    @Override
    public IExpression retype() {
        switch (this.parameter.getType()) {
            case Byte: {
                return new AbsByte((IExpressionByte)this.parameter);
            }
            case Short: {
                return new AbsShort((IExpressionShort)this.parameter);
            }
            case Int: {
                return new AbsInt((IExpressionInt) this.parameter);
            }
            case Long: {
                return new AbsLong((IExpressionLong) this.parameter);
            }
            case Float: {
                return new AbsFloat((IExpressionFloat) this.parameter);
            }
            case Double: {
                return new AbsDouble((IExpressionDouble) this.parameter);
            }
            default: {
                throw new IllegalArgumentException("abs() cannot have a parameter of type " + this.parameter.getType());
            }
        }
    }
}
