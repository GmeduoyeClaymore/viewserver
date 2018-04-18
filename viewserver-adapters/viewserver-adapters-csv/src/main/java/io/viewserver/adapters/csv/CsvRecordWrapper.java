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

package io.viewserver.adapters.csv;

import io.viewserver.adapters.common.BaseRecordWrapper;
import io.viewserver.core.NullableBool;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.DataSource;
import javolution.text.TypeFormat;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvRecordWrapper extends BaseRecordWrapper {
    private final DateTime startDate;
    private static String dateFormatStr = "dd/MM/yyyy";
    private static String dateTimeFormatStr = "dd/MM/yyyy HH:mm";
    private static final Logger log = LoggerFactory.getLogger(CsvRecordWrapper.class);

    protected static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(dateFormatStr);
        }
    };
    protected static final ThreadLocal<SimpleDateFormat> dateTimeFormat = new ThreadLocal() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(dateTimeFormatStr);
        }
    };

    public static void setDateTimeFormat(String dateTimeFormatStr){
        CsvRecordWrapper.dateTimeFormatStr = dateTimeFormatStr;
    }

    public static void setDateFormat(String dateFormatStr){
        CsvRecordWrapper.dateFormatStr = dateFormatStr;
    }

    private static final Pattern datePattern = Pattern.compile("\\[(\\w+)[\\s]*([\\+\\-])[\\s]*([0-9]+)\\]");
    protected DateTime startTime;
    private CSVRecord record;


    public CsvRecordWrapper(DateTime startTime) {
        super();
        this.startTime = startTime;
        this.startDate = startTime.withMillisOfDay(0);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public Object getValue(String columnName) {

        Column column = dataSource.getSchema().getColumn(columnName);
        switch (column.getType()) {
            case Bool: {
                return getBool(columnName);
            }
            case NullableBool: {
                return getNullableBool(columnName);
            }
            case Byte: {
                return getByte(columnName);
            }
            case Short: {
                return getShort(columnName);
            }
            case Int: {
                return getInt(columnName);
            }
            case Long: {
                return getLong(columnName);
            }
            case Float: {
                return getFloat(columnName);
            }
            case Double: {
                return getDouble(columnName);
            }
            case String: {
                String value = getString(columnName);
                /*if(columnName.equals("productId") ){
                    return  UUID.randomUUID().toString() + "_" + value;
                }*/
                return value;
            }
            case Date: {
                return getDate(columnName);
            }
            case DateTime: {
                return getDateTime(columnName);
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unsupported column type %s in CSV record", column.getType()));
            }
        }
    }

    @Override
    public boolean hasValue(String columnName) {
        return true;
    }

    @Override
    public String toString() {
     return this.record.toString();
    }

    public CSVRecord getRecord() {
        return record;
    }

    public void setRecord(CSVRecord record) {
        this.record = record;
    }

    @Override
    public byte getByte(String columnName) {
        String string = getString(columnName);
        return string == null ? 0 : TypeFormat.parseByte(string); //Byte.parseByte(getString(columnName));
    }

    @Override
    public String getString(String columnName) {
        String value = record.isSet(columnName) ? record.get(columnName) : null;
        String s = this.replaceNullValues(columnName, value, String.class);
        return s == null ? null : s.trim();
    }

    @Override
    public boolean getBool(String columnName) {
        String string = getString(columnName);
        return string != null && TypeFormat.parseBoolean(string); //Boolean.parseBoolean(getString(columnName));
    }

    @Override
    public NullableBool getNullableBool(String columnName) {
        String value = getString(columnName);
        if (value == null || "".equals(value)) {
            return NullableBool.Null;
        }
        return NullableBool.fromBoolean(Boolean.parseBoolean(value));
    }

    @Override
    public short getShort(String columnName) {
        String string = getString(columnName);
        return (string == null || string.trim().equals("")) ? -1 : TypeFormat.parseShort(string.trim());
    }

    @Override
    public int getInt(String columnName) {
        Double dbl = getDouble(columnName);
        return dbl == null ? -1 : (int) Math.round(dbl);
    }

    @Override
    public long getLong(String columnName) {
        String string = getString(columnName);
        return (string == null || string.trim().equals("")) ? -1 : TypeFormat.parseLong(string.trim());
    }

    @Override
    public float getFloat(String columnName) {
        String string = getString(columnName);
        return (string == null || string.trim().equals("")) ? -1 : TypeFormat.parseFloat(string.trim());
    }

    @Override
    public double getDouble(String columnName) {
        String string = getString(columnName);
        return (string == null || string.trim().equals("")) ? -1 : TypeFormat.parseDouble(string.trim());
    }

    @Override
    public Date getDate(String columnName) {
        String value = getString(columnName);
        if(value == null){
            return null;
        }
        try {
            Matcher matcher = datePattern.matcher(value);

            if (matcher.find()) {
                boolean isNegative = matcher.group(2).equals("-");
                long delta = Long.parseLong(matcher.group(3));

                return startDate.plus(delta * (isNegative ? -1 : 1)).toDate();
            }

            return value.equals("") ? null : dateFormat.get().parse(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Date getDateTime(String columnName) {
        try {
            String value = getString(columnName);
            if(value == null){
                return null;
            }
            Matcher matcher = datePattern.matcher(value);

            if (matcher.find()) {
                String day = matcher.group(1);
                boolean isNegative = matcher.group(2).equals("-");
                int dayValue = dayStringToInt(day);
             //   int dayOffset = dayValue == 0 ? 0 : Days.daysBetween(startDate, startDate.withDayOfWeek(dayValue)).getDays();

              //  long millisToAdd = dayOffset * 24 * 60 * 60 * 1000;
                int delta = Integer.parseInt(matcher.group(3));

                return startTime.plusDays(delta * (isNegative ? -1 : 1)).toDate();
            }

            return value.equals("") ? null : dateTimeFormat.get().parse(value);
        } catch (Throwable e) {
            log.error("Problem parsing date",e);
            return new Date();
        }
    }

    private int dayStringToInt(String day){
        switch (day){
            case "MONDAY":
                return 1;
            case "TUESDAY":
                return 2;
            case "WEDNESDAY":
                return 3;
            case "THURSDAY":
                return 4;
            case "FRIDAY":
                return 5;
            case "SATURDAY":
                return 6;
            case "SUNDAY":
                return 7;
            case "NOW":
            default:
                return  0;
        }
    }
}
