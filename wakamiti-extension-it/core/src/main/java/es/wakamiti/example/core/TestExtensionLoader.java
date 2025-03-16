/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.example.core;

import es.wakamiti.extension.ExtensionLoader;
import es.wakamiti.extension.annotation.Scope;

import java.util.Optional;
import java.util.ServiceLoader;

public class TestExtensionLoader implements ExtensionLoader {

    public static Object lastExtensionLoaded;

    @Override
    public <T> Optional<T> load(ServiceLoader.Provider<T> provider, Scope scope) {
        System.out.println("getting "+scope+" instance using external extension loader");
        try {
            var instance = provider.type().getConstructor().newInstance();
            lastExtensionLoaded = instance;
            return Optional.of(instance);
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }

}
