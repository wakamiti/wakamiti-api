/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.config;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * This annotation allows classes to be used as a data source for a configuration
 */
@Retention(RUNTIME)
@Target({TYPE})
@Documented
public @interface AnnotatedConfiguration {

    /**
     * Pairs of [key,value] that defines the configuration
     */
    Property[] value() default {};

}
