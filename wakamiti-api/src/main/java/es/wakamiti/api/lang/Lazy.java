/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.lang;


import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


public class Lazy<T> {

    private final Supplier<T> supplier;
    private T instance;

    private Lazy(
            Supplier<T> supplier
    ) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(
            Supplier<T> supplier
    ) {
        return new Lazy<>(supplier);
    }

    public static <T> Lazy<T> ofOptional(
            Supplier<Optional<T>> supplier
    ) {
        return new Lazy<>(supplier).map(Optional::orElseThrow);
    }

    public T get() {
        if (instance == null) {
            instance = supplier.get();
        }
        return instance;
    }


    public void reset() {
        instance = null;
    }


    public <U> Lazy<U> map(
            Function<T, U> function
    ) {
        return new Lazy<>(() -> function.apply(supplier.get()));
    }

}
