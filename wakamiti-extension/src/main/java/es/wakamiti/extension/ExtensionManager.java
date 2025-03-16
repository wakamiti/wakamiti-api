/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension;


import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Stream;


public interface ExtensionManager {

    static ExtensionManager create() {
        return ServiceLoader.load(ExtensionManager.class).findFirst()
                .orElseThrow(() -> new NoSuchElementException("No extension manager found."))
                .withModuleLayerProvider(ModuleLayerProvider.boot());
    }

    ExtensionManager withModuleLayerProvider(ModuleLayerProvider moduleLayerProvider);

    ExtensionManager withInjectionProvider(InjectionProvider injectionProvider);

    <T> Optional<T> getExtension(Class<T> extensionPoint);

    <T> Optional<T> getExtension(Class<T> extensionPoint, Predicate<Class<?>> filter);

    <T> Optional<T> getExtensionByName(Class<T> extensionPoint, String name);

    <T> Optional<T> getExtensionByName(Class<T> extensionPoint, Predicate<String> name);

    <T> Stream<T> getExtensions(Class<T> extensionPoint);

    <T> Stream<T> getExtensions(Class<T> extensionPoint, Predicate<Class<?>> filter);

    <T> Stream<T> getExtensionsByName(Class<T> extensionPoint, Predicate<String> filter);

    void clear();

}
