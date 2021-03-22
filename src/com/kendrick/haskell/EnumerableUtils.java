package com.kendrick.haskell;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

/**
 * A package-private Utility class that assists us with the from, fromThen, fromTo & fromThenTo functions of the LazyList class.
 * Because Java does not support something like traits, we cannot just "make" the default classes like Integer, Double,
 * etc. enumerable.
 */
final class EnumerableUtils {

    private EnumerableUtils() {} // Utility class

    /*
    static boolean isOfEnumerableType(Object obj) {
        // Number = AtomicInteger, AtomicLong, BigDecimal, BigInteger,
        //          Byte, Double, DoubleAccumulator, DoubleAdder, Float,
        //          Integer, Long, LongAccumulator, LongAdder, Short
        return (obj instanceof Number) ||
                (obj instanceof Character) ||
                (obj instanceof Boolean);
    }
     */

    /**
     *
     * @param x This value should NOT be mutated by this function!
     * @param <T>
     * @return null when there is no successor because the last element of the enumeration was given (e.g. True for Boolean)
     * @throws NotEnumerableException
     */
    static <T> T successor(T x) throws NotEnumerableException {
        if (x == null) {
            throw new NotEnumerableException(); // "null is not enumerable."
        } else if (x instanceof Character) {
            Character c = (Character) x;
            if (c.equals(Character.MAX_VALUE)) {
                return null;
            } else {
                return (T) (Character) (char) (c+1);
            }
        } else if (x instanceof Boolean) {
            Boolean b = (Boolean) x;
            if (b.equals(true)) { // true = "Boolean.MAX_VALUE"
                return null; // True has no successor.
            } else {
                return (T) Boolean.TRUE; // The successor of false is true.
            }
        } else if (x instanceof Number) {

            if (x instanceof Byte) {
                Byte b = (Byte) x;
                if (b.equals(Byte.MAX_VALUE)) {
                    return null;
                } else {
                    return (T) (Byte) (byte) (b+1);
                }
            } else if (x instanceof Short) {
                Short s = (Short) x;
                if (s.equals(Short.MAX_VALUE)) {
                    return null;
                } else {
                    return (T) (Short) (short) (s+1);
                }
            } else if (x instanceof Integer) {
                Integer i = (Integer) x;
                if (i.equals(Integer.MAX_VALUE)) {
                    return null;
                } else {
                    return (T) (Integer) (i+1);
                }
            } else if (x instanceof Long) {
                Long l = (Long) x;
                if (l.equals(Long.MAX_VALUE)) {
                    return null;
                } else {
                    return (T) (Long) (l+1);
                }
            } else if (x instanceof Float) {
                Float f = (Float) x;
                return (T) (Float) (f+1);
            } else if (x instanceof Double) {
                Double d = (Double) x;
                return (T) (Double) (d+1);
            } else if (x instanceof BigInteger) {
                BigInteger i = (BigInteger) x;
                return (T) i.add(BigInteger.ONE);
            } else if (x instanceof BigDecimal) {
                BigDecimal d = (BigDecimal) x;
                return (T) d.add(BigDecimal.ONE);
            } else if (x instanceof AtomicInteger) {
                AtomicInteger i = (AtomicInteger) x;
                return (T) new AtomicInteger(i.get()+1);
            } else if (x instanceof AtomicLong) {
                AtomicLong l = (AtomicLong) x;
                return (T) new AtomicLong(l.get()+1);
            } else if (x instanceof LongAccumulator) {
                throw new UnsupportedOperationException("ToDo");
            } else if (x instanceof DoubleAccumulator) {
                throw new UnsupportedOperationException("ToDo");
            } else if (x instanceof LongAdder) {
                throw new UnsupportedOperationException("ToDo");
            } else if (x instanceof DoubleAdder) {
                throw new UnsupportedOperationException("ToDo");
            } else { // x is instance of some subclass of Number that's not in the standard library:
                throw new NotEnumerableException();
                // While we could extract a Number's value using ((Number) x).intValue();
                // we wouldn't have any way to create a new instance of that class which represents x+1 !!!
            }

        } else {
            throw new NotEnumerableException();
        }
    }

}
