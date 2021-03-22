package com.kendrick.haskell;

@FunctionalInterface // Source: https://gist.github.com/andreluisdias/992b4df5131ea7e192392c3e477631b8
public interface TriFunction<A, B, C, R> {
    public R apply(A first, B second, C third);
}
