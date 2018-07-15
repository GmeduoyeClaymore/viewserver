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

package io.viewserver.operators.group.summary.minmax;

import io.viewserver.operators.group.ISummary;
import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.operators.group.summary.ISummaryFactory;
import io.viewserver.operators.group.summary.MultiTypeSummaryBase;
import io.viewserver.schema.column.ColumnHolder;

public class MinMaxFactory implements ISummaryFactory {
    public static final String NAME_MIN = "min";
    public static final String NAME_MAX = "max";
    private boolean isMinimum;

    public MinMaxFactory(String name) {
        this.isMinimum = name.equals(NAME_MIN);
    }

    @Override
    public ISummary createSummary(String name, String target, Object[] arguments) {
        int heapSize = 30;
        if (arguments != null && arguments.length > 0) {
            if (arguments[0] instanceof Integer) {
                heapSize = (int)arguments[0];
            }
        }
        return new MinMax(name, target, isMinimum, heapSize);
    }

    @Override
    public String getName() {
        return isMinimum ? NAME_MIN : NAME_MAX;
    }

    private static class MinMax extends MultiTypeSummaryBase {
        private boolean isMinimum;
        private int heapSize;

        public MinMax(String name, String target, boolean isMinimum, int heapSize) {
            super(name, target);
            this.isMinimum = isMinimum;
            this.heapSize = heapSize;
        }

        @Override
        public void initialise(ISummaryContext context) {
            ColumnHolder valueColumnHolder = context.getInboundSchema().getColumnHolder(target);
            if(valueColumnHolder == null){
                throw new RuntimeException(String.format("Unable to find column %s in schema %s",target,context.getInboundSchema()));
            }
            switch (valueColumnHolder.getType()) {
                case Byte: {
                    this.internalSummary = new MinMaxByte(name, target, isMinimum, heapSize);
                    break;
                }
                case Short: {
                    this.internalSummary = new MinMaxShort(name, target, isMinimum, heapSize);
                    break;
                }
                case Int: {
                    this.internalSummary = new MinMaxInt(name, target, isMinimum, heapSize);
                    break;
                }
                case Long: {
                    this.internalSummary = new MinMaxLong(name, target, isMinimum, heapSize);
                    break;
                }
                case Float: {
                    this.internalSummary = new MinMaxFloat(name, target, isMinimum, heapSize);
                    break;
                }
                case Double: {
                    this.internalSummary = new MinMaxDouble(name, target, isMinimum, heapSize);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Cannot have a min/max for type " + valueColumnHolder.getType());
                }
            }
            this.internalSummary.initialise(context);
        }
    }
}
