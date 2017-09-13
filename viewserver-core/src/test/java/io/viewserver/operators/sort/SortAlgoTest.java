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

import io.viewserver.schema.column.*;
import io.viewserver.schema.column.chunked.ChunkedColumnBool;
import io.viewserver.schema.column.chunked.ChunkedColumnInt;
import io.viewserver.schema.column.chunked.ChunkedColumnLong;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

/**
 * Created by nickc on 15/10/2014.
 */
public class SortAlgoTest {
    @Test
    public void testBurstsort() throws Exception {
        test(new SortAlgo() {
            @Override
            public void run(String[] strings) throws Exception {
                Burstsort.sort(strings);
            }
        });
    }

    @Test
    public void testBurstsortParallel() throws Exception {
        test(new SortAlgo() {
            @Override
            public void run(String[] strings) throws Exception {
                Burstsort.sortThreadPool(strings);
            }
        });
    }

    @Test
    public void testQuicksort() throws Exception {
        test(new SortAlgo() {
            @Override
            public void run(String[] strings) throws Exception {
                Quicksort.sort(strings);
            }
        });
    }

    @Test
    public void testMultikeyQuicksort() throws Exception {
        test(new SortAlgo() {
            @Override
            public void run(String[] strings) throws Exception {
                MultikeyQuicksort.sort(strings);
            }
        });
    }

    @Test
    public void testRadixIntegerSort() throws Exception {
        int[] ints = new int[1500000];
        Random random = new Random(new Date().getTime());
        for (int i = 0; i < 1500000; i++) {
            ints[i] = random.nextInt(1000000);
        }
        long start = System.nanoTime();
        RadixSort.sort(ints);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testRadixIntegerSort2() throws Exception {
        int[] ints = new int[1500000];
        Random random = new Random(new Date().getTime());
        for (int i = 0; i < 1500000; i++) {
            ints[i] = random.nextInt(1000000);
        }
        long start = System.nanoTime();
        int[] sorted = RadixSort.sort2(ints);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testColumnQuickSortInt() throws Exception {
        ColumnHolderInt columnHolder = (ColumnHolderInt) ColumnHolderUtils.createColumnHolder("test", ColumnType.Int);
        ChunkedColumnInt column = new ChunkedColumnInt(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {
            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024);
        columnHolder.setColumn(column);
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[1500000];
        for (int i = 0; i < 1500000; i++) {
            rowIds[i] = i;
            column.setInt(i, random.nextInt(10000));
        }
        long start = System.nanoTime();
        ColumnQuicksort.quicksort(rowIds, 0, 1500000 - 1, new ComparerInt(column, false));
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testColumnQuickSortLong() throws Exception {
        ColumnHolderLong columnHolder = (ColumnHolderLong) ColumnHolderUtils.createColumnHolder("test", ColumnType.Long);
        ChunkedColumnLong column = new ChunkedColumnLong(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {
            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024);
        columnHolder.setColumn(column);
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[1500000];
        for (int i = 0; i < 1500000; i++) {
            rowIds[i] = i;
            column.setLong(i, random.nextLong());
        }
        long start = System.nanoTime();
        ColumnQuicksort.quicksort(rowIds, 0, 1500000 - 1, new ComparerLong(column, false));
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testColumnRadixIntegerSort() throws Exception {
        ColumnHolderInt columnHolder = (ColumnHolderInt) ColumnHolderUtils.createColumnHolder("test", ColumnType.Int);
        ChunkedColumnInt column = new ChunkedColumnInt(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {
            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024);
        columnHolder.setColumn(column);
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[1500000];
        for (int i = 0; i < 1500000; i++) {
            rowIds[i] = i;
            column.setInt(i, random.nextInt(10000));
        }
        long start = System.nanoTime();
        ColumnRadixSort.sort(rowIds, columnHolder);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testColumnRadixIntegerSort2() throws Exception {
        ColumnHolderInt columnHolder = (ColumnHolderInt) ColumnHolderUtils.createColumnHolder("test", ColumnType.Int);
        ChunkedColumnInt column = new ChunkedColumnInt(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {
            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024);
        columnHolder.setColumn(column);
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[1500000];
        for (int i = 0; i < 1500000; i++) {
            rowIds[i] = i;
            column.setInt(i, random.nextInt(10000));
        }
        long start = System.nanoTime();
        ColumnRadixSort.sort2(rowIds, columnHolder, false);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testColumnRadixIntegerSort2Desc() throws Exception {
        ColumnHolderInt columnHolder = (ColumnHolderInt) ColumnHolderUtils.createColumnHolder("test", ColumnType.Int);
        ChunkedColumnInt column = new ChunkedColumnInt(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {
            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024);
        columnHolder.setColumn(column);
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[1000];
        for (int i = 0; i < 1000; i++) {
            rowIds[i] = i;
            column.setInt(i, random.nextInt(10000));
        }
        long start = System.nanoTime();
        rowIds = ColumnRadixSort.sort2(rowIds, columnHolder, true);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
        for (int i = 0; i < 1000; i++) {
            System.out.println(columnHolder.getInt(rowIds[i]));
        }
    }

    @Test
    public void testColumnRadixLongSort() throws Exception {
        ColumnHolderLong columnHolder = (ColumnHolderLong) ColumnHolderUtils.createColumnHolder("test", ColumnType.Long);
        ChunkedColumnLong column = new ChunkedColumnLong(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {
            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024);
        columnHolder.setColumn(column);
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[1500000];
        for (int i = 0; i < 1500000; i++) {
            rowIds[i] = i;
            column.setLong(i, random.nextLong());
        }
        long start = System.nanoTime();
        ColumnRadixSort.sort(rowIds, columnHolder);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testColumnRadixLongSort2() throws Exception {
        ColumnHolderLong columnHolder = (ColumnHolderLong) ColumnHolderUtils.createColumnHolder("test", ColumnType.Long);
        ChunkedColumnLong column = new ChunkedColumnLong(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {
            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024);
        columnHolder.setColumn(column);
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[1500000];
        for (int i = 0; i < 1500000; i++) {
            rowIds[i] = i;
            column.setLong(i, random.nextLong());
        }
        long start = System.nanoTime();
        ColumnRadixSort.sort2(rowIds, columnHolder);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testColumnBooleanSort() throws Exception {
        ColumnHolderBool columnHolder = (ColumnHolderBool) ColumnHolderUtils.createColumnHolder("test", ColumnType.Bool);
        ChunkedColumnBool column = new ChunkedColumnBool(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {
            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024);
        columnHolder.setColumn(column);
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[1500000];
        for (int i = 0; i < 1500000; i++) {
            rowIds[i] = i;
            column.setBool(i, random.nextBoolean());
        }
        long start = System.nanoTime();
        ColumnBooleanSort.sort(rowIds, columnHolder, false);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    @Test
    public void testMultiColumnSort() throws Exception {
        ColumnHolderInt[] holders = new ColumnHolderInt[2];
        for (int i = 0; i < 2; i++) {
            holders[i] = (ColumnHolderInt) ColumnHolderUtils.createColumnHolder("test", ColumnType.Int);
            ChunkedColumnInt column = new ChunkedColumnInt(holders[i], new IColumnWatcher() {
                @Override
                public void markDirty(int rowId, int columnId) {
                }

                @Override
                public boolean isDirty(int rowId, int columnId) {
                    return false;
                }

                @Override
                public void markColumnDirty(int columnId) {

                }
            }, null, 1024, 1024);
            holders[i].setColumn(column);
        }

        int size = 10000;
        Random random = new Random(new Date().getTime());
        int[] rowIds = new int[size];
        for (int i = 0; i < size; i++) {
            rowIds[i] = i;
            ((IWritableColumnInt)holders[0].getColumn()).setInt(i, random.nextInt(10000));
            ((IWritableColumnInt)holders[1].getColumn()).setInt(i, random.nextInt(10000));
        }
        long start = System.nanoTime();
        ColumnRadixSort.sort2(rowIds, holders[1], false);
        ColumnRadixSort.sort2(rowIds, holders[0], false);
        long elapsed = System.nanoTime() - start;
        for (int i = 0; i < size; i++) {
            System.out.println(i + " - " + rowIds[i] + " - " + holders[0].getInt(rowIds[i]) + ", " + holders[1].getInt(rowIds[i]));
        }
        System.out.println("Took " + elapsed / 1000000f + "ms");
    }

    private void test(SortAlgo sortAlgo) throws Exception {
        String[] strings = new String[1500000];
        Random random = new Random(new Date().getTime());
        for (int i = 0; i < 1500000; i++) {
            strings[i] = ((Integer)random.nextInt(10000)).toString();
        }

        long start = System.nanoTime();
        sortAlgo.run(strings);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000f + "ms");
    }

    private interface SortAlgo {
        void run(String[] strings) throws Exception;
    }
}
