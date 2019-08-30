package com.gmail.wristylotus;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class AsyncIO<T> {

    private final AsyncContextIO<T> context = new AsyncContextIO<>();

    private final Future<?> future;

    public AsyncIO(AsyncTask<T> task) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        this.future = executor.submit(() -> task.apply(context));
        executor.shutdown();
    }

    public IO<Unit> join() {
        return IO.of(() -> IO.convertToUnchecked((Procedure) future::get));
    }

    public Optional<T> syncVal() {
        return Optional.ofNullable(context.value());
    }

    public synchronized void asyncVal(Consumer<Optional<T>> consumer) {
        new Thread(() -> {
            join();
            consumer.accept(syncVal());
        }).start();
    }
}
