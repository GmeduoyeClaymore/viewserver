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

package io.viewserver.schema.column.chunked;

import io.viewserver.core._KeyType_;

import java.util.Arrays;

/**
 * Created by nickc on 23/09/2014.
 */
public class ChunkedArray_KeyName_ {
    private String name;
    private _KeyType_ defaultValue;
    private int chunkSize;
    private int chunkMask;
    private int chunkQuotient;
    private int chunkCount;
    private _KeyType_ data[][];

    public ChunkedArray_KeyName_(String name, int capacity, int chunkSize, _KeyType_ defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        setChunkSize(chunkSize);
        ensureCapacity(capacity);

        data = new _KeyType_[chunkCount][chunkSize];
        clear();
    }

    public int getCapacity() {
        return chunkCount * chunkSize;
    }

    private void setChunkSize(int minimum) {
        this.chunkSize = 1;
        this.chunkQuotient = 0;
        while (this.chunkSize < minimum) {
            this.chunkSize <<= 1;
            this.chunkQuotient++;
        }
        this.chunkMask = this.chunkSize - 1;
    }

    private void clear() {
        for (int i = 0; i < chunkCount; i++) {
            Arrays.fill(data[i], defaultValue);
        }
    }

    public void ensureCapacity(int capacity) {
        int chunk = capacity >>> chunkQuotient;
        if ((capacity & chunkMask) != 0) {
            chunk++;
        }
        if (chunk >= chunkCount) {
            _KeyType_[][] newData = new _KeyType_[chunk + 1][];
            for (int i = 0; i < chunkCount; i++) {
                newData[i] = data[i];
            }
            for (int i = chunkCount; i <= chunk; i++) {
                newData[i] = new _KeyType_[chunkSize];
                Arrays.fill(newData[i], defaultValue);
            }
            this.data = newData;
            chunkCount = chunk + 1;
        }
    }

    public void setValue(int row, _KeyType_ value) {
        int chunk = row >>> chunkQuotient;
        int offset = row & chunkMask;
        data[chunk][offset] = value;
    }

    public _KeyType_ getValue(int row) {
        int chunk = row >>> chunkQuotient;
        int offset = row & chunkMask;
        return data[chunk][offset];
    }

    public String getName() {
        return name;
    }

    public void resetAll() {
        clear();
    }
}
