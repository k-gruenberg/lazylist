package com.kendrick.haskell;

import java.util.function.Function;
import java.util.function.Predicate;

final class Main {

    public static void main(String[] args) {
        fibonacciExample();
        // eulerBrickExample();
        // collatzConjectureExample();
        // goldbachsConjectureExample();
    }

    /*

    isPrime :: Integer -> Bool
    isPrime n = all (\d -> n `mod` d /= 0) [2..floor(sqrt(fromIntegral(n)))] -- a number n is prime if no number from 2 up to sqrt(n) divides it     -- all = not any

    primes = [p | p <- [2..], isPrime p]

    fibs = 0:1:(zipWith (+) fibs (tail fibs))

    fibonacciPrimes = intersectOrd fibs primes -- gives [2,3,5,13,89,233,1597,28657,514229

    intersectOrd :: Ord a => [a] -> [a] -> [a] -- computes the intersection of two ordered (possibly infinte!) lists
    intersectOrd _ [] = []
    intersectOrd [] _ = []
    intersectOrd xList@(x:xs) yList@(y:ys)
      | x == y = [x] ++ intersectOrd xs ys
      | x < y = intersectOrd xs yList -- we can discard the smaller one as that is guaranteed to never occur in the other list (because they're ordered!)
      | x > y = intersectOrd xList ys

     */

    public static void fibonacciExample() {
        Predicate<Integer> isPrime = (n) -> LazyList.fromTo(2,n).all((d) -> n % d != 0);
        LazyList<Integer> primes = LazyList.comprehension1(LazyList.from(2), isPrime);

        LazyList<Integer> fibs = LazyList.recursiveDefinition(0,1, Integer::sum);
        LazyList<Integer> fibonacciPrimes = fibs.intersectOrdered(primes);
        fibonacciPrimes.print(System.out);

        // -- Haskell:
        // isPrime n = all (\d -> n `mod` d /= 0) [2..n]
        // primes = [p | p <- [2..], isPrime p]
        // fibs = 0:1:(zipWith (+) fibs (tail fibs))
        // fibonacciPrimes = intersectOrd fibs primes where intersectOrd ...
    }

    /*

    isSquare :: Int -> Bool -- https://stackoverflow.com/questions/2807686/whats-the-way-to-determine-if-an-int-is-a-perfect-square-in-haskell
    isSquare n = flooredSqrt * flooredSqrt == n
        where flooredSqrt = floor $ sqrt $ (fromIntegral n::Double)

    isEulerBrick :: (Int, Int, Int) -> Bool -- https://en.wikipedia.org/wiki/Euler_brick
    isEulerBrick (a,b,c) = isSquare (a*a+b*b) && isSquare (a*a+c*c) && isSquare (b*b+c*c)

    eulerBricks = [(a,b,c) | a <- [1..], b <- [1..a], c <- [1..b], isEulerBrick (a,b,c)]

    primitiveEulerBricks = filter coprime eulerBricks
        where coprime (a,b,c) = (gcd a (gcd b c)) == 1 -- https://math.stackexchange.com/questions/93731/finding-the-gcd-of-three-numbers
    -- gives [(240,117,44),(275,252,240),(693,480,140),(720,132,85),(792,231,160),(1155,1100,1008)

    -- All five primitive Euler bricks with dimensions under 1000
    --   https://en.wikipedia.org/wiki/File:Euler_brick_examples.svg
    --   (240, 117, 44), (275, 252, 240), (720, 132, 85), (792, 231, 160), (693, 480, 140)

     */

    public static void eulerBrickExample() {
        Predicate<Integer> isSquare = (n) -> Math.pow(Math.floor(Math.sqrt(n)),2) == n;
        TriPredicate<Integer,Integer,Integer> isEulerBrick =
                (a,b,c) -> isSquare.test(a*a+b*b) && isSquare.test(a*a+c*c) && isSquare.test(b*b+c*c);
        LazyList<Triple<Integer,Integer,Integer>> eulerBricks =
                LazyList.comprehension3(Triple::new, LazyList.from(1), LazyList.from(1), LazyList.from(1), isEulerBrick);
        Predicate<Triple<Integer,Integer,Integer>> coprime = (triple) -> true; // ToDo
        LazyList<Triple<Integer,Integer,Integer>> primitiveEulerBricks = eulerBricks.filter(coprime);
        primitiveEulerBricks.print(System.out);
    }

    /*

    -- written on Oct 31 2020

    f n
      | even n = n `div` 2
      | otherwise = 3*n+1

    collatzSeq n = takeWhile (>1) (iterate f n)

    collatzLength = length . collatzSeq

    collatzLengths = map collatzLength [1..]
    -- gives [0,1,7,2,5,8,16,3,19,6,14,9,9,17,17,4,12,20,20,7,7,15,15,10,23,10,111,18,18,18,106,5,26,13,13,21,21,21,34,8,109,8,29,16,16,16,104,11,24,24, ...
    -- Wikipedia: 0, 1, 7, 2, 5, 8, 16, 3, 19, 6, 14, 9, 9, 17, 17, 4, 12, 20, 20, 7, 7, 15, 15, 10, 23, 10, 111, 18, 18, 18, 106, 5, 26, 13, 13, 21, 21, 21, 34, 8, 109, 8, 29, 16, 16, 16, 104, 11, 24, 24, ... (A006577) (Number of steps for n to reach 1)



    -- takes a function that maps the natural numbers [1..] to some ordinal value and returns the sub-list of natural numbers which map to a larger value than all natural numbers before them
    recordHolders :: Ord o => (Int -> o) -> [Int]
    recordHolders f = filter (isRecordHolder f) [1..]
      where isRecordHolder f n
              | n==1 = True -- special case: 1 is a record holder because it doesn't even have any record to break
              | otherwise = f n > maximum (take (n-1) (map f [1..])) -- n sets a new record: f(n) is larger than the maximum of f(1),...,f(n-1)



    -- Wikipedia: Numbers with a total stopping time longer than that of any smaller starting value form a sequence beginning with:
    -- 1, 2, 3, 6, 7, 9, 18, 25, 27, 54, 73, 97, 129, 171, 231, 313, 327, 649, 703, 871, 1161, 2223, 2463, 2919, 3711, 6171, ... (sequence A006877 in the OEIS).
    lengthRecordHolders = recordHolders collatzLength
    -- gives [1,2,3,6,7,9,18,25,27,54,73,97,129,171,231,313,327,649,703,871,1161,...



    -- Wikipedia: The starting values whose maximum trajectory point is greater than that of any smaller starting value are as follows:
    -- 1, 2, 3, 7, 15, 27, 255, 447, 639, 703, 1819, 4255, 4591, 9663, 20895, 26623, 31911, 60975, 77671, 113383, 138367, 159487, 270271, 665215, 704511, ... (A006884)
    maxTrajectoryPoint n -- (of a single collatz sequence)
      | n==1 = 1 -- maximum would throw an error here, because collatzSeq 1 gives the empty list []
      | otherwise = (maximum . collatzSeq) n
    trajectoryPointRecordHolders = recordHolders maxTrajectoryPoint
    -- gives [1,2,3,7,15,27,255,447,639,703,...

     */

    public static void collatzConjectureExample() {
        Function<Integer,Integer> f = (n) -> (n % 2 == 0) ? (n/2) : (3*n+1);
        Function<Integer,LazyList<Integer>> collatzSeq = (n) -> LazyList.iterate(f,n).takeWhile((x)->x>1);
        Function<Integer,Integer> collatzLength = (n) -> collatzSeq.apply(n).length();
        LazyList<Integer> collatzLengths = LazyList.from(1).map(collatzLength);
        collatzLengths.print(System.out); // should give A006577
    }

    /*

    -- written on Nov 1st 2020

    import Data.List

    -- Wikipedia Goldbach's conjecture: "Every even integer greater than 2 is the sum of two primes."
    --
    -- Another form of the statement of Goldbach's conjecture is that all even integers greater than 4 are Goldbach numbers (a positive even integer that can be expressed as the sum of two odd primes).
    --
    -- The number of ways in which 2n can be written as the sum of two primes (for n starting at 1) is:
    -- 0, 1, 1, 1, 2, 1, 2, 2, 2, 2, 3, 3, 3, 2, 3, 2, 4, 4, 2, 3, 4, 3, 4, 5, 4, 3, 5, 3, 4, 6, 3, 5, 6, 2, 5, 6, 5, 5, 7, 4, 5, 8, 5, 4, 9, 4, 5, 7, 3, 6, 8, 5, 6, 8, 6, 7, 10, 6, 6, 12, 4, 5, 10, 3, ... (sequence A045917 in the OEIS).


    -- cf. FibonacciPrimes2.hs:
    isPrime :: Integer -> Bool
    isPrime n = all (\d -> n `mod` d /= 0) [2..floor(sqrt(fromIntegral(n)))] -- a number n is prime if no number from 2 up to sqrt(n) divides it     -- all = not any

    primes = [p | p <- [2..], isPrime p]


    -- NEW STUFF:


    primeSums = [p + q | p <- primes, q <- takeWhile (<=p) primes] -- | p <- primes, q <- primes, q <= p] doesn't work
    -- gives [4,5,6,7,8,10,9,10,12,14,13,14,16,18,22,15,16,18,20,24,26,19,20,22,24,...

    primeSumsNub = nub primeSums -- nub (meaning "essence") removes duplicates elements from a list.
    -- gives [4,5,6,7,8,10,9,12,14,13,16,18,22,15,20,24,26,19,28,30,34,21,32,36,38,...

    evenIntegers = [4,6..] -- (2 is an exception, see statemant at the beginning)

    evenIntegersThatAreSumOfTwoPrimes = takeWhile (`elem` primeSums) evenIntegers
    -- as long as this keeps evaluating, Goldbach's conjecture appears(!) to be true...
    -- gives [4,6,8,10,12,14,16,18,20,22,.......,30104,...



    -- https://stackoverflow.com/questions/19554984/haskell-count-occurrences-function/29307068
    -- count   :: Eq a => a -> [a] -> Int
    -- count x =  length . filter (==x)

    -- Counting the occurences of a number in an infinite unordered list is actually impossible. Therefore we just stop looking after exceeding x^2
    unsafeCount :: (Num a, Ord a) => a -> [a] -> Int
    unsafeCount x = length . filter (==x) . takeWhile (<= (x*x))


    numberOfWays = map (`unsafeCount` primeSums) [2,4..]
    -- gives:              [0,1,1,1,2,1,2,2,2,2,3,3,3,2,3,2,4,4,2,3,4,3,4,5,4,3,5,3,4,6,3,5,6,2,5,6,5,5,7,4,5,8,5,4,9,4,5,7,3,6,8,5,6,8,6,7,10,6,6,12,4,5,10,3,7,9,6,5,8,7,8,11,...
    -- expected (A045917): [0,1,1,1,2,1,2,2,2,2,3,3,3,2,3,2,4,4,2,3,4,3,4,5,4,3,5,3,4,6,3,5,6,2,5,6,5,5,7,4,5,8,5,4,9,4,5,7,3,6,...

     */

    public static void goldbachsConjectureExample() {
        Predicate<Integer> isPrime = (n) -> LazyList.fromTo(2,n).all((d) -> n % d != 0);
        LazyList<Integer> primes = LazyList.comprehension1(LazyList.from(2), isPrime);

        LazyList<Integer> primeSums = LazyList.comprehension2(Integer::sum, primes, primes);
        LazyList<Integer> primeSumsNub = primeSums.nub();
        LazyList<Integer> evenIntegers = LazyList.fromThen(4,6); // evenIntegers = [4,6..]
        LazyList<Integer> evenIntegersThatAreSumOfTwoPrimes
                = evenIntegers.takeWhile(primeSums::elem);
        evenIntegersThatAreSumOfTwoPrimes.print(System.out);
    }

}
