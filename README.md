# LazyList
A Java implementation of a Haskell-like linked list with lazy evaluation. 
This means that elements of a LazyList are only computed/evaluated when they have to be and that LazyLists can therefore also be infinite.  

A LazyList is intended to work as similarly as lists in Haskell as possible. 
However, unlike lists in Haskell (which are immutable), a LazyList is mutable as it implements the java.util.List interface.

LazyLists are very similar to Streams from the java.util.stream package but there are some major differences:
1. Streams don't implement the List interface
2. Streams don't support many of the Haskell-borrowed functions like foldr(), scanr(), reverse(), product(), ...
3. LazyLists also provide some functions that neither Java Streams nor Haskell provide by default, e.g. lengthIsAtLeast().

There is also the LazyLists Utility Class that provides some useful LazyLists like primeNumbers(), fibonacciNumbers(), etc...

## Examples
...

## Functionality
    String toString();
    void print(java.io.PrintStream);
    LazyList<Character> show();
    String asString() throws UnsupportedOperationException;
    
    static <T> LazyList<T> view(Iterable<T> collection);
    static <T> LazyList<T> emptyList();
    static <T> LazyList<T> of(T... elements);
    static <T> LazyList<T> pure(T el);
    static <T> LazyList<T> repeat(T value);
    static <T> LazyList<T> replicate(int n, T value);
    static <T> LazyList<T> iterate(Function<T,T> function, T startValue);
    
    static <T> LazyList<T> comprehension1(Iterable<T> source, Predicate<T> predicate);
    static <S,T> LazyList<T> comprehension1(Function<S,T> mapping, Iterable<S> source, Predicate<S> predicate);
    static <X,Y,T> LazyList<T> comprehension2(BiFunction<X,Y,T> bifunction, Iterable<X> source1, Iterable<Y> source2);
    static <X,Y,T> LazyList<T> comprehension2(BiFunction<X,Y,T> bifunction, Iterable<X> source1, Iterable<Y> source2, BiPredicate<X,Y> bipredicate);
    static <X,Y,Z,T> LazyList<T> comprehension3(TriFunction<X,Y,Z,T> trifunction, Iterable<X> source1, Iterable<Y> source2, Iterable<Z> source3, TriPredicate<X,Y,Z> tripredicate);
    static <T> LazyList<T> recursiveDefinition(T firstValue, T secondValue, BiFunction<T,T,T> zippingFunction);
    
    static <T> LazyList<T> from(T from) throws NotEnumerableException;
    static <T> LazyList<T> fromThen(T from, T then) throws NotEnumerableException;
    static <T> LazyList<T> fromTo(T from, T to) throws NotEnumerableException;
    static <T> LazyList<T> fromThenTo(T from, T then, T to) throws NotEnumerableException;
    
    static <T> LazyList<T> concat(LazyList<LazyList<T>> listOfLists);
    
    public LazyList<Object> concat() throws ClassCastException;
    ...