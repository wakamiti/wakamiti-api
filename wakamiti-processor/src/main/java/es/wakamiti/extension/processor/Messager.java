/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.processor;


import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class Messager {

    private static final String PROCESSOR = ExtensionProcessor.class.getSimpleName();

    private final List<Runnable> errors = Collections.synchronizedList(new LinkedList<>());
    private final javax.annotation.processing.Messager messager;

    public Messager(
            ProcessingEnvironment processingEnv
    ) {
        this.messager = processingEnv.getMessager();
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    void showErrors() {
        errors.forEach(Runnable::run);
    }

    /**
     * Logs an informational message.
     *
     * @param message     the message to log
     * @param messageArgs the message arguments
     */
    void info(
            String message,
            Object... messageArgs
    ) {
        log(Diagnostic.Kind.NOTE, message, messageArgs);
    }

    void fatal(
            Throwable e
    ) {
        log(Diagnostic.Kind.ERROR, "UNEXPECTED ERROR: {}", toString(e));
    }

    /**
     * Logs an error message.
     *
     * @param message     the message to log
     * @param messageArgs the message arguments
     */
    void error(
            String message,
            Object... messageArgs
    ) {
        errors.add(() -> log(Diagnostic.Kind.ERROR, message, messageArgs));
    }

    /**
     * Logs an error message for the given element.
     *
     * @param element     the element to log the error for
     * @param message     the message to log
     * @param messageArgs the message arguments
     */
    void error(
            Element element,
            String message,
            Object... messageArgs
    ) {
        errors.add(() -> log(Diagnostic.Kind.ERROR, element, message, messageArgs));
    }

    void error(
            Element element,
            AnnotationMirror mirror,
            String message,
            Object... messageArgs
    ) {
        errors.add(() -> log(Diagnostic.Kind.ERROR, element, mirror, message, messageArgs));
    }

    /**
     * Logs a message.
     *
     * @param kind        the kind of message
     * @param message     the message to log
     * @param messageArgs the message arguments
     */
    private void log(
            Diagnostic.Kind kind,
            String message,
            Object... messageArgs
    ) {
        messager.printMessage(kind, format("[{}] :: ", PROCESSOR) + format(message, messageArgs));
    }

    private void log(
            Diagnostic.Kind kind,
            Element element,
            String message,
            Object... messageArgs
    ) {
        messager.printMessage(kind, format("[{}] at {} :: ", PROCESSOR, element.asType())
                + format(message, messageArgs), element);
    }

    private void log(
            Diagnostic.Kind kind,
            Element element,
            AnnotationMirror mirror,
            String message,
            Object... messageArgs
    ) {
        messager.printMessage(kind, format("[{}] at {} :: ", PROCESSOR, element.asType())
                + format(message, messageArgs), element, mirror);
    }

    /**
     * Converts the throwable stack trace to a string.
     *
     * @param throwable the throwable to convert
     * @return the string representation of the throwable
     */
    private String toString(
            Throwable throwable
    ) {
        try (Writer writer = new StringWriter()) {
            try (PrintWriter printWriter = new PrintWriter(writer)) {
                throwable.printStackTrace(printWriter);
                return writer.toString();
            }
        } catch (IOException e) {
            return throwable.toString();
        }
    }

    /**
     * Formats the message with the given arguments.
     *
     * @param message the message to format
     * @param args    the arguments to format the message with
     * @return the formatted message
     */
    private String format(
            String message,
            Object... args
    ) {
        message = message.replace("{}", "%s")
                .replaceAll("\\{(\\d+)}", "%$1\\$s");
        return message.formatted(args);
    }

}
