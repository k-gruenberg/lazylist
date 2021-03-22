package com.kendrick.haskell;

import java.util.LinkedList;
import java.util.List;

/**
 * This Utility Class provides the most important mathematical sequences as infinite LazyLists.
 * Examples: factorials(), powersOfTwo(), fibonacciNumbers(), primeNumbers(), etc...
 *
 * All functions returns LazyLists of Longs. When LazyLists of Integers are needed instead, simply use
 * .map(Long::intValue)
 * or
 * .map(Math::toIntExact)
 * to throw an ArithmeticException as soon as the Longs get too large to be stored in an Integer.
 *
 * e.g.: LazyList<Integer> fibs = LazyLists.fibonacciNumbers().map(Long::intValue);
 *
 * @author Kendrick Gruenberg
 */
public final class LazyLists {

    private LazyLists() {
        // Utility Class!
    }

    public static LazyList<Long> naturalNumbers() {
        return LazyList.from((long)0);
    }

    public static LazyList<Long> evenNumbers() {
        return LazyList.fromThen((long)0,(long)2);
    }

    public static LazyList<Long> oddNumbers() {
        return LazyList.fromThen((long)1,(long)3);
    }

    public static LazyList<Long> squareNumbers() {
        return LazyList.from((long)0).map((x) -> x*x);
    }

    public static LazyList<Long> cubeNumbers() {
        return LazyList.from((long)0).map((x) -> x*x*x);
    }

    public static LazyList<Long> factorials() {
        return new LazyList<Long>(
                new LinkedList<Long>(List.of((long)1)),
                (list) -> {
                    int size = list.size();
                    Long next = list.get(size-1) * size;
                    list.add(next); // !!!
                    return next;
                }
        );
    }

    public static LazyList<Long> powersOfTwo() {
        return new LazyList<Long>(
                new LinkedList<Long>(List.of((long)1)),
                (list) -> {
                    Long next = list.get(list.size()-1) * 2;
                    list.add(next); // !!!
                    return next;
                }
        );
    }

    public static LazyList<Long> fibonacciNumbers() {
        return new LazyList<Long>(
                new LinkedList<Long>(List.of((long)0,(long)1)),
                (list) -> {
                    int size = list.size();
                    Long sum = list.get(size-1) + list.get(size-2);
                    list.add(sum); // !!!
                    return sum;
                }
        );
    }

    public static LazyList<Long> lucasNumbers() {
        return new LazyList<Long>(
                new LinkedList<Long>(List.of((long)2,(long)1)),
                (list) -> {
                    int size = list.size();
                    Long sum = list.get(size-1) + list.get(size-2);
                    list.add(sum); // !!!
                    return sum;
                }
        );
    }

    public static LazyList<Long> primeNumbers() {
        return new LazyList<Long>(
                new LinkedList<Long>(List.of((long)2)),
                (list) -> {
                    Long currentLargestPrime = list.get(list.size()-1);
                    for (long next = currentLargestPrime+1; true; next++) {
                        final long _next = next;
                        if (list.stream().allMatch((x) -> _next % x != 0)) { // 'next' is prime --> next prime found!
                            list.add(next); // !!!
                            return next;
                        }
                    }
                }
        );
    }

}
