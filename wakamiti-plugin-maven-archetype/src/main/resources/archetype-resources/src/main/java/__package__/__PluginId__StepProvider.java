/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ${package};


import es.wakamiti.api.annotation.I18nResource;
import es.wakamiti.api.annotation.Step;
import es.wakamiti.api.contributor.StepProvider;
import es.wakamiti.api.log.WakamitiLogger;
import es.wakamiti.extension.annotation.Extension;


@Extension(name = "${pluginId}-steps")
@I18nResource("${package2}")
public class ${PluginId}StepProvider implements StepProvider {

    private static final WakamitiLogger LOGGER = WakamitiLogger.of("${pluginId}");


    // the following is an example of step definitions

    @Step("${pluginId}.step1")
    public void step1() {
        // FIXME: this is just an example
    }

}