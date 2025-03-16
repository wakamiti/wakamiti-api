/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.cli.internal;


import es.wakamiti.api.cli.CommandLine;
import es.wakamiti.api.cli.Option;
import es.wakamiti.api.cli.Options;
import es.wakamiti.api.cli.Util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static es.wakamiti.api.cli.Util.EQUAL;


public class CommandLineParser {

    protected CommandLine cmd;
    protected Options options;
    protected String currentToken;
    protected Option currentOption;
    protected boolean skipParsing;
    protected Set<Object> expectedOpts;

    private int indexOfEqual(final String token) {
        return token.indexOf(EQUAL);
    }

    /**
     * Gets a list of matching option strings for the given token, depending on
     * the selected partial matching policy.
     *
     * @param token the token (may contain leading dashes)
     *
     * @return the list of matching option strings or an empty list if no
     * matching option could be found
     */
    private List<String> getMatchingLongOptions(final String token) {
        return options.getMatchingOptions(token);
    }

    /**
     * Searches for a prefix that is the long name of an option (-Xmx512m)
     *
     * @param token
     */
    private String getLongPrefix(final String token) {
        final String t = Util.stripLeadingHyphens(token);
        for (int i = t.length() - 2; i > 1; i--) {
            final String prefix = t.substring(0, i);
            if (options.hasLongOption(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    /**
     * Tests if the token looks like a long option.
     *
     * @param token
     */
    private boolean isLongOption(final String token) {
        if (token == null || !token.startsWith("-") || token.length() == 1) {
            return false;
        }
        final int pos = indexOfEqual(token);
        final String t = pos == -1 ? token : token.substring(0, pos);
        if (!getMatchingLongOptions(t).isEmpty()) {
            // long or partial long options (--L, -L, --L=V, -L=V, --l, --l=V)
            return true;
        }
        // -LV
        return getLongPrefix(token) != null && !token.startsWith("--");
    }

    /**
     * Tests if the token looks like a short option.
     *
     * @param token
     */
    private boolean isShortOption(final String token) {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (token == null || !token.startsWith("-") || token.length() == 1) {
            return false;
        }
        // remove leading "-" and "=value"
        final int pos = indexOfEqual(token);
        final String optName = pos == -1 ? token.substring(1) : token.substring(1, pos);
        if (options.hasShortOption(optName)) {
            return true;
        }
        // check for several concatenated short options
        return !optName.isEmpty() && options.hasShortOption(String.valueOf(optName.charAt(0)));
    }

    /**
     * Tests if the token is a negative number.
     *
     * @param token
     */
    private boolean isNegativeNumber(final String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    /**
     * Tests if the token looks like an option.
     *
     * @param token
     */
    private boolean isOption(final String token) {
        return isLongOption(token) || isShortOption(token);
    }

    /**
     * Tests if the token is a valid argument.
     *
     * @param token
     */
    private boolean isArgument(final String token) {
        return !isOption(token) || isNegativeNumber(token);
    }

    /**
     * Tests if the specified token is a Java-like property (-Dkey=value).
     */
    private boolean isJavaProperty(final String token) {
        final String opt = token.isEmpty() ? null : token.substring(0, 1);
        final Option option = options.getOption(opt);
        return option != null && (option.getArgs() >= 2 || option.getArgs() == Option.UNLIMITED_VALUES);
    }

    /**
     * Handles an unknown token. If the token starts with a dash, an
     * {@link IllegalArgumentException} is thrown. Otherwise, the token is added
     * to the arguments of the command line. If the stopAtNonOption flag is set,
     * this stops the parsing, and the remaining tokens are added as-is in the
     * arguments of the command line.
     *
     * @param token the command line token to handle
     */
    private void handleUnknownToken(final String token) {
        if (token.startsWith("-") && token.length() > 1) {
            throw new IllegalArgumentException("Unrecognized option: %s".formatted(token));
        }
        cmd.addArg(token);
    }

    private void handleAmbiguousException(String token, List<String> matches) {
        String msg = """
                Ambiguous option: '%s'
                could be: %s
                """
                .formatted(token, matches.stream()
                        .map("'%s'"::formatted).collect(Collectors.joining(", ")));
        throw new IllegalArgumentException(msg);
    }

    /**
     * Handles the following tokens:
     * <p>
     * --L -L --l -l
     *
     * @param token the command line token to handle
     */
    private void handleLongOptionWithoutEqual(final String token) {
        final List<String> matchingOpts = getMatchingLongOptions(token);
        if (matchingOpts.isEmpty()) {
            handleUnknownToken(currentToken);
        } else if (matchingOpts.size() > 1 && !options.hasLongOption(token)) {
            handleAmbiguousException(token, matchingOpts);
        } else {
            final String key = options.hasLongOption(token) ? token : matchingOpts.get(0);
            handleOption(options.getOption(key));
        }
    }

    /**
     * Handles the following tokens:
     * <p>
     * --L=V -L=V --l=V -l=V
     *
     * @param token the command line token to handle
     */
    private void handleLongOptionWithEqual(final String token) {
        final int pos = indexOfEqual(token);
        final String value = token.substring(pos + 1);
        final String opt = token.substring(0, pos);
        final List<String> matchingOpts = getMatchingLongOptions(opt);
        if (matchingOpts.isEmpty()) {
            handleUnknownToken(currentToken);
        } else if (matchingOpts.size() > 1 && !options.hasLongOption(opt)) {
            handleAmbiguousException(opt, matchingOpts);
        } else {
            final String key = options.hasLongOption(opt) ? opt : matchingOpts.get(0);
            final Option option = options.getOption(key);
            if (option.acceptsArg()) {
                handleOption(option);
                currentOption.processValue(value);
                currentOption = null;
            } else {
                handleUnknownToken(currentToken);
            }
        }
    }

    /**
     * Handles the following tokens:
     * <p>
     * -S -SV -S V -S=V -S1S2 -S1S2 V -SV1=V2
     * <p>
     * -L -LV -L V -L=V -l
     *
     * @param hyphenToken the command line token to handle
     */
    private void handleShortAndLongOption(final String hyphenToken) {
        final String token = Util.stripLeadingHyphens(hyphenToken);
        final int pos = indexOfEqual(token);
        if (token.length() == 1) {
            // -S
            if (options.hasShortOption(token)) {
                handleOption(options.getOption(token));
            } else {
                handleUnknownToken(hyphenToken);
            }
        } else if (pos == -1) {
            // no equal sign found (-xxx)
            if (options.hasShortOption(token)) {
                handleOption(options.getOption(token));
            } else if (!getMatchingLongOptions(token).isEmpty()) {
                // -L or -l
                handleLongOptionWithoutEqual(hyphenToken);
            } else {
                // look for a long prefix (-Xmx512m)
                final String opt = getLongPrefix(token);

                if (opt != null && options.getOption(opt).acceptsArg()) {
                    handleOption(options.getOption(opt));
                    currentOption.processValue(token.substring(opt.length()));
                    currentOption = null;
                } else if (isJavaProperty(token)) {
                    // -SV1 (-Dflag)
                    handleOption(options.getOption(token.substring(0, 1)));
                    currentOption.processValue(token.substring(1));
                    currentOption = null;
                } else {
                    // -S1S2S3 or -S1S2V
                    handleConcatenatedOptions(hyphenToken);
                }
            }
        } else {
            // equal sign found (-xxx=yyy)
            final String opt = token.substring(0, pos);
            final String value = token.substring(pos + 1);

            if (opt.length() == 1) {
                // -S=V
                final Option option = options.getOption(opt);
                if (option != null && option.acceptsArg()) {
                    handleOption(option);
                    currentOption.processValue(value);
                    currentOption = null;
                } else {
                    handleUnknownToken(hyphenToken);
                }
            } else if (isJavaProperty(opt)) {
                // -SV1=V2 (-Dkey=value)
                handleOption(options.getOption(opt.substring(0, 1)));
                currentOption.processValue(opt.substring(1));
                currentOption.processValue(value);
                currentOption = null;
            } else {
                // -L=V or -l=V
                handleLongOptionWithEqual(hyphenToken);
            }
        }
    }

    /**
     * Breaks {@code token} into its constituent parts using the following
     * algorithm.
     *
     * <ul>
     * <li>ignore the first character ({@code -})</li>
     *
     * <li>for each remaining character check if an {@link Option} exists with
     * that id.</li>
     *
     * <li>if an {@link Option} does exist then add that character prepended
     * with {@code -} to the list of processed tokens.</li>
     *
     * <li>if the {@link Option} can have an argument value and there are
     * remaining characters in the token then add the remaining characters as a
     * token to the list of processed tokens.</li>
     *
     * <li>if an {@link Option} does <b>NOT</b> exist <b>AND</b>
     * {@code stopAtNonOption} <b>IS</b> set then add the special token
     * {@code --} followed by the remaining characters and also the remaining
     * tokens directly to the processed tokens list.</li>
     *
     * <li>if an {@link Option} does <b>NOT</b> exist <b>AND</b>
     * {@code stopAtNonOption} <b>IS NOT</b> set then add that character
     * prepended with {@code -}.</li>
     * </ul>
     *
     * @param token The current token to be <b>burst</b> at the first non-Option
     *              encountered.
     */
    protected void handleConcatenatedOptions(final String token) {
        for (int i = 1; i < token.length(); i++) {
            final String ch = String.valueOf(token.charAt(i));
            if (!options.hasOption(ch)) {
                handleUnknownToken(token);
                break;
            }
            handleOption(options.getOption(ch));
            if (currentOption != null && token.length() != i + 1) {
                // add the trail as an argument of the option
                currentOption.processValue(token.substring(i + 1));
                break;
            }
        }
    }

    /**
     * Handles the following tokens:
     * <p>
     * --L --L=V --L V --l
     *
     * @param token the command line token to handle
     */
    private void handleLongOption(final String token) {
        if (indexOfEqual(token) == -1) {
            handleLongOptionWithoutEqual(token);
        } else {
            handleLongOptionWithEqual(token);
        }
    }

    private void handleOption(final Option option) {
        // check the previous option before handling the next one
        checkRequiredArgs();
        final Option copy = (Option) option.clone();
        cmd.addOption(copy);
        currentOption = copy.hasArg() ? copy : null;
    }

    private void checkRequiredArgs() {
        if (currentOption != null && currentOption.requiresArg()) {
            if (isJavaProperty(currentOption.getKey()) && currentOption.getValuesList().size() == 1) {
                return;
            }
            throw new NullPointerException("Missing argument for option: %s".formatted(currentOption.getKey()));
        }
    }

    /**
     * Handles any command line token.
     *
     * @param token the command line token to handle
     */
    private void handleToken(final String token) {
        if (token != null) {
            currentToken = token;
            if (skipParsing) {
                cmd.addArg(token);
            } else if ("--".equals(token)) {
                skipParsing = true;
            } else if (currentOption != null && currentOption.acceptsArg() && isArgument(token)) {
                currentOption.processValue(Util.stripLeadingAndTrailingQuotes(token));
            } else if (token.startsWith("--")) {
                handleLongOption(token);
            } else if (token.startsWith("-") && !"-".equals(token)) {
                handleShortAndLongOption(token);
            } else {
                handleUnknownToken(token);
            }
            if (currentOption != null && !currentOption.acceptsArg()) {
                currentOption = null;
            }
        }
    }

    public CommandLine parse(
            final Options options,
            final String[] arguments
    ) {
        this.options = options;
        skipParsing = false;
        currentOption = null;
        cmd = CommandLine.builder().build();
        if (arguments != null) {
            for (final String argument : arguments) {
                handleToken(argument);
            }
        }
        checkRequiredArgs();
        return cmd;
    }

}
