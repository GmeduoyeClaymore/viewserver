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

import io.viewserver.schema.column.ColumnHolderString;

/**
 * Simple implementation of insertion sort algorithm. Used by the
 * multikey quicksort implementation for small subarrays.
 *
 * @author Nathan Fiedler
 */
public class ColumnInsertionsort {

    private ColumnInsertionsort() {
    }

    /**
     * Sort the array of comparables. Uses a simple insertion sort
     * algorithm, so expect O(n^2) running time.
     *
     * @param  <T>   type of comparable to be sorted.
     * @param  arr   comparables to be sorted.
     */
    public static <T extends Comparable<? super T>> void sort(T[] arr) {
        if (arr != null) {
            sort(arr, 0, arr.length - 1);
        }
    }

    /**
     * Sort the array of comparables within the given range of elements.
     * Uses a simple insertion sort algorithm, so expect O(n^2) running
     * time.
     *
     * @param  <T>   type of comparable to be sorted.
     * @param  arr   comparables to be sorted.
     * @param  low   low end of range to sort (inclusive).
     * @param  high  high end of range to sort (inclusive).
     */
    public static <T extends Comparable<? super T>> void sort(T[] arr, int low, int high) {
        if (arr == null || arr.length < 2 || low < 0 || high <= low) {
            return;
        }

        for (int i = low + 1; i <= high; i++) {
            T pivot = arr[i];
            int j = i;
            while (j > low && pivot.compareTo(arr[j - 1]) < 0) {
                arr[j] = arr[j - 1];
                j--;
            }
            arr[j] = pivot;
        }
    }

    /**
     * Sort the strings in the array using an insertion sort, but only
     * consider the characters in the strings starting from the given
     * offset <em>depth</em>. That is, the method will ignore all characters
     * appearing before the <em>depth</em> character.
     *
     * @param  strings  array of strings to sort.
     * @param  low      low offset into the array (inclusive).
     * @param  high     high offset into the array (exclusive).
     * @param  depth    offset of first character in each string to compare.
     */
    public static void sort(int[] strings, ColumnHolderString columnHolder, int low, int high, int depth) {
        if (strings == null || low < 0 || high <= low || depth < 0) {
            return;
        }
        for (int i = low + 1; i < high; i++) {
            for (int j = i; j > low; j--) {
                int idx = depth;
                String stringJ = columnHolder.getString(strings[j]);
                String stringJMinus1 = columnHolder.getString(strings[j - 1]);
                char s = idx < stringJMinus1.length() ? stringJMinus1.charAt(idx) : 0;
                char t = idx < stringJ.length() ? stringJ.charAt(idx) : 0;
                while (s == t && idx < stringJMinus1.length()) {
                    idx++;
                    s = idx < stringJMinus1.length() ? stringJMinus1.charAt(idx) : 0;
                    t = idx < stringJ.length() ? stringJ.charAt(idx) : 0;
                }
                if (s <= t) {
                    break;
                }
                int tmp = strings[j];
                strings[j] = strings[j - 1];
                strings[j - 1] = tmp;
            }
        }
    }
}
