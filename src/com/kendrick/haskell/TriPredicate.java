package com.kendrick.haskell;

@FunctionalInterface // Source: https://www.javatips.net/api/Flaxbeards-Steam-Power-master/Esteemed-Innovation-1.10/src/api/java/eiteam/esteemedinnovation/api/util/TriPredicate.java
public interface TriPredicate<A, B, C> {
    boolean test(A p1, B p2, C p3);
}
