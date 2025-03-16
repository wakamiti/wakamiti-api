/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.test.log;


import es.wakamiti.api.log.WakamitiLogger;
import es.wakamiti.api.test.util.QueueAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.event.Level.*;


public class WakamitiLoggerTest {

    private final WakamitiLogger log = WakamitiLogger.of();
    private final WakamitiLogger logInfo = WakamitiLogger.of("custom");
    private final WakamitiLogger logOff = WakamitiLogger.of(WakamitiLoggerTest.class);

    private static Stream<Arguments> provideLevels() {
        return Stream.of(
                Arguments.of(ERROR, "\u001B[31mTest logging something\u001B[m"),
                Arguments.of(WARN, "\u001B[33mTest logging something\u001B[m"),
                Arguments.of(INFO, "Test logging something"),
                Arguments.of(DEBUG, "Test logging something"),
                Arguments.of(TRACE, "Test logging something")
        );
    }

    private static Stream<Arguments> provideThrowableLevels() {
        return Stream.of(
                Arguments.of(ERROR, 31),
                Arguments.of(WARN, 33)
        );
    }

    @AfterEach
    public void tearDown() {
        QueueAppender.clear();
        WakamitiLogger.setAnsiEnabled(true);
    }

    @ParameterizedTest
    @MethodSource("provideLevels")
    public void testWhenMessageWithoutArgs(Level level, String message) {
        Map<Level, Consumer<String>> logs = Map.of(
                ERROR, log::error,
                WARN, log::warn,
                INFO, log::info,
                DEBUG, log::debug,
                TRACE, log::trace
        );

        logs.get(level).accept("Test logging something");

        assertThat(QueueAppender.count()).isEqualTo(1);
        assertThat(QueueAppender.poll()).isEqualTo(message);
    }

    @ParameterizedTest
    @MethodSource("provideLevels")
    public void testWhenMessageWithArgs(Level level, String message) {
        Map<Level, BiConsumer<String, Object>> logs = Map.of(
                ERROR, log::error,
                WARN, log::warn,
                INFO, log::info,
                DEBUG, log::debug,
                TRACE, log::trace
        );

        logs.get(level).accept("Test logging {}", "something");

        assertThat(QueueAppender.count()).isEqualTo(1);
        assertThat(QueueAppender.poll()).isEqualTo(message);
    }

    @ParameterizedTest
    @MethodSource("provideLevels")
    public void testErrorWhenMessageWithSuppliers(Level level, String message) {
        Map<Level, BiConsumer<String, Supplier<?>>> logs = Map.of(
                ERROR, log::error,
                WARN, log::warn,
                INFO, log::info,
                DEBUG, log::debug,
                TRACE, log::trace
        );

        logs.get(level).accept("Test logging {}", () -> "something");

        assertThat(QueueAppender.count()).isEqualTo(1);
        assertThat(QueueAppender.poll()).isEqualTo(message);
    }

    @ParameterizedTest
    @MethodSource("provideThrowableLevels")
    public void testErrorWhenMessageWithThrowable(Level level, int code) {
        Map<Level, BiConsumer<String, Object>> logs = Map.of(
                ERROR, logInfo::error,
                WARN, logInfo::warn
        );

        logs.get(level).accept("Test logging something",
                new Exception("Some exception", new Exception("Cause exception")));

        assertThat(QueueAppender.count()).isEqualTo(1);
        assertThat(QueueAppender.poll())
                .isEqualTo(("\u001B[%1$smTest logging something: \u001B[m" +
                        "\u001B[%1$s;1mCause exception\u001B[m\u001B[%1$sm\u001B[m").formatted(code));
    }

    @ParameterizedTest
    @MethodSource("provideThrowableLevels")
    public void testErrorWhenMessageWithThrowableAndDebugEnabled(Level level, int code) {
        Map<Level, BiConsumer<String, Object>> logs = Map.of(
                ERROR, log::error,
                WARN, log::warn
        );

        logs.get(level).accept("Test logging something",
                new Exception("Some exception", new Exception("Cause exception")));

        assertThat(QueueAppender.count()).isEqualTo(2);
        assertThat(QueueAppender.poll())
                .isEqualTo(("\u001B[%1$smTest logging something: \u001B[m" +
                        "\u001B[%1$s;1mCause exception\u001B[m\u001B[%1$sm\u001B[m").formatted(code));
        assertThat(QueueAppender.poll())
                .startsWith("java.lang.Exception: Some exception")
                .contains("Caused by: java.lang.Exception: Cause exception");
    }

    @Test
    public void testErrorWhenOnlyThrowable() {
        logInfo.error(new Exception("Some exception", new Exception("Cause exception")));

        assertThat(QueueAppender.count()).isEqualTo(1);
        assertThat(QueueAppender.poll())
                .isEqualTo("\u001B[31m\u001B[m\u001B[31;1mCause exception\u001B[m\u001B[31m\u001B[m");
    }

    @Test
    public void testErrorWhenOnlyThrowableAndDebugEnabled() {
        log.error(new Exception("Some exception", new Exception("Other exception", new Exception("Cause exception"))));

        assertThat(QueueAppender.count()).isEqualTo(2);
        assertThat(QueueAppender.poll())
                .isEqualTo("\u001B[31m\u001B[m\u001B[31;1mCause exception\u001B[m\u001B[31m\u001B[m");
        assertThat(QueueAppender.poll())
                .startsWith("java.lang.Exception: Some exception")
                .contains("Caused by: java.lang.Exception: Cause exception");
    }

    @ParameterizedTest
    @MethodSource("provideThrowableLevels")
    public void testErrorWhenMessageWithArgsAndThrowable(Level level, int code) {
        Map<Level, BiMultiConsumer<String, Object>> logs = Map.of(
                ERROR, logInfo::error,
                WARN, logInfo::warn
        );

        logs.get(level).accept("Test logging {}", "something",
                new Exception("Some exception", new Exception("Cause exception")));

        assertThat(QueueAppender.count()).isEqualTo(1);
        assertThat(QueueAppender.poll())
                .isEqualTo(("\u001B[%1$smTest logging something: " +
                        "\u001B[m\u001B[%1$s;1mCause exception\u001B[m\u001B[%1$sm\u001B[m").formatted(code));
    }

    @ParameterizedTest
    @MethodSource("provideThrowableLevels")
    public void testErrorWhenMessageWithArgsAndThrowableAndDebugEnabled(Level level, int code) {
        Map<Level, BiMultiConsumer<String, Object>> logs = Map.of(
                ERROR, log::error,
                WARN, log::warn
        );

        logs.get(level).accept("Test logging {}", "something",
                new InvocationTargetException(new Exception("Cause exception"), "Some exception"));

        assertThat(QueueAppender.count()).isEqualTo(2);
        assertThat(QueueAppender.poll())
                .isEqualTo(("\u001B[%1$smTest logging something: " +
                        "\u001B[m\u001B[%1$s;1mCause exception\u001B[m\u001B[%1$sm\u001B[m").formatted(code));
        assertThat(QueueAppender.poll())
                .startsWith("java.lang.reflect.InvocationTargetException: Some exception")
                .contains("Caused by: java.lang.Exception: Cause exception");
    }

    @Test
    public void testAddStyle() {
        WakamitiLogger.addStyle("custom", "magenta");

        log.info("{!custom} Test logging something");
        log.info("Test logging {custom}", "something");

        assertThat(QueueAppender.count()).isEqualTo(2);
        assertThat(QueueAppender.poll())
                .isEqualTo("\u001B[35mTest logging something\u001B[m");
        assertThat(QueueAppender.poll())
                .isEqualTo("Test logging \u001B[35msomething\u001B[m");
    }

    @Test
    public void testAddStyles() {
        Properties styles = new Properties();
        styles.put("custom", "magenta");
        WakamitiLogger.setStyles(styles);

        log.error("{!custom} Test logging something");
        log.error("{!important} Test logging {custom}", "something");

        assertThat(QueueAppender.count()).isEqualTo(2);
        assertThat(QueueAppender.poll())
                .isEqualTo("\u001B[31;35mTest logging something\u001B[m");
        assertThat(QueueAppender.poll())
                .isEqualTo("\u001B[31;1mTest logging \u001B[m" +
                        "\u001B[31;1;35msomething\u001B[m\u001B[31;1m\u001B[m");
    }

    @Test
    public void testWhenAnsiDisabled() {
        WakamitiLogger.setAnsiEnabled(false);

        log.info("{!custom} Test logging something");
        log.info("{!important} Test logging {custom}", "something");

        assertThat(QueueAppender.count()).isEqualTo(2);
        assertThat(QueueAppender.poll())
                .isEqualTo("Test logging something");
        assertThat(QueueAppender.poll())
                .isEqualTo("Test logging something");
        assertThat(WakamitiLogger.isAnsiEnabled()).isFalse();
    }

    @Test
    public void testWhenLoggerOff() {
        logOff.info("Test logging something");
        logOff.info("Test logging {}", "something");
        logOff.info("Test logging {}", () -> "something");
        logOff.warn("Test logging something");
        logOff.warn("Test logging {}", "something");
        logOff.warn("Test logging {}", () -> "something");
        logOff.error("Test logging something");
        logOff.error("Test logging {}", "something");
        logOff.error("Test logging {}", () -> "something");
        logOff.debug("Test logging something");
        logOff.debug("Test logging {}", "something");
        logOff.debug("Test logging {}", () -> "something");
        logOff.trace("Test logging something");
        logOff.trace("Test logging {}", "something");
        logOff.trace("Test logging {}", () -> "something");

        assertThat(QueueAppender.count()).isEqualTo(0);
        assertThat(WakamitiLogger.isAnsiEnabled()).isTrue();
    }

    @FunctionalInterface
    private interface BiMultiConsumer<T, U> {
        void accept(T t, U... u);
    }
}
