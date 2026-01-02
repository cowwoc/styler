package io.github.cowwoc.styler.ast.core;

/**
 * Centralized security configuration for parser limits and constraints.
 * <p>
 * <strong>SEC-016:</strong> Provides a single source of truth for all security-related thresholds.
 * All constants are calibrated for single-user code formatting scenarios where legitimate
 * Java source files rarely exceed these limits, but malicious inputs could cause DoS.
 * <p>
 * <strong>Threat Model:</strong> Resource exhaustion attacks via crafted input files.
 * <br><strong>Defense Strategy:</strong> Multi-layered limits (file size, tokens, nodes, time, depth).
 */
public final class SecurityConfig
{
	/**
	 * Maximum source file size in bytes (10MB).
	 * <p>
	 * <strong>SEC-001: File Size Limit</strong>
	 * <p>
	 * <strong>Rationale:</strong> Prevents memory exhaustion from loading enormous files.
	 * Typical Java source files are 100-500KB; 10MB accommodates generated code while
	 * blocking multi-GB attack files.
	 * <p>
	 * <strong>Threat Prevention:</strong> Attackers cannot exhaust heap by providing
	 * arbitrarily large input files (e.g., 2GB of whitespace or comments).
	 * <p>
	 * <strong>Performance:</strong> Checked once in constructor, O(1) overhead.
	 */
	public static final int MAX_SOURCE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

	/**
	 * Maximum token count after lexing (1 million tokens).
	 * <p>
	 * <strong>SEC-007: Token Count Limit</strong>
	 * <p>
	 * <strong>Rationale:</strong> Prevents memory exhaustion from pathologically tokenizable
	 * input. At ~50 bytes/token average, this allows ~50MB of tokens while blocking attacks
	 * that generate millions of single-character tokens.
	 * <p>
	 * <strong>Threat Prevention:</strong> Blocks lexical bombs (e.g., 10MB file of semicolons
	 * generating 10M tokens consuming gigabytes).
	 * <p>
	 * <strong>Threshold Determination:</strong> Based on largest real-world Java files
	 * (typically 10K-50K tokens) with 20x safety margin.
	 * <p>
	 * <strong>Performance:</strong> Checked once after lexing, O(1) overhead.
	 */
	public static final int MAX_TOKEN_COUNT = 1_000_000; // 1M tokens

	/**
	 * Maximum arena node capacity (100K nodes, ~1.6MB).
	 * <p>
	 * <strong>SEC-011: Arena Capacity Limit</strong>
	 * <p>
	 * <strong>Rationale:</strong> Prevents unbounded AST growth during parsing. Each node
	 * occupies exactly 16 bytes (index-overlay pattern), so 100K nodes = 1.6MB fixed cost.
	 * Typical ASTs have 1K-10K nodes; 100K provides 10x safety margin for large files.
	 * <p>
	 * <strong>Threat Prevention:</strong> Blocks breadth-heavy AST attacks
	 * (e.g., files with excessive method counts or deeply nested blocks).
	 * <p>
	 * <strong>Performance:</strong> Checked only during arena growth (exponential backoff),
	 * amortized O(1) overhead.
	 */
	public static final int MAX_ARENA_CAPACITY = 100_000; // 100K nodes (~1.6MB)

	/**
	 * Maximum AST node nesting depth (100 levels).
	 * <p>
	 * <strong>SEC-012: Node Depth Limit</strong>
	 * <p>
	 * <strong>Rationale:</strong> Prevents stack overflow from pathologically nested
	 * expressions. Java's practical nesting limit is ~50-100 levels; this value must trigger
	 * before JVM stack overflow occurs (~200-500 depending on stack size).
	 * <p>
	 * <strong>Threat Prevention:</strong> Blocks stack exhaustion via deeply nested
	 * expressions (e.g., {@code (((((...)))))} with 10,000 parentheses).
	 * <p>
	 * <strong>Performance:</strong> Checked on every recursive call to parseUnary() and
	 * parsePrimary(), minimal overhead (integer increment + comparison).
	 */
	public static final int MAX_NODE_DEPTH = 100;

	/**
	 * Parsing timeout in milliseconds (30 seconds).
	 * <p>
	 * <strong>SEC-006: Processing Timeout</strong>
	 * <p>
	 * <strong>Rationale:</strong> Prevents infinite loops and algorithmic complexity attacks.
	 * Typical files parse in 0.1-2 seconds; 30 seconds accommodates slow systems and large
	 * files while preventing indefinite hangs.
	 * <p>
	 * <strong>Threat Prevention:</strong> Blocks worst-case complexity attacks exploiting
	 * parser backtracking (e.g., ambiguous grammar constructs causing exponential behavior).
	 * <p>
	 * <strong>Performance:</strong> System.currentTimeMillis() checked periodically
	 * (see {@code enterDepth()}), ~100ns per check.
	 * <p>
	 * <strong>Threshold Determination:</strong> Empirical testing showed 10MB files parse
	 * in ~20 seconds; 30-second timeout provides safety margin without false positives.
	 */
	public static final long PARSING_TIMEOUT_MS = 30_000; // 30 seconds

	private SecurityConfig()
	{
	}
}
