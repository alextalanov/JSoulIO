package com.gmail.wristylotus;

import java.util.Optional;

public final class Tuple<L, R> {

    private final L left;
    private final R right;

    public Tuple(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Tuple<L, R> of(L left, R right) {
        return new Tuple<>(left, right);
    }

    public L left() {
        return left;
    }

    public R right() {
        return right;
    }

    public Optional<L> leftOpt() {
        return Optional.ofNullable(left);
    }

    public Optional<R> rightOpt() {
        return Optional.ofNullable(right);
    }
}
