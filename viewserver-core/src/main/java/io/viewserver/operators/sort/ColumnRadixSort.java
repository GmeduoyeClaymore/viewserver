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

import io.viewserver.schema.column.ColumnHolderInt;
import io.viewserver.schema.column.ColumnHolderLong;

/**
 * Created by bemm on 16/10/2014.
 */
public class ColumnRadixSort {
    public static int[] sort(int[] rowIds, ColumnHolderInt columnHolder) {
        // this sucks a bit, but pulling numbers out of the column in the loop below is SLOOOOOOOOOOW!
        int maxRowId = -1;
        for (int i = 0; i < rowIds.length; i++) {
            if (rowIds[i] > maxRowId) {
                maxRowId = rowIds[i];
            }
        }
        int[] numbers = new int[maxRowId + 1];
        for (int i = 0; i < rowIds.length; i++) {
            numbers[i] = columnHolder.getInt(i);
        }

        // Loop for every bit in the integers
        for (int shift = Integer.SIZE - 1; shift > -1; shift--) {
            // The array to put the partially sorted array into
            int length = rowIds.length;
            int[] tmp = new int[length];
            // The number of 0s
            int j = 0;

            // Move the 0s to the new array, and the 1s to the old one
            for (int i = 0; i < length; i++) {
                // If there is a 1 in the bit we are testing, the number will be negative
//                long now = System.nanoTime();
                boolean move = numbers[i] << shift >= 0;
//                nanos += System.nanoTime() - now;

                // If this is the last bit, negative numbers are actually lower
                if (shift == 0 ? !move : move) {
                    tmp[j] = rowIds[i];
                    j++;
                } else {
                    // It's a 1, so stick it in the old array for now
                    rowIds[i - j] = rowIds[i];
                }
            }

            // Copy over the 1s from the old array
            for (int i = j; i < tmp.length; i++) {
                tmp[i] = rowIds[i - j];
            }

            // And now the tmp array gets switched for another round of sorting
            rowIds = tmp;
        }

        return rowIds;
    }

    public static int[] sort(int[] rowIds, ColumnHolderLong columnHolder) {
        // this sucks a bit, but pulling numbers out of the column in the loop below is SLOOOOOOOOOOW!
        int maxRowId = -1;
        for (int i = 0; i < rowIds.length; i++) {
            if (rowIds[i] > maxRowId) {
                maxRowId = rowIds[i];
            }
        }
        long[] numbers = new long[maxRowId + 1];
        for (int i = 0; i < rowIds.length; i++) {
            numbers[i] = columnHolder.getLong(i);
        }

//        long nanos = 0;
        // Loop for every bit in the integers
        for (int shift = Long.SIZE - 1; shift > -1; shift--) {
            // The array to put the partially sorted array into
            int length = rowIds.length;
            int[] tmp = new int[length];
            // The number of 0s
            int j = 0;

            // Move the 0s to the new array, and the 1s to the old one
            for (int i = 0; i < length; i++) {
                // If there is a 1 in the bit we are testing, the number will be negative
//                long now = System.nanoTime();
                boolean move = numbers[i] << shift >= 0;
//                nanos += System.nanoTime() - now;

                // If this is the last bit, negative numbers are actually lower
                if (shift == 0 ? !move : move) {
                    tmp[j] = rowIds[i];
                    j++;
                } else {
                    // It's a 1, so stick it in the old array for now
                    rowIds[i - j] = rowIds[i];
                }
            }

            // Copy over the 1s from the old array
            for (int i = j; i < tmp.length; i++) {
                tmp[i] = rowIds[i - j];
            }

            // And now the tmp array gets switched for another round of sorting
            rowIds = tmp;
        }

//        System.out.println(nanos);
        return rowIds;
    }

    public static int[] sort2(int[] rowIds, ColumnHolderInt columnHolder, boolean descending) {
        // this sucks a bit, but pulling numbers out of the column in the loop below is SLOOOOOOOOOOW!
        int maxRowId = -1;
        for (int i = 0; i < rowIds.length; i++) {
            if (rowIds[i] > maxRowId) {
                maxRowId = rowIds[i];
            }
        }
        int[] numbers = new int[maxRowId + 1];
        for (int i = 0; i < rowIds.length; i++) {
            numbers[rowIds[i]] = columnHolder.getInt(rowIds[i]);
        }

        int[] sorted = new int[rowIds.length];
        for (int shift = 0; shift < 32; shift += 8) {
            int[] count = new int[256];
            for (int i = 0; i < rowIds.length; i++) {
                count[(numbers[rowIds[i]] >> shift) & 0xff]++;
            }
            int[] bucket = new int[256];
            if (descending) {
                for (int i = 255, j = 0; i >= 0; j += count[i--]) {
                    bucket[i] = j;
                }
            } else {
                for (int i = 0, j = 0; i < 256; j += count[i++]) {
                    bucket[i] = j;
                }
            }
            for (int i = 0; i < rowIds.length; i++) {
                sorted[bucket[(numbers[rowIds[i]] >> shift) &0xff]++] = rowIds[i];
            }
            int[] tmp = rowIds;
            rowIds = sorted;
            sorted = tmp;
        }
        return rowIds;
    }

    public static int[] sort2(int[] rowIds, ColumnHolderLong columnHolder) {
        // this sucks a bit, but pulling numbers out of the column in the loop below is SLOOOOOOOOOOW!
        int maxRowId = -1;
        for (int i = 0; i < rowIds.length; i++) {
            if (rowIds[i] > maxRowId) {
                maxRowId = rowIds[i];
            }
        }
        long[] numbers = new long[maxRowId + 1];
        for (int i = 0; i < rowIds.length; i++) {
            numbers[i] = columnHolder.getLong(i);
        }

        int[] sorted = new int[rowIds.length];
        for (int shift = 0; shift < 64; shift += 8) {
            int[] count = new int[256];
            for (int i = 0; i < rowIds.length; i++) {
                count[(int)((numbers[rowIds[i]] >> shift) & 0xff)]++;
            }
            int[] bucket = new int[256];
            for (int i = 0, j = 0; i < 256; j += count[i++]) {
                bucket[i] = j;
            }
            for (int i = 0; i < rowIds.length; i++) {
                sorted[bucket[(int)((numbers[rowIds[i]] >> shift) &0xff)]++] = rowIds[i];
            }
            int[] tmp = rowIds;
            rowIds = sorted;
            sorted = tmp;
        }
        return rowIds;
    }
}
