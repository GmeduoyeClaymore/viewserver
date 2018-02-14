package io.viewserver.operators;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.IReactorCommandWheel;
import io.viewserver.reactor.ITask;
import io.viewserver.reactor.SimpleReactorCommandWheel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TestReactor implements IReactor {
    Executor current = Executors.newSingleThreadExecutor();
    SimpleReactorCommandWheel wheel = new SimpleReactorCommandWheel();
    List<Runnable> loopTasks = new ArrayList<Runnable>();

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public Executor getExecutor() {
        return current;
    }

    @Override
    public IReactorCommandWheel getCommandWheel() {
        return wheel;
    }

    @Override
    public void addLoopTask(Runnable task) {
        loopTasks.add(task);
    }

    @Override
    public void addLoopTask(Runnable task, int priority) {
        loopTasks.add(task);
    }

    @Override
    public void removeLoopTask(Runnable task) {
        loopTasks.remove(task);
    }

    @Override
    public void wakeUp() {

    }

    @Override
    public void shutDown() {

    }

    @Override
    public void waitForShutdown() {

    }

    @Override
    public void scheduleTask(ITask task, long delay, long frequency) {

    }

    @Override
    public <V> void addCallback(ListenableFuture<V> future, FutureCallback<? super V> callback) {

    }

    @Override
    public void start() {

    }
}
