/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension;


import es.wakamiti.extension.annotation.Scope;

import java.util.Optional;
import java.util.ServiceLoader;


/**
 * This interface allows third-party contributors to implement custom
 * mechanisms to retrieve extension instances, instead of using the Java
 * {@link ServiceLoader} approach.
 * <p>
 * This is specially suited for IoC injection frameworks that may manage
 * object instances in a wide range of different ways.
 */
public interface ExtensionLoader {

    /**
     * Loads an extension instance using the specified service provider and scope.
     *
     * @param <T>      the type of the extension
     * @param provider the service provider for the extension
     * @param scope    the scope in which the extension should be loaded
     * @return an Optional containing the loaded extension instance, or empty if the instance could not be loaded
     */
    <T> Optional<T> load(
            ServiceLoader.Provider<T> provider,
            Scope scope
    );

}