/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.annotation;


import java.lang.annotation.*;


/**
 * This annotation allows marking an interface or abstract class as an
 * extension point managed by the {@code ExtensionManager}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ExtensionPoint {

    /**
     * The load strategy used when an extension is requested.
     *
     * @return The load strategy for the extension point.
     */
    LoadStrategy loadStrategy() default LoadStrategy.UNDEFINED;

}