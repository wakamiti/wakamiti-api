/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension;


import java.util.stream.Stream;


public interface ModuleLayerProvider {

    static ModuleLayerProvider boot() {
        return () -> Stream.of(ModuleLayer.boot());
    }

    static ModuleLayerProvider empty() {
        return () -> Stream.of(ModuleLayer.empty());
    }

    static ModuleLayerProvider compose(ModuleLayerProvider... moduleLayerProviders) {
        return () -> Stream.of(moduleLayerProviders).flatMap(ModuleLayerProvider::moduleLayers);
    }

    Stream<ModuleLayer> moduleLayers();

}
