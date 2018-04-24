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

/**
 * Created by bemm on 13/10/2014.
 */
public class EqualsFunctions {
    public static boolean equals(boolean first, boolean second) {
        return first == second;
    }

    public static boolean equals(byte first, byte second) {
        return first == second;
    }

    public static boolean equals(short first, short second) {
        return first == second;
    }

    public static boolean equals(int first, int second) {
        return first == second;
    }

    public static boolean equals(long first, long second) {
        return first == second;
    }

    public static boolean equals(float first, float second) {
        return first == second;
    }

    public static boolean equals(double first, double second) {
        return first == second;
    }

    public static boolean equals(Object first, Object second) {
        if (first == null) {
            return second == null;
        } else {
            return first.equals(second);
        }
    }
}
