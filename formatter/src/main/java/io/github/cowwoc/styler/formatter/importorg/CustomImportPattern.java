package io.github.cowwoc.styler.formatter.importorg;

import java.util.regex.Pattern;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A custom import pattern for grouping imports with user-defined regex.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param groupName name of the import group this pattern belongs to
 * @param pattern   compiled regex pattern to match import qualified names
 * @throws NullPointerException     if any argument is null
 * @throws IllegalArgumentException if groupName is empty
 */
public record CustomImportPattern(
	String groupName,
	Pattern pattern)
{
	/**
	 * Creates a custom import pattern with validation.
	 */
	public CustomImportPattern
	{
		requireThat(groupName, "groupName").isNotBlank();
		requireThat(pattern, "pattern").isNotNull();
	}

	/**
	 * Creates a custom import pattern from a regex string.
	 * <p>
	 * Validates the pattern for ReDoS vulnerabilities and compiles it.
	 *
	 * @param groupName name of the import group this pattern belongs to
	 * @param regex     regex pattern string to match import qualified names
	 * @return a new CustomImportPattern
	 * @throws NullPointerException     if any argument is null
	 * @throws IllegalArgumentException if groupName is empty, regex is empty, or regex contains
	 *                                  nested quantifiers (ReDoS vulnerability)
	 */
	public static CustomImportPattern of(String groupName, String regex)
	{
		requireThat(groupName, "groupName").isNotBlank();
		requireThat(regex, "regex").isNotBlank();
		validatePatternSecurity(regex);
		return new CustomImportPattern(groupName, Pattern.compile(regex));
	}

	/**
	 * Validates a regex pattern for ReDoS vulnerabilities.
	 * Rejects patterns with nested quantifiers that could cause catastrophic backtracking.
	 *
	 * @param regex the regex pattern to validate
	 * @throws IllegalArgumentException if pattern has nested quantifiers
	 */
	private static void validatePatternSecurity(String regex)
	{
		// Check for nested quantifiers: (a+)+, (.*?)+, etc.
		// These patterns can cause catastrophic backtracking
		if (regex.matches(".*\\([^)]*[+*?{][^)]*\\)[+*?{].*"))
		{
			throw new IllegalArgumentException(
				"Pattern contains nested quantifiers (ReDoS vulnerability): " + regex);
		}
	}

	/**
	 * Returns whether the given qualified name matches this pattern.
	 *
	 * @param qualifiedName the import qualified name to test
	 * @return true if the name matches this pattern
	 */
	public boolean matches(String qualifiedName)
	{
		return pattern.matcher(qualifiedName).matches();
	}
}
