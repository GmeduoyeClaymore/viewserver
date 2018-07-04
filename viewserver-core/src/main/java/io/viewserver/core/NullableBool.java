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

/**
 * Created by bemm on 09/03/2015.
 */
public enum NullableBool {
    Null((byte)0),
    False((byte)1),
    True((byte)2);

    private byte numericValue;

    NullableBool(byte numericValue) {
        this.numericValue = numericValue;
    }

    public byte getNumericValue() {
        return numericValue;
    }

    public boolean getBooleanValue() {
        if (this == Null) {
            throw new IllegalArgumentException("NullableBool.Null has no boolean value!");
        }
        return this == True;
    }

    public NullableBool and(NullableBool operand) {
        if (this == Null || operand == Null) {
            return Null;
        } else {
            return NullableBool.fromBoolean(this.getBooleanValue() && operand.getBooleanValue());
        }
    }

    public NullableBool or(NullableBool operand) {
        if (this == Null || operand == Null) {
            return Null;
        } else {
            return NullableBool.fromBoolean(this.getBooleanValue() || operand.getBooleanValue());
        }
    }

    public NullableBool not() {
        if (this == Null) {
            return Null;
        } else {
            return NullableBool.fromBoolean(!this.getBooleanValue());
        }
    }

    public static NullableBool fromBoolean(boolean bool) {
        return bool ? True : False;
    }
}
