package io.github.cowwoc.styler.parser;

/**
 * Enumeration of Java language versions for version-specific parsing.
 *
 * This enum provides a type-safe way to specify which Java version features
 * should be supported during parsing. Different versions introduce different
 * language features that require specialized parsing strategies.
 *
 * <h2>Version-Specific Features</h2>
 * <ul>
 * <li><strong>Java 8:</strong> Lambda expressions, method references, default methods</li>
 * <li><strong>Java 9:</strong> Module system, private interface methods</li>
 * <li><strong>Java 10:</strong> Local variable type inference (var)</li>
 * <li><strong>Java 14:</strong> Switch expressions, records (preview), pattern matching (preview)</li>
 * <li><strong>Java 17:</strong> Sealed classes, pattern matching for instanceof</li>
 * <li><strong>Java 21:</strong> String templates, virtual threads, pattern matching for switch</li>
 * <li><strong>Java 25:</strong> Primitive type patterns, flexible constructor bodies</li>
 * </ul>
 *
 * <h2>Usage in Parser</h2>
 * The parser uses this enum to determine which {@link ParseStrategy} implementations
 * should be active for a given parsing session. Higher version numbers include
 * all features from lower versions.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * JavaVersion version = JavaVersion.JAVA_21;
 * if (version.isAtLeast(JavaVersion.JAVA_14)) {
 *     // Can parse switch expressions and records
 * }
 * }</pre>
 *
 * @since 1.0
 * @see ParseStrategy
 * @see IndexOverlayParser
 */
public enum JavaVersion {
    JAVA_8(8),
    JAVA_9(9),
    JAVA_10(10),
    JAVA_11(11),
    JAVA_12(12),
    JAVA_13(13),
    JAVA_14(14),
    JAVA_15(15),
    JAVA_16(16),
    JAVA_17(17),
    JAVA_18(18),
    JAVA_19(19),
    JAVA_20(20),
    JAVA_21(21),
    JAVA_22(22),
    JAVA_23(23),
    JAVA_24(24),
    JAVA_25(25);

    private final int versionNumber;

    JavaVersion(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    /**
     * Gets the numeric version number for this Java version.
     *
     * @return The version number (e.g., 21 for Java 21)
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Gets the human-readable display name for this version.
     *
     * @return The display name (e.g., "Java 21")
     */
    public String getDisplayName() {
        return "Java " + versionNumber;
    }

    /**
     * Checks if this version is at least the specified minimum version.
     *
     * This is useful for determining feature availability. For example,
     * if parsing code that uses switch expressions, you can check:
     * {@code version.isAtLeast(JavaVersion.JAVA_14)}
     *
     * @param minimum The minimum required version
     * @return true if this version is greater than or equal to the minimum
     */
    public boolean isAtLeast(JavaVersion minimum) {
        return this.versionNumber >= minimum.versionNumber;
    }

    /**
     * Gets the Java version enum constant from a numeric version number.
     *
     * @param versionNumber The numeric version (e.g., 21)
     * @return The corresponding JavaVersion enum constant
     * @throws IllegalArgumentException if the version number is not supported
     */
    public static JavaVersion fromNumber(int versionNumber) {
        for (JavaVersion version : values()) {
            if (version.versionNumber == versionNumber) {
                return version;
            }
        }
        throw new IllegalArgumentException("Unsupported Java version: " + versionNumber);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}