/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.annotation;


import es.wakamiti.extension.ExtensionLoader;

import java.lang.annotation.*;


/**
 * This annotation allows marking a class as an extension managed by the
 * {@code ExtensionManager}.
 * <p>
 * Notice that any class not annotated with {@link Extension} will not be
 * managed in spite of implementing or extending the {@link ExtensionPoint}
 * class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Extension {

    /**
     * The artifactId of the extension
     */
    String name() default "";

    /**
     * Priority used when extensions collide, the highest value has priority
     * over others.
     */
    Priority priority() default Priority.NORMAL;

    /**
     * The scope of the extension.
     * <ul>
     * <li>{@link Scope#LOCAL}: The extension is only available within the local context.</li>
     * <li>{@link Scope#GLOBAL}: The extension is available globally.</li>
     * </ul>
     */
    Scope scope() default Scope.LOCAL;

    /**
     * A custom extension loader that will be used to load the extension.
     * Generally, this is only necessary for extensions designed to be integrated
     * with some external IoC mechanism.
     */
    Class<? extends ExtensionLoader> loader() default ExtensionLoader.class;

}