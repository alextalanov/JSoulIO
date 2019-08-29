package com.gmail.wristylotus;

import java.util.concurrent.Callable;

public class JSoulIO {

    public static <T> Callable<CancelableIO<T>> cancelable(CancelableTask<T> cancalebleTask) {
        return () -> new CancelableIO<>(cancalebleTask);
    }

    public static Tuple<CancelableIO, CancelableIO> race(CancelableIO left, CancelableIO right) {
        while (true) {
            if (left.isDone() || left.isCanceled()) {
                right.syncCancel();
                return Tuple.of(left, right);
            } else if (right.isDone() || right.isCanceled()) {
                left.syncCancel();
                return Tuple.of(right, left);
            }
        }
    }

    public static <T> T convertToUnchecked(Callable<T> callable) {
        try {
            return callable.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
