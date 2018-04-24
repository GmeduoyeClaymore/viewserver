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

import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;

/**
 * Created by bemm on 21/10/2014.
 */
public class BusinessDayCalculator {

    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static final Logger logger = LoggerFactory.getLogger(BusinessDayCalculator.class);

    public int getBusinessDay(ReadableDateTime start, ReadableDateTime end) {
        DateMidnight startMidnight = new DateMidnight(start);
        DateMidnight endMidnight = new DateMidnight(end);

        int weekdayStart = startMidnight.get(DateTimeFieldType.dayOfWeek());
        int weekdayEnd = endMidnight.get(DateTimeFieldType.dayOfWeek());

        if (weekdayStart == DateTimeConstants.SATURDAY) {
            startMidnight = startMidnight.plusDays(2);
            weekdayStart = DateTimeConstants.MONDAY;
        } else if (weekdayStart == DateTimeConstants.SUNDAY) {
            startMidnight = startMidnight.plusDays(1);
            weekdayStart = DateTimeConstants.MONDAY;
        }

        if (weekdayEnd == DateTimeConstants.SATURDAY) {
            endMidnight = endMidnight.minusDays(1);
            weekdayEnd = DateTimeConstants.FRIDAY;
        } else if (weekdayEnd == DateTimeConstants.SUNDAY) {
            endMidnight = endMidnight.minusDays(2);
            weekdayEnd = DateTimeConstants.FRIDAY;
        }
        int days = Days.daysBetween(startMidnight, endMidnight).getDays();

        startMidnight = startMidnight.plusDays(DateTimeConstants.SATURDAY - weekdayStart);
        endMidnight = endMidnight.plusDays(DateTimeConstants.SATURDAY - weekdayEnd);
        int daysBetweenWeekends = (int)((endMidnight.getMillis() - startMidnight.getMillis()) / (24 * 60 * 60 * 1000));
        int weekendDays = daysBetweenWeekends * 2 / 7;
        days -= weekendDays;

        return days;
    }
}
