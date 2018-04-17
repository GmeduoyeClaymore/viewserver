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

package io.viewserver.messages.common;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by nick on 04/12/15.
 */
public class ValueLists {
    public static IValueList valueListOf(Object... values) {
        if (values == null || values.length == 0) {
            return EMPTY_LIST;
        }
        if (values[0] instanceof Boolean) {
            final BooleanList booleanList = new BooleanList();
            for (int i = 0; i < values.length; i++) {
                booleanList.add(i, (boolean)values[i]);
            }
            return booleanList;
        }
        if (values[0] instanceof Integer) {
            final IntegerList integerList = new IntegerList();
            for (int i = 0; i < values.length; i++) {
                integerList.add((int)values[i]);
            }
            return integerList;
        }
        if (values[0] instanceof Long) {
            final LongList longList = new LongList();
            for (int i = 0; i < values.length; i++) {
                longList.add((long)values[i]);
            }
            return longList;
        }
        if (values[0] instanceof Float) {
            final FloatList floatList = new FloatList();
            for (int i = 0; i < values.length; i++) {
                floatList.add((float)values[i]);
            }
            return floatList;
        }
        if (values[0] instanceof Double) {
            final DoubleList doubleList = new DoubleList();
            for (int i = 0; i < values.length; i++) {
                doubleList.add((double)values[i]);
            }
            return doubleList;
        }
        if (values[0] instanceof String) {
            final StringList stringList = new StringList();
            for (int i = 0; i < values.length; i++) {
                stringList.add((String)values[i]);
            }
            return stringList;
        }
        throw new IllegalArgumentException(String.format("Unsupported type in values %s",values[0]));
    }

    public interface IValueList {
        int size();

        default boolean isEmpty() {
            return size() == 0;
        }

        String getValuesString(String separator);

        IValueList copy();

        Object[] toArray();
    }

    public static final IValueList EMPTY_LIST = new IValueList() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public String getValuesString(String separator) {
            return null;
        }

        @Override
        public IValueList copy() {
            return this;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }
    };

    public interface IBooleanList extends IValueList {
        boolean get(int index);

        void add(int index, boolean value);

        default String getValuesString(String separator) {
            final int size = size();
            if (size == 0) {
                return "";
            }
            if (size == 1) {
                return Boolean.toString(get(0));
            }
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    builder.append(separator);
                }
                builder.append(get(i));
            }
            return builder.toString();
        }

        default IBooleanList copy() {
            return new BooleanList(this);
        }

        default Object[] toArray() {
            final int size = size();
            final Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = get(i);
            }
            return array;
        }
    }

    public static class BooleanList implements IBooleanList {
        private final BitSet innerList;
        private int size;

        public BooleanList() {
            this.innerList = new BitSet();
        }

        public BooleanList(IBooleanList other) {
            this.size = other.size();
            innerList = new BitSet(size);
            for (int i = 0; i < size; i++) {
                add(i, other.get(i));
            }
        }

        @Override
        public boolean get(int index) {
            if (index >= size) {
                throw new IndexOutOfBoundsException();
            }
            if (index >= innerList.length()) {
                return false;
            }
            return innerList.get(index);
        }

        @Override
        public void add(int index, boolean value) {
            if (value) {
                innerList.set(index, value);
            }
        }

        @Override
        public int size() {
            return size;
        }
    }

    public interface IIntegerList extends IValueList {
        int get(int index);

        void add(int value);

        default String getValuesString(String separator) {
            final int size = size();
            if (size == 0) {
                return "";
            }
            if (size == 1) {
                return Integer.toString(get(0));
            }
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    builder.append(separator);
                }
                builder.append(get(i));
            }
            return builder.toString();
        }

        default IIntegerList copy() {
            return new IntegerList(this);
        }

        default Object[] toArray() {
            final int size = size();
            final Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = get(i);
            }
            return array;
        }
    }

    public static class IntegerList implements IIntegerList {
        private final TIntArrayList innerList;

        public IntegerList() {
            this.innerList = new TIntArrayList();
        }

        public IntegerList(IIntegerList other) {
            final int size = other.size();
            this.innerList = new TIntArrayList(size);
            for (int i = 0; i < size; i++) {
                add(other.get(i));
            }
        }

        @Override
        public int get(int index) {
            return innerList.get(index);
        }

        @Override
        public void add(int value) {
            innerList.add(value);
        }

        @Override
        public int size() {
            return innerList.size();
        }
    }

    public interface ILongList extends IValueList {
        long get(int index);

        void add(long value);

        default String getValuesString(String separator) {
            final int size = size();
            if (size == 0) {
                return "";
            }
            if (size == 1) {
                return Long.toString(get(0));
            }
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    builder.append(separator);
                }
                builder.append(get(i));
            }
            return builder.toString();
        }
        
        default ILongList copy() {
            return new LongList(this);
        }

        default Object[] toArray() {
            final int size = size();
            final Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = get(i);
            }
            return array;
        }
    }

    public static class LongList implements ILongList {
        private final TLongArrayList innerList;

        public LongList() {
            this.innerList = new TLongArrayList();
        }

        public LongList(ILongList other) {
            final int size = other.size();
            this.innerList = new TLongArrayList(size);
            for (int i = 0; i < size; i++) {
                add(other.get(i));
            }
        }

        @Override
        public long get(int index) {
            return innerList.get(index);
        }

        @Override
        public void add(long value) {
            innerList.add(value);
        }

        @Override
        public int size() {
            return innerList.size();
        }
    }

    public interface IFloatList extends IValueList {
        float get(int index);

        void add(float value);

        default String getValuesString(String separator) {
            final int size = size();
            if (size == 0) {
                return "";
            }
            if (size == 1) {
                return Float.toString(get(0));
            }
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    builder.append(separator);
                }
                builder.append(get(i));
            }
            return builder.toString();
        }
        
        default IFloatList copy() {
            return new FloatList(this);
        }

        default Object[] toArray() {
            final int size = size();
            final Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = get(i);
            }
            return array;
        }
    }

    public static class FloatList implements IFloatList {
        private final TFloatArrayList innerList;

        public FloatList() {
            this.innerList = new TFloatArrayList();
        }

        public FloatList(IFloatList other) {
            final int size = other.size();
            this.innerList = new TFloatArrayList(size);
            for (int i = 0; i < size; i++) {
                add(other.get(i));
            }
        }

        @Override
        public float get(int index) {
            return innerList.get(index);
        }

        @Override
        public void add(float value) {
            innerList.add(value);
        }

        @Override
        public int size() {
            return innerList.size();
        }
    }

    public interface IDoubleList extends IValueList {
        double get(int index);

        void add(double value);

        default String getValuesString(String separator) {
            final int size = size();
            if (size == 0) {
                return "";
            }
            if (size == 1) {
                return Double.toString(get(0));
            }
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    builder.append(separator);
                }
                builder.append(get(i));
            }
            return builder.toString();
        }

        default IDoubleList copy() {
            return new DoubleList(this);
        }

        default Object[] toArray() {
            final int size = size();
            final Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = get(i);
            }
            return array;
        }
    }

    public static class DoubleList implements IDoubleList {
        private final TDoubleArrayList innerList;

        public DoubleList() {
            this.innerList = new TDoubleArrayList();
        }

        public DoubleList(IDoubleList other) {
            final int size = other.size();
            this.innerList = new TDoubleArrayList(size);
            for (int i = 0; i < size; i++) {
                add(other.get(i));
            }
        }

        @Override
        public double get(int index) {
            return innerList.get(index);
        }

        @Override
        public void add(double value) {
            innerList.add(value);
        }

        @Override
        public int size() {
            return innerList.size();
        }
    }

    public interface IStringList extends IValueList {
        String get(int index);

        void add(String value);

        void add(int index, String value);

        default String getValuesString(String separator) {
            final int size = size();
            if (size == 0) {
                return "";
            }
            if (size == 1) {
                return get(0);
            }
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    builder.append(separator);
                }
                builder.append(get(i));
            }
            return builder.toString();
        }

        default IStringList copy() {
            return new StringList(this);
        }

        default Object[] toArray() {
            final int size = size();
            final String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = get(i);
            }
            return array;
        }

        default String[] toArray(String[] array) {
            final int size = size();
            if (array.length < size) {
                array = new String[size];
            }
            for (int i = 0; i < size; i++) {
                array[i] = get(i);
            }
            return array;
        }
    }

    public static class StringList implements IStringList {
        private final List<String> innerList;

        public StringList() {
            this.innerList = new ArrayList<>();
        }

        public StringList(IStringList other) {
            final int size = other.size();
            this.innerList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                add(other.get(i));
            }
        }

        @Override
        public String get(int index) {
            return innerList.get(index);
        }

        @Override
        public void add(String value) {
            innerList.add(value);
        }

        @Override
        public void add(int index, String value) {
            innerList.set(index, value);
        }

        @Override
        public int size() {
            return innerList.size();
        }
    }
}
