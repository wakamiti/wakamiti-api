/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.contributor;


import es.wakamiti.api.config.Configuration;
import es.wakamiti.extension.annotation.ExtensionPoint;


@ExtensionPoint
public interface ConfigurationProvider extends Contributor {

    /**
     * Retrieves the default configuration settings for the object being
     * configured.
     *
     * @return The default configuration
     */
    Configuration configuration();

}
