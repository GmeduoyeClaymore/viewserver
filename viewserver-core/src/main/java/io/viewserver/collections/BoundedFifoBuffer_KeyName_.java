// :_KeyName_=Bool,_KeyType_=boolean;_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double;_KeyName_=String,_KeyType_=String

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

package io.viewserver.collections;

import io.viewserver.core._KeyType_;

import java.nio.BufferUnderflowException;

/**
 * Created by nick on 01/10/15.
 */
public class BoundedFifoBuffer_KeyName_ {
    private final _KeyType_[] elements;
    private int start = 0;
    private int end = 0;
    private final int maxElements;
    private boolean full;

    public BoundedFifoBuffer_KeyName_(int size) {
        elements = new _KeyType_[size];
        maxElements = size;
    }

    public int size() {
        int size;
        if (end < start) {
            size = maxElements - start + end;
        } else if (end == start) {
            size = full ? maxElements : 0;
        } else {
            size = end - start;
        }
        return size;
    }

    public int maxSize() {
        return maxElements;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isFull() {
        return full;
    }

    public boolean add(_KeyType_ element) {
        if (full) {
            remove();
        }

        elements[end++] = element;

        if (end >= maxElements) {
            end = 0;
        }

        if (end == start) {
            full = true;
        }

        return true;
    }

    public _KeyType_ get() {
        if (isEmpty()) {
            throw new BufferUnderflowException();
        }
        return elements[start];
    }

    public _KeyType_ remove() {
        if (isEmpty()) {
            throw new BufferUnderflowException();
        }

        _KeyType_ element = elements[start++];

        if (start >= maxElements) {
            start = 0;
        }

        full = false;

        return element;
    }

    public void clear() {
        full = false;
        start = 0;
        end = 0;
    }
}
