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

import io.viewserver.schema.column.ColumnHolderBool;

/**
 * Created by bemm on 16/10/2014.
 */
public class ColumnBooleanSort {
    public static void sort(int[] rowIds, ColumnHolderBool columnHolder, boolean descending) {
        int[] falses = new int[rowIds.length];
        int[] trues = new int[rowIds.length];
        int trueCount = 0, falseCount = 0;
        for (int i = 0; i < rowIds.length; i++) {
            if (columnHolder.getBool(rowIds[i])) {
                trues[trueCount++] = rowIds[i];
            } else {
                falses[falseCount++] = rowIds[i];
            }
        }

        if (descending) {
            System.arraycopy(trues, 0, rowIds, 0, trueCount);
            System.arraycopy(falses, 0, rowIds, trueCount, falseCount);
        } else {
            System.arraycopy(falses, 0, rowIds, 0, falseCount);
            System.arraycopy(trues, 0, rowIds, falseCount, trueCount);
        }
    }
}
