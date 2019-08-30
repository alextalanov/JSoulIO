package com.gmail.wristylotus;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public final class CancelableContextIO<T> {

    private final AtomicReference<T> value = new AtomicReference<>();

    private final ExecutorService executor;
    protected volatile boolean isDone = false;
    private final IO cancel;
    private final ConcurrentLinkedQueue<Future<?>> taskQueue = new ConcurrentLinkedQueue<>();

    protected CancelableContextIO(IO<Unit> cancel, ExecutorService executor) {
        this.cancel = cancel;
        this.executor = executor;
    }

    public void blocking(Runnable task) {
        final Future<?> futureTask = executor.submit(() -> {
            task.run();
            cancel.unsafeRun();
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
}