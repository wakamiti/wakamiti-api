/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * The {@code @PostConstruct} annotation is used on a method that
 * needs to be executed after dependency injection is done to
 * perform any initialization. This method MUST be invoked before
 * the extension class is instantiated. This annotation MUST be
 * supported on all classes annotated with {@link Extension}
 * annotation.
 * Only one method can be annotated with this annotation. The method
 * on which the {@code @PostConstruct} annotation is applied MUST
 * fulfill all of the following criteria:
 * <ol>
 * <li>The method MUST NOT have any parameters.</li>
 * <li>If a method returns a value, it will be ignored.</li>
 * <li>The method on which {@code @PostConstruct} is applied MAY be
 * public, protected, package private or private.</li>
 * <li>The method MUST NOT be static.</li>
 * <li>The method MAY be final.</li>
 * </ol>
 *
 * @see Inject
 * @see PreDestroy
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface PostConstruct {
}