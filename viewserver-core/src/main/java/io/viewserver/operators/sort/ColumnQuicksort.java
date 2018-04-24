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

/**
 * Created by bemm on 16/10/2014.
 */
public class ColumnQuicksort {
    public static void quicksort(int[] array, int low, int high, IComparer comparer) {
        int i = low, j = high;

        int pivotIndex = low + (high - low) / 2;
        comparer.setPivotValue(array[pivotIndex]);

        while (i <= j) {
            while (comparer.compare(array[i]) < 0) {
                i++;
            }

            while (comparer.compare(array[j]) > 0) {
                j--;
            }

            if (i <= j) {
                swap(array, i, j);
                i++;
                j--;
            }
        }

        if (low < j) {
            quicksort(array, low, j, comparer);
        }
        if (i < high) {
            quicksort(array, i, high, comparer);
        }
    }

    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
