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

import io.viewserver.core.ConsistentHash;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TLongProcedure;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by nickc on 22/11/2014.
 */
public class ConsistentHashTest extends BenchmarkTestBase {
    @Test
    public void testConsistentHashAddBucket() throws Exception {
        final int rows = 1500000;

        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                HashFunction hashFunction = Hashing.murmur3_32();
                Charset charset = Charset.forName("utf8");
                ConsistentHash consistentHash = new ConsistentHash(500, Arrays.asList(0, 1, 2, 3, 4));

                long[] keys = new long[rows];
                for (int i = 0; i < rows; i++) {
                    keys[i] = hashFunction.hashString(UUID.randomUUID().toString(), charset).padToLong();
                }

                List<TLongArrayList> shards = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    shards.add(new TLongArrayList());
                }

                for (int i = 0; i < rows; i++) {
                    int shardId = consistentHash.getBinFor(keys[i]);
                    shards.get(shardId).add(keys[i]);
                }

                printBuckets(shards);

                int oldBuckets = shards.size();
                for (TLongArrayList shard : shards) {
                    shard.clear();
                }
                shards.add(new TLongArrayList());

                ConsistentHash newConsistentHash = new ConsistentHash(500, Arrays.asList(0, 1, 2, 3, 4, 5));

                int moves = 0;
                benchmarks.startBenchmark("rehash");
                for (int i = 0; i < rows; i++) {
                    int oldShardId = consistentHash.getBinFor(keys[i]);
                    int newShardId = newConsistentHash.getBinFor(keys[i]);
                    if (oldShardId != newShardId) {
                        moves++;
                    }
                    shards.get(newShardId).add(keys[i]);
                }
                benchmarks.stopBenchmark("rehash");

                System.out.println(moves + " keys were moved");

                printBuckets(shards);
            }
        });
    }

    @Test
    public void testConsistentHashRemoveBucket() throws Exception {
        final int rows = 1500000;

        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                HashFunction hashFunction = Hashing.murmur3_32();
                Charset charset = Charset.forName("utf8");
                ConsistentHash consistentHash = new ConsistentHash(500, Arrays.asList(0, 1, 2, 3, 4));

                long[] keys = new long[rows];
                for (int i = 0; i < rows; i++) {
                    keys[i] = hashFunction.hashString(UUID.randomUUID().toString(), charset).padToLong();
                }

                TIntObjectHashMap<TLongArrayList> shards = new TIntObjectHashMap<>();
                for (int i = 0; i < 5; i++) {
                    shards.put(i, new TLongArrayList());
                }

                for (int i = 0; i < rows; i++) {
                    int shardId = consistentHash.getBinFor(keys[i]);
                    shards.get(shardId).add(keys[i]);
                }

                printBuckets(shards);

                TLongArrayList shardToRemove = shards.remove(3);

                consistentHash.removeBin(3);

                final int[] moves = new int[]{0};
                benchmarks.startBenchmark("rehash");
                shardToRemove.forEach(new TLongProcedure() {
                    @Override
                    public boolean execute(long value) {
                        int newShardId = consistentHash.getBinFor(value);
                        shards.get(newShardId).add(value);
                        moves[0]++;
                        return true;
                    }
                });
                benchmarks.stopBenchmark("rehash");

                System.out.println(moves[0] + " keys were moved");

                printBuckets(shards);
            }
        });
    }

    private void printBuckets(TIntObjectHashMap<TLongArrayList> buckets) {
        buckets.forEachEntry(new TIntObjectProcedure<TLongArrayList>() {
            @Override
            public boolean execute(int a, TLongArrayList b) {
                System.out.println("Bucket " + a + " - " + b.size());
                return true;
            }
        });
    }

    @Test
    public void testJumpConsistentHashAddBucket() throws Exception {
        final int rows = 1500000;

        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                HashFunction hashFunction = Hashing.murmur3_32();
                Charset charset = Charset.forName("utf8");

                long[] keys = new long[rows];
                for (int i = 0; i < rows; i++) {
                    keys[i] = hashFunction.hashString(UUID.randomUUID().toString(), charset).padToLong();
                }

                List<TLongArrayList> shards = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    shards.add(new TLongArrayList());
                }

                for (int i = 0; i < rows; i++) {
                    int shardId = Hashing.consistentHash(keys[i], shards.size());
                    shards.get(shardId).add(keys[i]);
                }

                printBuckets(shards);

                int oldBuckets = shards.size();
                for (TLongArrayList shard : shards) {
                    shard.clear();
                }
                shards.add(new TLongArrayList());

                int moves = 0;
                benchmarks.startBenchmark("rehash");
                for (int i = 0; i < rows; i++) {
                    int oldShardId = Hashing.consistentHash(keys[i], oldBuckets);
                    int newShardId = Hashing.consistentHash(keys[i], shards.size());
                    if (oldShardId != newShardId) {
                        moves++;
                    }
                    shards.get(newShardId).add(keys[i]);
                }
                benchmarks.stopBenchmark("rehash");

                System.out.println(moves + " keys were moved");

                printBuckets(shards);
            }
        });
    }

    @Test
    public void testJumpConsistentHashRemoveBucket() throws Exception {
        final int rows = 1500;

        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                HashFunction hashFunction = Hashing.murmur3_32();
                Charset charset = Charset.forName("utf8");

                long[] keys = new long[rows];
                for (int i = 0; i < rows; i++) {
                    keys[i] = hashFunction.hashString(UUID.randomUUID().toString(), charset).padToLong();
                }

                List<TLongArrayList> shards = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    shards.add(new TLongArrayList());
                }

                for (int i = 0; i < rows; i++) {
                    int shardId = Hashing.consistentHash(keys[i], shards.size());
                    System.out.println(keys[i] + " -> " + shardId);
                    shards.get(shardId).add(keys[i]);
                }

                printBuckets(shards);

                TLongArrayList shardToRemove = shards.remove(2);

                final int[] moves = new int[]{0};
                benchmarks.startBenchmark("rehash");
                shardToRemove.forEach(new TLongProcedure() {
                    @Override
                    public boolean execute(long value) {
                        int newShardId = Hashing.consistentHash(value, shards.size());
                        System.out.println(value + " -> " + newShardId);
                        shards.get(newShardId).add(value);
                        moves[0]++;
                        return true;
                    }
                });
                benchmarks.stopBenchmark("rehash");

                System.out.println(moves[0] + " keys were moved");

                printBuckets(shards);
            }
        }, 1);
    }

    @Test
    public void testJumpConsistentHashRemoveBucket2() throws Exception {
        final int rows = 1500000;

        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                HashFunction hashFunction = Hashing.murmur3_32();
                Charset charset = Charset.forName("utf8");

                long[] keys = new long[rows];
                for (int i = 0; i < rows; i++) {
                    keys[i] = hashFunction.hashString(UUID.randomUUID().toString(), charset).padToLong();
                }

                List<TLongArrayList> shards = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    shards.add(new TLongArrayList());
                }

                for (int i = 0; i < rows; i++) {
                    int shardId = Hashing.consistentHash(keys[i], shards.size());
                    shards.get(shardId).add(keys[i]);
                }

                printBuckets(shards);

                int oldBuckets = shards.size();
                shards.remove(2);
                for (TLongArrayList shard : shards) {
                    shard.clear();
                }

                int moves = 0;
                benchmarks.startBenchmark("rehash");
                for (int i = 0; i < rows; i++) {
                    int oldShardId = Hashing.consistentHash(keys[i], oldBuckets);
                    int newShardId = Hashing.consistentHash(keys[i], shards.size());
                    if (oldShardId != newShardId) {
                        moves++;
                    }
                    shards.get(newShardId).add(keys[i]);
                }
                benchmarks.stopBenchmark("rehash");

                System.out.println(moves + " keys were moved");

                printBuckets(shards);
            }
        });
    }

    private void printBuckets(List<TLongArrayList> buckets) {
        for (int i = 0; i < buckets.size(); i++) {
            System.out.println("Bucket " + i + " - " + buckets.get(i).size());
        }
    }

    @Test
    public void benchmarkJumpConsistentHash() throws Exception {
        final int rows = 1500000;

        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                HashFunction hashFunction = Hashing.murmur3_32();
                Charset charset = Charset.forName("utf8");

                String[] keys = new String[rows];
                for (int i = 0; i < rows; i++) {
                    keys[i] = UUID.randomUUID().toString();
                }

                long[] hashes = new long[rows];
                for (int i = 0; i < rows; i++) {
                    hashes[i] = hashFunction.hashString(keys[i], charset).padToLong();
                }

                benchmarks.startBenchmark("hash");
                for (int i = 0; i < rows; i++) {
                    Hashing.consistentHash(hashes[i], 5);
                }
                benchmarks.stopBenchmark("hash");
            }
        });
    }

    @Test
    public void benchmarkConsistentHash() throws Exception {
        final int rows = 1500000;

        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                HashFunction hashFunction = Hashing.murmur3_32();
                Charset charset = Charset.forName("utf8");

                String[] keys = new String[rows];
                for (int i = 0; i < rows; i++) {
                    keys[i] = UUID.randomUUID().toString();
                }

                long[] hashes = new long[rows];
                for (int i = 0; i < rows; i++) {
                    hashes[i] = hashFunction.hashString(keys[i], charset).padToLong();
                }

                ConsistentHash hash = new ConsistentHash(200, Arrays.asList(0, 1, 2, 3, 4));
                benchmarks.startBenchmark("hash");
                for (int i = 0; i < rows; i++) {
                    hash.getBinFor(hashes[i]);
                }
                benchmarks.stopBenchmark("hash");
            }
        });
    }

    @Test
    public void benchmarkMd5() throws Exception {
        benchmarkHashFunction(Hashing.md5());
    }

    @Test
    public void benchmarkMurmur32() throws Exception {
        benchmarkHashFunction(Hashing.murmur3_32());
    }

    private void benchmarkHashFunction(HashFunction hashFunction) throws Exception {
        final int rows = 1500000;

        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                Charset charset = Charset.forName("utf8");

                String[] keys = new String[rows];
                for (int i = 0; i < rows; i++) {
                    keys[i] = UUID.randomUUID().toString();
                }

                benchmarks.startBenchmark("hash");
                for (int i = 0; i < rows; i++) {
                    hashFunction.hashString(keys[i], charset);
                }
                benchmarks.stopBenchmark("hash");
            }
        }, 10);
    }
}
