/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.manifest;


import es.wakamiti.extension.Version;

import java.util.StringTokenizer;


public record PluginID(
        String groupId,
        String artifactId
) {

    public static PluginID of(
            String coordinates
    ) {
        if (coordinates == null || coordinates.isBlank()) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(coordinates, ":");
        return new PluginID(tokenizer.nextToken(), tokenizer.nextToken());
    }

    public ArtifactID version(
            Version version
    ) {
        return new ArtifactID(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId;
    }

}
