/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.config;


import java.util.Objects;
import java.util.Optional;


public class PropertyDefinition {


    private final String property;
    private final String description;
    private final boolean required;
    private final boolean multiValue;
    private final String defaultValue;
    private final PropertyType propertyType;


    PropertyDefinition(
            String property,
            String description,
            boolean required,
            boolean multiValue,
            String defaultValue,
            PropertyType propertyType
    ) {
        this.property = Objects.requireNonNull(property, "property");
        this.description = (description == null ? "" : description);
        this.required = required;
        this.multiValue = multiValue;
        this.defaultValue = defaultValue;
        this.propertyType = propertyType;
    }

    public static PropertyDefinitionBuilder builder() {
        return new PropertyDefinitionBuilder();
    }

    public static PropertyDefinitionBuilder builder(
            String property
    ) {
        return new PropertyDefinitionBuilder().property(property);
    }

    public String property() {
        return property;
    }

    public String description() {
        return description;
    }

    public boolean required() {
        return required;
    }

    public boolean multiValue() {
        return multiValue;
    }

    public Optional<String> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public String type() {
        return propertyType.name();
    }


    public String hint() {
        return String.format(
                "%s%s%s",
                propertyType.hint(),
                defaultValue != null ? " [default: " + defaultValue + "]" : "",
                required ? " (required)" : ""
        );
    }


    public Optional<String> validate(
            String value
    ) {
        if (value == null || value.isBlank()) {
            if (required) {
                return Optional.of("Property is required but not present");
            }
        } else if (!propertyType.accepts(value)) {
            return Optional.of("Invalid value '" + value + "', expected: " + hint());
        }
        return Optional.empty();
    }


    @Override
    public String toString() {
        var hint = multiValue ?
                "List of " + hint().substring(0, 1).toLowerCase() + hint().substring(1) :
                hint();
        return String.format(
                "- %s: %s%s",
                property,
                description.isBlank() ? hint : description,
                description.isBlank() ? "" : "\n  " + hint
        );
    }

}
