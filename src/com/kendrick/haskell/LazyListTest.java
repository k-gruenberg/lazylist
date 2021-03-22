package com.kendrick.haskell;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class LazyListTest {

    /**
     * This function does the exact opposite of
     * org.junit.jupiter.api.Assertions.assertTimeout()
     * It tests that the given Runnable DOES time out.
     *
     * @param testName a description of what this assertion is supposed to test
     * @param action the Runnable that should NOT finish execution!
     * @throws RuntimeException when the Runnable finished execution within 5000ms or when
     *     calling Thread.sleep(5000); threw an InterruptedException
     */
    static void assertTimeout_(String testName, Runnable action) throws RuntimeException {
        Thread thread = new Thread(action);
        thread.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            throw new RuntimeException(); // (to cause the calling unit test to fail!)
        }

        if (!thread.isAlive()) { // Thread HAS finished :(
            System.err.println("Timeout assertion failed: " + testName);
            throw new RuntimeException(); // (to cause the calling unit test to fail!)
        } else { // Thread hasn't finished as it was supposed to be! :)
            thread.stop();
        }
    }

    @Test
    void testConstructorsToStringAndPrint() {
        LazyList<Integer> emptyLazyList = new LazyList<>((Integer) null, null);
        LazyList<Integer> singleElementLazyList = new LazyList<>(42, null);
        LazyList<Integer> twoElementLazyList = new LazyList<>(11, new LazyList<>(22, null));

        LazyList<Integer> emptyLazyListFromCollection = new LazyList<>(Collections.emptyList());
        LazyList<Integer> singleElementLazyListFromCollection = new LazyList<>(List.of(1));
        LazyList<Integer> twoElementLazyListFromCollection = new LazyList<>(List.of(1,2));
        LazyList<Integer> longerLazyListFromCollection = new LazyList<>(List.of(9,8,7,6,5,4,3,2,1,0));

        assertEquals("[]", emptyLazyList.toString());
        assertEquals("[42]", singleElementLazyList.toString());
        assertEquals("[11, 22]", twoElementLazyList.toString());

        assertEquals("[]", emptyLazyListFromCollection.toString());
        assertEquals("[1]", singleElementLazyListFromCollection.toString());
        assertEquals("[1, 2]", twoElementLazyListFromCollection.toString());
        assertEquals("[9, 8, 7, 6, 5, 4, 3, 2, 1, 0]", longerLazyListFromCollection.toString());

        emptyLazyList.print(System.out);
        System.out.print("\n");
        singleElementLazyList.print(System.out);
        System.out.print("\n");
        twoElementLazyList.print(System.out);
        System.out.print("\n");

        emptyLazyListFromCollection.print(System.out);
        System.out.print("\n");
        singleElementLazyListFromCollection.print(System.out);
        System.out.print("\n");
        twoElementLazyListFromCollection.print(System.out);
        System.out.print("\n");
        longerLazyListFromCollection.print(System.out);
        System.out.print("\n");
    }

    @Test
    void testOfEmptyListAndEquals() {
        assertEquals(LazyList.of(), LazyList.emptyList());
        assertEquals(LazyList.emptyList(), LazyList.of());

        assertEquals(LazyList.of(), LazyList.of());
        assertEquals(LazyList.emptyList(), LazyList.emptyList());

        assertEquals(LazyList.of(666), LazyList.of(666));
        assertEquals(LazyList.of(32,54), LazyList.of(32,54));
        assertEquals(LazyList.of(43,77,32), LazyList.of(43,77,32));
        assertEquals(LazyList.of(1,2,3,4,5,6), LazyList.of(1,2,3,4,5,6));

        assertNotEquals(LazyList.of(1), LazyList.of());
        assertNotEquals(LazyList.of(1), LazyList.emptyList());
        assertNotEquals(LazyList.of(), LazyList.of(1));
        assertNotEquals(LazyList.emptyList(), LazyList.of(1));

        assertNotEquals(LazyList.of(1,2), LazyList.of(2,1));
        assertNotEquals(LazyList.of(1,2,3,4,5), LazyList.of(1,2,3,4,5,6));
        assertNotEquals(LazyList.of(1,2,3,4,5), LazyList.of(0,1,2,3,4,5));
        assertNotEquals(LazyList.of(1,2,3,4,5,6,7), LazyList.of(1,2,3,5,5,6,7));
        assertNotEquals(LazyList.of(87,65,43,22), LazyList.of(22, 44, 3));

        assertNotEquals(LazyList.of(1,2,3), null);
        assertNotEquals(null, LazyList.of(1,2,3));

        // Test equality to non-lazy lists!:
        // cf.: new LinkedList<String>(List.of("Hello","World")).equals(new ArrayList<String>(List.of("Hello","World"))) == true
        assertEquals(LazyList.of(1,2,3), List.of(1,2,3));
        assertEquals(List.of(1,2,3), LazyList.of(1,2,3));
    }

    @Test
    void testView() {
        LazyList<Double> list = LazyList.view(List.of(1.1,2.2,3.3,4.4,5.5,6.6,7.7,8.8));
        assertEquals(list, LazyList.of(1.1,2.2,3.3,4.4,5.5,6.6,7.7,8.8));
    }

    @Test
    void testRepeatReplicateLengthAndGet() {
        assertTrue(LazyList.of(1,2,3,4).lengthEquals(4));
        assertTrue(LazyList.of(1,2,3,4,5,6,7,8).lengthEquals(8));
        assertFalse(LazyList.of(1,2,3,4,5,6,7).lengthEquals(8));
        assertFalse(LazyList.of(1,2,3,4,5,6,7,8,9).lengthEquals(8));

        LazyList<Long> infiniteTwenties = LazyList.repeat((long)20);
        LazyList<Character> hundredXs = LazyList.replicate(100, 'X');

        assertFalse(infiniteTwenties.lengthEquals(0));
        assertFalse(infiniteTwenties.lengthEquals(10));
        assertTrue(infiniteTwenties.lengthIsAtLeast(300));
        assertFalse(infiniteTwenties.lengthIsAtMost(200));
        assertFalse(infiniteTwenties.lengthEquals(100));

        assertTrue(hundredXs.lengthIsAtLeast(10));
        assertTrue(hundredXs.lengthIsAtLeast(20));
        assertTrue(hundredXs.lengthIsAtLeast(50));
        assertTrue(hundredXs.lengthIsAtLeast(100));
        assertTrue(hundredXs.lengthIsAtMost(100));
        assertTrue(hundredXs.lengthIsAtMost(200));
        assertTrue(hundredXs.lengthIsAtMost(300));

        assertFalse(hundredXs.lengthIsAtLeast(101));
        assertFalse(hundredXs.lengthIsAtMost(99));

        assertTrue(hundredXs.lengthEquals(100));
        assertEquals(100, hundredXs.length());
        assertTrue(hundredXs.lengthEquals(100));
        assertFalse(hundredXs.lengthEquals(99));
        assertFalse(hundredXs.lengthEquals(101));

        assertEquals((long)20, infiniteTwenties.get(0));
        assertEquals((long)20, infiniteTwenties.get(1));
        assertEquals((long)20, infiniteTwenties.get(2));
        assertEquals((long)20, infiniteTwenties.get(10));
        assertEquals((long)20, infiniteTwenties.get(100));
        assertEquals((long)20, infiniteTwenties.get(200));
        assertEquals((long)20, infiniteTwenties.get(300));

        assertEquals('X', hundredXs.get(0));
        assertEquals('X', hundredXs.get(1));
        assertEquals('X', hundredXs.get(2));
        assertEquals('X', hundredXs.get(10));
        assertEquals('X', hundredXs.get(20));
        assertEquals('X', hundredXs.get(50));
        assertEquals('X', hundredXs.get(99));
        assertThrows(IndexOutOfBoundsException.class, () -> hundredXs.get(100));
    }

    @Test
    void testThatReplicateIsLazy() {
        // Test whether replicate() really IS lazy!:
        LazyList<Character> aGazillionYs = LazyList.replicate(Integer.MAX_VALUE, 'Y');
        assertEquals('Y', aGazillionYs.get(0));
        assertEquals('Y', aGazillionYs.get(1));
        assertEquals('Y', aGazillionYs.get(2));
        assertEquals('Y', aGazillionYs.get(3));
        assertTrue(aGazillionYs.lengthIsAtLeast(10));
        assertTrue(aGazillionYs.lengthIsAtLeast(50));
    }

    @Test
    void testIterate() {
        LazyList<Integer> powersOfTwo = LazyList.iterate((x) -> x*2, 1);
        assertEquals(1, powersOfTwo.get(0));
        assertEquals(2, powersOfTwo.get(1));
        assertEquals(4, powersOfTwo.get(2));
        assertEquals(8, powersOfTwo.get(3));
        assertEquals(16, powersOfTwo.get(4));
        assertEquals(32, powersOfTwo.get(5));
        assertEquals(64, powersOfTwo.get(6));
        assertEquals(128, powersOfTwo.get(7));
        assertEquals(256, powersOfTwo.get(8));
        assertEquals(512, powersOfTwo.get(9));
        assertEquals(1024, powersOfTwo.get(10));
        // do NOT directly evaluate the intermediate values!!
        assertEquals(1048576, powersOfTwo.get(20));
    }

    @Test
    void testComprehensions() {
        // ToDo
    }

    @Test
    void testRecursiveDefinitionAndTake() {
        // https://oeis.org/A000045
        // 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, ...
        LazyList<Integer> fibonacciNumbers = LazyList.recursiveDefinition(0, 1, Integer::sum);
        assertEquals(LazyList.of(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233), fibonacciNumbers.take(14));
        assertEquals(0, fibonacciNumbers.get(0));
        assertEquals(1, fibonacciNumbers.get(1));
        assertEquals(1, fibonacciNumbers.get(2));
        assertEquals(2, fibonacciNumbers.get(3));
        assertEquals(3, fibonacciNumbers.get(4));
        assertEquals(5, fibonacciNumbers.get(5));
        assertEquals(8, fibonacciNumbers.get(6));
        assertEquals(13, fibonacciNumbers.get(7));
        assertEquals(21, fibonacciNumbers.get(8));

        // https://en.wikipedia.org/wiki/Lucas_number
        // 2, 1, 3, 4, 7, 11, 18, 29, 47, 76, 123, ....
        LazyList<Integer> lucasNumbers = LazyList.recursiveDefinition(2, 1, Integer::sum);
        assertEquals(2, lucasNumbers.get(0));
        assertEquals(1, lucasNumbers.get(1));
        assertEquals(3, lucasNumbers.get(2));
        assertEquals(4, lucasNumbers.get(3));
        assertEquals(7, lucasNumbers.get(4));
        assertEquals(11, lucasNumbers.get(5));
        assertEquals(18, lucasNumbers.get(6));
        assertEquals(29, lucasNumbers.get(7));
        assertEquals(47, lucasNumbers.get(8));
        // (index 9 left out on purpose)
        assertEquals(123, lucasNumbers.get(10));
    }

    @Test
    void testFromFromThenFromToAndFromThenTo() {
        assertEquals(5, LazyList.from(0).get(5));
        assertEquals(13, LazyList.from(13).get(0));
        assertEquals(LazyList.of(7,8,9,10,11,12), LazyList.fromTo(7,12));
        assertEquals(LazyList.of(8,7,6,5,4), LazyList.fromTo(8,4));

        assertEquals(40, LazyList.fromThen(10,20).get(3));
        assertEquals(LazyList.of(5,10,15,20,25), LazyList.fromThenTo(5,10,25));
    }

    @Test
    void testConcatAndfromString() {
        // --- Static function concat(): ---
        LazyList<LazyList<Character>> list =
                LazyList.of(LazyList.of('H'), LazyList.of('e','l','l'), LazyList.of('o', ' '),
                        LazyList.fromString("World"));
        LazyList<Character> concated = LazyList.concat(list);
        assertEquals('o', concated.get(4));
        assertEquals(LazyList.fromString("Hello World"), concated);

        // --- Instance method concat(): ---
        assertTrue( LazyList.of("Hello ", "World").concat().asString().equals("Hello World") );
        assertTrue( LazyList.of(LazyList.of(1,2), LazyList.of(3,4)).concat().equals(LazyList.of(1,2,3,4)) );
        // (copied the 2 examples from the javadoc of the concat() method)
    }

    @Test
    void testToLinkedListAndToArrayList() {
        LinkedList<Integer> lili = new LinkedList<>(List.of(1,2,3,4,5));
        LazyList<Integer> lazyList = new LazyList<>(lili);
        assertEquals(lili, lazyList.toLinkedList());
        assertEquals(new ArrayList<Integer>(List.of(1,2,3,4,5)), lazyList.toArrayList());
    }

    @Test
    void testLength() {
        assertEquals(0, LazyList.emptyList().length());
        assertEquals(0, LazyList.of().length());
        assertEquals(1, LazyList.of(76).length());
        assertEquals(2, LazyList.of(55,44).length());
        assertEquals(3, LazyList.of(23,1,4).length());
        assertEquals(4, LazyList.of(7,6,4,3).length());
    }

    @Test
    void testElem() {
        assertTrue(LazyList.of(1,2,3,4,5,6).elem(1));
        assertTrue(LazyList.of(1,2,3,4,5,6).elem(3));
        assertTrue(LazyList.of(1,2,3,4,5,6).elem(6));

        assertFalse(LazyList.of(1,2,3,4,5,6).elem(0));
        assertFalse(LazyList.of(1,2,3,4,5,6).elem(7));
        assertFalse(LazyList.of(1,2,3,4,5,6).elem(null));
        assertFalse(LazyList.of().elem(0));
        assertFalse(LazyList.of().elem(1));
        assertFalse(LazyList.of().elem(null));
    }

    @Test
    void testHeadTailInitLast() {
        assertEquals(11, LazyList.of(11,22,33,44).head());
        assertEquals(LazyList.of(22,33,44), LazyList.of(11,22,33,44).tail());

        assertEquals(LazyList.of(11,22,33), LazyList.of(11,22,33,44).init());
        assertEquals(44, LazyList.of(11,22,33,44).last());

        assertNull(LazyList.emptyList().headOrNull());
        assertNull(LazyList.of().headOrNull());
        assertNull(LazyList.emptyList().lastOrNull());
        assertNull(LazyList.of().lastOrNull());

        assertThrows(EmptyListException.class, () -> LazyList.emptyList().head());
        assertThrows(EmptyListException.class, () -> LazyList.of().head());
        assertThrows(EmptyListException.class, () -> LazyList.emptyList().last());
        assertThrows(EmptyListException.class, () -> LazyList.of().last());
    }

    @Test
    void testTakeAndDrop() {
        assertEquals(LazyList.of(1,2,3), LazyList.of(1,2,3,4,5).take(3));
        assertEquals(LazyList.of(3,4,5), LazyList.of(1,2,3,4,5).drop(2));

        assertEquals(LazyList.of(7,8,9), LazyList.of(7,8,9).take(3));
        assertEquals(LazyList.of(7,8,9), LazyList.of(7,8,9).take(50));
        assertEquals(LazyList.emptyList(), LazyList.of(7,8,9).drop(3));
        assertEquals(LazyList.emptyList(), LazyList.of(7,8,9).drop(4));
        assertEquals(LazyList.emptyList(), LazyList.of(7,8,9).drop(50));
    }

    @Test
    void testMapAndFilter() {
        assertEquals(LazyList.of(1,4,9,16,25,36), LazyList.of(1,2,3,4,5,6).map((x) -> x*x));
        assertEquals(LazyList.of(11,12,13,14,15,16), LazyList.of(1,2,3,4,5,6).map((x) -> x+10));
        assertEquals(LazyList.emptyList(), LazyList.of().map((x) -> x));

        assertEquals(LazyList.of(2,4,6), LazyList.of(1,2,3,4,5,6).filter((x) -> x % 2 == 0));
        assertEquals(LazyList.of(1,2,3,4), LazyList.of(1,2,3,4,5,6).filter((x) -> x < 5));
        assertEquals(LazyList.of(1,2,3,4,5,6), LazyList.of(1,2,3,4,5,6).filter((x) -> x < 10));
        assertEquals(LazyList.of(3,4,5), LazyList.of(1,2,3,4,5,6).filter((x) -> 2 < x && x < 6));
    }

    @Test
    void testTakeWhileAndDropWhile() {
        assertEquals(LazyList.of(1,2,3,4), LazyList.from(1).takeWhile((x) -> x < 5));
        assertEquals(LazyList.of(1,2,3,4), LazyList.fromTo(1,10).takeWhile((x) -> x < 5));

        assertEquals(LazyList.of(5,6,7,8,9,10), LazyList.fromTo(1,10).dropWhile((x) -> x < 5));
    }

    @Test
    void testAllAndAny() {
        assertTrue(LazyList.of(3,7,8,6,1,9).all((x) -> x < 10));
        assertFalse(LazyList.of(3,7,8,6,10,1,9).all((x) -> x < 10));
        assertFalse(LazyList.from(10).all((x) -> x < 50));

        assertTrue(LazyList.of(1,2,3,4,5,6).any((x) -> x == 3));
        assertTrue(LazyList.of(10,20,30,1,40,50).any((x) -> x < 5));
        assertFalse(LazyList.of(1,2,3,4,5).any((x) -> x > 5));
        assertTrue(LazyList.from(100).any((x) -> x > 150));

        // Finally, test the empty LazyList:
        LazyList<Integer> emptyL = LazyList.emptyList();
        assertTrue(emptyL.all((x) -> x == 100));
        assertFalse(emptyL.any((x) -> x == 100));
    }

    @Test
    void testCycle() {
        LazyList<Integer> cycle1 = LazyList.of(1,2,3).cycle(); // a fully evaluated LazyList
        assertEquals(1, cycle1.get(0));
        assertEquals(2, cycle1.get(1));
        assertEquals(3, cycle1.get(2));
        assertEquals(1, cycle1.get(3));
        assertEquals(2, cycle1.get(4));
        assertEquals(3, cycle1.get(5));
        assertEquals(1, cycle1.get(6));
        assertEquals(2, cycle1.get(7));
        assertEquals(3, cycle1.get(8));
        assertEquals(1, cycle1.get(9));
        assertEquals(2, cycle1.get(10));
        assertEquals(3, cycle1.get(11));
        assertEquals(1, cycle1.get(12));

        LazyList<Integer> cycle2 = LazyList.fromTo(1,10).cycle(); // a non-fully evaluated LazyList
        assertEquals(1, cycle2.get(0));
        assertEquals(2, cycle2.get(1));
        assertEquals(3, cycle2.get(2));
        // ...
        assertEquals(10, cycle2.get(9));
        assertEquals(1, cycle2.get(10));
        assertEquals(2, cycle2.get(11));
        assertEquals(3, cycle2.get(12));
        // ...
        assertEquals(10, cycle2.get(19));
        assertEquals(1, cycle2.get(20));
        assertEquals(2, cycle2.get(21));
        assertEquals(3, cycle2.get(22));
        // ...
        assertEquals(10, cycle2.get(29));
        assertEquals(1, cycle2.get(30));

        LazyList<Integer> cycle3 = LazyList.from(0).cycle(); // an infinite LazyList
        assertEquals(0, cycle3.get(0));
        assertEquals(1, cycle3.get(1));
        assertEquals(2, cycle3.get(2));
        assertEquals(56, cycle3.get(56));

        // Test whether they actually cycle and cycle and cycle... (infinitely):
        assertTrue(cycle1.lengthIsAtLeast(100));
        assertTrue(cycle2.lengthIsAtLeast(100));
        assertTrue(cycle3.lengthIsAtLeast(100));
    }

    @Test
    void testZipWith() {
        assertEquals(LazyList.of(11,22,33), LazyList.of(1,2,3).zipWith(Integer::sum, LazyList.of(10,20,30)));
        // ToDo: an example with 2 different types for the zip function
    }

    @Test
    void testZip() {
        assertEquals("[(1,10), (2,20)]", LazyList.of(1,2).zip(LazyList.of(10,20)).toString());
    }

    @Test
    void testAppend() {
        assertEquals(LazyList.of(1,2,3,4,5), LazyList.of(1,2).append(LazyList.of(3,4,5)));
        assertEquals(LazyList.of(1,2,3,4,5), LazyList.of().append(LazyList.of(1,2,3,4,5)));
        assertEquals(LazyList.of(1,2,3,4,5), LazyList.of(1,2,3,4,5).append(LazyList.of()));
    }

    @Test
    void testSetOperations() {
        assertEquals(LazyList.of(1,2,2,3,4,5,6), LazyList.of(1,2,2,3,4).union(LazyList.of(2,3,5,4,6)));
        assertEquals(LazyList.of(1,3,5), LazyList.of(1,2,3,4,5,6).intersect(LazyList.of(5,3,1)));
        assertEquals(LazyList.of(1,3,5), LazyList.of(1,2,3,4,5,6).intersect(LazyList.of(5,5,3,1,1,3)));

        // Calling intersect() on an infinite LazyList:
        LazyList<Integer> l1 = LazyList.from(6).intersect(LazyList.of(0,5,10,15,20));
        assertEquals(10, l1.get(0));
        assertEquals(15, l1.get(1));
        assertEquals(20, l1.get(2));
        assertTimeout_("([6..] `intersect` [0,5..20])!!3 should never terminate", () -> l1.get(3));

        LazyList<Integer> l2 = LazyList.from(0).intersectOrdered(LazyList.from(5));
        LazyList<Integer> l3 = LazyList.from(5).intersectOrdered(LazyList.from(0));
        assertEquals(5, l2.get(0));
        assertEquals(5, l3.get(0));
        assertEquals(6, l2.get(1));
        assertEquals(6, l3.get(1));
        assertEquals(7, l2.get(2));
        assertEquals(7, l3.get(2));

        // without():
        assertEquals(LazyList.of(3,6,8), LazyList.fromTo(1,10).without(LazyList.of(1,2,4,5,7,9,10)));

        // nub():
        assertEquals(LazyList.of(1,2,3,4), LazyList.of(1,1,2,3,3,4,4,4,4).nub());
        assertEquals(LazyList.of(1,2,3,4), LazyList.of(1,1,2,3,3,4,4,4,4,3,1,2,4,4,2,1,2,3,4).nub());
        LazyList<Integer> infiniteNub = LazyList.from(22).nub();
        assertEquals(22, infiniteNub.get(0));
        assertEquals(23, infiniteNub.get(1));
        assertEquals(24, infiniteNub.get(2));
        assertEquals(25, infiniteNub.get(3));
    }

    @Test
    void testFolds() {
        assertEquals(55, LazyList.fromTo(0,10).foldr1(Integer::sum));
        assertEquals(55, LazyList.fromTo(0,10).foldr(Integer::sum, 0));

        // foldr and foldr1 should behave differently on an empty LazyList!:
        LazyList<Integer> emptyLazyList = LazyList.emptyList();
        assertEquals(42, emptyLazyList.foldr(Integer::sum, 42));
        assertThrows(EmptyListException.class, () -> emptyLazyList.foldr1(Integer::sum));

        // ToDo: other (non-Integer) examples
    }

    @Test
    void testMinimumAndMaxiumum() {
        assertEquals(4, LazyList.of(4,5,6,7,8,9,10,33).minimum());
        assertEquals(33, LazyList.of(4,5,6,7,8,9,10,33).maximum());
        assertEquals(2, LazyList.of(55,33,7,4,4,2,11,22,7).minimum());
        assertEquals(99, LazyList.of(1,2,3,4,99,55,77,6,7,88,11,1).maximum());

        assertThrows(EmptyListException.class, () -> LazyList.emptyList().minimum());
        assertThrows(EmptyListException.class, () -> LazyList.of().maximum());
    }

    @Test
    void testSumAndProduct() {
        assertEquals(0, LazyList.emptyList().sum());
        assertEquals(1, LazyList.of().product());

        assertEquals(55, LazyList.fromTo(0,10).sum());
        assertEquals(720, LazyList.fromTo(1,6).product()); // 6! = 720

        assertThrows(ClassCastException.class, () -> LazyList.of('X','Y').sum());
        assertThrows(ClassCastException.class, () -> LazyList.of('Y','Z').product());
    }

    @Test
    void testScans() {
        // ToDo
    }

    @Test
    void testFromStringAndAsString() {
        assertEquals(LazyList.of('H','e','y'), LazyList.fromString("Hey"));
        assertEquals("Hey", LazyList.of('H','e','y').asString());
        assertEquals("Hello World 123", LazyList.fromString("Hello World 123").asString());
        assertThrows(UnsupportedOperationException.class, () -> LazyList.of(1,2,3).asString());
    }

    @Test
    void testListInterfaceMethods() {
        assertTrue(LazyList.emptyList().isEmpty());
        assertTrue(LazyList.of().isEmpty());
        assertFalse(LazyList.of(1).isEmpty());
        assertFalse(LazyList.of(1,2).isEmpty());
        assertFalse(LazyList.of(1,2,3).isEmpty());
        assertFalse(LazyList.from(1).isEmpty()); // Very important: isEmpty() also has to return on infinite LazyLists!!!

        assertEquals(0, LazyList.emptyList().size());
        assertEquals(0, LazyList.of().size());
        assertEquals(1, LazyList.of(1).size());
        assertEquals(2, LazyList.of(1,2).size());
        assertEquals(3, LazyList.of(1,2,3).size());

        // get(), indexOf(), lastIndexOf():
        LazyList<Integer> list = LazyList.of(1,2,3,4,5,6,7,8,9,10);
        assertEquals(1, list.get(0));
        assertEquals(6, list.get(5));
        assertEquals(10, list.get(9));
        assertEquals(6, list.indexOf(7));
        assertEquals(-1, list.indexOf(0));
        assertEquals(-1, list.indexOf(11));
        assertEquals(-1, list.indexOf('B'));
        assertEquals(4, list.lastIndexOf(5));
        assertEquals(-1, list.lastIndexOf(42));

        assertEquals(7, LazyList.of(1,2,3,3,2,1,1,2,3).lastIndexOf(2));

        // contains():
        assertTrue(LazyList.of(1,2,3,4,5).contains(1));
        assertTrue(LazyList.of(1,2,3,4,5).contains(3));
        assertTrue(LazyList.of(1,2,3,4,5).contains(5));
        assertFalse(LazyList.of(1,2,3,4,5).contains(0));
        assertFalse(LazyList.of(1,2,3,4,5).contains(6));

        // containsAll():
        assertTrue(LazyList.of(1,2,3,4,5,6).containsAll(List.of(3,4,5)));
        assertTrue(LazyList.of(1,2,3,4,5,6).containsAll(List.of(1,2,3,4,5,6)));
        assertFalse(LazyList.of(1,2,3,4,5,6).containsAll(List.of(3,4,7,5)));
        assertFalse(LazyList.of(1,2,3,4,5,6).containsAll(List.of(10,11,12)));

        // subList():
        assertEquals(2, LazyList.of(0,1,2,3,4,5).subList(1,4).get(1));

        // toArray():
        Object[] arr = LazyList.of(111,222,333).toArray();
        assertEquals(3, arr.length);
        assertEquals(111, arr[0]);
        assertEquals(222, arr[1]);
        assertEquals(333, arr[2]);
    }

    @Test
    void testIterator() {
        for (Integer i : LazyList.of(1,2,3,4)) {
            // iterate...
        }
        assertFalse(LazyList.emptyList().iterator().hasNext());
        assertEquals(42, LazyList.of(42).iterator().next());
        assertEquals(42, LazyList.of(42,43).iterator().next());
        assertEquals(42, LazyList.of(42,43,44).iterator().next());
    }

    @Test
    void testMutability() {
        // add():
        LazyList<Integer> list = LazyList.of(11,22,33,44,55);
        assertEquals(5, list.length());
        list.add(66);
        assertEquals(6, list.length());
        assertEquals(66, list.get(5));
        assertEquals(LazyList.of(11,22,33,44,55,66), list);

        // remove():
        list.remove('A');
        assertEquals(LazyList.of(11,22,33,44,55,66), list);
        list.remove(99);
        assertEquals(LazyList.of(11,22,33,44,55,66), list);
        list.remove(33);
        assertEquals(LazyList.of(11,22,44,55,66), list);

        // addAll():
        list.addAll(List.of(1,2,3,4));
        assertEquals(LazyList.of(11,22,44,55,66,1,2,3,4), list);

        // removeAll():
        list.removeAll(List.of(3,5,22,3,66,5,1,2,33));
        assertEquals(LazyList.of(11,44,55,4), list);

        // retainAll():
        list.retainAll(List.of(33,44,45,55,66));
        assertEquals(LazyList.of(44,55), list);

        // set():
        list.set(0,4);
        assertEquals(LazyList.of(4,55), list);
        list.set(1,5);
        assertEquals(LazyList.of(4,5), list);

        // clear();
        list.clear();
        assertEquals(LazyList.of(), list);
        assertTrue(list.isEmpty());

        // add(index) & remove(index):
        LazyList<Integer> list2 = LazyList.of(11,22,44,55,66,66,77,88,99);
        list2.add(2, 33);
        assertEquals(LazyList.of(11,22,33,44,55,66,66,77,88,99), list2);
        list2.remove(6);
        assertEquals(LazyList.of(11,22,33,44,55,66,77,88,99), list2);
    }

    @Test
    void testComparableInterface() {
        // Haskell: compare [1,2,3] [1,2,3] == EQ
        assertEquals(0, LazyList.of(1,2,3).compareTo(LazyList.of(1,2,3)));
        assertEquals(0, LazyList.of(42).compareTo(LazyList.of(42)));
        assertEquals(0, LazyList.of().compareTo(LazyList.emptyList()));
        assertEquals(0, LazyList.emptyList().compareTo(LazyList.of()));
        assertEquals(0, LazyList.of().compareTo(LazyList.of()));
        assertEquals(0, LazyList.emptyList().compareTo(LazyList.emptyList()));

        // Haskell: compare [1,2,3] [1,2,3,4] == LT
        assertEquals(LazyList.LT, LazyList.of(1,2,3).compareTo(LazyList.of(1,2,3,4)));
        assertEquals(LazyList.LT, LazyList.of(1,2,3).compareTo(List.of(1,2,3,4)));
        // Haskell: compare [1,2,3,4] [1,2,3] == GT
        assertEquals(LazyList.GT, LazyList.of(1,2,3,4).compareTo(LazyList.of(1,2,3)));
        assertEquals(LazyList.GT, LazyList.of(1,2,3,4).compareTo(List.of(1,2,3)));
        // Haskell: compare [1,2,3,4] [1,2,30,4] == LT
        assertEquals(LazyList.LT, LazyList.of(1,2,3,4).compareTo(LazyList.of(1,2,30,4)));
        assertEquals(LazyList.LT, LazyList.of(1,2,3,4).compareTo(List.of(1,2,30,4)));
    }

    @Test
    void testLazyListsUtilityClass() {
        assertEquals(LazyList.of(0,1,2,3,4,5,6,7,8,9), LazyLists.naturalNumbers().take(10).map(Long::intValue));
        assertEquals(LazyList.of(0,2,4,6,8,10,12,14,16,18), LazyLists.evenNumbers().take(10).map(Long::intValue));
        assertEquals(LazyList.of(1,3,5,7,9,11,13,15,17,19), LazyLists.oddNumbers().take(10).map(Long::intValue));
        assertEquals(LazyList.of(0,1,4,9,16,25,36,49,64,81), LazyLists.squareNumbers().take(10).map(Long::intValue));
        assertEquals(LazyList.of(0,1,8,27,64,125,216,343,512,729), LazyLists.cubeNumbers().take(10).map(Long::intValue));
        assertEquals(LazyList.of(1,1,2,6,24,120,720,5040,40320,362880), LazyLists.factorials().map(Long::intValue).take(10));
        assertEquals(LazyList.of(1,2,4,8,16,32,64,128,256,512), LazyLists.powersOfTwo().map(Long::intValue).take(10));
        assertEquals(LazyList.of(0,1,1,2,3,5,8,13,21,34), LazyLists.fibonacciNumbers().map(Long::intValue).take(10));
        assertEquals(LazyList.of(2,1,3,4,7,11,18,29,47,76), LazyLists.lucasNumbers().map(Long::intValue).take(10));
        assertEquals(LazyList.of(2,3,5,7,11,13,17,19,23,29), LazyLists.primeNumbers().map(Long::intValue).take(10));
    }

    @Test
    void testReverse() {
        assertEquals(LazyList.of(), LazyList.of().reverse());
        assertEquals(LazyList.of(44), LazyList.of(44).reverse());
        assertEquals(LazyList.of(12,11), LazyList.of(11,12).reverse());
        assertEquals(LazyList.of(5,4,3,2,1), LazyList.of(1,2,3,4,5).reverse());
        assertTimeout_("Infinite lists cannot be reversed", () -> LazyList.from(1).reverse());
    }

    @Test
    void testShow() {
        // Calling show() on a finite LazyList:
        assertEquals("[1, 2, 3]", LazyList.of(1,2,3).show().asString());
        // Calling show() on an infinite LazyList:
        assertEquals("[11, 12, 13,", LazyList.from(11).show().take(12).asString());
    }

    @Test
    void testMap() { // Tests the fromMap(), lookup() and viewAsMap() methods.
        LazyList<Pair<Integer,String>> listFromMap = LazyList.fromMap(Map.of(1,"one",2,"two",3,"three"));

        assertEquals(3, listFromMap.length());
        assertTrue(listFromMap.contains(new Pair<>(1,"one")));
        assertTrue(listFromMap.contains(new Pair<>(2,"two")));
        assertTrue(listFromMap.contains(new Pair<>(3,"three")));

        assertEquals("one", listFromMap.lookup(1).get());
        assertEquals("two", listFromMap.lookup(2).get());
        assertEquals("three", listFromMap.lookup(3).get());
        assertThrows(NoSuchElementException.class, () -> listFromMap.lookup(0).get());
        assertThrows(NoSuchElementException.class, () -> listFromMap.lookup(4).get());

        // lookup() should throw an UnsupportedOperationException on LazyLists that are not
        // LazyLists of Pairs!:
        assertThrows(UnsupportedOperationException.class, () -> LazyList.of(1,2).lookup(1));



        // ----- ----- Test whether viewAsMap() actually works correctly as a view!!! ----- -----
        Map<Object,Object> mapView = listFromMap.viewAsMap();
        // --- Size, IsEmpty, Contains: ---
        assertEquals(3, mapView.size());
        assertFalse(mapView.isEmpty());
        assertTrue(mapView.containsKey(1));
        assertFalse(mapView.containsKey(4));
        assertTrue(mapView.containsValue("three"));
        assertFalse(mapView.containsKey("four"));
        // --- Get: ---
        assertEquals("one", mapView.get(1));
        assertEquals("two", mapView.get(2));
        assertEquals("three", mapView.get(3));
        assertNull(mapView.get(0));
        assertNull(mapView.get(4));
        // --- Removal: ---
        assertEquals(3, listFromMap.length());
        assertTrue(listFromMap.contains(new Pair<>(2,"two")));
        mapView.remove(2);
        assertEquals(2, mapView.size());
        assertEquals(2, listFromMap.length());
        assertFalse(listFromMap.contains(new Pair<>(2,"two")));
        assertFalse(mapView.containsKey(2));
        assertFalse(mapView.containsValue("two"));
        // --- Put: ---
        // ToDo
        // --- PutAll: ---
        // ToDo
        // --- KeySet: ---
        // ToDo
        // --- Values: ---
        // ToDo
        // --- EntrySet: ---
        // ToDo
        // --- Clear: ---
        mapView.clear();
        assertTrue(mapView.isEmpty());
        assertTrue(listFromMap.isEmpty());
    }

    @Test
    void testForEach() {
        // Test whether the forEach methods actually return the same list again:
        assertEquals(LazyList.of(1,2,3), LazyList.of(1,2,3).forEachAndThen(System.out::print));
        assertEquals(LazyList.of(1,2,3), LazyList.of(1,2,3).forEachWithIndex((el,i) -> {}));
        assertEquals(LazyList.of(1,2,3), LazyList.of(1,2,3).forEachWithIndexAndLength((el,i,len) -> {}));

        System.out.println("===== ===== ===== ===== =====");

        // Examples given in the javadocs (they should definitely work!):
        System.out.println(
            LazyList.fromTo(10,0)
            .forEachAndThen(System.out::println)
            .sum()
        );
        System.out.println("===== ===== ===== ===== =====");
        LazyList.fromThen(10,20).forEachWithIndex((el,i) -> System.out.println("#" + i + ": " + el));
        System.out.println("===== ===== ===== ===== =====");
        LazyList.fromThenTo(10,20,100).forEachWithIndexAndLength((el,i,len) -> System.out.println("#" + i + "/" + len + ": " + el));
    }

    @Test
    void testTraverseAndForce() {
        assertEquals(LazyList.of(1,2,3), LazyList.of(1,2,3).traverse());
        assertEquals(LazyList.of(1,2,3), LazyList.of(1,2,3).force());
        assertTimeout_("Traversal of infinite list times out 1", () -> LazyList.from(1).traverse());
        assertTimeout_("Calling force() on an infinite list times out", () -> LazyList.from(1).force());
        assertTimeout_("Traversal of infinite list times out 2", () -> LazyList.from(1).traverseAndThen((x) -> {}));
        LazyList.of(1,2,3).traverseAndThen(System.out::print);
    }

    @Test
    void testLinesUnlinesWordsUnwords() {
        // Check lines() and unlines() separately:
        assertEquals(LazyList.of("Hello", "World"), LazyList.fromString("Hello\nWorld").lines());
        assertEquals(LazyList.fromString("Hello\nWorld"), LazyList.of("Hello", "World").unlines());
        // Check whether lines() and unlines() really are the inverse of one another:
        assertEquals("Hello\nWorld\n", LazyList.fromString("Hello\nWorld\n").lines().unlines().asString());
        assertEquals(LazyList.of("Hello","World"), LazyList.of("Hello","World").unlines().lines());

        // Check words() and unwords() separately:
        assertEquals(LazyList.of("Hello", "World"), LazyList.fromString("Hello World").words());
        assertEquals(LazyList.fromString("Hello World"), LazyList.of("Hello", "World").unlines());
        // Check whether words() and unwords() really are the inverse of one another:
        assertEquals("Hello World", LazyList.fromString("Hello World").words().unwords().asString());
        assertEquals(LazyList.of("Hello","World"), LazyList.of("Hello","World").unwords().words());

        // Test Exceptions:
        assertThrows(UnsupportedOperationException.class, () -> LazyList.of(1,2,3).lines());
        assertThrows(UnsupportedOperationException.class, () -> LazyList.of(1,2,3).unlines());
        assertThrows(UnsupportedOperationException.class, () -> LazyList.of(1,2,3).words());
        assertThrows(UnsupportedOperationException.class, () -> LazyList.of(1,2,3).unwords());
    }

    @Test
    void testInputStreamConversion() throws IOException {
        // LazyList --> InputStream:
        InputStream inputStream1 = LazyList.fromThen(10,20).asInputStream();
        assertEquals(10, inputStream1.read());
        assertEquals(20, inputStream1.read());
        assertEquals(30, inputStream1.read());
        assertEquals(40, inputStream1.read());
        assertEquals(50, inputStream1.read());
        assertEquals(60, inputStream1.read());
        assertEquals(70, inputStream1.read());
        assertEquals(80, inputStream1.read());
        assertEquals(90, inputStream1.read());
        assertEquals(100, inputStream1.read());
        // (this is actually quite a bad example because by contract InputStream.read() shouldn't
        // return a number greater than 255!)

        // InputStream --> LazyList:
        InputStream inputStream2 = new ByteArrayInputStream(new byte[]{42,35,66,77});
        // b.read() ==> 42, b.read() ==> 35, b.read() ==> 66, b.read() ==> 77, b.read() ==> -1
        LazyList<Integer> lazyList = LazyList.fromInputStream(inputStream2);
        assertEquals(List.of(42,35,66,77), lazyList);
    }

    @Test
    void complexInputStreamExample1() throws FileNotFoundException {
        // --- Example: Reading a text file lazily --- (in javadoc of fromInputStream()):
        LazyList<Integer> lazyBytes = LazyList.fromInputStream(new FileInputStream("MyClass.java"));
        LazyList<String> lazyLines = lazyBytes.map((i) -> (char)(int)i).lines();
        LazyList<String> linesWithoutTabs = lazyLines.map(String::trim);
        LazyList<String> nonCommentLines = linesWithoutTabs.filter((line) -> !line.startsWith("//"));
        LazyList<String> methodDeclarations = nonCommentLines.filter((l) -> l.matches("(void|public|private|protected)*"));
        LazyList<String> declarationsWithoutParameters = methodDeclarations.map((d) -> d.split("\\(")[0]);
        if (declarationsWithoutParameters.any((str) -> str.contains("_"))) {
            System.out.println("You're not supposed to use snake_case for method names mate!");
        }
    }

    @Test
    void complexInputStreamExample2() throws FileNotFoundException {
        // --- Example: Reading a text file lazily --- (in javadoc of fromInputStream()):
        LazyList.fromInputStream(new FileInputStream("MyClass.java"))
            .map((i) -> (char)(int)i).lines()
            .map(String::trim)
            .filter((line) -> !line.startsWith("//"))
            .filter((l) -> l.matches("(void|public|private|protected)*"))
            .map((d) -> d.split("\\(")[0])
            .ifAny((str) -> str.contains("_"))
            .then(() -> System.out.println("You're not supposed to use snake_case for method names mate!"))
            .orElse(() -> System.out.println("Great, you didn't use snake_case!"));
    }

    @Test
    void testBooleanAnswer() {
        // Example from javadoc of BooleanAnswer Class:
        LazyList.of(1,2,3,4)
                .ifAny((num) -> num > 4)
                .then(() -> System.out.println("List contains number larger than 4."))
                .orElse(() -> System.out.println("List does not contain number larger than 4.")); // optional!
    }

    // ----- Examples from JavaDoc of LazyList: -----

    boolean isPalindrome(String s) {
        return LazyList.fromString(s).reverse().asString().equalsIgnoreCase(s);
    }

    double calculateProduct(Iterable<? extends Number> collection) {
        return LazyList.view(collection).product();
    }

    void printAllFibonacciPrimes() {
        LazyLists.fibonacciNumbers().intersectOrdered(LazyLists.primeNumbers()).print(System.out);
    }

    @Test
    void testJavadocExamples() {
        assertTrue(isPalindrome("anna"));
        assertTrue(isPalindrome("Anna"));
        assertTrue(isPalindrome("lagerregal"));
        assertTrue(isPalindrome("Lagerregal"));
        assertTrue(isPalindrome(""));
        assertTrue(isPalindrome("x"));
        assertTrue(isPalindrome("xx"));
        assertFalse(isPalindrome("xy"));
        assertFalse(isPalindrome("xyz"));
        assertFalse(isPalindrome("Hello World"));

        assertEquals(60.0, calculateProduct(List.of(3,4,5)));
        assertEquals(292.656, calculateProduct(List.of(5.6,6.7,7.8)));
    }

    @Test
    void testFibonacciPrimes() {
        LazyList<Long> fibonacciPrimes = LazyLists.fibonacciNumbers().intersectOrdered(LazyLists.primeNumbers());
        assertEquals(LazyList.of((long)2,3,5,13,89,233), fibonacciPrimes.take(6));
    }

    @Test
    void testSplitAt() {
        /*
        Examples from the JavaDoc of splitAt():
        splitAt 2 [0,1,2,3,4] == ([0,1],[2,3,4])
        splitAt (-1) [0,1,2,3,4] == ([],[0,1,2,3,4])
        splitAt 100 [0,1,2,3,4] == ([0,1,2,3,4],[])
         */
        assertEquals(LazyList.of(0,1,2,3,4).splitAt(2), Pair.newPair(LazyList.of(0,1), LazyList.of(2,3,4)));
        assertEquals(LazyList.of(0,1,2,3,4).splitAt(-1), Pair.newPair(LazyList.of(), LazyList.of(0,1,2,3,4)));
        assertEquals(LazyList.of(0,1,2,3,4).splitAt(100), Pair.newPair(LazyList.of(0,1,2,3,4), LazyList.of()));
    }

    @Test
    void testUnzip() {
        Pair<LazyList<Integer>,LazyList<String>> pair = new Pair<>(LazyList.of(1,2,3),LazyList.of("one","two","three"));
        LazyList<Pair<Integer,String>> pairs = LazyList.of(new Pair<Integer,String>(1,"one"), new Pair<Integer,String>(2,"two"), new Pair<Integer,String>(3,"three"));
        assertEquals(pairs, pair.fst().zip(pair.snd()));
        assertEquals(pair, pairs.unzip());
        assertEquals(pair, pair.fst().zip(pair.snd()).unzip()); // = combination of the above 2
    }

    @Test
    void testIsOrderedBy() {
        assertTrue(LazyList.of(2,3,4,5,7,10,30).isOrderedBy((x,y) -> x < y));
        assertTrue(LazyList.of(2,3,4,5,7,10,30).isOrderedBy((x,y) -> x <= y));

        assertFalse(LazyList.of(2,3,4,5,7,7,10,30).isOrderedBy((x,y) -> x < y));
        assertTrue(LazyList.of(2,3,4,5,7,7,10,30).isOrderedBy((x,y) -> x <= y));

        assertFalse(LazyList.of(2,3,4,5,4,10,30).isOrderedBy((x,y) -> x < y));
        assertFalse(LazyList.of(2,3,4,5,4,10,30).isOrderedBy((x,y) -> x <= y));

        assertFalse(LazyList.of(2,3,4,5,7,10,30).isOrderedBy((x,y) -> x > y));
        assertTrue(LazyList.of(10,9,8,7,6,5,4,3,2,1,0).isOrderedBy((x,y) -> x > y));
        assertTrue(LazyList.of(10,9,8,7,6,5,4,3,2,1,0).isOrderedBy((x,y) -> x >= y));
        assertFalse(LazyList.of(10,9,8,8,7,6,5,4,3,2,1,0).isOrderedBy((x,y) -> x > y));
    }

    @Test
    void testFromASCIIFile() {
        // ToDo: first: create ASCII file
        // LazyList<Character> fromASCIIFile = LazyList.fromASCIIFile();
    }

    @Test
    void testWriteToOutputStream() throws IOException {
        OutputStream outputStream = new OutputStream() {
            int position = 0;
            @Override
            public void write(int b) throws IOException {
                assertEquals(10+position*10, b);
                assertTrue(position <= 9); // exactly, i.e. no more than 10 elements should be written!
                position++;
            }
        };
        LazyList.fromThenTo(10,20, 100).writeToOutputStream(outputStream);
    }

    @Test
    void testWriteAsStringToTextFile() {
        // ToDo
    }

    @Test
    void testWriteAsBinaryToFile() {
        // ToDo
    }

    @Test
    void testSeqAndDeepSeq() {
        assertEquals("Hello", LazyList.of(1,2,3,4,5).seq("Hello"));
        assertEquals("World", LazyList.of(1,2,3,4,5).deepseq("World"));
        assertTimeout_("deepseq() times out on infinite list", () -> LazyList.fromThen(5,7).deepseq("abc"));
    }

}
