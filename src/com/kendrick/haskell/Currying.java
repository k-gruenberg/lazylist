package com.kendrick.haskell;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A utility class for currying and uncurrying.
 * Currying is the process of transforming a function with multiple arguments into
 * a sequence of functions that take only one argument each.
 */
public final class Currying {

    private Currying() {} // Utility class

    /**
     *
     * Haskell analogue: curry :: ((a, b) -> c) -> a -> b -> c
     *
     * @param uncurriedFunction
     * @param value
     * @param <A>
     * @param <B>
     * @param <C>
     * @return
     */
    public <A,B,C> Function<B,C> curry(BiFunction<A,B,C> uncurriedFunction, A value) {
        return (b) -> uncurriedFunction.apply(value,b);
    }

    /**
     *
     * Haskell analogue: uncurry :: (a -> b -> c) -> (a, b) -> c
     *
     * @param curriedFunction
     * @param <A>
     * @param <B>
     * @param <C>
     * @return
     */
    public <A,B,C> BiFunction<A,B,C> uncurry(Function<A,Function<B,C>> curriedFunction) {
        return (a,b) -> curriedFunction.apply(a).apply(b);
    }

}
