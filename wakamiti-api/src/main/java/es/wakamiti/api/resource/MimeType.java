/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.resource;

import es.wakamiti.api.lang.WakamitiException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class MimeType {

    private static final Map<String, String> MIME_TYPES = getMimeTypes();

    private static Map<String, String> getMimeTypes() {
        Properties properties = new Properties();
        try {
            properties.load(MimeType.class.getClassLoader().getResourceAsStream("mime-types.properties"));
        } catch (IOException e) {
            throw new WakamitiException("Cannot load mime types.", e);
        }
        return properties.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()));
    }

    private MimeType() {

    }

    public static String getMimeType(String extension) {
        return MIME_TYPES.get(extension);
    }

    public static String getExtension(String mimeType) {
        return MIME_TYPES.entrySet().stream()
                .filter(e -> e.getValue().equals(mimeType))
                .map(Map.Entry::getKey)
                .findFirst().orElse(MIME_TYPES.get("txt"));
    }

}
