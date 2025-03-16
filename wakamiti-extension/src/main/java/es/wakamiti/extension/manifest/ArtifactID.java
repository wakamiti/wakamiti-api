/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.manifest;



import es.wakamiti.extension.Version;

import java.util.StringTokenizer;


/**
 * This class represents the identifier or an artifact (either a plugin or a plugin dependency).
 * <p>
 * It is formed by a groupId, a artifactId, and optionally a version.
 */
public record ArtifactID(
        String groupId,
        String artifactId,
        Version version
) {

    public static ArtifactID of(
            String coordinates
    ) {
        if (coordinates == null || coordinates.isBlank()) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(coordinates, ":");
        if (tokenizer.countTokens() < 2) {
            throw new IllegalArgumentException("Invalid coordinates format: %s".formatted(coordinates));
        }
        return new ArtifactID(
                tokenizer.nextToken(),
                tokenizer.nextToken(),
                Version.of(tokenizer.nextToken())
        );
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public PluginID pluginID() {
        return new PluginID(groupId, artifactId);
    }

}
