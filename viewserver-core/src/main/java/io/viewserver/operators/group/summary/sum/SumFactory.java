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

package io.viewserver.operators.group.summary.sum;

import io.viewserver.operators.group.ISummary;
import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.operators.group.summary.ISummaryFactory;
import io.viewserver.operators.group.summary.MultiTypeSummaryBase;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnType;

public class SumFactory implements ISummaryFactory {
    public static final String NAME = "sum";

    @Override
    public ISummary createSummary(String name, String target, Object[] arguments) {
        ColumnType columnType = null;
        if (arguments != null && arguments.length > 0) {
            if (arguments[0] instanceof ColumnType) {
                columnType = (ColumnType) arguments[0];
            } else if (arguments[0] instanceof String) {
                columnType = ColumnType.valueOf((String)arguments[0]);
            } else {
                throw new IllegalArgumentException("Argument 1 for Sum should be of type ContentType or String");
            }
        }
        return new Sum(name, target, columnType);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class Sum extends MultiTypeSummaryBase {
        private ColumnType columnType;

        public Sum(String name, String target, ColumnType columnType) {
            super(name, target);
            this.columnType = columnType;
        }

        @Override
        public void initialise(ISummaryContext context) {
            this.internalSummary = createInternalSummary(context);
            this.internalSummary.initialise(context);
        }

        private ISummary createInternalSummary(ISummaryContext context) {
            ColumnHolder valueColumnHolder = context.getInboundSchema().getColumnHolder(target);
            if(valueColumnHolder == null){
                throw new RuntimeException("Unable to find column holder for name \"" + target + "\"");
            }
            if (columnType == null) {
                columnType = valueColumnHolder.getType();
            }
            switch (valueColumnHolder.getType()) {
                case Byte: {
                    switch (columnType) {
                        case Byte: {
                            return new SumByteByte(name, target);
                        }
                        case Short: {
                            return new SumByteShort(name, target);
                        }
                        case Int: {
                            return new SumByteInt(name, target);
                        }
                        case Long: {
                            return new SumByteLong(name, target);
                        }
                        case Float: {
                            return new SumByteFloat(name, target);
                        }
                        case Double: {
                            return new SumByteDouble(name, target);
                        }
                    }
                }
                case Short: {
                    switch (columnType) {
                        case Short: {
                            return new SumShortShort(name, target);
                        }
                        case Int: {
                            return new SumShortInt(name, target);
                        }
                        case Long: {
                            return new SumShortLong(name, target);
                        }
                        case Float: {
                            return new SumShortFloat(name, target);
                        }
                        case Double: {
                            return new SumShortDouble(name, target);
                        }
                    }
                }
                case Int: {
                    switch (columnType) {
                        case Int: {
                            return new SumIntInt(name, target);
                        }
                        case Long: {
                            return new SumIntLong(name, target);
                        }
                        case Float: {
                            return new SumIntFloat(name, target);
                        }
                        case Double: {
                            return new SumIntDouble(name, target);
                        }
                    }
                }
                case Long: {
                    switch (columnType) {
                        case Long: {
                            return new SumLongLong(name, target);
                        }
                        case Double: {
                            return new SumLongDouble(name, target);
                        }
                    }
                }
                case Float: {
                    switch (columnType) {
                        case Float: {
                            return new SumFloatFloat(name, target);
                        }
                        case Double: {
                            return new SumFloatDouble(name, target);
                        }
                    }
                }
                case Double: {
                    return new SumDoubleDouble(name, target);
                }
            }
            throw new IllegalArgumentException("Cannot create " + columnType + " sum of " + valueColumnHolder.getType());
        }
    }
}
