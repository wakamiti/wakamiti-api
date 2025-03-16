/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
module wakamiti.plugin.${pluginId} {

    exports ${package};

    requires wakamiti.extension;
    requires wakamiti.api;

    opens ${package} to wakamiti.extension;

    provides es.wakamiti.api.contributor.ConfigurationProvider with
        ${package}.${PluginId}ConfigurationProvider;
    provides es.wakamiti.api.contributor.StepProvider with
        ${package}.${PluginId}StepProvider;

}