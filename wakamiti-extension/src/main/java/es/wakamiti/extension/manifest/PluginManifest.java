/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.manifest;


import es.wakamiti.extension.Version;

import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public record PluginManifest(
        PluginID id,
        Version version,
        String module,
        String pluginJarFile,
        ArtifactID parentArtifact,
        String parentModule,
        String name,
        String description,
        List<ArtifactID> dependencies,
        Map<String, List<String>> extensions
) {
    public static final String PLUGIN_SECTION = "Plugin";

    public static final String PLUGIN_ID = "Plugin-ID";
    public static final String PLUGIN_VERSION = "Plugin-Version";
    public static final String PLUGIN_MODULE = "Plugin-Module";
    public static final String PLUGIN_JAR_FILE = "Plugin-Jar-File";
    public static final String PLUGIN_PARENT_ARTIFACT = "Plugin-Parent-Artifact";
    public static final String PLUGIN_PARENT_MODULE = "Plugin-Parent-Module";
    public static final String PLUGIN_NAME = "Plugin-Name";
    public static final String PLUGIN_DESCRIPTION = "Plugin-Description";
    public static final String PLUGIN_DEPENDENCIES = "Plugin-Dependencies";
    public static final String PLUGIN_EXTENSIONS = "Plugin-Extensions";
    public static final String DELIMITER = ";";

    public PluginManifest {
        requireNonNull(id, "id");
        requireNonNull(version, "version");
        requireNonNull(module, "module");
    }

    public static PluginManifest of(
            Properties properties
    ) {
        return new PluginManifest(
                PluginID.of(properties.getProperty(PLUGIN_ID)),
                Version.of(properties.getProperty(PLUGIN_VERSION)),
                properties.getProperty(PLUGIN_MODULE),
                properties.getProperty(PLUGIN_JAR_FILE),
                ArtifactID.of(properties.getProperty(PLUGIN_PARENT_ARTIFACT)),
                properties.getProperty(PLUGIN_PARENT_MODULE),
                properties.getProperty(PLUGIN_NAME),
                properties.getProperty(PLUGIN_DESCRIPTION),
                Optional.ofNullable(properties.getProperty(PLUGIN_DEPENDENCIES))
                        .map(p -> Stream.of(p.split(DELIMITER)).map(ArtifactID::of).toList())
                        .orElse(List.of()),
                Optional.ofNullable(properties.getProperty(PLUGIN_EXTENSIONS))
                        .map(p -> Stream.of(p.split(DELIMITER)).collect(Collectors.toMap(
                                line -> line.split("=")[0],
                                line -> List.of(line.split("=")[1].split(","))
                        ))).orElse(Map.of())
        );
    }

    public static PluginManifest of(
            Manifest manifest
    ) {
        Properties properties = new Properties();
        manifest.getAttributes(PLUGIN_SECTION)
                .forEach((key, value) -> properties.setProperty(key.toString(), value.toString()));
        return PluginManifest.of(properties);
    }

    public Map<String, String> asMap() {
        Map<String, String> map = new LinkedHashMap<>();
        put(map, PLUGIN_ID, id);
        put(map, PLUGIN_VERSION, version);
        put(map, PLUGIN_MODULE, module);
        put(map, PLUGIN_JAR_FILE, pluginJarFile);
        put(map, PLUGIN_PARENT_ARTIFACT, parentArtifact);
        put(map, PLUGIN_PARENT_MODULE, parentModule);
        put(map, PLUGIN_NAME, name);
        put(map, PLUGIN_DESCRIPTION, description);

        if (!dependencies.isEmpty()) {
            map.put(
                    PLUGIN_DEPENDENCIES,
                    dependencies.stream().map(ArtifactID::toString).collect(Collectors.joining(DELIMITER))
            );
        }
        if (!extensions.isEmpty()) {
            map.put(
                    PLUGIN_EXTENSIONS,
                    extensions.entrySet().stream()
                            .map(e -> e.getKey() + "=" + String.join(",", e.getValue()))
                            .collect(Collectors.joining(DELIMITER))
            );
        }
        map.values().removeAll(Collections.singleton(null));
        return map;
    }

    private void put(
            Map<String, String> map,
            String key,
            Object value
    ) {
        if (value == null || value.toString().isBlank()) {
            return;
        }
        map.put(key, value.toString());
    }

    public ArtifactID artifactID() {
        return new ArtifactID(id.groupId(), id.artifactId(), version);
    }
}
