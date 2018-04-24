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

package io.viewserver.schema.column.disk;

import io.viewserver.core._KeyType_;

import java.nio.ByteBuffer;

/**
 * Created by bemm on 02/12/2014.
 */
public class ByteBufferWrapper {
    private ByteBuffer buffer;

    public ByteBufferWrapper(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public byte getByte(int index) {
        return buffer.get(index);
    }

    public void putByte(int index, byte value) {
        buffer.put(index, value);
    }

    public short getShort(int index) {
        return buffer.getShort(index);
    }

    public void putShort(int index, short value) {
        buffer.putShort(index, value);
    }

    public int getInt(int index) {
        return buffer.getInt(index);
    }

    public void putInt(int index, int value) {
        buffer.putInt(index, value);
    }

    public long getLong(int index) {
        return buffer.getLong(index);
    }

    public void putLong(int index, long value) {
        buffer.putLong(index, value);
    }

    public float getFloat(int index) {
        return buffer.getFloat(index);
    }

    public void putFloat(int index, float value) {
        buffer.putFloat(index, value);
    }

    public double getDouble(int index) {
        return buffer.getDouble(index);
    }

    public void putDouble(int index, double value) {
        buffer.putDouble(index, value);
    }

    public _KeyType_ get_KeyName_(int index) {
        throw new UnsupportedOperationException("This method only exists for source generation purposes!");
    }

    public void put_KeyName_(int index, _KeyType_ value) {
        throw new UnsupportedOperationException("This method only exists for source generation purposes!");
    }
}
