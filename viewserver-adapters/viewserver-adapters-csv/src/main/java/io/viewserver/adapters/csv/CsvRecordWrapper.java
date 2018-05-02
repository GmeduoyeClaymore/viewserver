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
import io.viewserver.datasource.SchemaConfig;
import javolution.text.TypeFormat;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.IOUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvRecordWrapper extends BaseRecordWrapper {
    private final DateTime startDate;
    private SchemaConfig config;
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


    public CsvRecordWrapper(DateTime startTime, SchemaConfig config) {
        super(config);
        this.startTime = startTime;
        this.startDate = startTime.withMillisOfDay(0);
        this.config = config;
    }


    @Override
    public Object getValue(String columnName) {

        Column column = this.config .getColumn(columnName);
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
                return readReferencedFiles(value);
            }
            case Json: {
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

    private String readReferencedFiles(String value) {
        if (value != null && value.startsWith("ref://")) {
            String fileReference = value.substring(6);
            return getJsonStringFromFile(fileReference);
        }
        return value;
    }

    private String getJsonStringFromFile(String dataFile) {
        return getJsonStringFromFile(dataFile, null);
    }

    private String getJsonStringFromFile(String dataFile, List<Map<String, String>> records) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dataFile);
        if (inputStream == null) {
            throw new RuntimeException("Unable to find resource at at " + dataFile);
        }
        Reader reader = getBufferedReader(inputStream);
        try {
            String json = readStringAndClose(reader, 0);
            json = replaceTokens(json);
            return json;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String replaceTokens(String json) {
        for(Column col : config.getColumns()){
            if(json.contains("{" + col.getName() + "}")){
                json = json.replace("{" + col.getName() + "}", getValue(col.getName()) + "");
            }
        }
        return json;
    }

    public static Reader getBufferedReader(InputStream var0) {
        return var0 == null?null:new BufferedReader(new InputStreamReader(var0));
    }

    public static long copyAndCloseInput(Reader var0, Writer var1, long var2) throws IOException {
        try {
            long var4 = 0L;
            int var6 = (int)Math.min(var2, 4096L);
            char[] var7 = new char[var6];

            while(true) {
                if(var2 > 0L) {
                    var6 = var0.read(var7, 0, var6);
                    if(var6 >= 0) {
                        if(var1 != null) {
                            var1.write(var7, 0, var6);
                        }

                        var2 -= (long)var6;
                        var6 = (int)Math.min(var2, 4096L);
                        var4 += (long)var6;
                        continue;
                    }
                }

                long var8 = var4;
                return var8;
            }
        } catch (Exception var13) {
            throw new RuntimeException(var13);
        } finally {
            var0.close();
        }
    }

    public static String readStringAndClose(Reader var0, int var1) throws IOException {
        String var4;
        try {
            if(var1 <= 0) {
                var1 = 2147483647;
            }

            int var2 = Math.min(4096, var1);
            StringWriter var3 = new StringWriter(var2);
            copyAndCloseInput(var0, var3, (long)var1);
            var4 = var3.toString();
        } finally {
            var0.close();
        }

        return var4;
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
        String value = record.isSet(columnName) ? readReferencedFiles(record.get(columnName)) : null;
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
    public Short getShort(String columnName) {
        String string = getString(columnName);
        return (string == null || string.trim().equals("")) ? -1 : TypeFormat.parseShort(string.trim());
    }

    @Override
    public int getInt(String columnName) {
        Double dbl = getDouble(columnName);
        return dbl == null ? -1 : (int) Math.round(dbl);
    }

    @Override
    public Long getLong(String columnName) {
        String string = getString(columnName);
        return (string == null || string.trim().equals("")) ? -1 : TypeFormat.parseLong(string.trim());
    }

    @Override
    public Float getFloat(String columnName) {
        String string = getString(columnName);
        return (string == null || string.trim().equals("")) ? -1 : TypeFormat.parseFloat(string.trim());
    }

    @Override
    public Double getDouble(String columnName) {
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
