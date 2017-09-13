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
import io.viewserver.schema.column.ColumnType;

/**
 * Created by nickc on 28/10/2014.
 */
public class Text implements IUserDefinedFunction, IExpressionString {
    private IExpression parameter;
    private IExpressionString format;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length < 1) {
            throw new IllegalArgumentException("text() takes at least 1 argument");
        }
        parameter = parameters[0];
        if (parameters.length > 1) {
            if (!(parameters[1] instanceof IExpressionString)) {
                throw new IllegalArgumentException("text() takes a string format as the 2nd argument");
            }
            format = (IExpressionString) parameters[1];
        }
    }

    @Override
    public String getString(int row) {
        String format = this.format != null ? this.format.getString(row) : null;
        switch (parameter.getType()) {
            case Bool: {
                boolean value = ((IExpressionBool) parameter).getBool(row);
                return format != null ? String.format(format, value) : Boolean.toString(value);
            }
            case Byte: {
                byte value = ((IExpressionByte) parameter).getByte(row);
                return format != null ? String.format(format, value) : Byte.toString(value);
            }
            case Short: {
                short value = ((IExpressionShort) parameter).getShort(row);
                return format != null ? String.format(format, value) : Short.toString(value);
            }
            case Int: {
                int value = ((IExpressionInt) parameter).getInt(row);
                return format != null ? String.format(format, value) : Integer.toString(value);
            }
            case Long: {
                long value = ((IExpressionLong) parameter).getLong(row);
                return format != null ? String.format(format, value) : Long.toString(value);
            }
            case Float: {
                float value = ((IExpressionFloat) parameter).getFloat(row);
                return format != null ? String.format(format, value) : Float.toString(value);
            }
            case Double: {
                double value = ((IExpressionDouble) parameter).getDouble(row);
                return format != null ? String.format(format, value) : Double.toString(value);
            }
            case String: {
                return ((IExpressionString)parameter).getString(row);
            }
            default: {
                throw new RuntimeException("text() cannot handle expressions of type " + parameter.getType());
            }
        }
    }

    @Override
    public ColumnType getType() {
        return ColumnType.String;
    }
}
