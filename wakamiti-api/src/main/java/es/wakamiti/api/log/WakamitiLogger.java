/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.log;


import es.wakamiti.api.log.internal.AnsiSupport;
import es.wakamiti.api.log.internal.DefaultStyles;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.slf4j.helpers.MessageFormatter.getThrowableCandidate;


public class WakamitiLogger {

    private static final String NAME = "es.wakamiti";
    private static final List<Runnable> configurationChangeObservers = new ArrayList<>();
    private static Properties styles = DefaultStyles.asProperties();
    private static final AnsiSupport ansi = AnsiSupport.instance;

    private final Logger delegate;


    private WakamitiLogger(String name) {
        this.delegate = LoggerFactory.getLogger(name);
    }

    public static WakamitiLogger of() {
        return new WakamitiLogger(NAME);
    }

    public static WakamitiLogger of(String category) {
        return new WakamitiLogger("%s.%s".formatted(NAME, category));
    }

    public static WakamitiLogger of(Class<?> type) {
        String category = type.getPackage().getName()
                .replaceAll("^%s.([^.]+?).++$".formatted(NAME), "$1");
        return new WakamitiLogger(category);
    }

    /**
     * Enable/disable the Ansi capabilities
     */
    public static void setAnsiEnabled(boolean enabled) {
        Ansi.setEnabled(enabled);
    }


    /**
     * @return Whether the Ansi capabilities are enabled
     */
    public static boolean isAnsiEnabled() {
        return Ansi.isEnabled();
    }

    /**
     * Set the Ansi styles used
     */
    public static void setStyles(Properties styles) {
        Properties properties = DefaultStyles.asProperties();
        for (Object key : styles.keySet()) {
            properties.put(key,styles.getProperty(key.toString()));
        }
        WakamitiLogger.styles = properties;
        configurationChangeObservers.forEach(Runnable::run);
    }


    /**
     * Add a new Ansi style
     */
    public static void addStyle(String key, String value) {
        styles.put(key,value);
        configurationChangeObservers.forEach(Runnable::run);
    }


    /**
     * @return The current Ansi styles
     */
    public static Properties styles() {
        return styles;
    }

    /**
     * Add a configuration change observer, that will be invoked each time the
     * styles change.
     */
    public static void addConfigurationChangeObserver(Runnable observerMethod) {
        configurationChangeObservers.add(observerMethod);
    }

    private String ansi(String message) {
        String level = new Exception().getStackTrace()[1].getMethodName()
                .replaceAll("lambda\\$(\\w+)\\$.*", "$1");
         return ansi.ansi(level, message);
    }


    public void error(
            String message
    ) {
        delegate.error(ansi(message));
    }

    public void error(
            String message,
            Object... arguments
    ) {
        Optional<Throwable> throwable = Optional.ofNullable(getThrowableCandidate(arguments));
        throwable.map(this::getCause)
                .ifPresentOrElse(e -> {
                    arguments[arguments.length - 1] = e.getMessage();
                    delegate.error(ansi("%s: {important}".formatted(message)), arguments);
                }, () -> delegate.error(ansi(message), arguments));
        if (throwable.isPresent() && delegate.isDebugEnabled()) {
            delegate.debug(toString(throwable.get()));
        }
    }

    public void error(
            String message,
            Supplier<?>... arguments
    ) {
        if (delegate.isErrorEnabled()) {
            delegate.error(ansi(message), resolve(arguments));
        }
    }

    public void error(
            Throwable throwable
    ) {
        delegate.error(ansi("%s".formatted("{important}")), getCause(throwable).getMessage());
        if (delegate.isDebugEnabled()) {
            delegate.debug(toString(throwable));
        }
    }

    public void error(
            String message,
            Throwable throwable
    ) {
        delegate.error(ansi("%s: {important}".formatted(message)), getCause(throwable).getMessage());
        if (delegate.isDebugEnabled()) {
            delegate.debug(toString(throwable));
        }
    }

    public void warn(
            String message
    ) {
        delegate.warn(ansi(message));
    }

    public void warn(
            String message,
            Object... arguments
    ) {
        Optional<Throwable> throwable = Optional.ofNullable(getThrowableCandidate(arguments));
        throwable.map(this::getCause)
                .ifPresentOrElse(e -> {
                    arguments[arguments.length - 1] = e.getMessage();
                    delegate.warn(ansi("%s: {important}".formatted(message)), arguments);
                }, () -> delegate.warn(ansi(message), arguments));
        if (throwable.isPresent() && delegate.isDebugEnabled()) {
            delegate.debug(toString(throwable.get()));
        }
    }

    public void warn(
            String message,
            Supplier<?>... arguments
    ) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(ansi(message), resolve(arguments));
        }
    }

    public void info(
            String message
    ) {
        delegate.info(ansi(message));
    }

    public void info(
            String message,
            Object... arguments
    ) {
        delegate.info(ansi(message), arguments);
    }

    public void info(
            String message,
            Supplier<?>... arguments
    ) {
        if (delegate.isInfoEnabled()) {
            delegate.info(ansi(message), resolve(arguments));
        }
    }

    public void debug(
            String message
    ) {
        delegate.debug(ansi(message));
    }

    public void debug(
            String message,
            Object... arguments
    ) {
        delegate.debug(ansi(message), arguments);
    }

    public void debug(
            String message,
            Supplier<?>... arguments
    ) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(ansi(message), resolve(arguments));
        }
    }

    public void trace(
            String message
    ) {
        delegate.trace(ansi(message));
    }

    public void trace(
            String message,
            Object... arguments
    ) {
        delegate.trace(ansi(message), arguments);
    }

    public void trace(
            String message,
            Supplier<?>... arguments
    ) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(ansi(message), resolve(arguments));
        }
    }

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

    private Object[] resolve(
            Supplier<?>... suppliers
    ) {
        return Stream.of(suppliers).map(Supplier::get).toArray();
    }

    private Throwable getCause(
            Throwable throwable
    ) {
        if (throwable instanceof InvocationTargetException e) {
            throwable = e.getTargetException();
        }
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            return getCause(throwable.getCause());
        } else {
            return throwable;
        }
    }

}
