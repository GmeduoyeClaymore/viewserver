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

package io.viewserver.expression.function.conditional;

import io.viewserver.expression.Cast;
import io.viewserver.expression.function.IRetypeableUserDefinedFunction;
import io.viewserver.expression.tree.*;
import io.viewserver.schema.column.ColumnType;

/**
 * Created by nickc on 14/10/2014.
 */
public class IsNull implements IRetypeableUserDefinedFunction {
    private IExpression primaryValue;
    private IExpression fallbackValue;

    @Override
    public ColumnType getType() {
        return ColumnType.Unknown;
    }

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 2) {
            throw new IllegalArgumentException("isNull() takes 2 parameters");
        }

        ColumnType columnType = Cast.pickCommonType(parameters[0], parameters[1]);
        this.primaryValue = Cast.wrapIfNecessary(parameters[0], columnType);
        this.fallbackValue = Cast.wrapIfNecessary(parameters[1], columnType);
    }

    @Override
    public IExpression retype() {
        switch (this.primaryValue.getType()) {
            case Bool: {
                return new IsNullBool((IExpressionBool)this.primaryValue, (IExpressionBool)this.fallbackValue);
            }
            case Byte: {
                return new IsNullByte((IExpressionByte)this.primaryValue, (IExpressionByte)this.fallbackValue);
            }
            case Short: {
                return new IsNullShort((IExpressionShort)this.primaryValue, (IExpressionShort)this.fallbackValue);
            }
            case Int: {
                return new IsNullInt((IExpressionInt)this.primaryValue, (IExpressionInt)this.fallbackValue);
            }
            case Long: {
                return new IsNullLong((IExpressionLong)this.primaryValue, (IExpressionLong)this.fallbackValue);
            }
            case Float: {
                return new IsNullFloat((IExpressionFloat)this.primaryValue, (IExpressionFloat)this.fallbackValue);
            }
            case Double: {
                return new IsNullDouble((IExpressionDouble)this.primaryValue, (IExpressionDouble)this.fallbackValue);
            }
            case String: {
                return new IsNullString((IExpressionString)this.primaryValue, (IExpressionString)this.fallbackValue);
            }
            default: {
                throw new IllegalArgumentException("isNull() cannot have 1st/2nd parameters of type " + this.primaryValue.getType());
            }
        }
    }
}
