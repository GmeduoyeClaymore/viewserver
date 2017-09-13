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

import io.viewserver.core.JacksonSerialiser;
import io.viewserver.expression.tree.*;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.IColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by nickc on 21/10/2014.
 */
public class Serialize implements IUserDefinedFunction, IExpressionString {
    private static final Logger log = LoggerFactory.getLogger(Serialize.class);
    private IExpression[] columns;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length < 1) {
            throw new IllegalArgumentException("Syntax: serialize(<input...>)");
        }

        columns = new IExpression[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (!(parameters[i] instanceof IExpressionColumn) && !(parameters[i] instanceof IColumn)) {
                throw new IllegalArgumentException("Serialize function only accepts IExpressionColumn or IColumn");
            }

            columns[i] = parameters[i];
        }
    }

    @Override
    public ColumnType getType() {
        return ColumnType.String;
    }

    @Override
    public String getString(int row) {
        HashMap<String, Object> values = new HashMap<>();

        for (int i = 0; i < columns.length; i++) {
            Object value = this.getStringValue(columns[i], row);
            values.put(this.getColumnName(columns[i]), value);
        }

        JacksonSerialiser serialiser = new JacksonSerialiser();
        return serialiser.serialise(values, false);

    }

    private String getColumnName(IExpression column) {
        if (column instanceof IColumn) {
            return ((IColumn) column).getName();
        } else if (column instanceof IExpressionColumn) {
            return ((IExpressionColumn) column).getName();
        } else {
            throw new IllegalArgumentException("Serialize function only accepts IExpressionColumn or IColumn");
        }
    }

    private Object getStringValue(IExpression column, int row) {
        switch (column.getType()) {
            case Bool: {
                return ((IExpressionBool) column).getBool(row);
            }
            case Byte: {
                byte value = ((IExpressionByte) column).getByte(row);
                return Byte.toString(value);
            }
            case Short: {
                return ((IExpressionShort) column).getShort(row);
            }
            case Int: {
                return ((IExpressionInt) column).getInt(row);
            }
            case Long: {
                return ((IExpressionLong) column).getLong(row);
            }
            case Float: {
                return ((IExpressionFloat) column).getFloat(row);
            }
            case Double: {
                return ((IExpressionDouble) column).getDouble(row);
            }
            case String: {
                return ((IExpressionString) column).getString(row);
            }
            default: {
                throw new RuntimeException("text() cannot handle expressions of type " + column.getType());
            }
        }
    }
}
