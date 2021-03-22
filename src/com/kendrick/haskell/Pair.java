package com.kendrick.haskell;

import java.util.Objects;

/**
 * An immutable 2-tuple (pair) of two values of possibly different types.
 *
 * Haskell analogue: (a,b)
 *
 * @param <A>
 * @param <B>
 */
public class Pair<A,B> {

    final private A first;
    final private B second;

    /**
     * Construct a new Pair.
     *
     * @param first
     * @param second
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Generic Pair Construction.
     *
     * @param first
     * @param second
     * @return
     */
    public static Pair<?,?> newPair(Object first, Object second) {
        return new Pair<>(first, second);
    }

    /**
     * Retrieve the first element of this Pair.
     *
     * Haskell analogue: fst :: (a, b) -> a
     *
     * @return the first element of this Pair.
     */
    public A fst() {
        return first;
    }

    /**
     * Retrieve the second element of this Pair.
     *
     * Haskell analogue: snd :: (a, b) -> b
     *
     * @return the second element of this Pair.
     */
    public B snd() {
        return second;
    }

    /**
     * Return this Pair with its first and second element swapped.
     *
     * Haskell analogue: Data.Tuple.swap :: (a, b) -> (b, a)
     *
     * @return this Pair swapped.
     */
    public Pair<B,A> swap() {
        return new Pair<>(this.second, this.first);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ',' + second + ')';
    }

}
