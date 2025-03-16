/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.lang;


import java.io.Serial;

import static org.slf4j.helpers.MessageFormatter.getThrowableCandidate;
import static org.slf4j.helpers.MessageFormatter.trimmedCopy;


public class WakamitiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6196016511564036001L;

    public WakamitiException(
            String message
    ) {
        super(message);
    }

    public WakamitiException(
            Throwable throwable
    ) {
        super(throwable);
    }

    public WakamitiException(
            String message,
            Throwable throwable
    ) {
        super(message, throwable);
    }

    public WakamitiException(
            String message,
            Object... args
    ) {
        super(format(message, argsWithoutThrowable(args)), getThrowableCandidate(args));
    }

    protected static String format(
            String message,
            Object... args
    ) {
        return message.replace("{}", "%s").formatted(args);
    }


    protected static Object[] argsWithoutThrowable(
            Object[] args
    ) {
        return getThrowableCandidate(args) == null ? args : trimmedCopy(args);
    }

}
