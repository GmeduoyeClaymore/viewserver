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

package io.viewserver.expression.tree;

import io.viewserver.schema.column.ColumnType;

/**
 * Created by nickc on 14/10/2014.
 */
public class UnaryMinusExpression {
    public static class Byte implements IExpressionByte {
        private IExpressionByte operand;

        public Byte(IExpressionByte operand) {
            this.operand = operand;
        }

        @Override
        public byte getByte(int row) {
            return (byte)-operand.getByte(row);
        }

        @Override
        public ColumnType getType() {
            return ColumnType.Byte;
        }
    }

    public static class Short implements IExpressionShort {
        private IExpressionShort operand;

        public Short(IExpressionShort operand) {
            this.operand = operand;
        }

        @Override
        public short getShort(int row) {
            return (short)-operand.getShort(row);
        }

        @Override
        public ColumnType getType() {
            return ColumnType.Short;
        }
    }

    public static class Int implements IExpressionInt {
        private IExpressionInt operand;

        public Int(IExpressionInt operand) {
            this.operand = operand;
        }

        @Override
        public int getInt(int row) {
            return -operand.getInt(row);
        }

        @Override
        public ColumnType getType() {
            return ColumnType.Int;
        }
    }

    public static class Long implements IExpressionLong {
        private IExpressionLong operand;

        public Long(IExpressionLong operand) {
            this.operand = operand;
        }

        @Override
        public long getLong(int row) {
            return -operand.getLong(row);
        }

        @Override
        public ColumnType getType() {
            return ColumnType.Long;
        }
    }

    public static class Float implements IExpressionFloat {
        private IExpressionFloat operand;

        public Float(IExpressionFloat operand) {
            this.operand = operand;
        }

        @Override
        public float getFloat(int row) {
            return -operand.getFloat(row);
        }

        @Override
        public ColumnType getType() {
            return ColumnType.Float;
        }
    }

    public static class Double implements IExpressionDouble {
        private IExpressionDouble operand;

        public Double(IExpressionDouble operand) {
            this.operand = operand;
        }

        @Override
        public double getDouble(int row) {
            return -operand.getDouble(row);
        }

        @Override
        public ColumnType getType() {
            return ColumnType.Double;
        }
    }
}
