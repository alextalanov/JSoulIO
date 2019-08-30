package com.gmail.wristylotus;

@FunctionalInterface
public interface CancelableTask<T> {
    IO<Unit> apply(CancelableContextIO<T> context);
}