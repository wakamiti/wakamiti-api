/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.test.util;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.LinkedList;
import java.util.List;


/**
 * A custom Logback appender that stores log messages in a queue.
 */
public class QueueAppender extends AppenderBase<ILoggingEvent> {

    private static final List<String> messages = new LinkedList<>();


    /**
     * Appends a log event to the internal message queue.
     *
     * @param event the log event to append
     */
    @Override
    protected void append(ILoggingEvent event) {
        messages.add(event.getFormattedMessage());
    }

    /**
     * Retrieves and removes the first log message from the queue.
     *
     * @return first log message
     */
    public static String poll() {
        return messages.remove(0);
    }

    /**
     * Returns the number of log messages in the queue.
     *
     * @return the number of log messages
     */
    public static int count() {
        return messages.size();
    }

    /**
     * Clears all log messages from the queue.
     */
    public static void clear() {
        messages.clear();
    }

}
