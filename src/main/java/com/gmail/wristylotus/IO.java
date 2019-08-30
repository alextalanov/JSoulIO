package com.gmail.wristylotus;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class IO<T> {

    private final Object task;

    private IO(Callable<T> task) {
        this.task = task;
    }

    private IO(Runnable task) {
        this.task = task;
    }

    public static <T> IO<T> of(Callable<T> task) {
        return new IO<>(task);
    }

    public static IO<Unit> of(Runnable task) {
        return new IO<>(task);
    }

    public static IO<Unit> unit() {
        return IO.of(() -> {});
    }

    public static IO<Unit> sleep(long time, TimeUnit timeUnit){
        return IO.of(() -> IO.convertToUnchecked(() -> timeUnit.sleep(time)));
    }

    public static <T> IO<T> pure(T value) {
        return IO.of(() -> value);
    }

    public T unsafeRun() {
        if (task instanceof Runnable) {
            ((Runnable) task).run();
            return null;
        } else {
            return convertToUnchecked((Callable<T>) task);
        }
    }

    public static <T> IO<AsyncIO<T>> async(AsyncTask<T> task) {
        return IO.of(() -> new AsyncIO<>(task));
    }

    public static <T> IO<CancelableIO<T>> cancelable(CancelableTask<T> cancelableTask) {
        return IO.of(() -> new CancelableIO<>(cancelableTask));
    }

    public static <T> Tuple<CancelableIO<T>, CancelableIO<T>> race(IO<CancelableIO<T>> left, IO<CancelableIO<T>> right) {
        final CancelableIO cancelableL = left.unsafeRun();
        final CancelableIO cancelableR = right.unsafeRun();
        while (true) {
            if (cancelableL.isDone() || cancelableL.isCanceled()) {
                cancelableR.syncCancel().unsafeRun();
                return Tuple.of(cancelableL, cancelableR);
            } else if (cancelableR.isDone() || cancelableR.isCanceled()) {
                cancelableL.syncCancel().unsafeRun();
                return Tuple.of(cancelableR, cancelableL);
            }
        }
    }

    public static <T> T convertToUnchecked(Callable<T> callable, Function<? super Exception, ? extends RuntimeException> handler) {
        try {
            return callable.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw handler.apply(ex);
        }
    }

    public static <T> T convertToUnchecked(Callable<T> callable) {
        return convertToUnchecked(callable, RuntimeException::new);
    }

    public static void convertToUnchecked(Procedure procedure, Function<? super Exception, ? extends RuntimeException> handler) {
        convertToUnchecked(() -> {
            procedure.call();
            return null;
        }, handler);
    }

    public static void convertToUnchecked(Procedure procedure) {
        convertToUnchecked(procedure, RuntimeException::new);
    }


}
