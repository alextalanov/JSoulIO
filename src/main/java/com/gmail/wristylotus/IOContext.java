package com.gmail.wristylotus;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public final class IOContext<T> {

    private final AtomicReference<T> value = new AtomicReference<>();

    private final ExecutorService executor = Executors.newCachedThreadPool();
    protected volatile boolean isDone = false;
    private final Cancel cancel;
    private final ConcurrentLinkedQueue<Future<?>> taskQueue = new ConcurrentLinkedQueue<>();

    protected IOContext(Cancel cancel) {
        this.cancel = cancel;
    }

    public void blocking(Task task) {
        final Future<?> futureTask = executor.submit(() -> {
            task.apply();
            try {
                cancel.apply();
            } catch (Exception ex) {
                throw new CancellationException(ex);
            }
            isDone = true;
        });
        taskQueue.add(futureTask);
    }

    protected Iterator<Future<?>> tasks() {
        return taskQueue.iterator();
    }

    public void result(T value) {
        this.value.set(value);
    }

    protected T value() {
        return this.value.get();
    }

    protected void destroy(){
        executor.shutdown();
    }

}