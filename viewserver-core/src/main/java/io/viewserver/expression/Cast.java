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

package io.viewserver.expression;

import io.viewserver.core.NullableBool;
import io.viewserver.expression.tree.*;
import io.viewserver.schema.column.ColumnType;

/**
 * Created by bemm on 22/10/2014.
 */
public class Cast {
    public static ColumnType pickCommonType(IExpression... expressions) {
        ColumnType commonType = null;
        for (IExpression expression : expressions) {
            if (expression.getType().equals(ColumnType.String)) {
                return ColumnType.String;
            } else if (commonType == null || commonType.getCastOrder() < expression.getType().getCastOrder()) {
                commonType = expression.getType();
            }
        }
        return commonType;
    }

    public static IExpression wrapIfNecessary(IExpression expression, ColumnType type) {
        if (!expression.getType().equals(type)) {
            return new CastExpressionWrapper(expression, type);
        }
        return expression;
    }

    public static class CastExpressionWrapper implements IExpressionBool, IExpressionNullableBool, IExpressionByte, IExpressionShort, IExpressionInt, IExpressionLong, IExpressionFloat, IExpressionDouble, IExpressionString {
        private IExpression expression;
        private ColumnType type;

        public CastExpressionWrapper(IExpression expression, ColumnType type) {
            this.expression = expression;
            this.type = type;
        }

        @Override
        public boolean getBool(int row) {
            switch (expression.getType()) {
                case Bool:
                case NullableBool: {
                    return ((IExpressionBool) expression).getBool(row);
                }
                case Byte: {
                    return ((IExpressionByte) expression).getByte(row) != 0;
                }
                case Short: {
                    return ((IExpressionShort) expression).getShort(row) != 0;
                }
                case Int: {
                    return ((IExpressionInt) expression).getInt(row) != 0;
                }
                case Long: {
                    return ((IExpressionLong) expression).getLong(row) != 0;
                }
                case Float: {
                    return ((IExpressionFloat) expression).getFloat(row) != 0;
                }
                case Double: {
                    return ((IExpressionDouble) expression).getDouble(row) != 0;
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }

        @Override
        public byte getByte(int row) {
            switch (expression.getType()) {
                case Bool: {
                    return (byte) (((IExpressionBool) expression).getBool(row) ? 1 : 0);
                }
                case Byte: {
                    return ((IExpressionByte) expression).getByte(row);
                }
                case Short: {
                    return (byte) ((IExpressionShort) expression).getShort(row);
                }
                case Int: {
                    return (byte) ((IExpressionInt) expression).getInt(row);
                }
                case Long: {
                    return (byte) ((IExpressionLong) expression).getLong(row);
                }
                case Float: {
                    return (byte) ((IExpressionFloat) expression).getFloat(row);
                }
                case Double: {
                    return (byte) ((IExpressionDouble) expression).getDouble(row);
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }

        @Override
        public double getDouble(int row) {
            switch (expression.getType()) {
                case Bool: {
                    return (double) (((IExpressionBool) expression).getBool(row) ? 1 : 0);
                }
                case Byte: {
                    return ((IExpressionByte) expression).getByte(row);
                }
                case Short: {
                    return (double) ((IExpressionShort) expression).getShort(row);
                }
                case Int: {
                    return (double) ((IExpressionInt) expression).getInt(row);
                }
                case Long: {
                    return (double) ((IExpressionLong) expression).getLong(row);
                }
                case Float: {
                    return (double) ((IExpressionFloat) expression).getFloat(row);
                }
                case Double: {
                    return (double) ((IExpressionDouble) expression).getDouble(row);
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }

        @Override
        public float getFloat(int row) {
            switch (expression.getType()) {
                case Bool: {
                    return (float) (((IExpressionBool) expression).getBool(row) ? 1 : 0);
                }
                case Byte: {
                    return ((IExpressionByte) expression).getByte(row);
                }
                case Short: {
                    return (float) ((IExpressionShort) expression).getShort(row);
                }
                case Int: {
                    return (float) ((IExpressionInt) expression).getInt(row);
                }
                case Long: {
                    return (float) ((IExpressionLong) expression).getLong(row);
                }
                case Float: {
                    return (float) ((IExpressionFloat) expression).getFloat(row);
                }
                case Double: {
                    return (float) ((IExpressionDouble) expression).getDouble(row);
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }

        @Override
        public int getInt(int row) {
            switch (expression.getType()) {
                case Bool: {
                    return (int) (((IExpressionBool) expression).getBool(row) ? 1 : 0);
                }
                case Byte: {
                    return ((IExpressionByte) expression).getByte(row);
                }
                case Short: {
                    return (int) ((IExpressionShort) expression).getShort(row);
                }
                case Int: {
                    return (int) ((IExpressionInt) expression).getInt(row);
                }
                case Long: {
                    return (int) ((IExpressionLong) expression).getLong(row);
                }
                case Float: {
                    return (int) ((IExpressionFloat) expression).getFloat(row);
                }
                case Double: {
                    return (int) ((IExpressionDouble) expression).getDouble(row);
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }

        @Override
        public long getLong(int row) {
            switch (expression.getType()) {
                case Bool: {
                    return (long) (((IExpressionBool) expression).getBool(row) ? 1 : 0);
                }
                case Byte: {
                    return ((IExpressionByte) expression).getByte(row);
                }
                case Short: {
                    return (long) ((IExpressionShort) expression).getShort(row);
                }
                case Int: {
                    return (long) ((IExpressionInt) expression).getInt(row);
                }
                case Long: {
                    return (long) ((IExpressionLong) expression).getLong(row);
                }
                case Float: {
                    return (long) ((IExpressionFloat) expression).getFloat(row);
                }
                case Double: {
                    return (long) ((IExpressionDouble) expression).getDouble(row);
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }

        @Override
        public short getShort(int row) {
            switch (expression.getType()) {
                case Bool: {
                    return (short) (((IExpressionBool) expression).getBool(row) ? 1 : 0);
                }
                case Byte: {
                    return ((IExpressionByte) expression).getByte(row);
                }
                case Short: {
                    return (short) ((IExpressionShort) expression).getShort(row);
                }
                case Int: {
                    return (short) ((IExpressionInt) expression).getInt(row);
                }
                case Long: {
                    return (short) ((IExpressionLong) expression).getLong(row);
                }
                case Float: {
                    return (short) ((IExpressionFloat) expression).getFloat(row);
                }
                case Double: {
                    return (short) ((IExpressionDouble) expression).getDouble(row);
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }

        @Override
        public ColumnType getType() {
            return type;
        }

        @Override
        public NullableBool getNullableBool(int row) {
            switch (expression.getType()) {
                case Bool:
                case NullableBool: {
                    return ((IExpressionBool) expression).getBool(row) ? NullableBool.True : NullableBool.False;
                }
                case Byte: {
                    return ((IExpressionByte) expression).getByte(row) != 0 ? NullableBool.True : NullableBool.False;
                }
                case Short: {
                    return ((IExpressionShort) expression).getShort(row) != 0 ? NullableBool.True : NullableBool.False;
                }
                case Int: {
                    return ((IExpressionInt) expression).getInt(row) != 0 ? NullableBool.True : NullableBool.False;
                }
                case Long: {
                    return ((IExpressionLong) expression).getLong(row) != 0 ? NullableBool.True : NullableBool.False;
                }
                case Float: {
                    return ((IExpressionFloat) expression).getFloat(row) != 0 ? NullableBool.True : NullableBool.False;
                }
                case Double: {
                    return ((IExpressionDouble) expression).getDouble(row) != 0 ? NullableBool.True : NullableBool.False;
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }

        @Override
        public String getString(int row) {
            switch (expression.getType()) {
                case Bool:
                case NullableBool: {
                    return ((IExpressionBool) expression).getBool(row) ? "true" : "false";
                }
                case Byte: {
                    return String.format("%d", ((IExpressionByte) expression).getByte(row));
                }
                case Short: {
                    return String.format("%d", ((IExpressionShort) expression).getShort(row));
                }
                case Int: {
                    return String.format("%d", ((IExpressionInt) expression).getInt(row));
                }
                case Long: {
                    return String.format("%d", ((IExpressionLong) expression).getLong(row));
                }
                case Float: {
                    return String.format("%f", ((IExpressionFloat) expression).getFloat(row));
                }
                case Double: {
                    return String.format("%f", ((IExpressionDouble) expression).getDouble(row));
                }
                case String: {
                    return ((IExpressionString)expression).getString(row);
                }
                default: {
                    throw new RuntimeException("Unknown column type " + expression.getType());
                }
            }
        }
    }
}
