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

import com.googlecode.javaewah.EWAHCompressedBitmap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

/**
 * Created by bemm on 26/09/2014.
 */
public class EwahTest {
    /*
    Cardinality 10:
Populate column took 43757498ns
Column uses 6000016kb
Create index took 68227095ns
Index uses 3346896kb

    - sorted:
Index uses 744888b
Or took 4268407ns

Cardinality 100:
Populate column took 44659838ns
Column uses 6000016b
Create index took 183125388ns
Index uses 44278136b
Or took 16319794ns

    - sorted:
Index uses 771552b
Or took 7633105ns

Cardinality 1000:
Populate column took 42473280ns
Column uses 6000016b
Create index took 207866680ns
Index uses 55905568b
Or took 9327601ns

    - sorted:
Index uses -625360b
Or took 3676086ns

Cardinality 10000:
Populate column took 44438445ns
Column uses 6035704b
Create index took 442256404ns
Index uses 81429400b
Or took 7468856ns

    - sorted:
Index uses 820600b
Or took 5054062ns

    - sorted snapshot (80%) then unsorted (20%):
Index uses 11152480b
Or took 4061727ns

Cardinality 1500000: (unique)
Populate column took 46293083ns
Column uses 6000016b
Create index took 1190745490ns
Index uses 187826584b
Or took 4284832ns

    - sorted:
Index uses 214937360b
Or took 7185869ns
     */
    @Test
    public void test() {
        int count = 1500000;
        int cardinality = 10000;

        Runtime runtime = Runtime.getRuntime();

        long initial = runtime.totalMemory() - runtime.freeMemory();

        long start;

        int[] col = new int[count];
        Random random = new Random(new Date().getTime());
        start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            col[i] = i / (count / cardinality); // sorted
//            col[i] = random.nextInt(cardinality); // unsorted
            col[i] = (i / (float)count) > 0.8 ? random.nextInt(cardinality) : (i / (count / cardinality)); // sorted snapshot, then unsorted
            if (i % 100000 == 0) {
                System.out.println(i);
            }
        }

        System.out.println(String.format("Populate column took %dns", System.nanoTime() - start));
        long withdata = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Column uses " + (withdata - initial) + "b");

        TIntObjectHashMap<EWAHCompressedBitmap> bitmaps = new TIntObjectHashMap<>();

        start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            int v = col[i];
            EWAHCompressedBitmap map = bitmaps.get(v);
            if (map == null) {
                map = EWAHCompressedBitmap.bitmapOf(i);
                bitmaps.put(v, map);
            } else {
                map.set(i);
            }
        }
        System.out.println(String.format("Create index took %dns", System.nanoTime() - start));

        long withIndex = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Index uses " + (withIndex - withdata) + "b");

        start = System.nanoTime();
        EWAHCompressedBitmap or = EWAHCompressedBitmap.or(bitmaps.get(0), bitmaps.get(3), bitmaps.get(7));
        System.out.println(String.format("Or took %dns", System.nanoTime() - start));
        start = System.nanoTime();
        System.out.println(or.toArray().length);
        System.out.println("Retrieve bits from Or took " + (System.nanoTime() - start) + "ns");
    }
}
