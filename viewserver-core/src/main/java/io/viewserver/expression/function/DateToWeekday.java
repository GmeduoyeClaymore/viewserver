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

import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionInt;
import io.viewserver.expression.tree.IExpressionLong;
import io.viewserver.schema.column.ColumnType;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nickc on 21/10/2014.
 */
public class DateToWeekday implements IUserDefinedFunction, IExpressionInt {
    private static final Logger log = LoggerFactory.getLogger(DateToWeekday.class);
    private final DateMidnight dateMidnight = new DateMidnight(DateTimeZone.UTC);
    private IExpressionLong dateExpression;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 1 || !(parameters[0] instanceof IExpressionLong)) {
            throw new IllegalArgumentException("weekday takes 1 long argument");
        }
        dateExpression = (IExpressionLong) parameters[0];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Int;
    }

    @Override
    public int getInt(int row) {
        return dateMidnight.withMillis(dateExpression.getLong(row)).getDayOfWeek();
    }
}
