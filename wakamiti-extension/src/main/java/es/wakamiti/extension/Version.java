/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension;


import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * Represents a version number following the Semantic Versioning scheme.
 * <p>
 * A version number consists of three parts: major, minor, and patch.
 * For example, in version {@code 2.4.1-SNAPSHOT}, {@code 2} is the major version,
 * {@code 4} is the minor version, and {@code 1-SNAPSHOT} is the patch version.
 *
 * @see <a href="https://semver.org/">Semantic Versioning</a>
 */
public class Version implements Comparable<Version> {

    private static final Comparator<Version> COMPARATOR = Comparator
            .comparingInt(Version::major)
            .thenComparingInt(Version::minor)
            .thenComparing(Version::patch);

    private final int major;
    private final int minor;
    private final String patch;


    /**
     * Constructs a Version instance from a string representation.
     *
     * @param version the string representation of the version
     * @throws IllegalArgumentException if the version format is invalid
     */
    private Version(String version) {
        var parts = Stream.of(version.split("\\.")).iterator();
        try {
            this.major = Integer.parseInt(parts.next());
            this.minor = parts.hasNext() ? Integer.parseInt(parts.next()) : 0;
            this.patch = parts.hasNext() ? parts.next() : "";
        } catch (NoSuchElementException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Not valid version number %s : %s".formatted(version, e.getMessage()));
        }
    }

    /**
     * Creates a new instance from a string representation.
     *
     * @param version The string representation of the version, like {@code 2.4.1-SNAPSHOT}
     * @return A new semantic version instance
     * @throws IllegalArgumentException if the version format is ill-formed
     */
    public static Version of(String version) {
        if (version == null || version.isBlank()) {
            return null;
        }
        return new Version(version);
    }

    /**
     * Checks whether the given string representation is a valid semantic version.
     *
     * @param version The string representation of the version
     * @return {@code true} if the version format is valid, {@code false} otherwise
     */
    public static boolean validate(String version) {
        try {
            of(version);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns the major part of the version.
     *
     * @return the major part of the version
     */
    public int major() {
        return major;
    }


    /**
     * Returns the minor part of the version.
     *
     * @return the minor part of the version, or {@code 0} if not present
     */
    public int minor() {
        return minor;
    }

    /**
     * Returns the patch part of the version.
     *
     * @return the patch part of the version, or an empty string if not present
     */
    public String patch() {
        return patch;
    }

    /**
     * Checks whether this version is compatible with the given version.
     * <p>
     * One version is considered compatible with another if they have the same major
     * version and the minor version of this version is greater than or equal to the
     * minor version of the other.
     *
     * @param otherVersion the version to compare with
     * @return {@code true} if the versions are compatible, {@code false} otherwise
     */
    public boolean isCompatibleWith(Version otherVersion) {
        return (major == otherVersion.major && minor >= otherVersion.minor);
    }

    /**
     * Returns the string representation of this version.
     *
     * @return the string representation of this version
     */
    @Override
    public String toString() {
        return patch.isBlank() ? major + "." + minor : major + "." + minor + "." + patch;
    }

    /**
     * Compares this version with the specified version for order.
     *
     * @param other the version to be compared
     * @return a negative integer, zero, or a positive integer as this version
     *         is less than, equal to, or greater than the specified version
     */
    @Override
    public int compareTo(Version other) {
        return COMPARATOR.compare(this, other);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the obj
     *         argument, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version that = (Version) o;
        return major == that.major && minor == that.minor && Objects.equals(patch, that.patch);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

}