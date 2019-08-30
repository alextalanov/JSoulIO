package com.gmail.wristylotus;

@FunctionalInterface
public interface AsyncTask<T> {
    void apply(AsyncContextIO<T> context);
}