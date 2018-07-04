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

import io.viewserver.BenchmarkTestBase;
import com.google.common.collect.MinMaxPriorityQueue;
import org.junit.Test;

import java.util.Random;

/**
 * Created by bemm on 16/03/2015.
 */
public class HeapTest extends BenchmarkTestBase {
    @Test
    public void benchmarkBinaryHeap() throws Exception {
        benchmark((benchmarks) -> {
            BinaryHeap<Integer> heap = new BinaryHeap<>();

            Random random = new Random();
            benchmarks.startBenchmark("adding to heap");
            for (int i = 0; i < 1500000; i++) {
                heap.add(random.nextInt());
            }
            benchmarks.stopBenchmark("adding to heap");

            benchmarks.startBenchmark("getting min");
            System.out.println(heap.peek());
            benchmarks.stopBenchmark("getting min");
        });
    }

    @Test
    public void benchmarkMinMaxPriorityQueue() throws Exception {
        benchmark((benchmarks) -> {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.maximumSize(30).create();

            Random random = new Random();
            benchmarks.startBenchmark("adding to heap");
            for (int i = 0; i < 150000; i++) {
                queue.add(random.nextInt());
            }
            benchmarks.stopBenchmark("adding to heap");

            benchmarks.startBenchmark("getting min");
            System.out.println(queue.peek());
            benchmarks.stopBenchmark("getting min");
        });
    }
}
