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

package io.viewserver.core;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Paul on 13/07/2015.
 */
public class BusinessDayCalculatorTest {
    private final BusinessDayCalculator businessDayCalculator = new BusinessDayCalculator();

    @Test
    public void testSaturday(){

        DateTime start = new DateTime(1436572800000l, DateTimeZone.UTC);
        DateTime now = DateTime.now(DateTimeZone.UTC);
        int businessDays = businessDayCalculator.getBusinessDay(start,
                now);

        System.out.println(now);
        System.out.println(start);
        System.out.println(businessDays);
    }

    @Test
    public void testDatesOverLastWeek() {
        DateMidnight now = DateMidnight.now(DateTimeZone.UTC);

        int zeroDays = businessDayCalculator.getBusinessDay(new DateMidnight(now.getMillis(), DateTimeZone.UTC),
                DateMidnight.now(DateTimeZone.UTC));

        Assert.assertEquals(0, zeroDays);

        now = now.minusDays(1);
        int oneDay = businessDayCalculator.getBusinessDay(new DateMidnight(now.getMillis(), DateTimeZone.UTC),
                DateMidnight.now(DateTimeZone.UTC));

        Assert.assertEquals(1, oneDay);

        now = now.minusDays(2);
        int twoDays = businessDayCalculator.getBusinessDay(new DateMidnight(now.getMillis(), DateTimeZone.UTC),
                DateMidnight.now(DateTimeZone.UTC));

        Assert.assertEquals(2, twoDays);
    }
}
