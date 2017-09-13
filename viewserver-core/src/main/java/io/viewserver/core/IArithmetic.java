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
 * Created by nick on 19/03/2015.
 */
public interface IArithmetic {
    public static Arithmetic_KeyName_ _KeyName_ = new Arithmetic_KeyName_();
    public static ArithmeticByte Byte = new ArithmeticByte();
    public static ArithmeticShort Short = new ArithmeticShort();
    public static ArithmeticInt Int = new ArithmeticInt();
    public static ArithmeticLong Long = new ArithmeticLong();
    public static ArithmeticFloat Float = new ArithmeticFloat();
    public static ArithmeticDouble Double = new ArithmeticDouble();

    public static class Arithmetic_KeyName_ {
        public _KeyType_ add(_KeyType_ x, _KeyType_ y) {
            throw new UnsupportedOperationException("Why are you calling this?");
        }

        public _KeyType_ subtract(_KeyType_ x, _KeyType_ y) {
            throw new UnsupportedOperationException("Why are you calling this?");
        }

        public _KeyType_ multiply(_KeyType_ x, _KeyType_ y) {
            throw new UnsupportedOperationException("Why are you calling this?");
        }
    }

    public static class ArithmeticByte {
        public byte add(byte x, byte y) {
            int result = x + y;
            if (((x ^ result) & (y ^ result)) < 0) {
                throw new ArithmeticException("byte overflow");
            }
            return (byte)result;
        }

        public byte subtract(byte x, byte y) {
            int result = x - y;
            if (((x ^ result) & (y ^ result)) < 0) {
                throw new ArithmeticException("byte overflow");
            }
            return (byte)result;
        }

        public byte multiply(byte x, byte y) {
            int result = x * y;
            if ((byte)result != result) {
                throw new ArithmeticException("byte overflow");
            }
            return (byte)result;
        }
    }

    public static class ArithmeticShort {
        public short add(short x, short y) {
            int result = x + y;
            if (((x ^ result) & (y ^ result)) < 0) {
                throw new ArithmeticException("short overflow");
            }
            return (short)result;
        }

        public short subtract(short x, short y) {
            int result = x - y;
            if (((x ^ result) & (y ^ result)) < 0) {
                throw new ArithmeticException("short overflow");
            }
            return (short)result;
        }

        public short multiply(short x, short y) {
            int result = x * y;
            if ((short)result != result) {
                throw new ArithmeticException("short overflow");
            }
            return (short)result;
        }
    }

    public static class ArithmeticInt {
        public int add(int x, int y) {
            return Math.addExact(x, y);
        }

        public int subtract(int x, int y) {
            return Math.subtractExact(x, y);
        }

        public int multiply(int x, int y) {
            return Math.multiplyExact(x, y);
        }
    }

    public static class ArithmeticLong {
        public long add(long x, long y) {
            return Math.addExact(x, y);
        }

        public long subtract(long x, long y) {
            return Math.subtractExact(x, y);
        }

        public long multiply(long x, long y) {
            return Math.multiplyExact(x, y);
        }
    }

    public static class ArithmeticFloat {
        public float add(float x, float y) {
            return x + y;
        }

        public float subtract(float x, float y) {
            return x - y;
        }

        public float multiply(float x, float y) {
            return x * y;
        }
    }

    public static class ArithmeticDouble {
        public double add(double x, double y) {
            return x + y;
        }

        public double subtract(double x, double y) {
            return x - y;
        }

        public double multiply(double x, double y) {
            return x * y;
        }
    }
}
