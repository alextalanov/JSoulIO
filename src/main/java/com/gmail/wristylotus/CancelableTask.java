package com.gmail.wristylotus;

@FunctionalInterface
public interface CancelableTask<T> {
    Cancel apply(IOContext<T> context);
}