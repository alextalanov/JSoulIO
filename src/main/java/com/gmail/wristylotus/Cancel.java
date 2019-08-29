package com.gmail.wristylotus;

@FunctionalInterface
public interface Cancel {
    void apply() throws Exception;
}