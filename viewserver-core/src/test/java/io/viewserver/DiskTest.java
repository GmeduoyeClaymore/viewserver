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

import io.viewserver.operators.sort.RadixSort;
import org.junit.Test;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Random;

/**
 * Created by nickc on 01/12/2014.
 */
public class DiskTest extends BenchmarkTestBase {
    @Test
    public void test() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                Path path = Paths.get("c:\\temp\\test.bin");
                try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    int bufStart = -1, bufEnd = -1;
                    ByteBuffer buffer = ByteBuffer.allocate(8192);
                    Random random = new Random(new Date().getTime());
                    benchmarks.startBenchmark("writing");
                    for (int i = 0; i < 1500000; i++) {
                        if (bufStart == -1 || (i < bufStart || i > bufEnd)) {
                            fc.write(buffer);
                            buffer.clear();
                            bufStart = i;
                            bufEnd = i + 8192 / 4 - 1;
                        }
                        buffer.putInt((i - bufStart) * 4, random.nextInt());
                    }
                    buffer.flip();
                    if (buffer.hasRemaining()) {
                        fc.write(buffer);
                    }
                    benchmarks.stopBenchmark("writing");

                    int[] ids = new int[100000];
                    for (int i = 0; i < 100000; i++) {
                        ids[i] = random.nextInt(1500000);
                    }
                    ids = RadixSort.sort(ids);

                    int[] ints = new int[100000];
                    buffer = ByteBuffer.allocate(8192);
                    benchmarks.startBenchmark("reading");
                    for (int i = 0; i < 100000; i++) {
                        if (ids[i] < bufStart || ids[i] > bufEnd) {
                            buffer.position(0);
                            int read = fc.read(buffer, ids[i] << 2);
                            bufStart = ids[i];
                            bufEnd = ids[i] + read / 4 - 1;
                        }
                        ints[i] = buffer.getInt((ids[i] - bufStart) * 4);
                    }
                    benchmarks.stopBenchmark("reading");
                    Thread.sleep(1);
                }
            }
        });
    }

    @Test
    public void test2() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(Benchmarks benchmarks) throws Exception {
                Path path = Paths.get("c:\\temp\\test.bin");
                try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
                    Random random = new Random(new Date().getTime());
                    benchmarks.startBenchmark("writing");
                    for (int i = 0; i < 1500000; i++) {
                        raf.writeInt(random.nextInt());
                    }
                    benchmarks.stopBenchmark("writing");

                    int[] ids = new int[100000];
                    for (int i = 0; i < 100000; i++) {
                        ids[i] = random.nextInt(1500000);
                    }

                    int[] ints = new int[100000];
                    benchmarks.startBenchmark("reading");
                    for (int i = 0; i < 100000; i++) {
                        raf.seek(ids[i] * 4);
                        ints[i] = raf.readInt();
                    }
                    benchmarks.stopBenchmark("reading");
                    Thread.sleep(1);
                }
            }
        });
    }
}
