package io.github.cowwoc.styler.discovery;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses .gitignore files and applies ignore rules.
 * <p>
 * Supports standard .gitignore syntax including:
 * - Comments (#)
 * - Negation (!)
 * - Directory-only rules (trailing /)
 * - Glob patterns (*, **, ?)
 *
 * <b>Thread-safety</b>: This class is stateless and thread-safe.
 */
public final class GitignoreParser
{
	/**
	 * Creates a new GitignoreParser.
	 */
	public GitignoreParser()
	{
	}

	/**
	 * Parses a .gitignore file.
	 *
	 * @param gitignorePath path to .gitignore file
	 * @return list of parsed rules
	 * @throws NullPointerException if {@code gitignorePath} is {@code null}
	 * @throws IOException if file cannot be read
	 */
	public List<GitignoreRule> parse(Path gitignorePath) throws IOException
	{
		requireThat(gitignorePath, "gitignorePath").isNotNull();

		List<GitignoreRule> rules = new ArrayList<>();
		List<String> lines = Files.readAllLines(gitignorePath, StandardCharsets.UTF_8);

		for (int lineNumber = 0; lineNumber < lines.size(); ++lineNumber)
		{
			String line = lines.get(lineNumber);
			GitignoreRule rule = parseLine(line, lineNumber + 1);
			if (rule != null)
			{
				rules.add(rule);
			}
		}

		return rules;
	}

	/**
	 * Tests if a path is ignored by the given rules.
	 * <p>
	 * Rules are applied in order. Negation rules (starting with !) can un-ignore previously ignored
	 * paths. Earlier rules take precedence unless negated.
	 *
	 * @param path the path to test (relative to .gitignore location)
	 * @param rules the gitignore rules to apply
	 * @return true if the path should be ignored
	 * @throws NullPointerException if {@code path} or {@code rules} is {@code null}
	 */
	public boolean isIgnored(Path path, List<GitignoreRule> rules)
	{
		requireThat(path, "path").isNotNull();
		requireThat(rules, "rules").isNotNull();

		boolean ignored = false;

		for (GitignoreRule rule : rules)
		{
			if (matches(path, rule))
			{
				ignored = !rule.negation();
			}
		}

		return ignored;
	}

	/**
	 * Parses a single line from a .gitignore file.
	 *
	 * @param line the line to parse
	 * @param lineNumber the line number (for debugging)
	 * @return parsed rule, or {@code null} if line is a comment or empty
	 */
	private GitignoreRule parseLine(String line, int lineNumber)
	{
		// Handle empty lines and comments
		String trimmed = line.strip();
		if (trimmed.isEmpty() || trimmed.startsWith("#"))
		{
			return null;
		}

		boolean negation = false;
		String pattern = trimmed;

		// Handle negation
		if (pattern.startsWith("!"))
		{
			negation = true;
			pattern = pattern.substring(1);
		}

		// Handle directory-only patterns
		boolean directoryOnly = false;
		if (pattern.endsWith("/"))
		{
			directoryOnly = true;
			pattern = pattern.substring(0, pattern.length() - 1);
		}

		// Determine if pattern is anchored (contains /)
		boolean anchored = pattern.contains("/");

		return new GitignoreRule(pattern, negation, directoryOnly, anchored, lineNumber);
	}

	/**
	 * Tests if a path matches a gitignore rule.
	 *
	 * @param path the path to test
	 * @param rule the rule to match against
	 * @return true if the rule matches the path
	 */
	private boolean matches(Path path, GitignoreRule rule)
	{
		String pathStr = path.toString().replace('\\', '/');
		String pattern = rule.pattern();

		// Handle directory-only patterns (ending with /)
		if (rule.directoryOnly())
		{
			return matchesDirectoryPattern(pathStr, pattern);
		}

		// Handle anchored patterns (starting with /)
		if (rule.anchored())
		{
			// If pattern starts with /, it matches only at root
			if (pattern.startsWith("/"))
			{
				String rootPattern = pattern.substring(1);
				return matchesPattern(pathStr, rootPattern);
			}
			// Anchored patterns with / in the middle match from the start
			return matchesPattern(pathStr, pattern);
		}

		// For non-anchored patterns, match against the filename or any path segment
		String filename = path.getFileName().toString();
		if (matchesPattern(filename, pattern))
		{
			return true;
		}

		// Also try matching the full path for patterns with wildcards
		return matchesPattern(pathStr, pattern) || matchesPattern(pathStr, "*/" + pattern);
	}

	/**
	 * Tests if a path matches a directory-only pattern.
	 *
	 * @param pathStr the path string to test
	 * @param pattern the directory pattern (without trailing /)
	 * @return true if the path matches the directory pattern
	 */
	private boolean matchesDirectoryPattern(String pathStr, String pattern)
	{
		// Match if path starts with the directory name
		if (pathStr.startsWith(pattern + "/") || pathStr.equals(pattern))
		{
			return true;
		}
		// For patterns with **, check if any segment matches
		if (pattern.contains("**"))
		{
			String dirName = pattern.replace("**/", "").replace("**", "");
			// Check if the directory appears anywhere in the path
			return pathStr.startsWith(dirName + "/") || pathStr.contains("/" + dirName + "/");
		}
		return false;
	}

	/**
	 * Simple glob pattern matching.
	 *
	 * @param text the text to match
	 * @param pattern the glob pattern
	 * @return true if text matches the pattern
	 */
	private boolean matchesPattern(String text, String pattern)
	{
		// Handle ** pattern for any directory depth
		if (pattern.contains("**"))
		{
			pattern = pattern.replace("**", "*");
		}

		return simpleGlobMatch(text, pattern);
	}

	/**
	 * Simple glob matching implementation for * and ? wildcards.
	 *
	 * @param text the text to match
	 * @param pattern the pattern with * and ? wildcards
	 * @return true if text matches pattern
	 */
	private boolean simpleGlobMatch(String text, String pattern)
	{
		int textIdx = 0;
		int patternIdx = 0;
		int textTmp = -1;
		int patternTmp = -1;

		while (textIdx < text.length())
		{
			if (patternIdx < pattern.length())
			{
				switch (pattern.charAt(patternIdx))
				{
					case '?':
						++textIdx;
						++patternIdx;
						break;
					case '*':
						textTmp = textIdx;
						patternTmp = patternIdx;
						++patternIdx;
						break;
					default:
						if (text.charAt(textIdx) == pattern.charAt(patternIdx))
						{
							++textIdx;
							++patternIdx;
						}
						else if (patternTmp != -1)
						{
							++textTmp;
							textIdx = textTmp;
							patternIdx = patternTmp + 1;
						}
						else
						{
							return false;
						}
						break;
				}
			}
			else if (patternTmp != -1)
			{
				++textTmp;
				textIdx = textTmp;
				patternIdx = patternTmp + 1;
			}
			else
			{
				return false;
			}
		}

		while (patternIdx < pattern.length() && pattern.charAt(patternIdx) == '*')
		{
			++patternIdx;
		}

		return patternIdx == pattern.length();
	}
}
