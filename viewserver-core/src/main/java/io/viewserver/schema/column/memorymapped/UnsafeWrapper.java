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

package io.viewserver.schema.column.memorymapped;

import io.viewserver.core._KeyType_;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by nick on 27/10/15.
 */
public class UnsafeWrapper {
    private static final Unsafe unsafe;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static byte getByte(long base, int index) {
        return unsafe.getByte(base + index);
    }

    public static void putByte(long base, int index, byte value) {
        unsafe.putByte(base + index, value);
    }

    public static short getShort(long base, int index) {
        return unsafe.getByte(base + (index << 1));
    }

    public static void putShort(long base, int index, short value) {
        unsafe.putShort(base + (index << 1), value);
    }

    public static int getInt(long base, int index) {
        return unsafe.getInt(base + (index << 2));
    }

    public static void putInt(long base, int index, int value) {
        unsafe.putInt(base + (index << 2), value);
    }

    public static long getLong(long base, int index) {
        return unsafe.getLong(base + (index << 3));
    }

    public static void putLong(long base, int index, long value) {
        unsafe.putLong(base + (index << 3), value);
    }

    public static float getFloat(long base, int index) {
        return unsafe.getFloat(base + (index << 2));
    }

    public static void putFloat(long base, int index, float value) {
        unsafe.putFloat(base + (index << 2), value);
    }

    public static double getDouble(long base, int index) {
        return unsafe.getDouble(base + (index << 3));
    }

    public static void putDouble(long base, int index, double value) {
        unsafe.putDouble(base + (index << 3), value);
    }

    public static _KeyType_ get_KeyName_(long base, int index) {
        throw new UnsupportedOperationException();
    }

    public static void put_KeyName_(long base, int index, _KeyType_ value) {
        throw new UnsupportedOperationException();
    }
}
