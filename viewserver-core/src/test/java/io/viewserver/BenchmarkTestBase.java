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

package io.viewserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 21/10/2014.
 */
public class BenchmarkTestBase {
    private int runs = 10;

    protected void benchmark(IBenchmarkRunner test) throws Exception {
        benchmark(test, runs);
    }

    protected void benchmark(IBenchmarkRunner test, int runs) throws Exception {
        benchmark(test, runs, false);
    }

    protected void benchmark(IBenchmarkRunner test, int runs, boolean includeFirstRun) throws Exception {
        Benchmarks benchmarks = new Benchmarks();
        if (runs == 1 || includeFirstRun) {
            benchmarks.firstRun = false;
        }

        for (int run = 0; run < runs; run++) {
            System.gc();
            System.gc();
            System.out.println("Starting run " + run);
            test.run(benchmarks);

            if (run > 0) {
                System.out.println("=============================================================");
                System.out.println("Run " + run);
                for (Benchmark benchmark : benchmarks.itemList) {
                    System.out.println(String.format("%-40s %3f", benchmark.description, (benchmark.last / 1000000f)));
                }
                System.out.println("=============================================================");
            }

            System.gc();
            System.gc();

            benchmarks.firstRun = false;
            benchmarks.run++;
        }

        System.out.println("=============================================================");
        for (Benchmark benchmark : benchmarks.itemList) {
            System.out.println(String.format("%-40s %3f", benchmark.description, (benchmark.total / 1000000f) / (float) ((runs == 1 || includeFirstRun) ? 1 : (runs - 1))));
        }
        System.out.println("=============================================================");
    }

    public interface IBenchmarkRunner {
        void run(Benchmarks benchmarks) throws Exception;
    }

    public class Benchmarks {
        private Map<String, Benchmark> items = new HashMap<>();
        private List<Benchmark> itemList = new ArrayList<>();
        private boolean firstRun = true;
        private int run = 0;

        public void startBenchmark(String description) {
            Benchmark benchmark = items.get(description);
            if (benchmark == null) {
                benchmark = new Benchmark(description);
                items.put(description, benchmark);
                itemList.add(benchmark);
            }
            System.out.println("Starting " + description);
            if (!firstRun) {
                benchmark.start();
            }
        }

        public void stopBenchmark(String description) {
            long time = System.nanoTime();
            if (!firstRun) {
                Benchmark benchmark = items.get(description);
                benchmark.stop(time);
            }
            System.out.println("Finished " + description);
        }

        public int getRun() {
            return run;
        }
    }

    private class Benchmark {
        private String description;
        private long start;
        private long last;
        private long total;

        public Benchmark(String description) {
            this.description = description;
        }

        public void start() {
            start = System.nanoTime();
        }

        public void stop(long time) {
            last = time - start;
            total += last;
        }
    }
}
