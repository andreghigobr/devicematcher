package com.experian.devicematcher.domain;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class SemVersionTest {
    @Test
    void constructor_whenMajorVersionIsNegative_shouldThrowException() {
        var ex = assertThrows(IllegalArgumentException.class, () -> new SemVersion(-1, 0, 0));
        assertEquals("Major version cannot be negative", ex.getMessage());
    }

    @Test
    void constructor_whenMinorVersionIsNegative_shouldThrowException() {
        var ex = assertThrows(IllegalArgumentException.class, () -> new SemVersion(0, -1, 0));
        assertEquals("Minor version cannot be negative", ex.getMessage());
    }

    @Test
    void constructor_whenPatchVersionIsNegative_shouldThrowException() {
        var ex = assertThrows(IllegalArgumentException.class, () -> new SemVersion(0, 0, -1));
        assertEquals("Patch version cannot be negative", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "''",
        "'abc'",
        "'.0.0'"
    })
    void parse_whenInvalidSyntax_shouldThrowException(
        String versionString
    ) {
        assertThrows(IllegalArgumentException.class, () -> SemVersion.parse(versionString));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, 0, 0",
        "1., 1, 0, 0",
        "1.2, 1, 2, 0",
        "1.2.3, 1, 2, 3",
        "0.0.0, 0, 0, 0",
        "1.0.0, 1, 0, 0",
        "0.0.1, 0, 0, 1",
    })
    void parse_whenValidSyntax_shouldReturnVersion(
        String versionString,
        long expectedMajor,
        long expectedMinor,
        long expectedPatch
    ) {
        SemVersion semVersion = SemVersion.parse(versionString);
        assertEquals(expectedMajor, semVersion.getMajor());
        assertEquals(expectedMinor, semVersion.getMinor());
        assertEquals(expectedPatch, semVersion.getPatch());
    }

    @Test
    void compareTo_whenSameVersion_shouldReturnZero() {
        SemVersion version1 = new SemVersion(1, 0, 0);
        SemVersion version2 = new SemVersion(1, 0, 0);
        assertEquals(0, version1.compareTo(version2));
    }

    @Test
    void compareTo_whenDifferentMajorVersion_shouldReturnNegative() {
        SemVersion version1 = new SemVersion(1, 0, 0);
        SemVersion version2 = new SemVersion(2, 0, 0);
        assertEquals(-1, version1.compareTo(version2));
    }

    @Test
    void compareTo_whenDifferentMinorVersion_shouldReturnPositive() {
        SemVersion version1 = new SemVersion(1, 0, 0);
        SemVersion version2 = new SemVersion(0, 1, 0);
        assertEquals(1, version1.compareTo(version2));
    }
}
