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

package io.viewserver.operators.sort;

import java.util.Arrays;

/**
 * Created by bemm on 16/10/2014.
 */
public class RadixSort {
    public static int[] sort(int[] old) {
//        long nanos = 0;
        // Loop for every bit in the integers
        for (int shift = Integer.SIZE - 1; shift > -1; shift--) {
            // The array to put the partially sorted array into
            int[] tmp = new int[old.length];
            // The number of 0s
            int j = 0;

            // Move the 0s to the new array, and the 1s to the old one
            for (int i = 0; i < old.length; i++) {
                // If there is a 1 in the bit we are testing, the number will be negative
//                long now = System.nanoTime();
                boolean move = old[i] << shift >= 0;
//                nanos += System.nanoTime() - now;

                // If this is the last bit, negative numbers are actually lower
                if (shift == 0 ? !move : move) {
                    tmp[j] = old[i];
                    j++;
                } else {
                    // It's a 1, so stick it in the old array for now
                    old[i - j] = old[i];
                }
            }

            // Copy over the 1s from the old array
            for (int i = j; i < tmp.length; i++) {
                tmp[i] = old[i - j];
            }

            // And now the tmp array gets switched for another round of sorting
            old = tmp;
        }

//        System.out.println(nanos);
        return old;
    }

    private static ThreadLocal<ArrayHolder> arrayHolder = new ThreadLocal<ArrayHolder>() {
        @Override
        protected ArrayHolder initialValue() {
            return new ArrayHolder();
        }
    };

    public static int[] sort2(int[] old) {
        return sort2(old, old.length);
    }

    public static int[] sort2(int[] old, int length) {
        final ArrayHolder arrayHolder = RadixSort.arrayHolder.get();
        int[] sorted = arrayHolder.getSorted(length);
        for (int shift = 0; shift < 32; shift += 8) {
            int[] count = arrayHolder.getCount();
            for (int i = 0; i < length; i++) {
                count[(old[i] >> shift) & 0xff]++;
            }
            int[] bucket = arrayHolder.bucket;
            for (int i = 0, j = 0; i < 256; j += count[i++]) {
                bucket[i] = j;
            }
            for (int i = 0; i < length; i++) {
                sorted[bucket[(old[i] >> shift) &0xff]++] = old[i];
            }
            int[] tmp = old;
            old = sorted;
            sorted = tmp;
        }
        return old;
    }

    private static class ArrayHolder {
        private int[] sorted;
        private int[] count = new int[256];
        private int[] bucket = new int[256];

        public int[] getSorted(int size) {
            if (sorted == null || sorted.length < size) {
                int adjustedSize = Integer.highestOneBit(size);
                if (adjustedSize != size) {
                    adjustedSize <<= 1;
                }
                sorted = new int[adjustedSize];
            }
            return sorted;
        }

        public int[] getCount() {
            Arrays.fill(count, 0);
            return count;
        }
    }
}
