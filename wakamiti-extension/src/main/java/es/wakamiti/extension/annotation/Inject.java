/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.annotation;


import java.lang.annotation.*;

/**
 * Identifies injectable constructors, methods, and fields. May apply to static
 * as well as instance members. An injectable member may have any access
 * modifier (private, package-private, protected, public). Constructors are
 * injected first, followed by fields, and then methods. Fields and methods
 * in superclasses are injected before those in subclasses. Ordering of
 * injection among fields and among methods in the same class is not specified.
 * <p>
 * Injectable constructors are annotated with {@code @Inject} and accept
 * zero or more dependencies as arguments. {@code @Inject} can apply to at most
 * one constructor per class.
 * <p>
 * {@code @Inject} is optional for public, no-argument constructors when no
 * other constructors are present. This enables injectors to invoke default
 * constructors.
 * <p>
 * Injectable fields:
 * <ul>
 *   <li>are annotated with {@code @Inject}.
 *   <li>are not final.
 *   <li>may have any otherwise valid name.</li>
 * </ul>
 * <p>
 * Injectable methods:
 * <ul>
 *   <li>are annotated with {@code @Inject}.</li>
 *   <li>are not abstract.</li>
 *   <li>do not declare type parameters of their own.</li>
 *   <li>may return a result</li>
 *   <li>may have any otherwise valid name.</li>
 *   <li>accept zero or more dependencies as arguments.</li>
 * </ul>
 * <p>
 * The injector ignores the result of an injected method, but
 * non-{@code void} return types are allowed to support use of the method in
 * other contexts (builder-style method chaining, for example).
 * <p>
 * Examples:
 * <pre>{@code
 *   public class Car {
 *     // Injectable constructor
 *     @Inject public Car(Engine engine) { ... }
 *
 *     // Injectable field
 *     @Inject private Provider<Seat> seatProvider;
 *
 *     // Injectable package-private method
 *     @Inject void install(Windshield windshield, Trunk trunk) { ... }
 *   }
 * }</pre>
 *
 * <p>A method annotated with {@code @Inject} that overrides another method
 * annotated with {@code @Inject} will only be injected once per injection
 * request per instance. A method with <i>no</i> {@code @Inject} annotation
 * that overrides a method annotated with {@code @Inject} will not be
 * injected.
 *
 * <p>Injection of members annotated with {@code @Inject} is required. While an
 * injectable member may use any accessibility modifier (including
 * <tt>private</tt>), platform or injector limitations (like security
 * restrictions or lack of reflection support) might preclude injection
 * of non-public members.
 */
// *
// * Annotation to mark fields or methods for dependency injection.
// * <p>
// * Fields annotated with {@code Inject} and having a type of other extension points
// * will be automatically assigned. Methods annotated with {@code Inject} will be
// * executed when an extension instance is created, after the injected fields have
// * been resolved.
// * <p>
// * This feature provides a minimal inversion of control mechanism, restricted
// * to extensions managed by the {@code ExtensionManager}.
// */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface Inject {


    /**
     * Specifies the name of a specific extension to be injected, in case more than one
     * is available, and the priority mechanism does not provide enough control.
     * Such extensions must inform the {@link Extension#name()} property.
     *
     * @return the name of the specific extension to be injected
     */
    String value() default "";

}
