package com.kendrick.haskell;

// ----- Development Time Tracking: -----
// Jan 09 2021 13:57-19:44
// Jan 10 2021 13:59-15:52 & 19:37-20:00
// Jan 11 2021 12:52-14:13
// Feb 28 2021 18:07-19:18
// Mar 01 2021 12:45-12:53 & 13:45-(19:18)  |  created LazyListTest
// Mar 02 2021 11:41-12:46 (reverse() & LazyLists Utility Class, including Unit Test) & 16:48-17:24
// Mar 03 2021 14:48-19:33
// Mar 04 2021 11:51-12:44 & 13:46-17:10
// Mar 05 2021 14:04-14:40
// Mar 07 2021 18:46-19:27

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A linked list with lazy evaluation (i.e. elements are only computed/evaluated when they have to be).
 * LazyLists can therefore also be infinite.
 *
 * A LazyList is intended to work as similarly as lists in Haskell as possible.
 * However, unlike lists in Haskell (which are immutable),
 * a LazyList is mutable as it implements the java.util.List interface.
 *
 * LazyLists are very similar to Streams from the java.util.stream package but there are some major differences:
 * 1) Streams don't implement the List interface
 * 2) Streams don't support many of the Haskell-borrowed functions like foldr(), scanr(), reverse(), product(), ...
 * 3) LazyLists also provide some functions that neither Java Streams nor Haskell provide by default, e.g. lengthIsAtLeast().
 *
 * Beware that some of the java.util.List methods never terminate on an infinite LazyList –
 * usage of the according LazyList functions (e.g. intersect() instead of retainAll()) instead
 * is therefore strongly recommended.
 *
 * The LazyLists Utility Class provides some useful LazyLists like primeNumbers(), fibonacciNumbers(), etc...
 *
 * Please note that a LazyList may NOT contain null values.
 *
 * Please also note that there are four different methods for turning a LazyList into human-readable text:
 * a) String toString()
 *    - fully evaluates the LazyList and then returns an ordinary Java String representation of it
 *    - intermediate results are not shown anywhere
 *    - will never terminate and should therefore never be called on an infinite LazyList
 * b) void print(java.io.PrintStream)
 *    - prints the LazyList element-by-element to the specified Stream, preferably System.out
 *    - will also never terminate on infinite LazyLists and should therefore usually be started in a separate thread
 * c) LazyList<Character> show()
 *    - the only of the four operations that returns / terminates when called on an infinite LazyList
 *    - most similar to the toString() method - only that the "String" it returns is lazy
 * d) String asString() throws UnsupportedOperationException
 *    - only works on LazyLists of Characters (LazyList<Character>), throws an UnsupportedOperationException otherwise
 *    - counterpart / inverse to the fromString() method
 *    - useful for converting between LazyLists of Characters (LazyList<Character>) and ordinary Java Strings
 *
 * Some examples for using LazyLists:
 *
 * boolean isPalindrome(String s) {
 *     return LazyList.fromString(s).reverse().asString().equalsIgnoreCase(s);
 * }
 *
 * double calculateProduct(Iterable<? extends Number> collection) {
 *     return LazyList.view(collection).product();
 * }
 *
 * void printAllFibonacciPrimes() {
 *     LazyLists.fibonacciNumbers().intersectOrdered(LazyLists.primeNumbers()).print(System.out);
 * }
 *
 * @param <T> the type of elements in this list
 * @author Kendrick Gruenberg
 */
public class LazyList<T> implements java.util.List<T>, Comparable<List<? extends Comparable<T>>> { // Superinterfaces of List: Collection & Iterable

    private LinkedList<T> internalList; // the part of this LazyList that has already been evaluated/generated
    private Function<List<T>,T> listElementGenerator;
    //   -> generates the next element that should come after the last element in internalList (lazy generation)
    //   -> generates null when internalList is already the entire list
    //   -> to ease the implementation of all other functions, this Function shall also update the List<T> it's given
    //       – conveniently already adding the newly generated element to it
    private LazyList<Object> backEnd;
    //   -> underlying LazyList (optional)
    //   -> the resulting LazyLists of functions like map() or cycle() need access to the LazyList they rely on
    //      (in order to either work lazily or to work at all)
    //   -> may be null when this LazyList does not have/need a back end LazyList

    LazyList(LinkedList<T> internalList, Function<List<T>,T> listElementGenerator) {
        this.internalList  = internalList;
        this.listElementGenerator = listElementGenerator;
    }

    /**
     * Constructs a new empty LazyList.
     */
    public LazyList() {
        this.internalList = new LinkedList<>();
        this.listElementGenerator = (x) -> null;
    }

    /**
     * Constructs a LazyList based on its head and its tail (which is also a LazyList).
     * Important note: This does NOT create a copy but actually just appends head to the beginning of tail!
     * tail should therefore NOT be used anymore afterwards!
     *
     * Haskell analogue: (x:xs) (the cons operator)
     *
     * @param head set this and the tail to null to construct an empty LazyList
     * @param tail set this to null to construct a single-element LazyList
     * @throws NullValuesNotPermittedInLazyListException when the specified head is null but tail isn't
     */
    public LazyList(T head, LazyList<T> tail) throws NullValuesNotPermittedInLazyListException {
        if (head == null && tail != null) {
            throw new NullValuesNotPermittedInLazyListException();
        } else if (head == null && tail == null) { // Construct an empty LazyList:
            this.internalList = new LinkedList<>();
            this.listElementGenerator = (x) -> null;
        } else if (head != null && tail == null) { // Construct a single-element LazyList:
            this.internalList = new LinkedList<>();
            this.internalList.add(head);
            this.listElementGenerator = (x) -> null;
        } else {
            this.internalList = tail.internalList;
            this.internalList.add(0, head);
            this.listElementGenerator = tail.listElementGenerator;
        }
    }

    /**
     * Creates a LazyList copy from an existing collection.
     * Please note that this is not a lazy operation!
     * If you want a lazy view of an existing collection, use LazyList.view()
     *
     * @param collection the Iterable to copy
     */
    public LazyList(Collection<T> collection) {
        this.internalList = new LinkedList<>(collection);
        this.listElementGenerator = (x) -> null;
    }

    /**
     * Enables lazy printing of this lazy list to console (or somewhere else).
     * A newline at the end has to be appended manually if it is wanted.
     *
     * Example:
     * lazyList1.print(System.out);
     * System.out.print("\n");
     *
     * Note: LazyList.toString() cannot possibly be lazy, so use this method
     * when lazy printing is needed (this means especially when this LazyList is infinite).
     *
     * @param printStream e.g. System.out
     */
    public void print(java.io.PrintStream printStream) {
        String internalListStr = this.internalList.toString(); // e.g. "[1, 2]"
        printStream.print(internalListStr.substring(0, internalListStr.length()-1)); // print but remove the last ']' - e.g. "[1, 2"
        printStream.flush();

        for (T next = listElementGenerator.apply(internalList);
             next != null;
             next = listElementGenerator.apply(internalList)) {
            printStream.print(", ");
            printStream.print(next.toString());
            printStream.flush();
        }

        // -- When this LazyList is infinite, this point is never reached and print() never terminates!
        printStream.print("]");
        printStream.flush();
    }






    // ----- Static methods: -----






    /**
     * Returns a lazy LazyList view of a given Collection (or even anything that just implements the Iterable interface).
     * Very important note:
     * The returned view IS mutable but modifications are NOT passed to the underlying collection.
     * Changes in the underlying collection are passed to the LazyList view IF AND ONLY IF they occur
     * before the LazyList retrieves them – which only happens if needed (a LazyList is lazy!).
     *
     * @param collection the Iterable to view
     * @param <T> the type of the stored elements
     * @return a lazy LazyList view of collection
     */
    public static <T> LazyList<T> view(Iterable<T> collection) {
        return new LazyList<T>(
                new LinkedList<>(),
                (list) -> {
                    if (collection.iterator().hasNext()) {
                        T next = collection.iterator().next();
                        list.add(next);
                        return next;
                    } else {
                        return null;
                    }
                }
        );
    }

    /**
     * Returns a new empty LazyList.
     *
     * Haskell analogue: [] :: [a]
     *
     * @param <T> the type of the new empty LazyList
     * @return a new empty LazyList
     */
    public static <T> LazyList<T> emptyList() {
        return new LazyList<T>(new LinkedList<T>(), (x) -> null);
    }

    /**
     * Creates a finite LazyList from a number of given elements.
     * Please note that this is not a lazy operation!
     *
     * Haskell analogue: [x,y,z] (list literal)
     *
     * @param elements
     * @param <T>
     * @return
     */
    public static <T> LazyList<T> of(T... elements) {
        return new LazyList<T>(new LinkedList<T>(Arrays.asList(elements)), (x) -> null);
    }

    /**
     * Constructs a single element LazyList.
     * This function simply exists for "Haskell-completeness".
     *
     * Haskell analogue: pure :: Applicative f => a -> f a
     *
     * ((pure 3)::[Int]) == [3]
     * ((pure 3)::(Maybe Int)) == Just 3
     *
     * @param el
     * @param <T>
     * @return
     */
    public static <T> LazyList<T> pure(T el) {
        return new LazyList<T>(el, null);
    }

    /**
     * Returns an infinite LazyList with the given parameter repeated infinitely often.
     *
     * Haskell analogue: repeat :: a -> [a]
     *
     *
     * @param value the value to be repeated infinitely often
     * @param <T> the type of value
     * @return an infinite LazyList with the value parameter repeated infinitely often
     */
    public static <T> LazyList<T> repeat(T value) {
        return new LazyList<T>(new LinkedList<T>(), (list) -> { list.add(value); return value; });
    }

    /**
     * Returns a finite LazyList with the second parameter repeated as often as specified by the first parameter.
     * Note: this function IS lazy, which means that you can specify a VERY large n without any problems!
     *
     * Haskell analogue: replicate :: Int -> a -> [a]
     *
     * @param n who often value should be replicated/repeated
     * @param value the value to to replicated/repeated n times
     * @param <T> the type of value
     * @return a finite LazyList with the value parameter repeated as often as specified by the n parameter
     */
    public static <T> LazyList<T> replicate(int n, T value) {
        return new LazyList<T>(
            new LinkedList<T>(),
            (list) -> {
                if (list.size() < n) {
                    list.add(value);
                    return value;
                } else {
                    return null;
                }
            }
        ); // stop generating values after the list has reached a size of n
    }

    /**
     * Iterates the given function on the given value infinitely often and
     * returns the infinite list of intermediate results.
     *
     * Haskell analogue: iterate :: (a -> a) -> a -> [a]
     *
     * @param function the function that's being iterated on startValue (return type has to be equal to parameter type)
     * @param startValue the "seed" for the infinite iteration
     * @param <T> the type of startValue and the type that function takes as input
     * @return the infinite list [startValue, function(startValue), function(function(startValue)), ...
     */
    public static <T> LazyList<T> iterate(Function<T,T> function, T startValue) {
        return new LazyList<T>(
                new LinkedList<T>(List.of(startValue)),
                (list) -> {
                    T next = function.apply(list.get(list.size()-1));
                    list.add(next);
                    return next;
                }
        ); // (apply function to the last list value)
    }

    /**
     * Creates a LazyList from all the values from the given (possibly infinite) source (which has to be iterable)
     * that satisfy the given predicate.
     *
     * Haskell analogue: [x | x <- source, predicate x] (list comprehension)
     *
     * @param source
     * @param predicate
     * @param <T>
     * @return
     */
    public static <T> LazyList<T> comprehension1(Iterable<T> source, Predicate<T> predicate) {
        return comprehension1(java.util.function.Function.identity(), source, predicate);
    }

    /**
     * Creates a LazyList from all the values from the given (possibly infinite) source (which has to be iterable)
     * that satisfy the given predicate and apply the given mapping on all of them.
     *
     * Haskell analogue: [mapping x | x <- source, predicate x] (list comprehension)
     *
     * @param mapping ... (use java.util.function.Function.identity() when no actual mapping is wanted)
     * @param source
     * @param predicate
     * @param <S> source type (can be the same as T for simple comprehensions but doesn't have to be in general)
     * @param <T> target type (the type of the resulting LazyList)
     * @return
     */
    public static <S,T> LazyList<T> comprehension1(Function<S,T> mapping, Iterable<S> source, Predicate<S> predicate) {
        return comprehension1_(mapping, source.iterator(), predicate);
    }

    private static <S,T> LazyList<T> comprehension1_(Function<S,T> mapping, Iterator<S> iterator, Predicate<S> predicate) {
        if (!iterator.hasNext()) {
            return emptyList();
        } else {
            S next = iterator.next();
            if (predicate.test(next)) {
                return new LazyList<T>(
                        () -> mapping.apply(next), // MAP happens here
                        () -> comprehension1_(mapping, iterator, predicate)
                );
            } else {
                return comprehension1_(mapping, iterator, predicate); // FILTER happens here
            }
        }
    }

    /**
     * Gives a LazyList of all possible functional combinations of the values from the two given sources.
     * Both sources may be infinite.
     *
     * Haskell analogue: [bifunction x y | x <- source1, y <- source2]
     *
     * @param bifunction
     * @param source1
     * @param source2
     * @param <X>
     * @param <Y>
     * @param <T>
     * @return
     */
    public static <X,Y,T> LazyList<T> comprehension2(BiFunction<X,Y,T> bifunction, Iterable<X> source1, Iterable<Y> source2) {
        return comprehension2(bifunction, source1, source2, (x,y) -> true); // no predicate specified
    }

    /**
     * Gives a LazyList of all possible functional combinations of the values from the two given sources
     * but only those that fulfill the given BiPredicate (a predicate on two values).
     * Both sources may be infinite.
     *
     * Haskell analogue: [bifunction x y | x <- source1, y <- source2, bipredicate x y]
     *
     * @param bifunction
     * @param source1
     * @param source2
     * @param bipredicate
     * @param <X>
     * @param <Y>
     * @param <T>
     * @return
     */
    public static <X,Y,T> LazyList<T> comprehension2(BiFunction<X,Y,T> bifunction, Iterable<X> source1, Iterable<Y> source2, BiPredicate<X,Y> bipredicate) {
        return null;
    }

    /**
     * The analogue of comprehension2() for 3 sources.
     *
     * Haskell analogue: [trifunction x y z | x <- source1, y <- source2, z <- source3, tripredicate x y]
     *
     * @param trifunction
     * @param source1
     * @param source2
     * @param source3
     * @param tripredicate
     * @param <X>
     * @param <Y>
     * @param <Z>
     * @param <T>
     * @return
     */
    public static <X,Y,Z,T> LazyList<T> comprehension3(TriFunction<X,Y,Z,T> trifunction, Iterable<X> source1, Iterable<Y> source2, Iterable<Z> source3, TriPredicate<X,Y,Z> tripredicate) {
        return null;
    }

    /**
     * This function exists when one wants to create a recursively defined LazyList using zipWith.
     *
     * e.g.     fibs = 0:1:(zipWith (+) fibs (tail fibs))
     *      --> LazyList<Integer> fibonacciNumbers = LazyList.recursiveDefinition(0, 1, Integer::sum);
     *
     * Haskell analogue:
     *   recursiveDefinition :: a -> a -> (a -> a -> a) -> [a]
     *   recursiveDefinition x y f = seq' where seq' = x:y:(zipWith f seq' (tail seq'))
     *
     * @param firstValue
     * @param secondValue
     * @param zippingFunction
     * @param <T>
     * @return
     */
    public static <T> LazyList<T> recursiveDefinition(T firstValue, T secondValue, BiFunction<T,T,T> zippingFunction) {
        return null;
    }

    /**
     * The (depending on type T possibly infinite) list beginning at 'from' and then increasing by 1 every step.
     * e.g. LazyList.from(3)   == [3,4,5,6,7,8,...
     *      LazyList.from('a') == ['a','b','c','d',...
     * If the type T has a MAX_VALUE this will be the last value and the resulting list will be finite.
     *
     * Haskell analogue: enumFrom :: a -> [a] -- [n..]
     *
     * @param from the value to start counting from
     * @param <T>
     * @return
     * @throws NotEnumerableException when the type T is not enumerable.
     *         The enumerable types are all the standard subclasses of Number, Character & Boolean.
     */
    public static <T> LazyList<T> from(T from) throws NotEnumerableException {
        // EnumerableUtils.successor() (see call further down) returns null on the last enumerable element (e.g. X.MAX_VALUE or True):
        if (from == null) {
            return null; // recursion base case for finite enumerations: e.g. [(True)..] == [True]
        }

        // recursive definition of a (potentially infinite) list:
        // [n..] = n:[(n+1)..]
        return new LazyList<T>(
                () -> from,
                () -> LazyList.from(EnumerableUtils.successor(from))
        );
    }

    /**
     * Similar to from(), only that the step by which to increase is not just +1 but can be specified by
     * specifying the second element in the result list.
     * When then is smaller than from, the resulting list will be in decreasing order.
     *
     * e.g. LazyList<Integer> evenNumbers = LazyList.fromThen(0,2);
     *
     * Haskell analogue: enumFromThen :: a -> a -> [a] -- [n,n'..]
     *
     * @param from
     * @param then
     * @param <T>
     * @return
     * @throws NotEnumerableException when the type T is not enumerable.
     *         The enumerable types are all the standard subclasses of Number, Character & Boolean.
     */
    public static <T> LazyList<T> fromThen(T from, T then) throws NotEnumerableException {
        return null;
    }

    /**
     * Similar to from() but the enumeration stops at the specified 'to' parameter
     *
     * e.g. LazyList.fromTo(5,10) == [5,6,7,8,9,10]
     *
     * Haskell analogue: enumFromTo :: a -> a -> [a] -- [n..m]
     *
     * @param from
     * @param to
     * @param <T>
     * @return
     * @throws NotEnumerableException when the type T is not enumerable.
     *         The enumerable types are all the standard subclasses of Number, Character & Boolean.
     */
    public static <T> LazyList<T> fromTo(T from, T to) throws NotEnumerableException {
        return null;
    }

    /**
     * The combination of fromThen() and fromTo().
     * The increment step and the value at which to stop (parameter 'to') are specified.
     *
     * e.g. LazyList.fromTo(2,4,10) == [2,4,6,8,10]
     *
     * Haskell analogue: enumFromThenTo :: a -> a -> a -> [a] -- [n,n'..m]
     *
     * @param from the first element of the resulting LazyList
     * @param then the second element of the resulting LazyList
     * @param to the last element of the resulting LazyList
     * @param <T> the type of the from, then and to parameters
     * @return [from, then, ..., to]
     * @throws NotEnumerableException when the type T is not enumerable.
     *         The enumerable types are all the standard subclasses of Number, Character & Boolean.
     */
    public static <T> LazyList<T> fromThenTo(T from, T then, T to) throws NotEnumerableException {
        return null;
    }

    /**
     * Takes a LazyList of LazyLists and appends them all into a single LazyList as the result.
     * This is a lazy operation, which means that the input can be a LazyList of infinitely many LazyLists.
     *
     * Haskell analogue:
     * concat :: [[a]] -> [a]
     * concat xss = foldr (++) [] xss
     *
     * @param listOfLists
     * @param <T>
     * @return
     */
    public static <T> LazyList<T> concat(LazyList<LazyList<T>> listOfLists) {
        return listOfLists.foldr(LazyList::append, LazyList.emptyList()); // concat xss = foldr (++) [] xss
    }






    // ----- Non-static methods: -----






    /**
     * The type-unsafe but more convenient to call version of the static LazyList.concat() function.
     *
     * Haskell analogue:
     * concat :: [[a]] -> [a]
     * concat xss = foldr (++) [] xss
     *
     * Examples:
     * LazyList.of("Hello ", "World").concat().asString().equals("Hello World");
     * LazyList.of(LazyList.of(1,2), LazyList.of(3,4)).concat().equals(LazyList.of(1,2,3,4));
     *
     * @return the lists in this list concatenated into a simple list
     * @throws ClassCastException when this is not a LazyList of either LazyLists or Strings!
     */
    public LazyList<Object> concat() throws ClassCastException {
        try {
            return LazyList.concat((LazyList<LazyList<Object>>) this);
        } catch (ClassCastException ex) {
            return LazyList.fromString(this.foldr((s1,s2) -> s1 + s2, "")).map((x) -> x);
            // .map((x) -> x) is necessary because a cast from LazyList<Character> to LazyList<Object> is not possible!
        }
    }



    /**
     * Turns this LazyList into an ordinary LinkedList from the java.util package.
     * This function won't terminate on an infinite LazyList.
     *
     * @return this LazyList as an ordinary LinkedList
     */
    public LinkedList<T> toLinkedList() {
        return new LinkedList<>(this);
    }

    /**
     * Turns this LazyList into an ordinary ArrayList from the java.util package.
     * This function won't terminate on an infinite LazyList.
     *
     * @return this LazyList as an ordinary ArrayList
     */
    public ArrayList<T> toArrayList() {
        return new ArrayList<>(this);
    }

    /**
     * The number of elements in this LazyList.
     * If this LazyList is infinite, this function will keep traversing it forever and never terminates!
     * When you just want to check whether a LazyList has at least (or at most / or exactly) n elements,
     * use the lengthIsAtLeast() or lengthIsAtMost() or lengthEquals() methods which will also terminate
     * on an infinite LazyList!
     *
     * Haskell analogue: length :: [a] -> Int
     *
     * @return the length of this LazyList
     */
    public int length() {
        while (this.listElementGenerator.apply(this.internalList) != null) {
            // repeat until listElementGenerator Function returns null, i.e. the LazyList has been fully evaluated...
        }
        return this.internalList.size();
    }

    /**
     * Check whether this LazyList consists of at least (i.e. no less than) n elements.
     * Unlike calling the length() method, this function is guaranteed to terminate - even on an infinite LazyList.
     *
     * @param n a number
     * @return whether this LazyList consists of at least n elements
     */
    public boolean lengthIsAtLeast(int n) {
        if (this.internalList.size() >= n) {
            return true; // at least n elements have already been evaluated, no need to evaluate any further
        } else if (this.listElementGenerator.apply(this.internalList) == null) {
            return false; // this.internalList.size() < n and there are no elements left to generate
        } else {
            while (internalList.size() < n && listElementGenerator.apply(internalList) != null) {
                // keep evaluating while possible and length is still < n
            }
            return this.internalList.size() >= n;
        }
    }

    /**
     * Check whether this LazyList consists of at most (i.e. no more than) n elements.
     * Unlike calling the length() method, this function is guaranteed to terminate - even on an infinite LazyList.
     *
     * @param n a number
     * @return whether this LazyList consists of at most n elements
     */
    public boolean lengthIsAtMost(int n) {
        if (this.internalList.size() > n) {
            return false; // there are already more than n elements evaluated!
        } else if (this.listElementGenerator.apply(this.internalList) == null) {
            return true; // this.internalList.size() <= n and there are no elements left to generate
        } else {
            while (internalList.size() <= n && listElementGenerator.apply(internalList) != null) {
                // keep evaluating while possible and length is still <= n
            }
            return this.internalList.size() <= n;
        }
    }

    /**
     * Check whether this LazyList consists of exactly n elements.
     * Unlike calling the length() method, this function is guaranteed to terminate - even on an infinite LazyList.
     *
     * @param n a number
     * @return whether this LazyList consists of exactly n elements
     */
    public boolean lengthEquals(int n) {
        if (this.internalList.size() > n) {
            return false; // there are already more than n elements evaluated!
        } else if (this.listElementGenerator.apply(this.internalList) == null) { // no elements left to generate
            return this.internalList.size() == n;
        } else {
            while (internalList.size() <= n && listElementGenerator.apply(internalList) != null) {
                // keep evaluating while possible and length is still <= n
                // (i.e. try to get >n)
                // (i.e. stop evaluating as soon as the length has already gotten bigger than n)
            }
            return this.internalList.size() == n;
        }
    }

    /**
     * Checks whether the specified value is an element of this LazyList.
     * The Object.equals() method is being used for comparison.
     * Null can never be element of a (properly constructed) LazyList!
     *
     * This function won't terminate on an infinite LazyList when the value is not in it
     * as it will keep searching and searching for it, i.e. this function will never return false
     * on an infinite LazyList.
     *
     * Haskell analogue: elem :: (Eq a) => a -> [a] -> Bool
     *
     * @param value
     * @return
     */
    public boolean elem(T value) {
        if (this.internalList.contains(value)) {
            return true;
        } else {
            for (T next = listElementGenerator.apply(internalList); true; next = listElementGenerator.apply(internalList)) {
                if (next == null) {
                    return false; // End of LazyList reached without finding the value.
                } else if (next.equals(value)) {
                    return true;
                } // else: keep on searching for value... (note: may never terminate on infinite LazyLists, see javadoc above)
            }
        }
    }

    /**
     * Returns the first value of this LazyList.
     *
     * Haskell analogue: head :: [a] -> a
     *
     * @return the first value of this LazyList, is never null
     * @throws EmptyListException when called on an empty LazyList as there is no value to be returned
     */
    public T head() throws EmptyListException {
        T head_or_null = this.headOrNull();
        if (head_or_null == null) {
            throw new EmptyListException();
        } else {
            return head_or_null; // which is not null as we just checked
        }
    }

    /**
     * Returns the first value of this LazyList – just like the head() method.
     * However instead of throwing an EmptyListException on an empty LazyList, this method will just return null.
     *
     * @return the first value of this LazyList or null on an empty LazyList
     */
    public T headOrNull() {
        if (this.internalList.size() >= 1) { // if there is already at least 1 evaluated element...
            return this.internalList.get(0); // ...return it!
        } else {
            return this.listElementGenerator.apply(this.internalList);
            // listElementGenerator already returns null when there's nothing left to generate by definition
        }
    }

    /**
     * Returns the tail of this LazyList, i.e. the entire LazyList but without the first value.
     * Returns the empty LazyList on a single-element LazyList.
     * Throws an EmptyListException on an empty LazyList.
     *
     * Haskell analogue: tail :: [a] -> [a]
     *
     * @throws EmptyListException when called on an empty LazyList
     * @return the tail of this LazyList
     *
     */
    public LazyList<T> tail() throws EmptyListException {
        return null;
    }

    /**
     * Returns this entire LazyList, except for the last value (which will be left out).
     * Returns the empty LazyList on a single-element LazyList.
     * Throws an EmptyListException on an empty LazyList.
     * This function is the identity on an infinite LazyList (which doesn't have a last value).
     *
     * Haskell analogue: init :: [a] -> [a]
     *
     * @return this entire LazyList, except for the last value
     * @throws EmptyListException when called on an empty LazyList
     */
    public LazyList<T> init() throws EmptyListException {
        return null;
    }

    /**
     * Returns the last value of this LazyList.
     * This method will never terminate when called on an infinite LazyList.
     *
     * Haskell analogue: last :: [a] -> a
     *
     * @return the last value of this LazyList, is never null
     * @throws EmptyListException when called on an empty LazyList as there is no value to be returned
     */
    public T last() throws EmptyListException {
        // cf. head() and headOrNull():
        T last_or_null = this.lastOrNull();
        if (last_or_null == null) {
            throw new EmptyListException();
        } else {
            return last_or_null; // which is not null as we just checked
        }
    }

    /**
     * Returns the last value of this LazyList – just like the last() method.
     * However instead of throwing an EmptyListException on an empty LazyList, this method will just return null.
     *
     * @return the last value of this LazyList or null on an empty LazyList
     */
    public T lastOrNull() {
        while (this.listElementGenerator.apply(this.internalList) != null) {
            // fully evaluate this list
        }
        if (this.internalList.size() > 0) {
            return this.internalList.get(this.internalList.size() - 1); // return the last element of the internal list
        } else {
            return null; // There are no elements in this LazyList.
        }
    }

    /**
     * Returns a new finite LazyList, consisting of the first n values of this LazyList.
     * When n is larger than this.length(), no Exception is thrown, but rather the entire list is returned.
     *
     * Haskell analogue: take :: Int -> [a] -> [a]
     *
     * @param n the number of elements to take from this LazyList
     * @return a finite LazyList, consisting of the first n values of this LazyList
     */
    public LazyList<T> take(int n) {
        while (this.internalList.size() < n && listElementGenerator.apply(internalList) != null) {
            // evaluate this list until either
            // a) a size of n is reached OR
            // b) it is fully evaluated, i.e. the end is reached
        }

        LinkedList<T> resultLili;
        if (this.internalList.size() > n) { // values have to be chopped off:
            resultLili = new LinkedList<>(this.internalList.subList(0,n));
        } else {
            resultLili = new LinkedList<>(this.internalList);
        }
        return new LazyList<T>(resultLili, (x) -> null);
    }

    /**
     * Drops the first n values of this LazyList and returns the remaining bit.
     * The result is a new LazyList (copy)!
     * When called on an infinite LazyList, the result will still be an infinite LazyList.
     * When n is larger than this.length(), no Exception is thrown, but rather the empty list is returned.
     *
     * Haskell analogue: drop :: Int -> [a] -> [a]
     *
     * @param n the number of elements to drop
     * @return the remaining bit of this LazyList, without the first n elements
     */
    public LazyList<T> drop(int n) {
        // TODO
        return null;
    }

    /**
     * Maps a function on every element of this LazyList and returns the (new) resulting LazyList (copy).
     *
     * Haskell analogue: map :: (a -> b) -> [a] -> [b]
     *
     * @param mapping
     * @param <S>
     * @return
     */
    public <S> LazyList<S> map(Function<T,S> mapping) {
        LinkedList<S> newLili = new LinkedList<>();
        for (T oldEl : this.internalList) {
            newLili.add(mapping.apply(oldEl));
        }

        //Function<List<S>,List<T>> sListToTList = (sList) -> sList.stream().map(mapping).collect();
        // ToDo: How to map List<S> -> List<T> when we're only given a T->S map ?!
        // Solution: map List cannot be independent but rather HAS TO BE backed by the original map internally!!
        Function<List<S>,S> newGenerator = this.listElementGenerator.andThen(mapping); // ToDo

        return new LazyList<S>(newLili, newGenerator);
    }

    /**
     * A synonym for map().
     * A LazyList is a Functor.
     * This method simply exists for completeness.
     *
     * Haskell analogue: fmap :: Functor f => (a -> b) -> f a -> f b
     *
     * map (^2) [1..10] == [1,4,9,16,25,36,49,64,81,100]
     * fmap (^2) [1..10] == [1,4,9,16,25,36,49,64,81,100]
     * fmap (^2) (Just 3) == Just 9
     *
     * @param mapping
     * @param <S>
     * @return
     */
    public <S> LazyList<S> fmap(Function<T,S> mapping) {
        return this.map(mapping);
    }

    /**
     * Returns a new LazyList, consisting of only the values of this LazyList that satisfy the given predicate.
     *
     * Haskell analogue: filter :: (a -> Bool) -> [a] -> [a]
     *
     * @param predicate
     * @return
     */
    public LazyList<T> filter(Predicate<T> predicate) {
        return null;
    }

    /**
     *
     * Haskell analogue: takeWhile :: (a -> Bool) -> [a] -> [a]
     *
     * @param predicate
     * @return
     */
    public LazyList<T> takeWhile(Predicate<T> predicate) {
        return null;
    }

    /**
     *
     * Haskell analogue: dropWhile :: (a -> Bool) -> [a] -> [a]
     *
     * @param predicate
     * @return
     */
    public LazyList<T> dropWhile(Predicate<T> predicate) {
        return null;
    }

    /**
     * Checks whether all elements of this LazyList satisfy the given predicate.
     * This method will never return true on an infinite LazyList.
     *
     * Haskell analogue: all :: (a -> Bool) -> [a] -> Bool
     *
     * @param predicate
     * @return whether all elements of this LazyList satisfy the given predicate
     */
    public boolean all(Predicate<T> predicate) {
        if (!this.internalList.stream().allMatch(predicate)) {
            return false; // There's already a mismatch in the evaluated part of this LazyList!
        } else {
            for (T next = listElementGenerator.apply(internalList); next != null; next = listElementGenerator.apply(internalList)) {
                if (!predicate.test(next)) {
                    return false;
                }
            }
            return true; // LazyList was finite and has been fully evaluated without finding a single mismatch!
        }
    }

    /**
     * Checks whether at least one element of this LazyList satisfies the given predicate.
     * This method will never return false on an infinite LazyList.
     *
     * Haskell analogue: any :: (a -> Bool) -> [a] -> Bool
     *
     * @param predicate
     * @return whether at least one element of this LazyList satisfies the given predicate
     */
    public boolean any(Predicate<T> predicate) {
        if (this.internalList.stream().anyMatch(predicate)) {
            return true;
        } else { // No matches in the evaluated part of this LazyList so far - keep looking...:
            for (T next = listElementGenerator.apply(internalList); next != null; next = listElementGenerator.apply(internalList)) {
                if (predicate.test(next)) {
                    return true;
                }
            }
            return false; // LazyList was finite and has been fully evaluated without finding any match!
        }
    }

    /**
     * Returns whether this LazyList is ordered according to the given ordering relation.
     *
     * Haskell analogue:
     * orderedBy :: (a -> a -> Bool) -> [a] -> Bool
     * orderedBy _ [] = True
     * orderedBy _ [_] = True
     * orderedBy comp (x:xs) = (x `comp` (head xs)) && (orderedBy comp xs)
     *
     * orderedBy (<=) [1,2,3,3,4,5] == True
     * orderedBy (<) [1,2,3,3,4,5] == False
     *
     * @param biPredicate
     * @return
     */
    public boolean isOrderedBy(BiPredicate<T,T> biPredicate) {
        throw new UnsupportedOperationException("ToDo: isOrderedBy");
    }

    /**
     * This function returns a new LazyList which is this LazyList in reverse order.
     * This function never terminates when called on an infinite LazyList!
     *
     * e.g. LazyList.of(1,2,3,4).reverse() == [4,3,2,1]
     *
     * Haskell analogue: reverse :: [a] -> [a]
     *
     * @return this LazyList in reverse order
     */
    public LazyList<T> reverse() {
        this.traverse(); // A reversal cannot possibly happen lazily, the last list element has to be known!
        LinkedList<T> reversedInternalList = new LinkedList<>(this.internalList);
        Collections.reverse(reversedInternalList);
        return new LazyList<T>(
                reversedInternalList,
                (x) -> null // (the reversed list has nothing to generate – this wouldn't make any sense!)
        );
    }

    /**
     * This function returns a new infinite LazyList which is this LazyList cycled infinitely often.
     * This function is the identity on infinite LazyLists.
     *
     * e.g. LazyList.of(1,2,3).cycle() == [1,2,3,1,2,3,1,2,3,1,...
     *
     * Haskell analogue: cycle :: [a] -> [a]
     *
     * @return this LazyList as an infinite cycle
     * @throws EmptyListException on an empty LazyList as there is nothing to cycle!
     */
    public LazyList<T> cycle() throws EmptyListException {
        if (listElementGenerator.apply(internalList) == null) { // This list is finite and already completely evaluated:
            if (this.internalList.isEmpty()) {
                throw new EmptyListException(); // it's impossible to cycle the empty list!!
            }

            int cycleLen = this.internalList.size();
            return new LazyList<T>(
                    new LinkedList<T>(this.internalList),
                    (list) -> {
                        int currentLen = list.size();
                        T newCycledValue = list.get(currentLen % cycleLen); // !!! (The cycle happens right here!)
                        list.add(newCycledValue);
                        return newCycledValue;
                    }
            );
        } else {
            throw new UnsupportedOperationException("ToDo: cycle LazyList that's not fully evaluated yet");
        }

        //return this.append(this.cycle());
        /*
        return new LazyList<T>(
                new LinkedList<>(this.internalList),
                (list) -> {
                    T next = this.listElementGenerator.apply(this.internalList);
                    if (next != null) {
                        return next;
                    } else {
                        // ???!!!
                    }
                }
        );
        */
    }

    /**
     * Zips two LazyLists together into one, using a certain function that has to be specified.
     *
     * Haskell analogue: zipWith :: (a -> b -> c) -> [a] -> [b] -> [c]
     *
     * @param zipFunction
     * @param otherList
     * @param <U>
     * @param <V>
     * @return
     */
    public <U,V> LazyList<V> zipWith(BiFunction<T,U,V> zipFunction, LazyList<U> otherList) {
        return new LazyList<V>(
                new LinkedList<V>(),
                (list) -> {
                    int index = list.size();
                    try {
                        T zipValue1 = this.get(index);
                        U zipValue2 = otherList.get(index);
                        V zippedValue = zipFunction.apply(zipValue1, zipValue2);
                        list.add(zippedValue);
                        return zippedValue;
                    } catch (IndexOutOfBoundsException ex) { // end of either this or otherList reached:
                        return null;
                    }
                }
        );
    }

    /**
     * Zips this LazyList with the other specified LazyList, creating a single LazyList of Pairs.
     * Calling this is equivalent to calling zipWith(Pair::new, otherList)
     *
     * Haskell analogue:
     * zip :: [a] -> [b] -> [(a, b)]
     * zip =  zipWith (,)
     *
     * @param otherList
     * @param <U>
     * @return
     */
    public <U> LazyList<Pair<T,U>> zip(LazyList<U> otherList) {
        return zipWith(Pair::new, otherList);
    }

    /**
     * Undoes the zip() operation.
     * Takes a LazyList of Pairs and gives back the two "original" LazyLists that were joined
     * together.
     *
     * Haskell analogue: unzip :: [(a, b)] -> ([a], [b])
     *
     * @return
     * @throws UnsupportedOperationException when this is not a LazyList of Pairs
     */
    public Pair<LazyList<?>,LazyList<?>> unzip() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("ToDo: unzip");
    }

    /**
     * Splits this LazyList at the specified index, giving back two LazyLists.
     * Just like in Haskell, this method won't throw an Exception when the specified index
     * is negative or too large (see examples below).
     *
     * Haskell analogue:
     * splitAt :: Int -> [a] -> ([a], [a])
     * splitAt n xs = (take n xs, drop n xs)
     * splitAt 2 [0,1,2,3,4] == ([0,1],[2,3,4])
     * splitAt (-1) [0,1,2,3,4] == ([],[0,1,2,3,4])
     * splitAt 100 [0,1,2,3,4] == ([0,1,2,3,4],[])
     *
     * @param index the number of elements in the first list or, equivalently,
     *              the first index that shall be in the second LazyList returned
     * @return a Pair of two LazyLists, the result of splitting this LazyList at the given index
     */
    public Pair<LazyList<T>,LazyList<T>> splitAt(int index) {
        return new Pair<>(this.take(index), this.drop(index));
    }

    /**
     * Concatenates two LazyLists.
     * Not to be confused with the LazyList.concat() function which concatenates all the LazyLists in a
     * LazyList of LazyLists, also giving a single LazyList as the result!
     *
     * Haskell analogue: (++) :: [a] -> [a] -> [a]
     *
     * @param secondList
     * @return
     */
    public LazyList<T> append(LazyList<T> secondList) {
        return new LazyList<T>(
                new LinkedList<T>(this.internalList),
                (list) -> {
                    T next = this.listElementGenerator.apply(this.internalList);
                    if (next != null) {
                        list.add(next);
                        return next;
                    } else {
                        throw new UnsupportedOperationException("ToDo ?!");
                    }
                }
        );
    }

    /**
     * Appends all the elements from otherList to this LazyList that aren't already in this LazyList.
     * Duplicates are eliminated from otherList but not from this LazyList.
     *
     * Haskell analogue: Data.List.union :: Eq a => [a] -> [a] -> [a]
     *
     * @param otherList
     * @return the union of this LazyList and otherList
     */
    public LazyList<T> union(LazyList<T> otherList) {
        return null;
    }

    /**
     * Retains only those elements from this LazyList that also occur in otherList.
     * Duplicates in otherList are irrelevant, duplicates in this LazyList are not eliminated!
     *
     * Haskell analogue: Data.List.intersect :: Eq a => [a] -> [a] -> [a]
     *
     * @param otherList
     * @return the intersection of this LazyList and otherList
     */
    public LazyList<T> intersect(LazyList<T> otherList) {
        return null;
    }

    /**
     * Unlike intersect() this function can also compute the intersection of two infinite LazyLists.
     * However, in order for that to work, both lists have to be in ascending order.
     *
     * @param otherList
     * @return
     */
    public LazyList<T> intersectOrdered(LazyList<T> otherList) {
        return null;
    }

    /**
     * Set difference:
     * Returns this LazyList with all the elements removed that also occur in otherList.
     * Duplicates in otherList are irrelevant, duplicates in this LazyList are not eliminated!
     *
     * Haskell analogue: (Data.List.\\) :: Eq a => [a] -> [a] -> [a]
     *
     * Synonyms: except() and minus()
     *
     * @param otherList
     * @return the set difference (this LazyList without otherList)
     */
    public LazyList<T> without(LazyList<T> otherList) {
        return null;
    }

    /**
     * Set difference:
     * Returns this LazyList with all the elements removed that also occur in otherList.
     * Duplicates in otherList are irrelevant, duplicates in this LazyList are not eliminated!
     *
     * Haskell analogue: (Data.List.\\) :: Eq a => [a] -> [a] -> [a]
     *
     * Synonyms: without() and minus()
     *
     * @param otherList
     * @return the set difference (this LazyList except otherList)
     */
    public LazyList<T> except(LazyList<T> otherList) {
        return this.without(otherList);
    }

    /**
     * Set difference:
     * Returns this LazyList with all the elements removed that also occur in otherList.
     * Duplicates in otherList are irrelevant, duplicates in this LazyList are not eliminated!
     *
     * Haskell analogue: (Data.List.\\) :: Eq a => [a] -> [a] -> [a]
     *
     * Synonyms: without() and except()
     *
     * @param otherList
     * @return the set difference (this LazyList minus otherList)
     */
    public LazyList<T> minus(LazyList<T> otherList) {
        return this.without(otherList);
    }



    /**
     * Returns this LazyList with duplicates removed.
     *
     * Haskell analogue: Data.List.nub :: Eq a => [a] -> [a]
     *
     * @return this LazyList with duplicates removed
     */
    public LazyList<T> nub() {
        return new LazyList<T>(
                new LinkedList<T>(),
                (list) -> {
                    return this.filter((x) -> !list.contains(x)).headOrNull(); // Incredibly smart solution!
                }
        );
    }






    // ----- Sequencing (Haskell Control.DeepSeq analogues): -----






    /**
     * Simply returns the given argument.
     *
     * Haskell analogue: seq :: a -> b -> b
     *
     * @param obj any Object
     * @return obj
     */
    public <U> U seq(U obj) {
        return obj;
    }

    /**
     * Fully evaluates this LazyList, before returning the given argument.
     * Unlike seq(), traverses this entire data structure (LazyList) evaluating it completely.
     *
     * This function won't terminate on an infinite LazyList.
     *
     * Haskell analogue:
     * Control.DeepSeq.deepseq :: Control.DeepSeq.NFData a => a -> b -> b
     * deepseq :: NFData a => a -> b -> b
     *
     * @param obj any Object
     * @return obj – but only after completely evaluating/traversing this LazyList!
     */
    public <U> U deepseq(U obj) {
        this.traverse();
        return obj;
    }

    /**
     * Fully evaluates this LazyList and then returns it.
     * A synonym for the traverse() method.
     *
     * This function won't terminate on an infinite LazyList.
     *
     * Haskell analogue:
     * Control.DeepSeq.force :: Control.DeepSeq.NFData a => a -> a
     * force :: NFData a => a -> a
     * force x = x `deepseq` x
     *
     * @return
     */
    public LazyList<T> force() {
        return this.traverse(); // == this.traverse(); return this;
    }






    // ----- Folds: -----






    /**
     *
     * Haskell analogue: foldr :: (a -> b -> b) -> b -> [a] -> b
     *
     * @param foldFunction
     * @param initialValue
     * @param <B>
     * @return
     */
    public <B> B foldr(BiFunction<T,B,B> foldFunction, B initialValue) {
        return null;
    }

    /**
     *
     * Haskell analogue: foldl :: (b -> a -> b) -> b -> [a] -> b
     *
     * @param foldFunction
     * @param initialValue
     * @param <B>
     * @return
     */
    public <B> B foldl(BiFunction<B,T,B> foldFunction, B initialValue) {
        return null;
    }

    /**
     *
     * Haskell analogue: foldr1 :: (a -> a -> a) -> [a] -> a
     *
     * @param foldFunction
     * @return
     */
    public T foldr1(BiFunction<T,T,T> foldFunction) throws EmptyListException {
        /*
        -- Haskell:
        foldr1           :: (a -> a -> a) -> [a] -> a
        foldr1 f [x]     =  x
        foldr1 f (x:xs)  =  f x (foldr1 f xs)
        foldr1 _ []      =  error "Prelude.foldr1: empty list"
        */

        return null;
    }

    /**
     *
     * Haskell analogue: foldl1 :: (a -> a -> a) -> [a] -> a
     *
     * @param foldFunction
     * @return
     */
    public T foldl1(BiFunction<T,T,T> foldFunction) throws EmptyListException {
        /*
        -- Haskell:
        foldl1           :: (a -> a -> a) -> [a] -> a
        foldl1 f (x:xs)  =  foldl f x xs
        foldl1 _ []      =  error "Prelude.foldl1: empty list"
         */

        return null;
    }

    /**
     * Returns the smallest value of this LazyList.
     * When the elements of this LazyList are not comparable, a ClassCastException is thrown.
     * The minimum of an empty LazyList cannot be evaluated, a EmptyListException will be thrown.
     *
     * Haskell analogue: minimum :: (Ord a) => [a] -> a
     *
     * @return the smallest value of this LazyList
     * @throws EmptyListException on an empty LazyList
     * @throws ClassCastException when the type T is not comparable (i.e. (Comparable<T>) cast fails)
     */
    public T minimum() throws EmptyListException, ClassCastException {
        // minimum []       =  error "Prelude.minimum: empty list"
        // minimum xs       =  foldl1 min xs
        if (this.isEmpty()) {
            throw new EmptyListException();
        } else {
            return this.foldl1((x,y) -> Math.signum(((Comparable<T>) x).compareTo(y)) == 1 ? y : x);  // ! unchecked cast !
        }
    }

    /**
     * Returns the largest value of this LazyList.
     * When the elements of this LazyList are not comparable, a ClassCastException is thrown.
     * The minimum of an empty LazyList cannot be evaluated, a EmptyListException will be thrown.
     *
     * Haskell analogue: maximum :: (Ord a) => [a] -> a
     *
     * @return the largest value of this LazyList
     * @throws EmptyListException on an empty LazyList
     * @throws ClassCastException when the type T is not comparable (i.e. (Comparable<T>) cast fails)
     */
    public T maximum() throws EmptyListException, ClassCastException {
        // maximum []       =  error "Prelude.maximum: empty list"
        // maximum xs       =  foldl1 max xs
        if (this.isEmpty()) {
            throw new EmptyListException();
        } else {
            return this.foldl1((x,y) -> Math.signum(((Comparable<T>) x).compareTo(y)) == -1 ? y : x);  // ! unchecked cast !
        }
    }

    /**
     * Returns the average of all the numbers in this LazyList as a Double.
     * Throws a ClassCastException when the type T of this LazyList cannot be cast to Double.
     *
     * @return the average of all the numbers in this LazyList as a Double
     * @throws ClassCastException when the type T of this LazyList cannot be cast to Double.
     */
    public double average() throws ClassCastException {
        throw new UnsupportedOperationException("ToDo: average()");
    }

    // -- https://www.haskell.org/onlinereport/standard-prelude.html :
    // -- sum and product compute the sum or product of a finite list of numbers.
    // sum, product     :: (Num a) => [a] -> a
    // sum              =  foldl (+) 0
    // product          =  foldl (*) 1

    /**
     * Returns the sum of all the numbers in this LazyList as a double.
     * If the list is empty, 0.0 (the empty sum) is returned.
     *
     * Haskell analogue:
     * sum :: (Num a) => [a] -> a
     * sum =  foldl (+) 0
     *
     * @return the sum of all the numbers in this LazyList as a double
     * @throws ClassCastException when type T is not a subclass of Number
     */
    public double sum() throws ClassCastException {
        return this.foldl((x,y) -> ((Number) x).doubleValue() + ((Number) y).doubleValue(), 0.0);
    }

    /**
     * Returns the product of all the numbers in this LazyList as a double.
     * If the list is empty, 1.0 (the empty product) is returned.
     *
     * Haskell analogue:
     * product :: (Num a) => [a] -> a
     * product =  foldl (*) 1
     *
     * @return the product of all the numbers in this LazyList as a double
     * @throws ClassCastException when type T is not a subclass of Number
     */
    public double product() throws ClassCastException {
        return this.foldl((x,y) -> ((Number) x).doubleValue() * ((Number) y).doubleValue(), 1.0);
    }






    // ----- Scans (work just like the Folds, just that instead of a single value the list of intermediate values is returned): -----






    /**
     *
     * Haskell analogue: scanr :: (a -> b -> b) -> b -> [a] -> [b]
     *
     * @param foldFunction
     * @param initialValue
     * @param <B>
     * @return
     */
    public <B> LazyList<B> scanr(BiFunction<T,B,B> foldFunction, B initialValue) {
        return null;
    }

    /**
     *
     * Haskell analogue: scanl :: (b -> a -> b) -> b -> [a] -> [b]
     *
     * @param foldFunction
     * @param initialValue
     * @param <B>
     * @return
     */
    public <B> LazyList<B> scanl(BiFunction<B,T,B> foldFunction, B initialValue) {
        return null;
    }

    /**
     *
     * Haskell analogue: scanr1 :: (a -> a -> a) -> [a] -> [a]
     *
     * @param foldFunction
     * @return
     */
    public LazyList<T> scanr1(BiFunction<T,T,T> foldFunction) throws EmptyListException {
        return null;
    }

    /**
     *
     * Haskell analogue: scanl1 :: (a -> a -> a) -> [a] -> [a]
     *
     * @param foldFunction
     * @return
     */
    public LazyList<T> scanl1(BiFunction<T,T,T> foldFunction) throws EmptyListException {
        return null;
    }






    // ----- String methods from Haskell: -----






    /**
     * Like the toString() method but lazy - the result is therefore a
     * LazyList<Character> ("lazy String") instead of an ordinary Java String.
     *
     * Haskell analogue: show :: Show a => a -> String
     *
     * @return a lazy String representation of this list
     */
    public LazyList<Character> show() {
        throw new UnsupportedOperationException("ToDo: show");
    }

    /**
     * Split this LazyList of Characters ("lazy String") by newlines.
     * The inverse of unlines().
     *
     * Haskell analogue:
     * lines :: String -> [String]
     * lines "Hello\nWorld" == ["Hello","World"]
     *
     * @return this LazyList of Characters split by newlines
     * @throws UnsupportedOperationException when this is not a LazyList of Characters!
     */
    public LazyList<String> lines() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("ToDo: lines");
    }

    /**
     * Join this LazyLists of (lazy) Strings together using newlines.
     * The inverse of lines().
     *
     * Haskell analogue:
     * unlines :: [String] -> String
     * unlines = concat . map (++ "\n")
     * unlines ["Hello","World"] == "Hello\nWorld\n"
     *
     * @return this LazyList of (lazy) Strings joined together using newlines
     * @throws UnsupportedOperationException when this is not a LazyList of either Strings or
     *   LazyLists of Characters
     */
    public LazyList<Character> unlines() throws UnsupportedOperationException {
        try {
            return this.map((str) -> (String) str + "\n").concat().map((x) -> (Character) x);
            // second map: because a cast from LazyList<Object> to LazyList<Character> is impossible
        } catch (Exception ex1) {
            // This is NOT a LazyList of Strings: try LazyList of LazyLists of Characters instead:
            try {
                return this.map((list) -> ((LazyList<Character>)list).append(LazyList.of('\n'))).concat().map((x) -> (Character) x);
            } catch (Exception ex2) {
                throw new UnsupportedOperationException(); // error!
            }
        }
    }

    /**
     * Split this LazyList of Characters ("lazy String") by spaces.
     * The inverse of unwords().
     *
     * Haskell analogue:
     * words :: String -> [String]
     * lines "Hello World" == ["Hello","World"]
     *
     * @return this LazyList of Characters split by spaces
     * @throws UnsupportedOperationException when this is not a LazyList of Characters!
     */
    public LazyList<String> words() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("ToDo: words");
    }

    /**
     * Join this LazyLists of (lazy) Strings together using spaces.
     * The inverse of words().
     *
     * Haskell analogue:
     * unwords :: [String] -> String
     * unwords ws =  foldr1 (\w s -> w ++ ' ':s) ws
     * unwords ["Hello","World"] == "Hello World"
     *
     * @return this LazyList of (lazy) Strings joined together using spaces
     * @throws UnsupportedOperationException when this is not a LazyList of either Strings or
     *   LazyLists of Characters
     */
    public LazyList<Character> unwords() throws UnsupportedOperationException {
        try {
            return this.map((str) -> (String) str + " ").concat().map((x) -> (Character) x).init();
            // second map: because a cast from LazyList<Object> to LazyList<Character> is impossible
            // init: so there's no "dangling" space at the end (cf. foldr1 definition above) !
        } catch (Exception ex) {
            // This is NOT a LazyList of Strings: try LazyList of LazyLists of Characters instead:
            try {
                return this.map((list) -> ((LazyList<Character>)list).append(LazyList.of(' '))).concat().map((x) -> (Character) x).init();
            } catch (Exception ex2) {
                throw new UnsupportedOperationException(); // error!
            }
        }
    }






    // ----- Advanced methods (that don't come from Haskell but are necessary/convenient in Java): -----






    /**
     * Split this "lazy String" (LazyList<Character>) by the specified delimiter.
     * Similar to public String[] split(String regex) of java.lang.String but with 2 differences:
     * a) input and output are lazy!
     * b) regular expressions are not supported (yet)
     *
     * @param delimiter a sequence of at least 1 character to split by
     * @return this "lazy String" split by the specified delimiter
     * @throws UnsupportedOperationException when this is not a LazyList of Characters!
     */
    public LazyList<String> split(String delimiter) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("ToDo: split");
    }



    /**
     * Turns a String into a LazyList of Characters.
     *
     * In Haskell lists of Chars are already exactly the same as Strings:
     *     type String = [Char]
     *
     * @param str
     * @return
     */
    public static LazyList<Character> fromString(String str) {
        // Transform String -> char[] -> LinkedList<Character> -> LazyList<Character>
        char[] charArray = str.toCharArray();
        LinkedList<Character> lili = new LinkedList<>();
        for (char chr : charArray) {
            lili.add(chr);
        }
        return new LazyList<Character>(lili, (x) -> null);
    } // This is not listed under 'Static methods' because it's closely related to the asString() method below.


    /**
     * If this is a finite LazyList of Characters, the representation as a String is returned.
     * When this LazyList is empty, the empty String is returned.
     * When this is an infinite LazyList of Characters, this method never returns.
     * When this is a LazyList of something other than Characters, an UnsupportedOperationException is thrown.
     *
     * In Haskell lists of Chars are already exactly the same as Strings:
     *     type String = [Char]
     *
     * @return this finite LazyList of Characters as a String
     * @throws UnsupportedOperationException when the Type T of this LazyList is something other than Character.
     */
    public String asString() throws UnsupportedOperationException {
        if (this.isEmpty()) {
            return "";
        } else {
            try {
                StringBuilder strB = new StringBuilder();
                for (T chr : this) {
                    strB.append((Character) chr);
                }
                return strB.toString();
            } catch (ClassCastException ex) {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * Turns an InputStream into a LazyList<Integer>.
     * The Integers resemble bytes and are therefore between 0 and 255.
     *
     * --- Example: Reading a text file lazily ---
     * LazyList<Integer> lazyBytes = LazyList.fromInputStream(new FileInputStream("MyClass.java"));
     * LazyList<String> lazyLines = lazyBytes.map((i) -> (char)(int)i).lines();
     * LazyList<String> linesWithoutTabs = lazyLines.map(String::trim);
     * LazyList<String> nonCommentLines = linesWithoutTabs.filter((line) -> !line.startsWith("//"));
     * LazyList<String> methodDeclarations = nonCommentLines.filter((l) -> l.matches("(void|public|private|protected)*"));
     * LazyList<String> declarationsWithoutParameters = methodDeclarations.map((d) -> d.split("\\(")[0]);
     * if (declarationsWithoutParameters.any((str) -> str.contains("_"))) {
     *     System.out.println("You're not supposed to use snake_case for method names mate!");
     * }
     *
     * @param inputStream a java.io.InputStream
     * @return a LazyList of bytes represented by Integers
     */
    public static LazyList<Integer> fromInputStream(InputStream inputStream) {
        return new LazyList<Integer>(
                new LinkedList<Integer>(),
                (list) -> {
                    if (inputStream == null) {
                        return null;
                    }

                    int newByte;
                    try {
                        newByte = inputStream.read();
                    } catch (IOException ex) {
                        return null; // IOException => LazyList ends
                    }

                    if (newByte == -1) { // The end of the stream has been reached:
                        try {
                            inputStream.close();
                        } catch (IOException ex) {}
                        return null; // stream ends => LazyList ends
                    } else {
                        list.add(newByte); // list.add((Byte) (byte) newByte);
                        return newByte; // return (Byte) (byte) newByte;
                    }
                }
        );
    }

    /**
     * Turns a LazyList<Integer> into an InputStream.
     *
     * @return an InputStream of this LazyList
     * @throws ClassCastException when the Type T of this LazyList cannot be cast to Integer.
     */
    public java.io.InputStream asInputStream() throws ClassCastException {
        return new InputStream() {
            int position = 0;
            @Override
            public int read() throws IOException {
                try {
                    return (Integer) get(position++);
                } catch (IndexOutOfBoundsException ex) {
                    return -1; // LazyList ends => InputStream ends
                }
            }
        };
    }

    /**
     * Read an ASCII text file as a "lazy String".
     *
     * @param file the ASCII text file to read
     * @return the given file read as a lazy String"
     * @throws FileNotFoundException when file was not found (passed from FileInputStream)
     */
    public static LazyList<Character> fromASCIIFile(File file) throws FileNotFoundException {
        return LazyList.fromInputStream(new FileInputStream(file)).map((i) -> (char)(int)i);
    }

    /**
     * Write the Integers in this LazyList to the given OutputStream.
     *
     * @param outputStream the OutputStream to write to
     * @throws IOException when the given outputStream throws an IOException on write() or close()
     * @throws ClassCastException when the type T of this LazyList cannot be cast to Integer
     */
    public void writeToOutputStream(OutputStream outputStream) throws IOException, ClassCastException {
        for (T el : this) {
            outputStream.write((Integer) el);
        }
        outputStream.close();
    }

    /**
     * Write the Characters in this LazyList to the given text file
     * (which is automatically created if it doesn't exist yet).
     *
     * @param textFile the text file to write this LazyList to
     * @throws UnsupportedOperationException when this is not a LazyList of Characters (i.e. "lazy String")
     */
    public void writeAsStringToTextFile(File textFile) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("ToDo: writeAsStringToTextFile");
    }

    /**
     * Write the Integers in this LazyList, interpreted as bytes, to the given file
     * (which is automatically created if it doesn't exist yet).
     *
     * @param file the (binary) file to write this LazyList to
     * @throws UnsupportedOperationException when the Type T of this LazyList cannot be cast to Integer
     */
    public void writeAsBinaryToFile(File file) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("ToDo: writeAsBinaryToFile");
    }

    /**
     * Completely traverses this LazyList from beginning to end, forcing a complete evaluation of all elements.
     * This function never returns when called on an infinite LazyList.
     * Calling this method is equivalent to calling the length() method and then discarding its result.
     * This function instead returns the LazyList again - possibly enabling more compact code.
     *
     * The Haskell equivalent is the Control.DeepSeq.force function, which is why there's also
     * a force() method which is just a synonym for traverse().
     *
     * @return this very LazyList (can be discarded)
     */
    public LazyList<T> traverse() {
        this.length(); // In order to determine the length, the LazyList has to be traversed completely.
        return this;
    }

    /**
     * Completely traverses this LazyList and then calls the given function on it.
     */
    public void traverseAndThen(Consumer<LazyList<T>> actionAfterTraversal) {
        this.traverse();
        actionAfterTraversal.accept(this);
    }






    // ----- LazyLists as maps: -----







    /**
     * Turn a Map into a LazyList of Pairs.
     *
     * @param map
     * @param <A>
     * @param <B>
     * @return
     */
    public static <A,B> LazyList<Pair<A,B>> fromMap(java.util.Map<A,B> map) {
        return new LazyList<>(map.entrySet()).map((mapEntry) -> new Pair<>(mapEntry.getKey(), mapEntry.getValue()));
    }

    /**
     * Lookup the value for a given key in a LazyList of pairs – which represents a Map.
     * This method will fail with an UnsupportedOperationException on LazyLists
     * that do not contain Pairs as their elements.
     *
     * Haskell analogue: lookup :: Eq a => a -> [(a, b)] -> Maybe b
     *
     * @param key the key to look up the value for
     * @return the value the given key is mapped to or the empty Optional if the given key wasn't found
     * @throws UnsupportedOperationException when this is not a LazyList of Pairs
     */
    public Optional<Object> lookup(Object key) throws UnsupportedOperationException {
        try {
            LazyList<Pair<?, ?>> matches = (LazyList<Pair<?, ?>>) this.filter((pair) -> key.equals(((Pair<?, ?>) pair).fst()));
            if (!matches.isEmpty()) { // == matches.lengthIsAtLeast(1): NOT just .length() so this only works on infinite maps!!
                return Optional.of(matches.get(0).snd()); // simply return the value for the first match, i.e. the first Pair with 'key' as its Key
            } else {
                return Optional.empty(); // not found.
            }
        } catch (ClassCastException ex) { // this is not a LazyList of Pairs!
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns a mutable view of this LazyList as a Map.
     * All changes in the LazyList are instantaneously passed to the Map view and vice-versa!
     * This only works when this LazyList is a LazyList of Pairs!
     *
     * Very important note:
     * This function essentially enables you to work with infinite Maps!!!
     *
     * Beware that the put() and putAll() methods will insert at the beginning and not at the end
     * of this LazyList to increase efficiency and in order to work with infinite LazyLists/Maps
     * at all!
     *
     * Beware that, for the same reason, there will be no duplicate-elimination/-checks
     * when calling put() or putAll()! (put() will always return null!)
     * This means you either have to ensure that no duplicates are inserted by yourself or that you
     * cannot trust
     * a) that size() will always be the exact number of distinct key-value-mappings in this Map
     * b) that remove() will actually remove a mapping
     * c) that there will be no two inconsistent mappings for the same key with different values
     *
     * Calling remove(key) on an infinite Map that doesn't contain a mapping for the specified key
     * will, of course, never terminate!
     *
     * @return a mutable view of this LazyList as a Map
     * @throws UnsupportedOperationException when this is not a LazyList of Pairs
     */
    public Map<Object,Object> viewAsMap() throws UnsupportedOperationException {
        return new Map<Object, Object>() {
            @Override
            public int size() {
                return LazyList.this.size();
            }

            @Override
            public boolean isEmpty() {
                return LazyList.this.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return LazyList.this.any((pair) -> key.equals(((Pair<?,?>)pair).fst()));
            }

            @Override
            public boolean containsValue(Object value) {
                return LazyList.this.any((pair) -> value.equals(((Pair<?,?>)pair).snd()));
            }

            @Override
            public Object get(Object key) {
                // For whatever reason, the Java Map interface does not use Optional<> for lookup.
                return LazyList.this.lookup(key).orElse(null);
            }

            @Override
            public Object put(Object key, Object value) {
                LazyList.this.internalList.add(0, (T)Pair.newPair(key, value)); // !!TRICKY GENERICS!!
                // Insertion happens at the beginning of the LazyList, see javadoc for explanation.
                return null; // Important: put() assumes that the key is always new, see javadoc for explanation.
            }

            @Override
            public Object remove(Object key) {
                // Important note: remove() doesn't think of duplicates and terminates
                // a) after the first successful key match
                // b) when it reaches the end of the underlying LazyList

                // 1) See whether there's a key match in the already evaluated part of this LazyList:
                for (Object pair : LazyList.this.internalList) {
                    if (key.equals(((Pair<?,?>)pair).fst())) {
                        Object previousValue = ((Pair<?,?>)pair).snd();
                        LazyList.this.internalList.remove(pair);
                        return previousValue;
                    }
                }

                // 2) Keep on traversing the LazyList - looking for a Pair with the desired 'key'
                //    until it's found or the LazyList ends:
                // (for loop: cf. any())
                for (T pair = listElementGenerator.apply(internalList); pair != null; pair = listElementGenerator.apply(internalList)) {
                    if (key.equals(((Pair<?,?>)pair).fst())) {
                        Object previousValue = ((Pair<?,?>)pair).snd();
                        LazyList.this.internalList.remove(pair); // has just been added/generated and is now immediately removed. cool, isn't it?!
                        return previousValue;
                    }
                }
                return null; // LazyList evaluation ended, key not found, nothing was removed!
            }

            @Override
            public void putAll(Map<?, ?> map) {
                // Naive implementation: just call put() repeatedly:
                for (Map.Entry<?,?> entry: map.entrySet()) {
                    this.put(entry.getKey(), entry.getValue());
                }
            }

            @Override
            public void clear() {
                LazyList.this.clear();
            }

            @Override
            public Set<Object> keySet() { // ToDo!
                return new Set<Object>() {
                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public Iterator<Object> iterator() {
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @Override
                    public <T> T[] toArray(T[] a) {
                        return null;
                    }

                    @Override
                    public boolean add(Object o) {
                        return false;
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }
                };
            }

            @Override
            public Collection<Object> values() { // ToDo!
                return new Collection<Object>() {
                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public Iterator<Object> iterator() {
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @Override
                    public <T> T[] toArray(T[] a) {
                        return null;
                    }

                    @Override
                    public boolean add(Object o) {
                        throw new UnsupportedOperationException();
                        // this operation would make no sense since there is no key specified!
                        // cf. https://docs.oracle.com/javase/8/docs/api/java/util/Map.html#values--
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<?> collection) {
                        throw new UnsupportedOperationException();
                        // this operation would make no sense since there is no key specified!
                        // cf. https://docs.oracle.com/javase/8/docs/api/java/util/Map.html#values--
                    }

                    @Override
                    public boolean removeAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }
                };
            }

            @Override
            public Set<Entry<Object, Object>> entrySet() { // ToDo!
                return new Set<Entry<Object, Object>>() {
                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public Iterator<Entry<Object, Object>> iterator() {
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @Override
                    public <T> T[] toArray(T[] a) {
                        return null;
                    }

                    @Override
                    public boolean add(Entry<Object, Object> objectObjectEntry) {
                        return false;
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends Entry<Object, Object>> c) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }
                };
            }
        };
    }






    // ----- Iterable.forEach(Consumer<? super T> action) Alternatives: -----






    /**
     * This function simply runs the .forEach() method and then returns back this very LazyList.
     * This function only exists to enable you to make more compact one-liners!
     *
     * --- Example with just the standard forEach(): ---
     * LazyList<Integer> countdown = LazyList.fromTo(10,0);
     * countdown.forEach(System.out::println);
     * System.out.println(countdown.sum());
     *
     * --- Example with forEachAndThen(): ---
     * System.out.println(
     *     LazyList.fromTo(10,0)
     *     .forEachAndThen(System.out::println)
     *     .sum()
     * );
     *
     * @param action The action to be performed for each element (using .forEach(action))
     * @return this very LazyList again
     */
    public LazyList<T> forEachAndThen(Consumer<? super T> action) {
        this.forEach(action);
        return this;
    }

    /**
     * Similar to the .forEach() method – only that the given consumer function doesn't just
     * get the elements in order but also their index.
     *
     * Example:
     * LazyList.fromThen(10,20).forEachWithIndex((el,i) -> System.out.println("#" + i + ": " + el));
     *
     * @param action The action to be performed for each element and index
     * @return this very LazyList again
     */
    public LazyList<T> forEachWithIndex(BiConsumer<? super T, Integer> action) {
        int index = 0;
        for (T el : this) {
            action.accept(el, index);
            index++;
        }
        return this;
    }

    /**
     * Similar to the .forEachWithIndex() method – only that the given consumer function doesn't just
     * get the elements in order and their index but also the length of the entire list.
     * Note that this obviously doesn't work on infinite LazyLists!
     *
     * Example:
     * LazyList.fromThenTo(10,20,100).forEachWithIndexAndLength((el,i,len) -> System.out.println("#" + i + "/" + len + ": " + el));
     *
     * @param action The action to be performed for each element, given index and total length
     * @return this very LazyList again
     */
    public LazyList<T> forEachWithIndexAndLength(TriConsumer<? super T, Integer, Integer> action) {
        int len = this.length();
        int index = 0;
        for (T el : this) {
            action.accept(el, index, len);
            index++;
        }
        return this;
    }






    // ----- All the methods that have to be implemented for the java.util.List interface: ----






    /**
     * Returns the number of elements in this list.  If this list contains
     * more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        return this.length();
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return {@code true} if this list contains no elements
     */
    @Override
    public boolean isEmpty() {
        // return (this.length() == 0);
        // -> TOTALLY WRONG! This wouldn't terminate on infinite lists and
        //    would also be terribly inefficient even on finite LazyLists!

        return this.lengthEquals(0); // !!!!!
    }

    /**
     * Returns {@code true} if this list contains the specified element.
     * More formally, returns {@code true} if and only if this list contains
     * at least one element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this list
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              list does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean contains(Object o) {
        try {
            return this.elem((T) o);
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int position = 0;

            @Override
            public boolean hasNext() {
                while (internalList.size() <= position && listElementGenerator.apply(internalList) != null) {
                    // Keep evaluating this LazyList until either:
                    // a) we've reached position and we know there IS a next value!
                    // b) there's nothing left to generate anymore and we know there ISN'T a next value!
                }
                return internalList.size() > position;
            }

            @Override
            public T next() {
                return get(position++);
            }
        };
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must
     * allocate a new array even if this list is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list in proper
     * sequence
     * @see java.util.Arrays#asList(Object[])
     */
    @Override
    public Object[] toArray() {
        int len = this.length(); // Will already fully evaluate this LazyList (won't terminate on infinite LazyList).
        Object[] arr = new Object[len];
        for (int i = 0; i < len; i++) {
            arr[i] = this.get(i);
        }
        return arr;
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element); the runtime type of
     * the returned array is that of the specified array.  If the list fits
     * in the specified array, it is returned therein.  Otherwise, a new
     * array is allocated with the runtime type of the specified array and
     * the size of this list.
     *
     * <p>If the list fits in the specified array with room to spare (i.e.,
     * the array has more elements than the list), the element in the array
     * immediately following the end of the list is set to {@code null}.
     * (This is useful in determining the length of the list <i>only</i> if
     * the caller knows that the list does not contain any null elements.)
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a list known to contain only strings.
     * The following code can be used to dump the list into a newly
     * allocated array of {@code String}:
     *
     * <pre>{@code
     *     String[] y = x.toArray(new String[0]);
     * }</pre>
     * <p>
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of this list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of this list
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this list
     * @throws NullPointerException if the specified array is null
     */
    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException("ToDo: toArray");
    }

    /**
     * Appends the specified element to the end of this list (optional
     * operation).
     *
     * <p>Lists that support this operation may place limitations on what
     * elements may be added to this list.  In particular, some
     * lists will refuse to add null elements, and others will impose
     * restrictions on the type of elements that may be added.  List
     * classes should clearly specify in their documentation any restrictions
     * on what elements may be added.
     *
     * @param t element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException if the {@code add} operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this list
     * @throws NullPointerException          if the specified element is null and this
     *                                       list does not permit null elements
     * @throws IllegalArgumentException      if some property of this element
     *                                       prevents it from being added to this list
     */
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException("ToDo: mutability.");
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present (optional operation).  If this list does not contain
     * the element, it is unchanged.  More formally, removes the element with
     * the lowest index {@code i} such that
     * {@code Objects.equals(o, get(i))}
     * (if such an element exists).  Returns {@code true} if this list
     * contained the specified element (or equivalently, if this list changed
     * as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     * @throws ClassCastException            if the type of the specified element
     *                                       is incompatible with this list
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified element is null and this
     *                                       list does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the {@code remove} operation
     *                                       is not supported by this list
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("ToDo: mutability.");
    }

    /**
     * Returns {@code true} if this list contains all of the elements of the
     * specified collection.
     *
     * @param c collection to be checked for containment in this list
     * @return {@code true} if this list contains all of the elements of the
     * specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              list
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this list does not permit null
     *                              elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>),
     *                              or if the specified collection is null
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator (optional operation).  The behavior of this
     * operation is undefined if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws UnsupportedOperationException if the {@code addAll} operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this list
     * @throws NullPointerException          if the specified collection contains one
     *                                       or more null elements and this list does not permit null
     *                                       elements, or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this list
     * @see #add(Object)
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("ToDo: mutability.");
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position (optional operation).  Shifts the
     * element currently at that position (if any) and any subsequent
     * elements to the right (increases their indices).  The new elements
     * will appear in this list in the order that they are returned by the
     * specified collection's iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the
     * operation is in progress.  (Note that this will occur if the specified
     * collection is this list, and it's nonempty.)
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c     collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws UnsupportedOperationException if the {@code addAll} operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this list
     * @throws NullPointerException          if the specified collection contains one
     *                                       or more null elements and this list does not permit null
     *                                       elements, or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this list
     * @throws IndexOutOfBoundsException     if the index is out of range
     *                                       ({@code index < 0 || index > size()})
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("ToDo: mutability.");
    }

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection (optional operation).
     *
     * @param c collection containing elements to be removed from this list
     * @return {@code true} if this list changed as a result of the call
     * @throws UnsupportedOperationException if the {@code removeAll} operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of an element of this list
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this list contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("ToDo: mutability.");
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this list all of its elements that are not contained in the
     * specified collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     * @throws UnsupportedOperationException if the {@code retainAll} operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of an element of this list
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this list contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("ToDo: mutability.");
    }

    /**
     * Removes all of the elements from this list (optional operation).
     * The list will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *                                       is not supported by this list
     */
    @Override
    public void clear() {
        // Reset:
        this.internalList = new LinkedList<>();
        this.listElementGenerator = (x) -> null;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    @Override
    public T get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        } else {
            // When (index < this.internalList.size()) from the beginning, this while loop never "runs":
            while (index >= this.internalList.size() && listElementGenerator.apply(internalList) != null) {
                // Keep evaluating this LazyList until either:
                // a) the index is reached
                // b) the LazyList ends
            }
            if (index < this.internalList.size()) {
                return this.internalList.get(index);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws UnsupportedOperationException if the {@code set} operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this list
     * @throws NullPointerException          if the specified element is null and
     *                                       this list does not permit null elements
     * @throws IllegalArgumentException      if some property of the specified
     *                                       element prevents it from being added to this list
     * @throws IndexOutOfBoundsException     if the index is out of range
     *                                       ({@code index < 0 || index >= size()})
     */
    @Override
    public T set(int index, T element) {
        if (element == null) {
            throw new NullPointerException(); // LazyLists do not support null elements!
        } else if (index < 0) {
            throw new IndexOutOfBoundsException();
        } else { // cf. get() above!:
            // First, we have to evaluate the LazyList up to the specified index:
            // When (index < this.internalList.size()) from the beginning, this while loop never "runs":
            while (index >= this.internalList.size() && listElementGenerator.apply(internalList) != null) {
                // Keep evaluating this LazyList until either:
                // a) the index is reached
                // b) the LazyList ends
            }
            if (index < this.internalList.size()) {
                return this.internalList.set(index, element);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).  Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws UnsupportedOperationException if the {@code add} operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this list
     * @throws NullPointerException          if the specified element is null and
     *                                       this list does not permit null elements
     * @throws IllegalArgumentException      if some property of the specified
     *                                       element prevents it from being added to this list
     * @throws IndexOutOfBoundsException     if the index is out of range
     *                                       ({@code index < 0 || index > size()})
     */
    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException("ToDo: mutability.");
    }

    /**
     * Removes the element at the specified position in this list (optional
     * operation).  Shifts any subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws UnsupportedOperationException if the {@code remove} operation
     *                                       is not supported by this list
     * @throws IndexOutOfBoundsException     if the index is out of range
     *                                       ({@code index < 0 || index >= size()})
     */
    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("ToDo: mutability.");
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index {@code i} such that
     * {@code Objects.equals(o, get(i))},
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     * this list, or -1 if this list does not contain the element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this list
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              list does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public int indexOf(Object o) {
        if (o == null) {
            throw new NullPointerException(); // LazyLists do not support null elements!
        }

        int internalIndexOf = this.internalList.indexOf(o);
        if (internalIndexOf != -1) {
            return internalIndexOf; // o already found in the evaluated part of this LazyList
        } else {
            // Keep evaluating this LazyList until either o is found or the LazyList ends:
            for (T next = listElementGenerator.apply(internalList); next != null; next = listElementGenerator.apply(internalList)) {
                if (next.equals(o)) {
                    break; // found!
                }
            }
            return this.internalList.indexOf(o);
        }
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index {@code i} such that
     * {@code Objects.equals(o, get(i))},
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in
     * this list, or -1 if this list does not contain the element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this list
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              list does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            throw new NullPointerException(); // LazyLists do not support null elements!
        }

        // Because the last occurrence could always be the last element, a complete traversal of the LazyList is needed:
        this.traverse();
        return this.internalList.lastIndexOf(o);
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * @return a list iterator over the elements in this list (in proper
     * sequence)
     */
    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("ToDo: listIterator()");
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * The specified index indicates the first element that would be
     * returned by an initial call to {@link ListIterator#next next}.
     * An initial call to {@link ListIterator#previous previous} would
     * return the element with the specified index minus one.
     *
     * @param index index of the first element to be returned from the
     *              list iterator (by a call to {@link ListIterator#next next})
     * @return a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("ToDo: listIterator(int index)");
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations supported
     * by this list.<p>
     * <p>
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).  Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>{@code
     *      list.subList(from, to).clear();
     * }</pre>
     * Similar idioms may be constructed for {@code indexOf} and
     * {@code lastIndexOf}, and all of the algorithms in the
     * {@code Collections} class can be applied to a subList.<p>
     * <p>
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex   high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *                                   ({@code fromIndex < 0 || toIndex > size ||
     *                                   fromIndex > toIndex})
     */
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        } else {
            // Keep evaluating this LazyList until the demanded subList lies completely within the internalList:
            while (toIndex > this.internalList.size()) {
                this.listElementGenerator.apply(this.internalList);
            }
            return this.internalList.subList(fromIndex, toIndex); // may throw IndexOutOfBoundsException!
        }
    }






    // ----- BooleanAnswer method variants: -----






    /**
     * Just like the any() method – only that it returns a BooleanAnswer instead of a boolean.
     *
     * @param predicate
     * @return
     */
    BooleanAnswer ifAny(Predicate<T> predicate) {
        return new BooleanAnswer(this.any(predicate));
    }

    /**
     *
     * @param predicate
     * @return
     */
    BooleanAnswer ifAll(Predicate<T> predicate) {
        return new BooleanAnswer(this.all(predicate));
    }

    /**
     *
     * @param n
     * @return
     */
    BooleanAnswer ifLengthIsAtLeast(int n) {
        return new BooleanAnswer(this.lengthIsAtLeast(n));
    }

    /**
     *
     * @param n
     * @return
     */
    BooleanAnswer ifLengthIsAtMost(int n) {
        return new BooleanAnswer(this.lengthIsAtMost(n));
    }

    /**
     *
     * @param value
     * @return
     */
    BooleanAnswer ifElem(T value) {
        return new BooleanAnswer(this.elem(value));
    }

    /**
     *
     * @param o
     * @return
     */
    BooleanAnswer ifContains(Object o) {
        return new BooleanAnswer(this.contains(o));
    }

    /**
     *
     * @param c
     * @return
     */
    BooleanAnswer ifContainsAll(Collection<?> c) {
        return new BooleanAnswer(this.containsAll(c));
    }

    /**
     *
     * @return
     */
    BooleanAnswer ifIsEmpty() {
        return new BooleanAnswer(this.isEmpty());
    }

    /**
     *
     * @param o
     * @return
     */
    BooleanAnswer ifEquals(Object o) {
        return new BooleanAnswer(this.equals(o));
    }






    // ----- The Comparable interface: ----






    /**
     * Haskell analogue: LT :: Ordering
     * Useful for comparison to the result of a LazyList.compareTo().
     */
    public static final int LT = -1;

    /**
     * Haskell analogue: EQ :: Ordering
     * Useful for comparison to the result of a LazyList.compareTo().
     */
    public static final int EQ = 0;

    /**
     * Haskell analogue: GT :: Ordering
     * Useful for comparison to the result of a LazyList.compareTo().
     */
    public static final int GT = +1;

    /**
     * Compares this LazyList to another List (which doesn't have to be a LazyList!)
     * lexicographically, i.e. the same way lists are compared in Haskell.
     * This function is completely type-safe (as long as the other List is not null) because
     * only Lists of elements that are comparable to T can be passed!
     *
     * Examples from Haskell:
     *   compare [1,2,3] [1,2,3] == EQ
     *   compare [1,2,3] [1,2,3,4] == LT
     *   compare [1,2,3,4] [1,2,3] == GT
     *   compare [1,2,3,4] [1,2,30,4] == LT
     *
     * @param o the List to compare to, has to be a List of elements that are comparable to T
     * @return a negative integer, zero, or a positive integer as this
     *    LazyList is less than (LT), equal to (EQ), or greater than (GT) the specified List.
     */
    @Override
    public int compareTo(List<? extends Comparable<T>> o) {
        // Haskell lists are compared in lexicographical order:
        // https://stackoverflow.com/questions/3651144/comparing-lists-in-haskell-or-more-specifically-what-is-lexicographical-order

        // This is simply the code of the equals(),
        // adapted to return either +1 or -1 instead of just false
        // and, of course, to use compareTo() instead of equals():

        Iterator<T> iteratorThis = this.iterator();
        Iterator<? extends Comparable<T>> iteratorOther = o.iterator();

        boolean thisHasNext, otherHasNext;
        while (true) {
            thisHasNext = iteratorThis.hasNext();
            otherHasNext = iteratorOther.hasNext();
            if (!thisHasNext && !otherHasNext) {
                return EQ; // both lists ended simultaneously/at the same point -> they're equal
            } else if (thisHasNext && otherHasNext) {
                T thisNext = iteratorThis.next();
                Comparable<T> otherNext = iteratorOther.next();
                switch (otherNext.compareTo(thisNext)) { // e.g. compare [1,2,3,4] [1,2,30,4] == LT
                    case LT: return GT; // has to be the other way around because thisNext.compareTo(otherNext) doesn't work!
                    case GT: return LT; // !!!
                } // case 0: no difference at this point yet, keep on looking...
            } else if (thisHasNext && !otherHasNext) { // other list ended before this list
                return GT; // !!! e.g. compare [1,2,3,4] [1,2,3] == GT
            } else { // (!thisHasNext && otherHasNext) // this list ended before other list
                return LT; // !!! e.g. compare [1,2,3] [1,2,3,4] == LT
            }
        }
        // Note that this while loop will never end on two infinite Lists that are equal!
        // cf. in Haskell "compare [0..] [0..]" also never terminates!
    }






    // ----- toString(), equals() and hashCode(): -----






    @Override
    public String toString() {
        this.traverse(); // (note: won't terminate on infinite LazyLists!)
        return this.internalList.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof List)) return false; // (o == null || getClass() != o.getClass())

        Iterator<T> iteratorThis = this.iterator();
        Iterator<?> iteratorOther = ((List<?>)o).iterator();

        boolean thisHasNext, otherHasNext;
        while (true) {
            thisHasNext = iteratorThis.hasNext();
            otherHasNext = iteratorOther.hasNext();
            if (!thisHasNext && !otherHasNext) {
                return true; // both lists ended simultaneously/at the same point -> they're equal
            } else if (thisHasNext && otherHasNext) {
                if (!Objects.equals(iteratorThis.next(), iteratorOther.next())) {
                    return false; // found a difference in the two lists -> they're not equal
                } // else: no difference at this point yet, keep on looking...
            } else {
                return false; // one of the two lists ended but the other one hasn't -> they're not equal
            }
        }
        // Note that this while loop will never end on two infinite Lists that are equal!
        // cf. in Haskell "[0..] == [0..]" also never terminates!

        /*
        try {
            LazyList<? extends Comparable<T>> otherLazyList = (LazyList<? extends Comparable<T>>) o;
            return (this.compareTo(otherLazyList) == 0);
        } catch (ClassCastException ex) {
            return false;
        }
         */
    }

    @Override
    public int hashCode() {
        // return this.internalList.hashCode();
        // --> WRONG! Equal lists have to have the same hash!!!
        // (and just because the evaluated part of two lists is different
        // doesn't mean that they are different!)

        if (this.isEmpty()) {
            return 0; // (the this.get(0) from below would fail on an empty list)
        } else {
            return Objects.hash(this.get(0));
        }

        // Note: Using only the first list element to calculate the hash has 2 advantages:
        // a) a hash can be calculated even of an infinite LazyList
        // b) a hash is calculated really quickly, making it fast to set up a HashSet of LazyLists for example
    }

}
