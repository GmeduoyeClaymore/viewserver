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

package io.viewserver.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by nick on 26/11/15.
 */
public class ReactorMonitor {
    public static final ReactorMonitor INSTANCE = new ReactorMonitor();
    private static final Logger log = LoggerFactory.getLogger(ReactorMonitor.class);
    private final List<ReactorStatus> monitoredReactors = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService executor;
    private final Semaphore lock = new Semaphore(0);
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");

    private ReactorMonitor() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "reactor-monitor"));
        executor.scheduleAtFixedRate(this::checkReactors, 30, 30, TimeUnit.SECONDS);
    }

    public void addReactor(IReactor reactor) {
        monitoredReactors.add(new ReactorStatus(reactor));
    }

    private void checkReactors() {
        final List<ReactorStatus> monitoredReactors = this.monitoredReactors;
        int size = monitoredReactors.size();
        for (int i = 0; i < size; i++) {
            final ReactorStatus reactorStatus = monitoredReactors.get(i);
            reactorStatus.dead = true;

            reactorStatus.reactor.scheduleTask(() -> {
                if (!reactorStatus.alive) {
                    log.info("Reactor {} came back!", reactorStatus.reactor.getName());
                }
                reactorStatus.alive = true;
                reactorStatus.dead = false;
                reactorStatus.lastSeen = System.currentTimeMillis();
                lock.release();
            }, 0, -1);
        }

        try {
            if (!lock.tryAcquire(size, 5000, TimeUnit.MILLISECONDS)) {
                for (int i = 0; i < size; i++) {
                    final ReactorStatus reactorStatus = monitoredReactors.get(i);
                    if (reactorStatus.dead && reactorStatus.alive) {
                        reactorStatus.alive = false;
                        log.warn("Reactor {} did not respond", reactorStatus.reactor.getName());
                    }
                }
                log.warn("Threads dumped to {}", lock.availablePermits(), dumpThreads());
            }
        } catch (InterruptedException e) {
            log.error("Reactor checking was interrupted", e);
        }
    }

    private String dumpThreads() {
        final StringBuilder builder = new StringBuilder();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), Integer.MAX_VALUE);
        int size = threadInfos.length;
        for (int i = 0; i < size; i++) {
            builder.append('"').append(threadInfos[i].getThreadName()).append('"').append(System.lineSeparator());
            final Thread.State state = threadInfos[i].getThreadState();
            builder.append("   java.lang.Thread.State: ").append(state).append(System.lineSeparator());
            final StackTraceElement[] stackTraceElements = threadInfos[i].getStackTrace();
            int stackTraceSize = stackTraceElements.length;
            for (int j = 0; j < stackTraceSize; j++) {
                builder.append("        at ").append(stackTraceElements[j]).append(System.lineSeparator());
            }
            builder.append(System.lineSeparator());
        }
        final String dumpText = builder.toString();
        final Path threadDumps = Paths.get("threadDumps");
        threadDumps.toFile().mkdirs();
        final Path threadDump = threadDumps.resolve(String.format("threadDump-%s", dateFormatter.format(new Date())));
        try (PrintStream stream = new PrintStream(threadDump.toFile())) {
            stream.print(dumpText);
        } catch (FileNotFoundException e) {
            log.warn("Could not write thread dump to file", e);
            log.warn("Thread dump:{}{}", System.lineSeparator(), dumpText);
        }
        return threadDump.toString();
    }

    private static class ReactorStatus {
        private final IReactor reactor;
        private boolean alive = true;
        private boolean dead;
        private long lastSeen;

        public ReactorStatus(IReactor reactor) {
            this.reactor = reactor;
        }
    }
}
