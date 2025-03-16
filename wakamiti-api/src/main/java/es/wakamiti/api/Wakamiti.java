/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api;


import es.wakamiti.api.config.Configuration;
import es.wakamiti.api.data.DataType;
import es.wakamiti.api.lang.WakamitiException;
import es.wakamiti.api.resource.ContentType;
import es.wakamiti.extension.ExtensionManager;

import java.io.IOException;
import java.io.Writer;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.stream.Stream;


public interface Wakamiti {

    static Wakamiti of() {
        return Builder.get().build();
    }

    static Wakamiti of(
            Configuration configuration
    ) {
        return Builder.get().configuration(configuration).build();
    }

    static Wakamiti of(
            Configuration configuration,
            ExtensionManager extensionManager
    ) {
        return Builder.get().configuration(configuration).extensionManager(extensionManager).build();
    }

    UUID buildPlan();

    void serialize(
            UUID planNodeID,
            Writer writer
    ) throws IOException;

    ExtensionManager extensionManager();

    interface Builder<T extends Wakamiti> {

        static Builder<?> get() {
            return ServiceLoader.load(Wakamiti.Builder.class).findFirst().orElseThrow(
                    () -> new WakamitiException("There is no Wakamiti implementation found!")
            );
        }

        Builder<T> configuration(
                Configuration configuration
        );

        Builder<T> extensionManager(
                ExtensionManager extensionManager
        );

        T build();

    }

    Stream<DataType> dataTypes();

    Stream<ContentType> contentTypes();

}
