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

package io.viewserver.util;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by nick on 11/02/2015.
 */
public abstract class AbstractValueReader implements IValueReader {
    @Override
    public Date readDate(String field) {
        Object val = get(field);
        if (val == null) return null;
        if (val instanceof Date) {
            Date date = (Date) val;
            if (date.getTime() > 0) return date;
            return null;
        }
        long date = (long) val;
        if (date < 0) return null;
        return new Date(date);
    }

    @Override
    public Timestamp readTimestamp(String field) {
        Object val = get(field);
        if (val == null) return null;
        if (val instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) val;
            if (timestamp.getTime() > 0) return timestamp;
            return null;
        }
        long time = (long) get(field);
        if (time < 0) return null;
        Timestamp timestamp = new Timestamp(time);
        timestamp.setNanos((int) get(field + "_nanos"));
        return timestamp;
    }

    @Override
    public Integer readInteger(String field) {
        if (get(field) == null) return null;
        int val = (int) get(field);
        if (val == Integer.MIN_VALUE) return null;
        return val;
    }

    @Override
    public Boolean readBoolean(String field) {
        if (get(field) == null) return null;
        return (boolean) get(field);
    }

    @Override
    public Long readLong(String field) {
        if (get(field) == null) return null;
        long val = (long) get(field);
        if (val == Long.MIN_VALUE) return null;

        return val;
    }

    @Override
    public Float readFloat(String field) {
        if (get(field) == null) return null;
        float val = (float) get(field);
        if (Float.isNaN(val)) return null;
        return val;
    }

    @Override
    public Double readDouble(String field) {
        if (get(field) == null) return null;
        double val = (double) get(field);
        if (Double.isNaN(val)) return null;
        return val;
    }

    @Override
    public String readString(String field) {
        return (String) get(field);
    }

    @Override
    public String[] readStringArray(String field) {
        Object o = get(field);
        if (o == null) return null;
        if (o instanceof String[]) return (String[]) o;
        if (o instanceof String) return new String[] { (String)o};
        if (o instanceof Object[]) {
            Object[] array = (Object[])o;
            String[] stringArray = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                stringArray[i] = new String((char[])array[i]);
            }
            return stringArray;
        }
        throw new ClassCastException("Could not convert type " + o.getClass() + " to " + String[].class);
    }

    @Override
    public double[] readDoubleArray(String field) {
        Object o = get(field);
        if (o == null) return null;
        if (o instanceof double[]) return (double[]) o;
        if (o instanceof Double) return new double[] { (double)o};

        throw new ClassCastException("Could not convert type " + o.getClass() + " to " + double[].class);
    }

    @Override
    public int[] readIntegerArray(String field) {
        Object o = get(field);
        if (o == null) return null;
        if (o instanceof int[]) return (int[]) o;

        if (o instanceof Integer) return new int[] { (int)o };

        throw new ClassCastException("Could not convert type " + o.getClass() + " to " + int[].class);
    }

    protected abstract Object get(String field);
}