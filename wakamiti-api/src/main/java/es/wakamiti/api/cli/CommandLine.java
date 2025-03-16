/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.cli;


import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;


public class CommandLine implements Serializable {

    @Serial
    private static final long serialVersionUID = -2546673279708645006L;

    private final List<String> args;
    private final List<Option> options;


    protected CommandLine() {
        this(new LinkedList<>(), new ArrayList<>());
    }

    private CommandLine(
            final List<String> args,
            final List<Option> options
    ) {
        this.args = Objects.requireNonNull(args, "args");
        this.options = Objects.requireNonNull(options, "options");
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Adds left-over unrecognized option/argument.
     *
     * @param arg the unrecognized option/argument.
     */
    public void addArg(
            final String arg
    ) {
        if (arg != null) {
            args.add(arg);
        }
    }

    /**
     * Adds an option to the command line. The values of the option are stored.
     *
     * @param opt the processed option.
     */
    public void addOption(
            final Option opt
    ) {
        if (opt != null) {
            options.add(opt);
        }
    }

    private <T> T get(
            final Supplier<T> supplier
    ) {
        return supplier == null ? null : supplier.get();
    }

    /**
     * Gets any left-over non-recognized options and arguments
     *
     * @return remaining items passed in but not parsed as a {@code List}.
     */
    public List<String> getArgList() {
        return args;
    }

    /**
     * Gets any left-over non-recognized options and arguments
     *
     * @return remaining items passed in but not parsed as an array.
     */
    public String[] getArgs() {
        return args.toArray(Util.EMPTY_STRING_ARRAY);
    }

    /**
     * Gets the map of values associated to the option. This is convenient for options specifying Java properties like
     * <code>-Dparam1=value1
     * -Dparam2=value2</code>. All odd numbered values are property keys
     * and even numbered values are property values.  If there are an odd number of values
     * the last value is assumed to be a boolean flag and the value is "true".
     *
     * @param option name of the option.
     *
     * @return The Properties mapped by the option, never {@code null} even if the option doesn't exists.
     */
    public Properties getOptionProperties(
            final Option option
    ) {
        final Properties props = new Properties();
        for (final Option processedOption : options) {
            if (processedOption.equals(option)) {
                processPropertiesFromValues(props, processedOption.getValuesList());
            }
        }
        return props;
    }

    /**
     * Gets the map of values associated to the option. This is convenient for options specifying Java properties like
     * <code>-Dparam1=value1
     * -Dparam2=value2</code>. The first argument of the option is the key, and the 2nd argument is the value. If the option
     * has only one argument ({@code -Dfoo}) it is considered as a boolean flag and the value is {@code "true"}.
     *
     * @param opt name of the option.
     *
     * @return The Properties mapped by the option, never {@code null} even if the option doesn't exists.
     */
    public Properties getOptionProperties(
            final String opt
    ) {
        final Properties props = new Properties();
        for (final Option option : options) {
            if (opt.equals(option.getOpt()) || opt.equals(option.getLongOpt())) {
                processPropertiesFromValues(props, option.getValuesList());
            }
        }
        return props;
    }

    /**
     * Gets an array of the processed {@link Option}s.
     *
     * @return an array of the processed {@link Option}s.
     */
    public Option[] getOptions() {
        return options.toArray(new Option[0]);
    }

    /**
     * Gets the first argument, if any, of this option.
     *
     * @param opt the character name of the option.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise null.
     */
    public String getOptionValue(
            final char opt
    ) {
        return getOptionValue(String.valueOf(opt));
    }

    /**
     * Gets the argument, if any, of an option.
     *
     * @param opt          character name of the option
     * @param defaultValue is the default value to be returned if the option is not specified.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise {@code defaultValue}.
     */
    public String getOptionValue(
            final char opt,
            final String defaultValue
    ) {
        return getOptionValue(String.valueOf(opt), () -> defaultValue);
    }

    /**
     * Gets the argument, if any, of an option.
     *
     * @param opt          character name of the option
     * @param defaultValue is a supplier for the default value to be returned if the option is not specified.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise {@code defaultValue}.
     */
    public String getOptionValue(
            final char opt,
            final Supplier<String> defaultValue
    ) {
        return getOptionValue(String.valueOf(opt), defaultValue);
    }

    /**
     * Gets the first argument, if any, of this option.
     *
     * @param option the option.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise null.
     */
    public String getOptionValue(
            final Option option
    ) {
        return Optional.ofNullable(getOptionValues(option))
                .filter(v -> v.length > 0)
                .map(v -> v[0])
                .orElse(null);
    }

    /**
     * Gets the first argument, if any, of an option.
     *
     * @param option       the option.
     * @param defaultValue is the default value to be returned if the option is not specified.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise {@code defaultValue}.
     */
    public String getOptionValue(
            final Option option,
            final String defaultValue
    ) {
        return getOptionValue(option, () -> defaultValue);
    }

    /**
     * Gets the first argument, if any, of an option.
     *
     * @param option       the option.
     * @param defaultValue is a supplier for the default value to be returned if the option is not specified.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise {@code defaultValue}.
     */
    public String getOptionValue(
            final Option option,
            final Supplier<String> defaultValue
    ) {
        final String answer = getOptionValue(option);
        return answer != null ? answer : get(defaultValue);
    }


    /**
     * Gets the first argument, if any, of this option.
     *
     * @param opt the name of the option.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise null.
     */
    public String getOptionValue(
            final String opt
    ) {
        return getOptionValue(resolveOption(opt));
    }

    /**
     * Gets the first argument, if any, of an option.
     *
     * @param opt          name of the option.
     * @param defaultValue is the default value to be returned if the option is not specified.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise {@code defaultValue}.
     */
    public String getOptionValue(
            final String opt,
            final String defaultValue
    ) {
        return getOptionValue(resolveOption(opt), () -> defaultValue);
    }

    /**
     * Gets the first argument, if any, of an option.
     *
     * @param opt          name of the option.
     * @param defaultValue is a supplier for the default value to be returned if the option is not specified.
     *
     * @return Value of the argument if option is set, and has an argument, otherwise {@code defaultValue}.
     */
    public String getOptionValue(
            final String opt,
            final Supplier<String> defaultValue
    ) {
        return getOptionValue(resolveOption(opt), defaultValue);
    }


    /**
     * Gets the array of values, if any, of an option.
     *
     * @param opt character name of the option.
     *
     * @return Values of the argument if option is set, and has an argument, otherwise null.
     */
    public String[] getOptionValues(
            final char opt
    ) {
        return getOptionValues(String.valueOf(opt));
    }

    /**
     * Gets the array of values, if any, of an option.
     *
     * @param option the option.
     *
     * @return Values of the argument if option is set, and has an argument, otherwise null.
     */
    public String[] getOptionValues(
            final Option option
    ) {
        if (option == null) {
            return null;
        }
        final List<String> values = new ArrayList<>();
        for (final Option processedOption : options) {
            if (processedOption.equals(option)) {
                values.addAll(processedOption.getValuesList());
            }
        }
        return values.isEmpty() ? null : values.toArray(Util.EMPTY_STRING_ARRAY);
    }

    /**
     * Gets the array of values, if any, of an option.
     *
     * @param opt string name of the option.
     *
     * @return Values of the argument if option is set, and has an argument, otherwise null.
     */
    public String[] getOptionValues(
            final String opt
    ) {
        return getOptionValues(resolveOption(opt));
    }

    /**
     * Gets a version of this {@code Option} converted to a particular type.
     *
     * @param opt the name of the option.
     *
     * @return the value parsed into a particular object.
     */
    public String getParsedOptionValue(
            final char opt
    ) {
        return getParsedOptionValue(String.valueOf(opt));
    }

    /**
     * Gets a version of this {@code Option} converted to a particular type.
     *
     * @param opt          the name of the option.
     * @param defaultValue the default value to return if opt is not set.
     *
     * @return the value parsed into a particular object.
     */
    public String getParsedOptionValue(
            final char opt,
            final Supplier<String> defaultValue
    ) {
        return getParsedOptionValue(String.valueOf(opt), defaultValue);
    }

    /**
     * Gets a version of this {@code Option} converted to a particular type.
     *
     * @param opt          the name of the option.
     * @param defaultValue the default value to return if opt is not set.
     *
     * @return the value parsed into a particular object.
     *
     * @since 1.7.0
     */
    public String getParsedOptionValue(
            final char opt,
            final String defaultValue
    ) {
        return getParsedOptionValue(String.valueOf(opt), defaultValue);
    }

    /**
     * Gets a version of this {@code Option} converted to a particular type.
     *
     * @param option the option.
     *
     * @return the value parsed into a particular object.
     */
    public String getParsedOptionValue(
            final Option option
    ) {
        return getParsedOptionValue(option, () -> null);
    }

    /**
     * Gets a {@code Option} string value.
     *
     * @param option       the option.
     * @param defaultValue the default value to return if opt is not set.
     *
     * @return the value parsed into a particular object.
     */
    public String getParsedOptionValue(
            final Option option,
            final Supplier<String> defaultValue
    ) {
        return Optional.ofNullable(option)
                .map(this::getOptionValue)
                .orElse(get(defaultValue));
    }

    /**
     * Gets a {@code Option} string value.
     *
     * @param option       the option.
     * @param defaultValue the default value to return if opt is not set.
     *
     * @return the value parsed into a particular object.
     */
    public String getParsedOptionValue(
            final Option option,
            final String defaultValue
    ) {
        return getParsedOptionValue(option, () -> defaultValue);
    }

    /**
     * Gets a {@code Option} string value.
     *
     * @param opt the name of the option.
     *
     * @return the value parsed into a particular object.
     */
    public String getParsedOptionValue(
            final String opt
    ) {
        return getParsedOptionValue(resolveOption(opt));
    }

    /**
     * Gets a {@code Option} string value.
     *
     * @param opt          the name of the option.
     * @param defaultValue the default value to return if opt is not set.
     *
     * @return the value parsed into a particular object.
     */
    public String getParsedOptionValue(
            final String opt,
            final Supplier<String> defaultValue
    ) {
        return getParsedOptionValue(resolveOption(opt), defaultValue);
    }

    /**
     * Gets a {@code Option} string value.
     *
     * @param opt          the name of the option.
     * @param defaultValue the default value to return if opt is not set.
     *
     * @return the value parsed into a particular object.
     */
    public String getParsedOptionValue(
            final String opt,
            final String defaultValue
    ) {
        return getParsedOptionValue(resolveOption(opt), defaultValue);
    }

    /**
     * Tests to see if an option has been set.
     *
     * @param opt character name of the option.
     *
     * @return true if set, false if not.
     */
    public boolean hasOption(
            final char opt
    ) {
        return hasOption(String.valueOf(opt));
    }

    /**
     * Tests to see if an option has been set.
     *
     * @param opt the option to check.
     *
     * @return true if set, false if not.
     */
    public boolean hasOption(
            final Option opt
    ) {
        return options.contains(opt);
    }

    /**
     * Tests to see if an option has been set.
     *
     * @param opt Short name of the option.
     *
     * @return true if set, false if not.
     */
    public boolean hasOption(
            final String opt
    ) {
        return hasOption(resolveOption(opt));
    }

    /**
     * Returns an iterator over the Option members of CommandLine.
     *
     * @return an {@code Iterator} over the processed {@link Option} members of this {@link CommandLine}.
     */
    public Iterator<Option> iterator() {
        return options.iterator();
    }

    /**
     * Parses a list of values as properties.  All odd numbered values are property keys
     * and even numbered values are property values.  If there are an odd number of values
     * the last value is assumed to be a boolean with a value of "true".
     *
     * @param props  the properties to update.
     * @param values the list of values to parse.
     */
    private void processPropertiesFromValues(
            final Properties props,
            final List<String> values
    ) {
        for (int i = 0; i < values.size(); i += 2) {
            if (i + 1 < values.size()) {
                props.put(values.get(i), values.get(i + 1));
            } else {
                props.put(values.get(i), "true");
            }
        }
    }

    /**
     * Retrieves the option object given the long or short option as a String
     *
     * @param opt short or long name of the option, may be null.
     *
     * @return Canonicalized option.
     */
    private Option resolveOption(
            final String opt
    ) {
        return Optional.ofNullable(Util.stripLeadingHyphens(opt))
                .stream()
                .flatMap(actual -> options.stream().filter(option ->
                        actual.equals(option.getOpt()) || actual.equals(option.getLongOpt())))
                .findFirst().orElse(null);
    }

    public static final class Builder {

        /**
         * The unrecognized options/arguments
         */
        private final List<String> args = new LinkedList<>();

        /**
         * The processed options
         */
        private final List<Option> options = new ArrayList<>();

        /**
         * Adds left-over unrecognized option/argument.
         *
         * @param arg the unrecognized option/argument.
         *
         * @return this Builder instance for method chaining.
         */
        public Builder addArg(
                final String arg
        ) {
            if (arg != null) {
                args.add(arg);
            }
            return this;
        }

        /**
         * Adds an option to the command line. The values of the option are stored.
         *
         * @param opt the processed option.
         *
         * @return this Builder instance for method chaining.
         */
        public Builder addOption(
                final Option opt
        ) {
            if (opt != null) {
                options.add(opt);
            }
            return this;
        }

        /**
         * Creates the new instance.
         *
         * @return the new instance.
         */
        public CommandLine build() {
            return new CommandLine(args, options);
        }

    }
}
