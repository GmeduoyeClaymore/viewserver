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

import io.viewserver.core.BusinessDayCalculator;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionInt;
import io.viewserver.schema.column.ColumnType;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;

/**
 * Created by nickc on 21/10/2014.
 */
public class  DayToBusinessDay implements IUserDefinedFunction, IExpressionInt {
    private final BusinessDayCalculator businessDayCalculator = new BusinessDayCalculator();
    private IExpressionInt dateExpression;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 1 || !(parameters[0] instanceof IExpressionInt)) {
            throw new IllegalArgumentException("dayToBusinessDay takes 1 int argument");
        }
        dateExpression = (IExpressionInt) parameters[0];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Int;
    }

    @Override
    public int getInt(int row) {


        return businessDayCalculator.getBusinessDay(new DateMidnight(DateTimeZone.UTC).minusDays(dateExpression.getInt(row)),
                DateMidnight.now(DateTimeZone.UTC));
    }
}
