# LazyList
A Java implementation of a Haskell-like linked list with lazy evaluation. 
This means that elements of a LazyList are only computed/evaluated when they have to be and that LazyLists can therefore also be infinite.  

A LazyList is intended to work as similarly as lists in Haskell as possible. 
However, unlike lists in Haskell (which are immutable), a LazyList is mutable as it implements the java.util.List interface.

LazyLists are very similar to Streams from the java.util.stream package but there are some major differences:
1. Streams don't implement the List interface
2. Streams don't support many of the Haskell-borrowed functions like `foldr()`, `scanr()`, `reverse()`, `product()`, ...
3. LazyLists also provide some functions that neither Java Streams nor Haskell provide by default, e.g. `lengthIsAtLeast()`.

There is also the LazyLists Utility Class that provides some useful LazyLists like `primeNumbers()`, `fibonacciNumbers()`, etc...

## Important note
This LazyList library is still in development - most functions do not have an implementation yet.
The LazyListTest Class includes many JUnit tests for all of them - which will hopefully all run through successfully some day.

## Examples
    boolean isPalindrome(String s) {
        return LazyList.fromString(s).reverse().asString().equalsIgnoreCase(s);
    }

    double calculateProduct(Iterable<? extends Number> collection) {
        return LazyList.view(collection).product();
    }

    void printAllFibonacciPrimes() {
        LazyLists.fibonacciNumbers().intersectOrdered(LazyLists.primeNumbers()).print(System.out);
    }

## Functionality
Apart from `toString()` which has to fully evaluate the list before returning, there are three additional methods for turning a LazyList into human-readable text.
`show()` is the lazy version of `toString()` and returns a LazyList of Characters instead of an ordinary Java String.
`print()` prints the LazyList element-by-element, for example to System.out.
`asString()` is the counterpart of `fromString()` and only works on LazyLists of Characters.
It concatenates the Characters instead of putting them in quotation marks and separating them with commas like the other methods.  
    String toString();
    void print(java.io.PrintStream);
    LazyList<Character> show();
    String asString() throws UnsupportedOperationException;
    static LazyList<Character> fromString(String str); // In Haskell Strings already are lazy lists of chars by definition: type String = [Char]

These are all the static functions that create a new LazyList in some way or the other:  
    static <T> LazyList<T> view(Iterable<T> collection);
    static <T> LazyList<T> emptyList();                  // Haskell analogue: [] :: [a]
    static <T> LazyList<T> of(T... elements);            // Haskell analogue: [x,y,z] (list literal)
    static <T> LazyList<T> pure(T el);                   // Haskell analogue: pure :: Applicative f => a -> f a
    static <T> LazyList<T> repeat(T value);              // Haskell analogue: repeat :: a -> [a]
    static <T> LazyList<T> replicate(int n, T value);    // Haskell analogue: replicate :: Int -> a -> [a]
    static <T> LazyList<T> iterate(Function<T,T> function, T startValue);
    
    // Haskell analogue: [x | x <- source, predicate x]
    static <T> LazyList<T> comprehension1(Iterable<T> source, Predicate<T> predicate);
    // Haskell analogue: [mapping x | x <- source, predicate x]
    static <S,T> LazyList<T> comprehension1(Function<S,T> mapping, Iterable<S> source, Predicate<S> predicate);
    // Haskell analogue: [bifunction x y | x <- source1, y <- source2]
    static <X,Y,T> LazyList<T> comprehension2(BiFunction<X,Y,T> bifunction, Iterable<X> source1, Iterable<Y> source2);
    // Haskell analogue: [bifunction x y | x <- source1, y <- source2, bipredicate x y]
    static <X,Y,T> LazyList<T> comprehension2(BiFunction<X,Y,T> bifunction, Iterable<X> source1, Iterable<Y> source2, BiPredicate<X,Y> bipredicate);
    // Haskell analogue: [trifunction x y z | x <- source1, y <- source2, z <- source3, tripredicate x y]
    static <X,Y,Z,T> LazyList<T> comprehension3(TriFunction<X,Y,Z,T> trifunction, Iterable<X> source1, Iterable<Y> source2, Iterable<Z> source3, TriPredicate<X,Y,Z> tripredicate);
    // Haskell analogue: recursiveDefinition x y f = seq' where seq' = x:y:(zipWith f seq' (tail seq'))
    static <T> LazyList<T> recursiveDefinition(T firstValue, T secondValue, BiFunction<T,T,T> zippingFunction);
    
    // Haskell anlogues: enumFrom, enumFromThen, enumFromTo, enumFromThenTo or [n..], [n,n'..], [n..m], [n,n'..m]
    static <T> LazyList<T> from(T from) throws NotEnumerableException;
    static <T> LazyList<T> fromThen(T from, T then) throws NotEnumerableException;
    static <T> LazyList<T> fromTo(T from, T to) throws NotEnumerableException;
    static <T> LazyList<T> fromThenTo(T from, T then, T to) throws NotEnumerableException;
    
    static <T> LazyList<T> concat(LazyList<LazyList<T>> listOfLists);

Instance methods:  
    LazyList<Object> concat() throws ClassCastException; // type-unsafe version of the static concat()
    LinkedList<T> toLinkedList();
    ArrayList<T> toArrayList();
    
    // Unlike length(), lengthIsAtLeast, lengthIsAtMost and lengthEquals are guaranteed to terminate - even on an infinite LazyList.
    int length(); // Haskell analogue: length :: [a] -> Int
    boolean lengthIsAtLeast(int n);
    boolean lengthIsAtMost(int n);
    boolean lengthEquals(int n);

    boolean elem(T value); // Haskell analogue: elem :: (Eq a) => a -> [a] -> Bool
    T head() throws EmptyListException; // Haskell analogue: head :: [a] -> a
    T headOrNull();
    LazyList<T> tail() throws EmptyListException; // Haskell analogue: tail :: [a] -> [a]
    LazyList<T> init() throws EmptyListException; // Haskell analogue: init :: [a] -> [a]
    T last() throws EmptyListException; // Haskell analogue: last :: [a] -> a
    T lastOrNull();
    LazyList<T> take(int n); // Haskell analogue: take :: Int -> [a] -> [a]
    LazyList<T> drop(int n); // Haskell analogue: drop :: Int -> [a] -> [a]
    
    <S> LazyList<S> map(Function<T,S> mapping); // Haskell analogue: map :: (a -> b) -> [a] -> [b]
    <S> LazyList<S> fmap(Function<T,S> mapping); // Haskell analogue: fmap :: Functor f => (a -> b) -> f a -> f b
    LazyList<T> filter(Predicate<T> predicate); // Haskell analogue: filter :: (a -> Bool) -> [a] -> [a]
    
    LazyList<T> takeWhile(Predicate<T> predicate); // Haskell analogue: takeWhile :: (a -> Bool) -> [a] -> [a]
    LazyList<T> dropWhile(Predicate<T> predicate); // Haskell analogue: dropWhile :: (a -> Bool) -> [a] -> [a]
    
    boolean all(Predicate<T> predicate); // Haskell analogue: all :: (a -> Bool) -> [a] -> Bool
    boolean any(Predicate<T> predicate); // Haskell analogue: any :: (a -> Bool) -> [a] -> Bool

    boolean isOrderedBy(BiPredicate<T,T> biPredicate); // (no equivalent in Haskell's Prelude)
    
    LazyList<T> reverse(); // Haskell analogue: reverse :: [a] -> [a]
    LazyList<T> cycle() throws EmptyListException; // Haskell analogue: cycle :: [a] -> [a]
    
    <U,V> LazyList<V> zipWith(BiFunction<T,U,V> zipFunction, LazyList<U> otherList); // zipWith :: (a -> b -> c) -> [a] -> [b] -> [c]
    <U> LazyList<Pair<T,U>> zip(LazyList<U> otherList); // zip :: [a] -> [b] -> [(a, b)]
    Pair<LazyList<?>,LazyList<?>> unzip() throws UnsupportedOperationException; // unzip :: [(a, b)] -> ([a], [b])

    Pair<LazyList<T>,LazyList<T>> splitAt(int index); // splitAt :: Int -> [a] -> ([a], [a])
    LazyList<T> append(LazyList<T> secondList); // (++) :: [a] -> [a] -> [a]
    
    // Set operations, boroughed from Haskell's Data.List module:
    LazyList<T> union(LazyList<T> otherList); // Data.List.union :: Eq a => [a] -> [a] -> [a]
    LazyList<T> intersect(LazyList<T> otherList); // Data.List.intersect :: Eq a => [a] -> [a] -> [a]
    LazyList<T> intersectOrdered(LazyList<T> otherList); // (no equivalent in Haskell's Data.List module)
    LazyList<T> without(LazyList<T> otherList); // (Data.List.\\) :: Eq a => [a] -> [a] -> [a]
    LazyList<T> except(LazyList<T> otherList); // (Data.List.\\) :: Eq a => [a] -> [a] -> [a]
    LazyList<T> minus(LazyList<T> otherList); // (Data.List.\\) :: Eq a => [a] -> [a] -> [a]
    LazyList<T> nub(); // Data.List.nub :: Eq a => [a] -> [a]
    
    // Analogues to Haskell's Control.DeepSeq:
    <U> U seq(U obj); // seq :: a -> b -> b
    <U> U deepseq(U obj); // Control.DeepSeq.deepseq :: Control.DeepSeq.NFData a => a -> b -> b
    LazyList<T> force(); // Control.DeepSeq.force :: Control.DeepSeq.NFData a => a -> a
    
    // Folds, analogues to the functions in Haskell with the same names:
    <B> B foldr(BiFunction<T,B,B> foldFunction, B initialValue);
    <B> B foldl(BiFunction<B,T,B> foldFunction, B initialValue);
    T foldr1(BiFunction<T,T,T> foldFunction) throws EmptyListException;
    T foldl1(BiFunction<T,T,T> foldFunction) throws EmptyListException;
    T minimum() throws EmptyListException, ClassCastException;
    T maximum() throws EmptyListException, ClassCastException;
    double sum() throws ClassCastException;
    double product() throws ClassCastException;
    
    double average() throws ClassCastException; // (no equivalent in Haskell's Prelude)
    
    // Scans, analogues to the functions in Haskell with the same names:
    <B> LazyList<B> scanr(BiFunction<T,B,B> foldFunction, B initialValue);
    <B> LazyList<B> scanl(BiFunction<B,T,B> foldFunction, B initialValue);
    LazyList<T> scanr1(BiFunction<T,T,T> foldFunction) throws EmptyListException;
    LazyList<T> scanl1(BiFunction<T,T,T> foldFunction) throws EmptyListException;
    
    // String functions:
    LazyList<String> lines() throws UnsupportedOperationException; // lines :: String -> [String]
    LazyList<Character> unlines() throws UnsupportedOperationException; // unlines :: [String] -> String
    LazyList<String> words() throws UnsupportedOperationException; // words :: String -> [String]
    LazyList<Character> unwords() throws UnsupportedOperationException; // unwords :: [String] -> String
    LazyList<String> split(String delimiter); // (no equivalent in Haskell's Prelude)
    
There are also some more advanced functions that do not come from Haskell but exist to make the LazyList's integration into Java's constructs are seamless as possible:  
    static LazyList<Integer> fromInputStream(InputStream inputStream);
    java.io.InputStream asInputStream() throws ClassCastException;
    static LazyList<Character> fromASCIIFile(File file) throws FileNotFoundException;
    void writeToOutputStream(OutputStream outputStream) throws IOException, ClassCastException;
    void writeAsStringToTextFile(File textFile) throws UnsupportedOperationException;
    void writeAsBinaryToFile(File file) throws UnsupportedOperationException;
    
    LazyList<T> traverse();
    void traverseAndThen(Consumer<LazyList<T>> actionAfterTraversal);

One can even use a LazyList as a (possibly infinite!) Map:  
    static <A,B> LazyList<Pair<A,B>> fromMap(java.util.Map<A,B> map);
    Optional<Object> lookup(Object key) throws UnsupportedOperationException; // Haskell analogue: lookup :: Eq a => a -> [(a, b)] -> Maybe b
    Map<Object,Object> viewAsMap() throws UnsupportedOperationException;

The following methods exist so that you can write even more compact code / one-liners:  
    LazyList<T> forEachAndThen(Consumer<? super T> action);
    LazyList<T> forEachWithIndex(BiConsumer<? super T, Integer> action);
    LazyList<T> forEachWithIndexAndLength(TriConsumer<? super T, Integer, Integer> action);
    
    BooleanAnswer ifAny(Predicate<T> predicate);
    BooleanAnswer ifAll(Predicate<T> predicate);
    BooleanAnswer ifLengthIsAtLeast(int n);
    BooleanAnswer ifLengthIsAtMost(int n);
    BooleanAnswer ifElem(T value);
    BooleanAnswer ifContains(Object o);
    BooleanAnswer ifContainsAll(Collection<?> c);
    BooleanAnswer ifIsEmpty();
    BooleanAnswer ifEquals(Object o);

LazyList also implements all the `java.util.List` interface methods (even the optional ones), as well as `Comparable<List<? extends Comparable<T>>>`.
