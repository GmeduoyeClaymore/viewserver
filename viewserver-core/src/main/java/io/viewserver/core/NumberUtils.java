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
 * Created by bemm on 06/10/2014.
 */
public class NumberUtils {
    public static long packLong(int hi, int lo) {
        return (((long)hi) << 32) | ((long)lo & 0x00000000ffffffffl);
    }

    public static int unpackLongHi(long val) {
        return (int)((val & 0xffffffff00000000l) >> 32);
    }

    public static int unpackLongLo(long val) {
        return (int)(val & 0x00000000ffffffffl);
    }
}
