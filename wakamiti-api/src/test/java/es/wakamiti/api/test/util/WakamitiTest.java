/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.test.util;


import es.wakamiti.api.Wakamiti;
import es.wakamiti.api.config.Configuration;
import es.wakamiti.api.data.DataType;
import es.wakamiti.api.resource.ContentType;
import es.wakamiti.extension.ExtensionManager;

import java.io.IOException;
import java.io.Writer;
import java.util.UUID;
import java.util.stream.Stream;


public class WakamitiTest implements Wakamiti {

    @Override
    public UUID buildPlan() {
        return null;
    }

    @Override
    public void serialize(UUID planNodeID, Writer writer) throws IOException {

    }

    @Override
    public ExtensionManager extensionManager() {
        return null;
    }

    @Override
    public Stream<DataType> dataTypes() {
        return Stream.empty();
    }

    @Override
    public Stream<ContentType> contentTypes() {
        return Stream.empty();
    }

    public static class Builder implements Wakamiti.Builder<WakamitiTest> {

        @Override
        public Wakamiti.Builder<WakamitiTest> configuration(Configuration configuration) {
            return this;
        }

        @Override
        public Wakamiti.Builder<WakamitiTest> extensionManager(ExtensionManager extensionManager) {
            return this;
        }

        @Override
        public WakamitiTest build() {
            return (WakamitiTest) Wakamiti.Builder.get();
        }
    }
}
