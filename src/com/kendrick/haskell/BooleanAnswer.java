package com.kendrick.haskell;

/**
 * This is just a wrapper for a boolean with some convenient methods on it.
 * It's returned by
 *
 * LazyList.ifAny(), LazyList.ifAll(),
 * LazyList.ifLengthIsAtLeast(), LazyList.ifLengthIsAtMost(),
 * LazyList.ifElem(), LazyList.ifIsEmpty(),
 * LazyList.ifContains(), LazyList.ifContainsAll(),
 * LazyList.ifEquals()
 *
 * and enables the programmer
 * to write even more compact code/one-liners without the need of using the Java if-else construct.
 *
 * Example:
 * LazyList.of(1,2,3,4)
 *     .ifAny((num) -> num > 4)
 *     .then(() -> System.out.println("List contains number larger than 4."))
 *     .orElse(() -> System.out.println("List does not contain number larger than 4.")); // optional!
 *
 * @author Kendrick Gruenberg
 */
public class BooleanAnswer {
    private final boolean bool;

    BooleanAnswer(boolean bool) {
        this.bool = bool;
    }

    /**
     * Executes the given Runnable action if and only if this BooleanAnswer is True.
     *
     * @param action the Runnable to run when this BooleanAnswer is True
     * @return this BooleanAnswer again so that .orElse() can be called afterwards (optional!)
     */
    public BooleanAnswer then(Runnable action) {
        if (this.bool) {
            action.run();
        }
        return this;
    }

    /**
     * Executes the given Runnable action if and only if this BooleanAnswer is False.
     *
     * @param action the Runnable to run when this BooleanAnswer is False
     */
    public void orElse(Runnable action) {
        if (!this.bool) {
            action.run();
        }
    }
}
