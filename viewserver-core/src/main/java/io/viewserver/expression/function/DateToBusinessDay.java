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
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionInt;
import io.viewserver.expression.tree.IExpressionLong;
import io.viewserver.expression.tree.literal.LiteralBool;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.calccol.ICalcColumnHolder;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnType;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by bemm on 21/10/2014.
 */
public class DateToBusinessDay implements IUserDefinedFunction, IHookingFunction, IExpressionInt {
    private static final Logger log = LoggerFactory.getLogger(DateToBusinessDay.class);
    private final BusinessDayCalculator businessDayCalculator = new BusinessDayCalculator();
    private IExpressionLong dateExpression;
    private IExpressionBool rolloverEnabled;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length < 1 || !(parameters[0] instanceof IExpressionLong)
                || (parameters.length == 2 && !(parameters[1] instanceof IExpressionBool))
                || parameters.length > 2) {
            throw new IllegalArgumentException("Syntax: businessDay(<date (long)>[, <rollover (boolean)>])");
        }
        dateExpression = (IExpressionLong) parameters[0];
        rolloverEnabled = parameters.length == 2 ? (IExpressionBool)parameters[1] : new LiteralBool(true);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Int;
    }

    @Override
    public int getInt(int row) {
        return businessDayCalculator.getBusinessDay(new DateMidnight(dateExpression.getLong(row), DateTimeZone.UTC),
                DateMidnight.now(DateTimeZone.UTC));
    }

    @Override
    public void hook(HookingContext hookingContext) {
        if (!rolloverEnabled.getBool(0)) {
            return;
        }

        long frequency = TimeUnit.DAYS.toMillis(1);
        //long frequency = TimeUnit.MINUTES.toMillis(1);
        long now = System.currentTimeMillis();
        long delay = frequency - (now % frequency) + 1;

        log.debug("Rollover will run in {} millis then every {} millis", delay, frequency);

        hookingContext.getReactor().scheduleTask(() -> {
            Schema schema = hookingContext.getSchema();
            log.debug("{} - ROLLOVER - updating column '{}' in operator '{}'", DateToBusinessDay.class.getName(),
                    hookingContext.getColumnName(), schema.getOwner().getOwner().getPath());
            ColumnHolder columnHolder = schema.getColumnHolder(hookingContext.getColumnName());
            ((ICalcColumnHolder)columnHolder.getColumn()).clearAllCalculated();
            ((CalcColOperator)schema.getOwner().getOwner()).getInput().refreshData();
        }, delay, frequency);
    }
}
