package com.experian.devicematcher.domain;

import static java.util.Comparator.comparingLong;

public class SemVersion implements Comparable<SemVersion> {
    private final long major;
    private final long minor;
    private final long patch;

    private final String versionString;

    public SemVersion(long major, long minor, long patch) {
        if (major < 0) {
            throw new IllegalArgumentException("Major version cannot be negative");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("Minor version cannot be negative");
        }
        if (patch < 0) {
            throw new IllegalArgumentException("Patch version cannot be negative");
        }

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.versionString = major + "." + minor + "." + patch;
    }

    public long getMajor() {
        return major;
    }

    public long getMinor() {
        return minor;
    }

    public long getPatch() {
        return patch;
    }

    @Override
    public String toString() {
        return versionString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemVersion that)) return false;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        return versionString.hashCode();
    }

    @Override
    public int compareTo(SemVersion o) {
        return comparingLong(SemVersion::getMajor)
            .thenComparingLong(SemVersion::getMinor)
            .thenComparingLong(SemVersion::getPatch)
            .compare(this, o);
    }

    public static SemVersion parse(String versionString) {
        if (versionString == null || versionString.isBlank()) {
            throw new IllegalArgumentException("Version string cannot be null or blank");
        }

        String[] parts = versionString.split("\\.");

        if (parts.length == 0 || parts[0].isBlank()) {
            throw new IllegalArgumentException("Major version is mandatory");
        }

        // Parse major version (mandatory)
        long major;
        try {
            major = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Major version must be a valid number: " + parts[0], e);
        }

        // Parse minor version (optional)
        long minor = 0;
        if (parts.length > 1 && !parts[1].isBlank()) {
            try {
                minor = Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Minor version must be a valid number: " + parts[1], e);
            }
        }

        // Parse patch version (optional)
        long patch = 0;
        if (parts.length > 2 && !parts[2].isBlank()) {
            try {
                patch = Long.parseLong(parts[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Patch version must be a valid number: " + parts[2], e);
            }
        }

        return new SemVersion(major, minor, patch);
    }
}
