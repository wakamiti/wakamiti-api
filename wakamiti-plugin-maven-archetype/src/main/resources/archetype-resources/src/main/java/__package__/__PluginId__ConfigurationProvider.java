/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ${package};


import es.wakamiti.api.config.Configuration;
import es.wakamiti.api.contributor.ConfigurationProvider;
import es.wakamiti.extension.annotation.Extension;

import java.nio.charset.StandardCharsets;


@Extension(name = "${pluginId}-config")
public class ${PluginId}ConfigurationProvider implements ConfigurationProvider {

    public static final String PROPERTY1 = "${pluginId}.property1";

    private static final Configuration CONFIGURATION = Configuration.factory()
            .accordingDefinitionsFromResource(
                    "${pluginId}-config.yml",
                    StandardCharsets.UTF_8,
                    ${PluginId}ConfigurationProvider.class.getModule().getClassLoader()
            );

    @Override
    public Configuration configuration() {
        return CONFIGURATION;
    }

}