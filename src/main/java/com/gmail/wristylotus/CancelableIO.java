package com.gmail.wristylotus;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CancelableIO<T> {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final CancelableContextIO<T> context = new CancelableContextIO<>(syncCancel(), executor);

    private final IO<Unit> cancel;

    public CancelableIO(CancelableTask<T> task) {
        this.cancel = task.apply(context);
    }

    public synchronized IO<Unit> syncCancel() {
        return IO.of(() -> {
            if (nonCanceled()) {
                cancel.unsafeRun();
                executor.shutdown();
            }
        });

    }

    public synchronized IO<Unit> asyncCancel() {
        return asyncCancel(() -> {});
    }

    public boolean isDone() {
        return context.isDone;
    }

    public Optional<T> syncVal() {
        return Optional.ofNullable(context.value());
    }

    public synchronized void asyncVal(Consumer<Optional<T>> consumer) {
        if (nonCanceled()) {
            executor.execute(() -> {
                join();
                consumer.accept(syncVal());
            });
        } else {
            throw new IllegalStateException("Cannot get value from canceled task.");
        }
    }

    public IO<Unit> join() {
        return IO.of(() ->
                context.tasks().forEachRemaining(task ->
                        IO.convertToUnchecked((Callable<?>) task::get, IllegalStateException::new))
        );
    }

    public synchronized IO<Unit> asyncCancel(Runnable callback) {
        return IO.of(() -> {
            if (nonCanceled()) {
                executor.submit(() -> {
                    syncCancel().unsafeRun();
                    callback.run();
                });
            } else {
                throw new IllegalStateException("Task already has been canceled.");
            }
        });
    }

    public boolean isCanceled() {
        return executor.isShutdown();
    }

    public boolean nonCanceled() {
        return !isCanceled();
    }
}
