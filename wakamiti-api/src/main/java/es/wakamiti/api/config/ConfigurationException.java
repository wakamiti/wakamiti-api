/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.config;


import java.io.Serial;


public class ConfigurationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7175876124782335084L;

    public ConfigurationException(
            Throwable throwable
    ) {
        super(throwable.getMessage(), throwable);
    }

    public ConfigurationException(
            String message
    ) {
        super(message);
    }

    public ConfigurationException(
            String message,
            Throwable throwable
    ) {
        super(message, throwable);
    }

}
