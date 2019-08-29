package com.gmail.wristylotus;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CancelableIO<T> {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final IOContext<T> context = new IOContext<>(this::syncCancel);

    private final Cancel cancel;

    public CancelableIO(CancelableTask<T> cancel) {
        this.cancel = cancel.apply(context);
    }

    public synchronized void syncCancel() {
        if (nonCanceled()) {
            try {
                cancel.apply();
                executor.shutdown();
                context.destroy();
            } catch (Exception ex) {
                throw new CancellationException(ex);
            }
        }
    }

    public static Cancel unit() {
        return () -> {};
    }

    public synchronized void asyncCancel() {
        asyncCancel(() -> {});
    }

    public boolean isDone() {
        return context.isDone;
    }

    public Optional<T> syncVal() {
        return Optional.ofNullable(context.value());
    }

    public synchronized void asyncVal(Consumer<Optional<T>> consumer) {
        if(nonCanceled()) {
            executor.execute(() -> {
                join();
                consumer.accept(syncVal());
            });
        } else {
            throw new IllegalStateException("Cannot get value from canceled task.");
        }
    }

    public void join() {
        context.tasks().forEachRemaining(task -> {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new CancellationException(ex);
            }
        });
    }

    public synchronized void asyncCancel(Runnable callback) {
        if (nonCanceled()) {
            executor.submit(() -> {
                try {
                    syncCancel();
                    callback.run();
                } catch (Exception ex) {
                    throw new CancellationException(ex);
                }
            });
        } else {
            throw new IllegalStateException("Task already has been canceled.");
        }
    }

    public boolean isCanceled() {
        return executor.isShutdown();
    }

    public boolean nonCanceled() {
        return !isCanceled();
    }

    public interface Empty {}
}
