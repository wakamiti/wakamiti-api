/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.cli;


import java.io.Serial;
import java.io.Serializable;
import java.util.*;


/**
 * Main entry-point into the library.
 * <p>
 * Options represents a collection of {@link Option} objects, which describe the
 * possible options for a command-line.
 * </p>
 * <p>
 * It may flexibly parse long and short options, with or without values.
 * Additionally, it may parse only a portion of a
 * commandline, allowing for flexible multi-stage parsing.
 * </p>
 *
 * @see CommandLine
 */
public class Options implements Serializable {

    @Serial
    private static final long serialVersionUID = 2957032618746037419L;

    /**
     * A map of the options with the character key
     */
    private final Map<String, Option> shortOpts = new LinkedHashMap<>();

    /**
     * A map of the options with the long key
     */
    private final Map<String, Option> longOpts = new LinkedHashMap<>();

    /**
     * Adds an option instance
     *
     * @param opt the option that is to be added
     *
     * @return the resulting Options instance
     */
    public Options addOption(
            final Option opt
    ) {
        final String key = opt.getKey();
        if (opt.hasLongOpt()) {
            longOpts.put(opt.getLongOpt(), opt);
        }
        shortOpts.put(key, opt);
        return this;
    }

    /**
     * Adds an option that only contains a short-name.
     * <p>
     * It may be specified as requiring an argument.
     * </p>
     *
     * @param opt         Short single-character name of the option.
     * @param hasArg      flag signalling if an argument is required after this
     *                    option
     * @param description Self-documenting description
     *
     * @return the resulting Options instance
     */
    public Options addOption(
            final String opt,
            final boolean hasArg,
            final String description
    ) {
        addOption(opt, null, hasArg, description);
        return this;
    }

    /**
     * Adds an option that only contains a short name.
     * <p>
     * The option does not take an argument.
     * </p>
     *
     * @param opt         Short single-character name of the option.
     * @param description Self-documenting description
     *
     * @return the resulting Options instance
     */
    public Options addOption(
            final String opt,
            final String description
    ) {
        addOption(opt, null, false, description);
        return this;
    }

    /**
     * Adds an option that contains a short-name and a long-name.
     * <p>
     * It may be specified as requiring an argument.
     * </p>
     *
     * @param opt         Short single-character name of the option.
     * @param longOpt     Long multi-character name of the option.
     * @param hasArg      flag signalling if an argument is required after this
     *                    option
     * @param description Self-documenting description
     *
     * @return the resulting Options instance
     */
    public Options addOption(
            final String opt,
            final String longOpt,
            final boolean hasArg,
            final String description
    ) {
        addOption(new Option(opt, longOpt, hasArg, description));
        return this;
    }


    public Options addOption(
            String option,
            String argName,
            String description,
            int argCount,
            char valueSeparator
    ) {
        addOption(new Option(option, argName, description, argCount, valueSeparator));
        return this;
    }



    public Options addOptions(
            final Options options
    ) {
        for (final Option opt : options.getOptions()) {
            if (hasOption(opt.getKey())) {
                throw new IllegalArgumentException("Duplicate key: " + opt.getKey());
            }
            addOption(opt);
        }
        return this;
    }

    /**
     * Gets the options with a long name starting with the name specified.
     *
     * @param opt the partial name of the option
     *
     * @return the options matching the partial name specified, or an empty list
     * if none matches
     */
    public List<String> getMatchingOptions(
            final String opt
    ) {
        final String clean = Util.stripLeadingHyphens(opt);
        final List<String> matchingOpts = new ArrayList<>();
        // for a perfect match return the single option only
        if (longOpts.containsKey(clean)) {
            return Collections.singletonList(clean);
        }
        for (final String longOpt : longOpts.keySet()) {
            if (longOpt.startsWith(clean)) {
                matchingOpts.add(longOpt);
            }
        }
        return matchingOpts;
    }

    /**
     * Gets the {@link Option} matching the long or short name specified.
     * <p>
     * The leading hyphens in the name are ignored (up to 2).
     * </p>
     *
     * @param opt short or long name of the {@link Option}
     *
     * @return the option represented by opt
     */
    public Option getOption(
            final String opt
    ) {
        final String clean = Util.stripLeadingHyphens(opt);
        final Option option = shortOpts.get(clean);
        return option != null ? option : longOpts.get(clean);
    }

    /**
     * Gets a read-only list of options in this set
     *
     * @return read-only Collection of {@link Option} objects in this descriptor
     */
    public Collection<Option> getOptions() {
        return Collections.unmodifiableCollection(helpOptions());
    }

    /**
     * Tests whether the named {@link Option} is a member of this {@link Options}.
     *
     * @param opt long name of the {@link Option}
     *
     * @return true if the named {@link Option} is a member of this {@link Options}
     */
    public boolean hasLongOption(
            final String opt
    ) {
        return longOpts.containsKey(Util.stripLeadingHyphens(opt));
    }

    /**
     * Tests whether the named {@link Option} is a member of this {@link Options}.
     *
     * @param opt short or long name of the {@link Option}
     *
     * @return true if the named {@link Option} is a member of this {@link Options}
     */
    public boolean hasOption(
            final String opt
    ) {
        final String clean = Util.stripLeadingHyphens(opt);
        return shortOpts.containsKey(clean) || longOpts.containsKey(clean);
    }

    /**
     * Tests whether the named {@link Option} is a member of this {@link Options}.
     *
     * @param opt short name of the {@link Option}
     *
     * @return true if the named {@link Option} is a member of this {@link Options}
     */
    public boolean hasShortOption(
            final String opt
    ) {
        final String clean = Util.stripLeadingHyphens(opt);
        return shortOpts.containsKey(clean);
    }

    /**
     * Returns the Options for use by the HelpFormatter.
     *
     * @return the List of Options
     */
    public List<Option> helpOptions() {
        return new LinkedList<>(shortOpts.values());
    }

    @Override
    public String toString() {
        return "[ Options: [ short %s ] [ long %s ]".formatted(shortOpts, longOpts);
    }
}
