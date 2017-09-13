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

package io.viewserver.operators.group.summary.stdDev;

import io.viewserver.operators.group.ISummary;
import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.operators.group.summary.ISummaryFactory;
import io.viewserver.operators.group.summary.MultiTypeSummaryBase;
import io.viewserver.schema.column.ColumnHolder;

/**
 * Created by nickc on 01/11/2014.
 */

public class StdDevFactory implements ISummaryFactory {
    public static final String NAME = "stdDev";

    @Override
    public ISummary createSummary(String name, String target, Object[] arguments) {
        String countColumn = null;
        if (arguments != null && arguments.length > 0) {
            if (!(arguments[0] instanceof String)) {
                throw new IllegalArgumentException("StdDev takes one optional String argument (for the count column)");
            }
            countColumn = (String) arguments[0];
        }
        return new StdDev(name, target, countColumn);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class StdDev extends MultiTypeSummaryBase {
        private String countColumn;

        public StdDev(String name, String target, String countColumn) {
            super(name, target);
            this.countColumn = countColumn;
        }

        @Override
        public void initialise(ISummaryContext context) {
            ColumnHolder valueColumnHolder = context.getInboundSchema().getColumnHolder(target);
            switch (valueColumnHolder.getType()) {
                case Byte: {
                    this.internalSummary = new StdDevByte(name, target, countColumn);
                    break;
                }
                case Short: {
                    this.internalSummary = new StdDevShort(name, target, countColumn);
                    break;
                }
                case Int: {
                    this.internalSummary = new StdDevInt(name, target, countColumn);
                    break;
                }
                case Long: {
                    this.internalSummary = new StdDevLong(name, target, countColumn);
                    break;
                }
                case Float: {
                    this.internalSummary = new StdDevFloat(name, target, countColumn);
                    break;
                }
                case Double: {
                    this.internalSummary = new StdDevDouble(name, target, countColumn);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Cannot have a standard deviation for type " + valueColumnHolder.getType());
                }
            }
            this.internalSummary.initialise(context);
        }
    }
}
