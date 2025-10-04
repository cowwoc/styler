package io.github.cowwoc.styler.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Parses .gitignore files and applies ignore rules to file paths.
 *
 * <p>Supports standard .gitignore patterns including:
 * <ul>
 *   <li>Glob patterns ({@code *.class}, {@code **}{@code /*.log})</li>
 *   <li>Negation patterns ({@code !important.log})</li>
 *   <li>Directory patterns ({@code build/})</li>
 *   <li>Comments ({@code # comment})</li>
 * </ul>
 *
 * <p>This class is immutable and thread-safe after construction.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * Path gitignorePath = repositoryRoot.resolve(".gitignore");
 * GitignoreParser parser = GitignoreParser.parse(gitignorePath);
 *
 * boolean ignored = parser.isIgnored(Paths.get("target/classes/Example.class"));
 * }</pre>
 *
 * <h2>Pattern Precedence:</h2>
 * <p>Later patterns override earlier patterns. Negation patterns ({@code !pattern})
 * un-ignore files that were previously ignored.
 *
 * @see <a href="https://git-scm.com/docs/gitignore">Git SCM - gitignore Documentation</a>
 */
public final class GitignoreParser
{
	private static final Logger log = LoggerFactory.getLogger(GitignoreParser.class);

	/** Maximum .gitignore file size: 1 MB. */
	private static final long MAX_FILE_SIZE = 1024 * 1024;

	private final List<GitignoreRule> rules;
	private final Path gitignoreFile;

	/**
	 * Private constructor - use {@link #parse(Path)} to create instances.
	 *
	 * @param gitignoreFile the .gitignore file that was parsed
	 * @param rules the parsed rules
	 */
	private GitignoreParser(Path gitignoreFile, List<GitignoreRule> rules)
	{
		this.gitignoreFile = gitignoreFile;
		this.rules = List.copyOf(rules);
	}

	/**
	 * Parses a .gitignore file and returns a parser instance.
	 *
	 * @param gitignoreFile the .gitignore file to parse, must not be {@code null}
	 * @return parser instance with loaded rules
	 * @throws NullPointerException if gitignoreFile is {@code null}
	 * @throws IOException if file cannot be read
	 * @throws IllegalArgumentException if file exceeds size limit
	 */
	public static GitignoreParser parse(Path gitignoreFile) throws IOException
	{
		Objects.requireNonNull(gitignoreFile, "gitignoreFile must not be null");

		if (!Files.exists(gitignoreFile))
		{
			log.debug("Gitignore file does not exist: {}", gitignoreFile);
			return new GitignoreParser(gitignoreFile, List.of());
		}

		long fileSize = Files.size(gitignoreFile);
		if (fileSize > MAX_FILE_SIZE)
		{
			throw new IllegalArgumentException(
				"Gitignore file exceeds maximum size of " + MAX_FILE_SIZE +
				" bytes: " + fileSize);
		}

		List<GitignoreRule> rules = new ArrayList<>();
		try (Stream<String> lines = Files.lines(gitignoreFile))
		{
			lines.forEach(line ->
			{
				GitignoreRule rule = parseRule(line);
				if (rule != null)
				{
					rules.add(rule);
				}
			});
		}

		log.debug("Parsed {} rules from {}", rules.size(), gitignoreFile);
		return new GitignoreParser(gitignoreFile, rules);
	}

	/**
	 * Parses a single .gitignore rule from a line.
	 *
	 * @param line the line to parse
	 * @return parsed rule, or {@code null} if line should be ignored
	 */
	private static GitignoreRule parseRule(String line)
	{
		// Trim leading/trailing whitespace
		line = line.trim();

		// Skip empty lines and comments
		if (line.isEmpty() || line.startsWith("#"))
		{
			return null;
		}

		// Check for negation pattern
		boolean negation = false;
		if (line.startsWith("!"))
		{
			negation = true;
			line = line.substring(1);
		}

		// Check for directory pattern
		boolean directoryOnly = line.endsWith("/");
		if (directoryOnly)
		{
			line = line.substring(0, line.length() - 1);
		}

		return new GitignoreRule(line, negation, directoryOnly);
	}

	/**
	 * Checks if the specified file should be ignored according to .gitignore rules.
	 *
	 * <p>Rules are applied in order, with later rules overriding earlier ones.
	 * Negation rules ({@code !pattern}) un-ignore previously ignored files.
	 *
	 * @param file the file to check, must not be {@code null}
	 * @return {@code true} if file should be ignored
	 * @throws NullPointerException if file is {@code null}
	 */
	public boolean isIgnored(Path file)
	{
		Objects.requireNonNull(file, "file must not be null");

		boolean ignored = false;

		for (GitignoreRule rule : rules)
		{
			if (rule.matches(file))
			{
				ignored = !rule.isNegation();
			}
		}

		return ignored;
	}

	/**
	 * Returns the .gitignore file that was parsed.
	 *
	 * @return the gitignore file path
	 */
	public Path getGitignoreFile()
	{
		return gitignoreFile;
	}

	/**
	 * Returns the number of rules loaded from the .gitignore file.
	 *
	 * @return rule count
	 */
	public int getRuleCount()
	{
		return rules.size();
	}

	/**
	 * Represents a single .gitignore rule.
	 */
	private static final class GitignoreRule
	{
		private final String pattern;
		private final boolean negation;
		private final boolean directoryOnly;

		GitignoreRule(String pattern, boolean negation, boolean directoryOnly)
		{
			this.pattern = pattern;
			this.negation = negation;
			this.directoryOnly = directoryOnly;
		}

		boolean isNegation()
		{
			return negation;
		}

		boolean matches(Path file)
		{
			String filePath = file.toString().replace('\\', '/');

			// Wildcard pattern matching (no regex to prevent ReDoS)
			if (pattern.contains("*"))
			{
				return matchesWildcard(filePath, pattern);
			}

			// Exact match or substring match
			return filePath.contains(pattern) || filePath.endsWith("/" + pattern);
		}

		/**
		 * Matches a file path against a wildcard pattern without using regex.
		 *
		 * <p>This prevents ReDoS (Regular Expression Denial of Service) attacks from
		 * malicious .gitignore patterns with many wildcards.
		 *
		 * @param filePath the file path to match (forward slashes)
		 * @param pattern the wildcard pattern (* matches any chars)
		 * @return {@code true} if the path matches the pattern
		 */
		private boolean matchesWildcard(String filePath, String pattern)
		{
			int fileIndex = 0;
			int patternIndex = 0;
			int fileLength = filePath.length();
			int patternLength = pattern.length();
			int starIndex = -1;
			int matchIndex = 0;

			while (fileIndex < fileLength)
			{
				if (patternIndex < patternLength &&
					(pattern.charAt(patternIndex) == '*'))
				{
					// Remember star position for backtracking
					starIndex = patternIndex;
					matchIndex = fileIndex;
					++patternIndex;
				}
				else if (patternIndex < patternLength &&
					(pattern.charAt(patternIndex) == filePath.charAt(fileIndex) ||
					pattern.charAt(patternIndex) == '?'))
				{
					// Characters match or ? wildcard
					++fileIndex;
					++patternIndex;
				}
				else if (starIndex != -1)
				{
					// Backtrack to last star and try matching more characters
					patternIndex = starIndex + 1;
					++matchIndex;
					fileIndex = matchIndex;
				}
				else
				{
					return false;
				}
			}

			// Check remaining pattern characters (must be all stars)
			while (patternIndex < patternLength && pattern.charAt(patternIndex) == '*')
			{
				++patternIndex;
			}

			return patternIndex == patternLength;
		}
	}
}
