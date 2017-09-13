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
public class Conditional implements IRetypeableUserDefinedFunction {
    private IExpression condition;
    private IExpression trueResult;
    private IExpression falseResult;

    @Override
    public ColumnType getType() {
        return ColumnType.Unknown;
    }

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 3) {
            throw new IllegalArgumentException("if() takes 3 parameters");
        }
        if (!(parameters[0] instanceof IExpressionBool)) {
            throw new IllegalArgumentException("if() takes a boolean as the 1st parameter");
        }
        ColumnType columnType = Cast.pickCommonType(parameters[1], parameters[2]);
        this.condition = parameters[0];
        this.trueResult = Cast.wrapIfNecessary(parameters[1], columnType);
        this.falseResult = Cast.wrapIfNecessary(parameters[2], columnType);
    }

    @Override
    public IExpression retype() {
        IExpressionBool condition = (IExpressionBool) this.condition;
        switch (this.trueResult.getType()) {
            case Bool: {
                return new ConditionalBool(condition, (IExpressionBool)this.trueResult, (IExpressionBool)this.falseResult);
            }
            case Byte: {
                return new ConditionalByte(condition, (IExpressionByte)this.trueResult, (IExpressionByte)this.falseResult);
            }
            case Short: {
                return new ConditionalShort(condition, (IExpressionShort)this.trueResult, (IExpressionShort)this.falseResult);
            }
            case Int: {
                return new ConditionalInt(condition, (IExpressionInt)this.trueResult, (IExpressionInt)this.falseResult);
            }
            case Long: {
                return new ConditionalLong(condition, (IExpressionLong)this.trueResult, (IExpressionLong)this.falseResult);
            }
            case Float: {
                return new ConditionalFloat(condition, (IExpressionFloat)this.trueResult, (IExpressionFloat)this.falseResult);
            }
            case Double: {
                return new ConditionalDouble(condition, (IExpressionDouble)this.trueResult, (IExpressionDouble)this.falseResult);
            }
            case String: {
                return new ConditionalString(condition, (IExpressionString)this.trueResult, (IExpressionString)this.falseResult);
            }
            default: {
                throw new IllegalArgumentException("if() cannot have 2nd/3rd parameters of type " + this.trueResult.getType());
            }
        }
    }
}
