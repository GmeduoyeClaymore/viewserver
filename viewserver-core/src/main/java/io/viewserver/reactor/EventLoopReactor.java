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

import io.viewserver.messages.IMessage;
import io.viewserver.network.IChannel;
import io.viewserver.network.INetworkAdapter;
import io.viewserver.network.INetworkMessageWheel;
import io.viewserver.network.Network;
import com.google.common.util.concurrent.*;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.*;

/**
 * Created by bemm on 06/10/2014.
 */
public class EventLoopReactor implements IReactor, IReactorCommandListener, INetworkMessageListener {
    private static final Logger log = LoggerFactory.getLogger(EventLoopReactor.class);
    public static final byte CONTROL_REFRESH = 0;
    public static final byte CONTROL_WAKEUP = 1;
    public static final byte CONTROL_SHUTDOWN = 2;
    private static final int LOOP_FREQUENCY = 3000;
    private final PriorityBlockingQueue<LoopTask> loopTasks;
    private final PriorityBlockingQueue<LoopTask> loopTasksCopy;
    private final String name;
    private final Network network;
    private int nextJobId = 0;
    private final PriorityBlockingQueue<Job> jobQueue;
    private Thread runThread;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final ListeningScheduledExecutorService executor;
    private final ListeningExecutorService controllerExecutor;
    private final IReactorCommandWheel commandWheel;
    private int timeUntilNextLoop = LOOP_FREQUENCY;
    private ScheduledFuture<?> nextLoop;
    private boolean shuttingDown;

    public EventLoopReactor(String name, Network network) {
        this.name = name;
        this.network = network;

        ReactorMonitor.INSTANCE.addReactor(this);

        jobQueue = new PriorityBlockingQueue<>(8, getJobComparator());
        loopTasks = new PriorityBlockingQueue<>(8, getLoopTaskComparator());
        loopTasksCopy = new PriorityBlockingQueue<>(8, getLoopTaskComparator());

        executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor(r -> runThread = new Thread(r, "reactor-" + name)));
        controllerExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5,new NamedThreadFactory("controllers")));

        commandWheel = new SimpleReactorCommandWheel();
        commandWheel.registerReactorCommandListener(this);
        commandWheel.startRotating();

        network.setReactor(this);
        final INetworkAdapter networkAdapter = network.getNetworkAdapter();
        final INetworkMessageWheel networkMessageWheel = networkAdapter.getNetworkMessageWheel();
        networkMessageWheel.registerNetworkMessageListener(this);
        // TODO: get rid of this?
        networkAdapter.registerListener(this);
        networkMessageWheel.startRotating();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public IReactorCommandWheel getCommandWheel() {
        return commandWheel;
    }

    private Comparator<Job> getJobComparator() {
        return (o1, o2) -> {
            long time1 = o1.nextRunTime;
            long time2 = o2.nextRunTime;
            int id1 = o1.id;
            int id2 = o2.id;

            if (time2 < time1) {
                return 1;
            } else if (time2 > time1) {
                return -1;
            } else {
                return id2 < id1 ? -1 : 1;
            }
        };
    }

    private Comparator<LoopTask> getLoopTaskComparator() {
        return (o1, o2) -> {
            if (o2.priority > o1.priority) {
                return 1;
            } else if (o2.priority < o1.priority) {
                return -1;
            } else {
                return 0;
            }
        };
    }

    public void start() {
        network.getNetworkAdapter().start();

        scheduleLoop(timeUntilNextLoop, false);
    }

    private void scheduleLoop(long delay, boolean force) {
        if (shuttingDown) {
            return;
        }

        synchronized (this) {
            if (nextLoop != null) {
                if (nextLoop.getDelay(TimeUnit.MILLISECONDS) <= delay) {
                    return;
                }
                nextLoop.cancel(false);
                nextLoop = null;
            }
            nextLoop = executor.schedule(() -> {
                synchronized (EventLoopReactor.this) {
                    nextLoop = null;
                }
                loop();
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void loop() {
        log.trace("Entered loop()");

        executeQueuedJobs();

        runLoopTasks();

        timeUntilNextLoop = LOOP_FREQUENCY;
        Job firstJob = jobQueue.peek();
        if (firstJob != null && firstJob.nextRunTime < System.currentTimeMillis() + timeUntilNextLoop) {
            timeUntilNextLoop = (int) (firstJob.nextRunTime - System.currentTimeMillis());
            timeUntilNextLoop = timeUntilNextLoop < 0 ? 0 : timeUntilNextLoop;
        }

        log.trace("Scheduling next loop in {} millis",timeUntilNextLoop);

        scheduleLoop(timeUntilNextLoop, true);

        log.trace("Exited loop()");
    }

    private void executeQueuedJobs() {
        long jobQueueStart = System.currentTimeMillis();
        while (true) {
            Job job = jobQueue.peek();
            if (job == null || job.nextRunTime > System.currentTimeMillis()) {
                break;
            }

            job = jobQueue.poll();
            try {
                job.task.execute();
            } catch (Throwable ex) {
                log.error("Unhandled exception in scheduled task", ex);
            }
            if (job.frequency > 0) {
                job.nextRunTime = System.currentTimeMillis() + job.frequency;
                jobQueue.add(job);
            }

            // don't let queued jobs hog the reactor for too long!
            if (System.currentTimeMillis() - jobQueueStart > 2000) {
                break;
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("{} - Spent {}ms processing job queue", name, System.currentTimeMillis() - jobQueueStart);
        }
    }

    private void runLoopTasks() {
        loopTasksCopy.clear();
        loopTasksCopy.addAll(this.loopTasks);
        while (true) {
            LoopTask loopTask = loopTasksCopy.peek();
            if (loopTask == null) {
                break;
            }

            loopTask = loopTasksCopy.poll();
            try {
                loopTask.runnable.run();
            } catch (Throwable ex) {
                log.error("Unhandled exception in loop task", ex);
            }
        }
    }

    @Override
    public void addLoopTask(Runnable task) {
        addLoopTask(task, 0);
    }

    @Override
    public void addLoopTask(Runnable task, int priority) {
        loopTasks.add(new LoopTask(priority, task));
    }

    @Override
    public void removeLoopTask(Runnable task) {
        loopTasks.remove(new LoopTask(-1, task));
    }

    @Override
    public void wakeUp() {
        sendControl(CONTROL_WAKEUP);
    }

    @Override
    public void shutDown() {
        sendControl(CONTROL_SHUTDOWN);
    }

    @Override
    public void waitForShutdown() {
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
        }
    }

    private void sendControl(byte control) {
        commandWheel.pushToWheel(control);
    }

    @Override
    public void scheduleTask(ITask task, long delay, long frequency) {
        if (delay <= 0 && Thread.currentThread() == runThread) {
            try {
                task.execute();
            } catch (Throwable ex) {
                log.error("A problem was encountered while executing a scheduled task", ex);
            }
            return;
        }
        jobQueue.add(new Job(nextJobId++, task, System.currentTimeMillis() + delay, frequency));
        if (delay <= 0) {
            wakeUp();
        } else if (delay < (nextLoop != null ? nextLoop.getDelay(TimeUnit.MILLISECONDS) : LOOP_FREQUENCY)) {
            scheduleLoop(delay, false);
        }
    }

    @Override
    public void onReactorCommand(ReactorCommand command) {
        byte control = command.getOpCode();
        switch (control) {
            case CONTROL_WAKEUP: {
                log.trace("{} - Received WAKEUP", name);
                scheduleLoop(0, false);
                break;
            }
            case CONTROL_SHUTDOWN: {
                log.info("{} - Received SHUTDOWN", name);
                shuttingDown = true;
//                if (nextLoop != null) {
//                    nextLoop.cancel(false);
//                    nextLoop = null;
//                }
                network.shutdown();
                executor.shutdownNow();
                try {
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        log.warn("Not all tasks shutdown cleanly");
                    }
                } catch (InterruptedException e) {
                }
                shutdownLatch.countDown();
                break;
            }
        }
    }

    @Override
    public void onConnection(IChannel channel) {
        final ListenableFuture<?> future = executor.submit(() -> network.handleConnect(channel));
        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
            }

            @Override
            public void onFailure(Throwable t) {
                log.error(String.format("Error handling connection on channel %s", channel), t);
            }
        });
    }

    @Override
    public void onDisconnection(IChannel channel) {
        final ListenableFuture<?> future = executor.submit(() -> network.handleDisconnect(channel));
        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
            }

            @Override
            public void onFailure(Throwable t) {
                log.error(String.format("Error handling disconnection on channel %s", channel), t);
            }
        });
    }

    @Override
    public void onNetworkMessage(IChannel channel, IMessage msg) {
        if(msg.getType().equals(IMessage.Type.Command) && "genericJSON".equals(msg.getCommand().getCommand())){
            controllerExecutor.submit(() -> network.waitForSession(channel).subscribe(c-> {
                network.receiveCommand(msg.getCommand(),c);
                EventLoopReactor.this.wakeUp();

            }, err -> {
                log.error("Failed to handle controller message:\n\r " + msg, err);
            }));
        }else{
            executor.submit(() -> network.waitForSession(channel).subscribe(
                    c-> {
                        if (network.receiveMessage(channel, msg)) {
                            log.trace("Waking up reactor because of - {}",msg);
                            EventLoopReactor.this.wakeUp();
                        }
                        msg.release();
                    },
                    err -> {
                log.error("Failed to handle controller message:\n\r " + msg, err);
            }));
        }
    }


    @Override
    public <V> void addCallback(ListenableFuture<V> future,
                                FutureCallback<? super V> callback) {
        Futures.addCallback(future, callback, executor);
    }

    private class Job {
        private final int id;
        private long nextRunTime;
        private long frequency;
        private final ITask task;

        private Job(int id, ITask task, long nextRunTime, long frequency) {
            this.id = id;
            this.nextRunTime = nextRunTime;
            this.task = task;
            this.frequency = frequency;
        }
    }

    private class LoopTask {
        private int priority;
        private Runnable runnable;

        private LoopTask(int priority, Runnable runnable) {
            this.priority = priority;
            this.runnable = runnable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LoopTask loopTask = (LoopTask) o;

            if (!runnable.equals(loopTask.runnable)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return runnable.hashCode();
        }
    }
}
