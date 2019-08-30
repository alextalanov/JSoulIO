package com.gmail.wristylotus;

import java.util.concurrent.atomic.AtomicReference;

public final class AsyncContextIO<T> {

    private final AtomicReference<T> value = new AtomicReference<>();

    protected AsyncContextIO() {
    }

    public void result(T value) {
        this.value.set(value);
    }

    protected T value() {
        return this.value.get();
    }
}
