package com.kendrick.haskell;

@FunctionalInterface
public interface TriConsumer<A, B, C> {
    public void accept(A first, B second, C third);
}
