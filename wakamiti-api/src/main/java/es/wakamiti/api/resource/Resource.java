/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.resource;


import es.wakamiti.api.lang.Lazy;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.function.Supplier;


public class Resource {

    private final ContentType contentType;
    private final URI URI;
    private final Path relativePath;
    private final Supplier<InputStream> reader;
    private final Lazy<Hash> hash;


    public Resource(
            ContentType contentType,
            URI URI,
            Path relativePath,
            Supplier<InputStream> reader
    ) {
        this.contentType = contentType;
        this.URI = URI;
        this.relativePath = relativePath;
        this.reader = reader;
        this.hash = Lazy.of(() -> Hash.of(URI));
    }


    public ContentType contentType() {
        return contentType;
    }


    public InputStream open() {
        return reader.get();
    }

    public Hash hash() {
        return hash.get();
    }

    public URI URI() {
        return URI;
    }


    public Path relativePath() {
        return relativePath;
    }

}
