/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.cli;


import es.wakamiti.api.cli.internal.OptionValidator;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static es.wakamiti.api.cli.Util.*;


/**
 * Describes a single command-line option. It maintains information regarding
 * the short-name of the option, the long-name, if any exists, and a
 * self-documenting description of the option.
 * <p>
 * An Option is not created independently, but is created through an instance of
 * {@link Options}. An Option is required to have at least a short or a long-name.
 * </p>
 *
 * @see Options
 * @see CommandLine
 */
public class Option implements Cloneable, Serializable {

    /**
     * Specifies the number of argument values has not been specified.
     */
    public static final int UNINITIALIZED = -1;
    /**
     * Specifies the number of argument values is infinite.
     */
    public static final int UNLIMITED_VALUES = -2;

    @Serial
    private static final long serialVersionUID = 7289029311259128601L;


    private final String option;
    private String longOption;
    private String argName;
    private String description;
    private boolean optionalArg;
    private int argCount = UNINITIALIZED;
    private List<String> values = new LinkedList<>();
    private char valueSeparator;


    /**
     * Private constructor used by the nested Builder class.
     *
     * @param builder builder used to create this option.
     */
    private Option(
            final Builder builder
    ) {
        this.argName = builder.argName;
        this.description = builder.description;
        this.longOption = builder.longOption;
        this.argCount = builder.argCount;
        this.option = builder.option;
        this.optionalArg = builder.optionalArg;
        this.valueSeparator = builder.valueSeparator;
    }

    /**
     * Creates an Option using the specified parameters.
     *
     * @param option      short representation of the option.
     * @param hasArg      specifies whether the Option takes an argument or not.
     * @param description describes the function of the option.
     *
     * @throws IllegalArgumentException if there are any non valid Option characters in {@code opt}.
     */
    public Option(
            final String option,
            final boolean hasArg,
            final String description
    ) throws IllegalArgumentException {
        this(option, null, hasArg, description);
    }

    /**
     * Creates an Option using the specified parameters. The option does not take an argument.
     *
     * @param option      short representation of the option.
     * @param description describes the function of the option.
     *
     * @throws IllegalArgumentException if there are any non valid Option characters in {@code opt}.
     */
    public Option(
            final String option,
            final String description
    ) throws IllegalArgumentException {
        this(option, null, false, description);
    }

    /**
     * Creates an Option using the specified parameters.
     *
     * @param option      short representation of the option.
     * @param longOption  the long representation of the option.
     * @param hasArg      specifies whether the Option takes an argument or not.
     * @param description describes the function of the option.
     *
     * @throws IllegalArgumentException if there are any non valid Option characters in {@code opt}.
     */
    public Option(
            final String option,
            final String longOption,
            final boolean hasArg,
            final String description
    ) throws IllegalArgumentException {
        this.option = OptionValidator.validate(option);
        this.longOption = longOption;
        if (hasArg) {
            this.argCount = 1;
        }
        this.description = description;
    }

    public Option(
            String option,
            String argName,
            String description,
            int argCount,
            char valueSeparator
    ) {
        this.option = OptionValidator.validate(option);
        this.argName = argName;
        this.description = description;
        this.argCount = argCount;
        this.valueSeparator = valueSeparator;
    }

    /**
     * Returns a {@link Builder} to create an {@link Option} using descriptive methods.
     *
     * @return a new {@link Builder} instance.
     *
     * @since 1.3
     */
    public static Builder builder() {
        return builder(null);
    }

    /**
     * Returns a {@link Builder} to create an {@link Option} using descriptive methods.
     *
     * @param option short representation of the option.
     *
     * @return a new {@link Builder} instance.
     *
     * @throws IllegalArgumentException if there are any non valid Option characters in {@code opt}.
     * @since 1.3
     */
    public static Builder builder(
            final String option
    ) {
        return new Builder(option);
    }

    /**
     * Tests whether the option can accept more arguments.
     *
     * @return false if the maximum number of arguments is reached.
     *
     * @since 1.3
     */
    public boolean acceptsArg() {
        return (hasArg() || hasArgs() || hasOptionalArg()) && (argCount <= 0 || values.size() < argCount);
    }

    /**
     * Adds the value to this Option. If the number of arguments is greater than zero and there is enough space in the list then add the value. Otherwise, throw
     * a runtime exception.
     *
     * @param value The value to be added to this Option.
     */
    private void add(
            final String value
    ) {
        if (!acceptsArg()) {
            throw new IllegalArgumentException("Cannot add value, list full.");
        }
        // store value
        values.add(value);
    }

    /**
     * A rather odd clone method - due to incorrect code in 1.0 it is public and in 1.1 rather than throwing a CloneNotSupportedException it throws a
     * RuntimeException so as to maintain backwards compatible at the API level.
     * <p>
     * After calling this method, it is very likely you will want to call clearValues().
     *
     * @return a clone of this Option instance.
     *
     * @throws RuntimeException if a {@link CloneNotSupportedException} has been thrown by {@code super.clone()}.
     */
    @Override
    public Object clone() {
        try {
            final Option option = (Option) super.clone();
            option.values = new LinkedList<>(values);
            return option;
        } catch (final CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(
            final Object obj
    ) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Option other)) {
            return false;
        }
        return Objects.equals(longOption, other.longOption) && Objects.equals(option, other.option);
    }

    /**
     * Gets the display name for the argument value.
     *
     * @return the display name for the argument value.
     */
    public String getArgName() {
        return Optional.ofNullable(argName)
                .or(() -> Optional.ofNullable(longOption)
                        .map(Util::unCamelCase)
                        .map(name -> name.replaceAll("[-\\s]+", "_"))
                )
                .map(String::toUpperCase)
                .filter(name -> name.matches("\\w+"))
                .orElse(null);
    }

    /**
     * Sets the display name for the argument value.
     *
     * @param argName the display name for the argument value.
     */
    public void setArgName(
            final String argName
    ) {
        this.argName = argName;
    }

    /**
     * Gets the number of argument values this Option can take.
     *
     * <p>
     * A value equal to the constant {@link #UNINITIALIZED} (= -1) indicates the number of arguments has not been specified. A value equal to the constant
     * {@link #UNLIMITED_VALUES} (= -2) indicates that this options takes an unlimited amount of values.
     * </p>
     *
     * @return num the number of argument values.
     *
     * @see #UNINITIALIZED
     * @see #UNLIMITED_VALUES
     */
    public int getArgs() {
        return argCount;
    }

    /**
     * Sets the number of argument values this Option can take.
     *
     * @param num the number of argument values.
     */
    public void setArgs(
            final int num
    ) {
        this.argCount = num;
    }

    /**
     * Gets the self-documenting description of this Option.
     *
     * @return The string description of this option.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the self-documenting description of this Option.
     *
     * @param description The description of this option.
     *
     * @since 1.1
     */
    public void setDescription(
            final String description
    ) {
        this.description = description;
    }

    /**
     * Gets the id of this Option. This is only set when the Option shortOpt is a single character. This is used for switch statements.
     *
     * @return the id of this Option.
     */
    public int getId() {
        return getKey().charAt(0);
    }

    /**
     * Gets the 'unique' Option identifier. This is the option value if set or the long value if the options value is not set.
     *
     * @return the 'unique' Option identifier.
     */
    public String getKey() {
        return option == null ? longOption : option;
    }

    /**
     * Gets the long name of this Option.
     *
     * @return Long name of this option, or null, if there is no long name.
     */
    public String getLongOpt() {
        return longOption;
    }

    /**
     * Sets the long name of this Option.
     *
     * @param longOpt the long name of this Option.
     */
    public void setLongOpt(
            final String longOpt
    ) {
        this.longOption = longOpt;
    }

    /**
     * Gets the name of this Option.
     * <p>
     * It is this String which can be used with {@link CommandLine#hasOption(String opt)} and {@link CommandLine#getOptionValue(String opt)} to check for
     * existence and argument.
     *
     * @return The name of this option.
     */
    public String getOpt() {
        return option;
    }

    /**
     * Gets the specified value of this Option or {@code null} if there is no value.
     *
     * @return the value/first value of this Option or {@code null} if there is no value.
     */
    public String getValue() {
        return hasNoValues() ? null : values.get(0);
    }

    /**
     * Gets the specified value of this Option or {@code null} if there is no value.
     *
     * @param index The index of the value to be returned.
     *
     * @return the specified value of this Option or {@code null} if there is no value.
     *
     * @throws IndexOutOfBoundsException if index is less than 1 or greater than the number of the values for this Option.
     */
    public String getValue(
            final int index
    ) throws IndexOutOfBoundsException {
        return hasNoValues() ? null : values.get(index);
    }

    /**
     * Gets the value/first value of this Option or the {@code defaultValue} if there is no value.
     *
     * @param defaultValue The value to be returned if there is no value.
     *
     * @return the value/first value of this Option or the {@code defaultValue} if there are no values.
     */
    public String getValue(
            final String defaultValue
    ) {
        final String value = getValue();
        return value != null ? value : defaultValue;
    }

    /**
     * Gets the values of this Option as a String array or null if there are no values.
     *
     * @return the values of this Option as a String array or null if there are no values.
     */
    public String[] getValues() {
        return hasNoValues() ? null : values.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Gets the value separator character.
     *
     * @return the value separator character.
     */
    public char getValueSeparator() {
        return valueSeparator;
    }

    /**
     * Sets the value separator. For example if the argument value was a Java property, the value separator would be '='.
     *
     * @param valueSeparator The value separator.
     */
    public void setValueSeparator(
            final char valueSeparator
    ) {
        this.valueSeparator = valueSeparator;
    }

    /**
     * Gets the values of this Option as a List or null if there are no values.
     *
     * @return the values of this Option as a List or null if there are no values.
     */
    public List<String> getValuesList() {
        return values;
    }

    /**
     * Tests whether this Option requires an argument.
     *
     * @return boolean flag indicating if an argument is required.
     */
    public boolean hasArg() {
        return argCount > 0 || argCount == UNLIMITED_VALUES;
    }

    /**
     * Tests whether the display name for the argument value has been set.
     *
     * @return if the display name for the argument value has been set.
     */
    public boolean hasArgName() {
        return argName != null && !argName.isEmpty();
    }

    /**
     * Tests whether this Option can take many values.
     *
     * @return boolean flag indicating if multiple values are allowed.
     */
    public boolean hasArgs() {
        return argCount > 1 || argCount == UNLIMITED_VALUES;
    }

    @Override
    public int hashCode() {
        return Objects.hash(longOption, option);
    }

    /**
     * Tests whether this Option has a long name.
     *
     * @return boolean flag indicating existence of a long name.
     */
    public boolean hasLongOpt() {
        return longOption != null;
    }

    /**
     * Tests whether this Option has any values.
     *
     * @return whether this Option has any values.
     */
    private boolean hasNoValues() {
        return values.isEmpty();
    }

    /**
     * Tests whether this Option can have an optional argument.
     *
     * @return whether this Option can have an optional argument.
     */
    public boolean hasOptionalArg() {
        return optionalArg;
    }

    /**
     * Tests whether this Option has specified a value separator.
     *
     * @return whether this Option has specified a value separator.
     */
    public boolean hasValueSeparator() {
        return valueSeparator > 0;
    }

    /**
     * Processes the value. If this Option has a value separator the value will have to be parsed into individual tokens. When n-1 tokens have been processed
     * and there are more value separators in the value, parsing is ceased and the remaining characters are added as a single token.
     *
     * @param value The String to be processed.
     */
    public void processValue(
            final String value
    ) {
        if (argCount == UNINITIALIZED) {
            throw new IllegalArgumentException("NO_ARGS_ALLOWED");
        }
        if (!hasValueSeparator()) {
            add(value);
            return;
        }
        char sep = getValueSeparator();
        Stream.of(value.split(String.valueOf(sep), argCount))
                .forEach(this::add);
    }

    /**
     * Tests whether the option requires more arguments to be valid.
     *
     * @return false if the option doesn't require more arguments.
     */
    public boolean requiresArg() {
        if (optionalArg) {
            return false;
        }
        if (argCount == UNLIMITED_VALUES) {
            return values.isEmpty();
        }
        return acceptsArg();
    }

    /**
     * Sets whether this Option can have an optional argument.
     *
     * @param optionalArg specifies whether the Option can have an optional argument.
     */
    public void setOptionalArg(
            final boolean optionalArg
    ) {
        this.optionalArg = optionalArg;
    }

    /**
     * Creates a String suitable for debugging.
     *
     * @return a String suitable for debugging.
     */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder().append("[ ");
        buf.append("Option ");
        buf.append(option);
        if (longOption != null) {
            buf.append(SP).append(longOption);
        }
        if (hasArgs()) {
            buf.append("[ARG...]");
        } else if (hasArg()) {
            buf.append(" [ARG]");
        }
        return buf.append(" :: ")
                .append(description)
                .append(" ]")
                .toString();
    }

    /**
     * Builds {@code Option} instances using descriptive methods.
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>
     * Option option = Option.builder("a").required(true).longOpt("arg-name").build();
     * </pre>
     */
    public static final class Builder {

        /**
         * The number of argument values this option can have.
         */
        private int argCount = UNINITIALIZED;

        /**
         * The name of the argument for this option.
         */
        private String argName;

        /**
         * Description of the option.
         */
        private String description;

        /**
         * The long representation of the option.
         */
        private String longOption;

        /**
         * The name of the option.
         */
        private String option;

        /**
         * Specifies whether the argument value of this Option is optional.
         */
        private boolean optionalArg;

        /**
         * The character that is the value separator.
         */
        private char valueSeparator;

        /**
         * Constructs a new {@code Builder} with the minimum required parameters for an {@code Option} instance.
         *
         * @param option short representation of the option.
         *
         * @throws IllegalArgumentException if there are any non valid Option characters in {@code opt}.
         */
        private Builder(
                final String option
        ) throws IllegalArgumentException {
            option(option);
        }

        /**
         * Sets the display name for the argument value.
         *
         * @param argName the display name for the argument value.
         *
         * @return this builder, to allow method chaining.
         */
        public Builder argName(
                final String argName
        ) {
            this.argName = argName;
            return this;
        }

        /**
         * Constructs an Option with the values declared by this {@link Builder}.
         *
         * @return the new {@link Option}.
         *
         * @throws IllegalArgumentException if neither {@code opt} or {@code longOpt} has been set.
         */
        public Option build() {
            if (option == null && longOption == null) {
                throw new IllegalArgumentException("Either opt or longOpt must be specified");
            }
            return new Option(this);
        }

        /**
         * Sets the description for this option.
         *
         * @param description the description of the option.
         *
         * @return this builder, to allow method chaining.
         */
        public Builder description(
                final String description
        ) {
            this.description = description;
            return this;
        }

        /**
         * Tests whether the Option will require an argument.
         *
         * @return this builder, to allow method chaining.
         */
        public Builder hasArg() {
            return hasArg(true);
        }

        /**
         * Tests whether the Option has an argument or not.
         *
         * @param hasArg specifies whether the Option takes an argument or not.
         *
         * @return this builder, to allow method chaining.
         */
        public Builder hasArg(
                final boolean hasArg
        ) {
            argCount = hasArg ? 1 : UNINITIALIZED;
            return this;
        }

        /**
         * Tests whether the Option can have unlimited argument values.
         *
         * @return this builder.
         */
        public Builder hasArgs() {
            argCount = UNLIMITED_VALUES;
            return this;
        }

        /**
         * Sets the long name of the Option.
         *
         * @param longOption the long name of the Option
         *
         * @return this builder.
         */
        public Builder longOpt(
                final String longOption
        ) {
            this.longOption = longOption;
            return this;
        }

        /**
         * Sets the number of argument values the Option can take.
         *
         * @param argCount the number of argument values
         *
         * @return this builder.
         */
        public Builder numberOfArgs(
                final int argCount
        ) {
            this.argCount = argCount;
            return this;
        }

        /**
         * Sets the name of the Option.
         *
         * @param option the name of the Option.
         *
         * @return this builder.
         *
         * @throws IllegalArgumentException if there are any non valid Option characters in {@code opt}.
         */
        public Builder option(
                final String option
        ) throws IllegalArgumentException {
            this.option = OptionValidator.validate(option);
            return this;
        }

        /**
         * Sets whether the Option can have an optional argument.
         *
         * @param optionalArg specifies whether the Option can have an optional argument.
         *
         * @return this builder.
         */
        public Builder optionalArg(
                final boolean optionalArg
        ) {
            if (optionalArg && argCount == UNINITIALIZED) {
                argCount = 1;
            }
            this.optionalArg = optionalArg;
            return this;
        }

        /**
         * The Option will use '=' as a means to separate argument value.
         *
         * @return this builder.
         */
        public Builder valueSeparator() {
            return valueSeparator(EQUAL);
        }

        /**
         * The Option will use {@code sep} as a means to separate argument values.
         * <p>
         * <b>Example:</b>
         * </p>
         *
         * <pre>
         * Option opt = Option.builder("D").hasArgs().valueSeparator('=').build();
         * Options options = new Options();
         * options.addOption(opt);
         * String[] args = { "-Dkey=value" };
         * CommandLineParser parser = new DefaultParser();
         * CommandLine line = parser.parse(options, args);
         * String propertyName = line.getOptionValues("D")[0]; // will be "key"
         * String propertyValue = line.getOptionValues("D")[1]; // will be "value"
         * </pre>
         *
         * @param valueSeparator The value separator.
         *
         * @return this builder.
         */
        public Builder valueSeparator(
                final char valueSeparator
        ) {
            this.valueSeparator = valueSeparator;
            return this;
        }

    }
}
