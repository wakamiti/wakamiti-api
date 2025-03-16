/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.cli;


import java.util.Objects;

/**
 * Contains useful helper methods for classes within this package.
 */
public final class Util {

    /**
     * An empty immutable {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = {};

    public static final char APOS = '\'';
    public static final char CR = '\r';
    public static final char EQUAL = '=';
    public static final char LF = '\n';
    public static final char SP = ' ';
    public static final char TAB = '\t';

    /**
     * Removes the leading and trailing quotes from {@code str}. E.g. if str is '"one two"', then 'one two' is returned.
     *
     * @param str The string from which the leading and trailing quotes should be removed.
     * @return The string without the leading and trailing quotes.
     */
    public static String stripLeadingAndTrailingQuotes(final String str) {
        if (Objects.isNull(str) || str.isEmpty()) {
            return str;
        }
        final int length = str.length();
        if (length > 1 && str.startsWith("\"") && str.endsWith("\"") && str.substring(1, length - 1).indexOf('"') == -1) {
            return str.substring(1, length - 1);
        }
        return str;
    }

    /**
     * Removes the hyphens from the beginning of {@code str} and return the new String.
     *
     * @param str The string from which the hyphens should be removed.
     * @return the new String.
     */
    public static String stripLeadingHyphens(final String str) {
        if (Objects.isNull(str) || str.isEmpty()) {
            return str;
        }
        if (str.startsWith("--")) {
            return str.substring(2);
        }
        if (str.startsWith("-")) {
            return str.substring(1);
        }
        return str;
    }

    public static String unCamelCase(
            String str
    ) {
        String[] words = str.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        return String.join(" ", words);
    }

    private Util() {
        // no instances
    }
}
