package io.github.cowwoc.styler.formatter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

/**
 * Configuration for brace placement formatting rules.
 *
 * <p>This configuration controls how braces are positioned for Java code constructs including
 * classes, methods, control structures (if/else, loops), and try/catch blocks. Supports multiple
 * industry-standard brace styles: K&R, Allman, and GNU.
 *
 * <p><strong>Brace Style Options:</strong>
 * <ul>
 *   <li><strong>{@link BraceStyle#K_AND_R K&R}</strong>: Opening brace on same line as declaration</li>
 *   <li><strong>{@link BraceStyle#ALLMAN Allman}</strong>: Opening brace on new line, same indentation</li>
 *   <li><strong>{@link BraceStyle#GNU GNU}</strong>: Opening brace on new line, indented one level</li>
 * </ul>
 *
 * <p><strong>Construct-Specific Overrides:</strong>
 * You can configure different brace styles for specific construct types (classes, methods, control
 * structures) by providing optional overrides. If not specified, the general {@code braceStyle}
 * applies to all constructs.
 *
 * <p><strong>Thread Safety:</strong> This class is immutable and thread-safe.
 *
 * <p><strong>Security:</strong> All configuration values are validated for security compliance.
 *
 * @see BraceStyle
 * @see EmptyBlockStyle
 * @see <a href="https://en.wikipedia.org/wiki/Indentation_style">Wikipedia: Indentation Style</a>
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"braceStyle", "classBraceStyle", "methodBraceStyle", "controlBraceStyle",
	"emptyBlockStyle", "requireBracesForSingleStatements", "allowSingleLineBlocks"
})
public final class BraceFormatterRuleConfiguration extends RuleConfiguration
{
	@JsonProperty("braceStyle")
	private final BraceStyle braceStyle;

	@JsonProperty("classBraceStyle")
	private final BraceStyle classBraceStyle;

	@JsonProperty("methodBraceStyle")
	private final BraceStyle methodBraceStyle;

	@JsonProperty("controlBraceStyle")
	private final BraceStyle controlBraceStyle;

	@JsonProperty("emptyBlockStyle")
	private final EmptyBlockStyle emptyBlockStyle;

	@JsonProperty("requireBracesForSingleStatements")
	private final boolean requireBracesForSingleStatements;

	@JsonProperty("allowSingleLineBlocks")
	private final boolean allowSingleLineBlocks;

	/**
	 * Creates a new brace formatter rule configuration.
	 *
	 * @param braceStyle the default brace style for all constructs (may be {@code null} for default)
	 * @param classBraceStyle the brace style override for class declarations (may be {@code null})
	 * @param methodBraceStyle the brace style override for method declarations (may be {@code null})
	 * @param controlBraceStyle the brace style override for control structures (may be {@code null})
	 * @param emptyBlockStyle the formatting style for empty blocks (may be {@code null} for default)
	 * @param requireBracesForSingleStatements whether to require braces for single-statement control structures
	 * @param allowSingleLineBlocks whether to allow opening and closing braces on the same line for short blocks
	 */
	@JsonCreator
	public BraceFormatterRuleConfiguration(
		@JsonProperty("braceStyle") BraceStyle braceStyle,
		@JsonProperty("classBraceStyle") BraceStyle classBraceStyle,
		@JsonProperty("methodBraceStyle") BraceStyle methodBraceStyle,
		@JsonProperty("controlBraceStyle") BraceStyle controlBraceStyle,
		@JsonProperty("emptyBlockStyle") EmptyBlockStyle emptyBlockStyle,
		@JsonProperty("requireBracesForSingleStatements") Boolean requireBracesForSingleStatements,
		@JsonProperty("allowSingleLineBlocks") Boolean allowSingleLineBlocks)
	{
		if (braceStyle != null)
		{
			this.braceStyle = braceStyle;
		}
		else
		{
			this.braceStyle = BraceStyle.K_AND_R;
		}

		this.classBraceStyle = classBraceStyle;
		this.methodBraceStyle = methodBraceStyle;
		this.controlBraceStyle = controlBraceStyle;

		if (emptyBlockStyle != null)
		{
			this.emptyBlockStyle = emptyBlockStyle;
		}
		else
		{
			this.emptyBlockStyle = EmptyBlockStyle.SAME_LINE;
		}

		if (requireBracesForSingleStatements != null)
		{
			this.requireBracesForSingleStatements = requireBracesForSingleStatements;
		}
		else
		{
			this.requireBracesForSingleStatements = true;
		}

		if (allowSingleLineBlocks != null)
		{
			this.allowSingleLineBlocks = allowSingleLineBlocks;
		}
		else
		{
			this.allowSingleLineBlocks = false;
		}

		try
		{
			validate();
		}
		catch (ConfigurationException e)
		{
			throw new IllegalArgumentException("Invalid brace formatter configuration at construction: " +
			                                 e.getMessage() + " (default style: " + this.braceStyle + ")", e);
		}
	}

	/**
	 * Creates a default brace formatter configuration.
	 *
	 * <p>Defaults to K&R style for all constructs, same-line empty blocks, braces required for
	 * single statements, and no single-line blocks allowed.
	 */
	public BraceFormatterRuleConfiguration()
	{
		this(null, null, null, null, null, null, null);
	}

	/**
	 * Validates this configuration.
	 *
	 * <p>Ensures all enum values are valid and configuration combinations are sensible.
	 *
	 * @throws ConfigurationException if the configuration is invalid
	 */
	@Override
	public void validate() throws ConfigurationException
	{
		validateParameter("braceStyle", braceStyle, BraceStyle.class);
		validateParameter("emptyBlockStyle", emptyBlockStyle, EmptyBlockStyle.class);

		if (classBraceStyle != null)
		{
			validateParameter("classBraceStyle", classBraceStyle, BraceStyle.class);
		}

		if (methodBraceStyle != null)
		{
			validateParameter("methodBraceStyle", methodBraceStyle, BraceStyle.class);
		}

		if (controlBraceStyle != null)
		{
			validateParameter("controlBraceStyle", controlBraceStyle, BraceStyle.class);
		}
	}

	@Override
	public RuleConfiguration merge(RuleConfiguration override)
	{
		if (!(override instanceof BraceFormatterRuleConfiguration other))
		{
			throw new IllegalArgumentException("Cannot merge BraceFormatterRuleConfiguration with " +
			                                 override.getClass().getSimpleName() +
			                                 " - incompatible configuration types");
		}

		// Merge logic: use other's value if it differs from default, otherwise use this's value
		BraceStyle mergedBraceStyle;
		if (other.braceStyle == BraceStyle.K_AND_R)
		{
			mergedBraceStyle = this.braceStyle;
		}
		else
		{
			mergedBraceStyle = other.braceStyle;
		}

		BraceStyle mergedClassBraceStyle;
		if (other.classBraceStyle == null)
		{
			mergedClassBraceStyle = this.classBraceStyle;
		}
		else
		{
			mergedClassBraceStyle = other.classBraceStyle;
		}

		BraceStyle mergedMethodBraceStyle;
		if (other.methodBraceStyle == null)
		{
			mergedMethodBraceStyle = this.methodBraceStyle;
		}
		else
		{
			mergedMethodBraceStyle = other.methodBraceStyle;
		}

		BraceStyle mergedControlBraceStyle;
		if (other.controlBraceStyle == null)
		{
			mergedControlBraceStyle = this.controlBraceStyle;
		}
		else
		{
			mergedControlBraceStyle = other.controlBraceStyle;
		}

		EmptyBlockStyle mergedEmptyBlockStyle;
		if (other.emptyBlockStyle == EmptyBlockStyle.SAME_LINE)
		{
			mergedEmptyBlockStyle = this.emptyBlockStyle;
		}
		else
		{
			mergedEmptyBlockStyle = other.emptyBlockStyle;
		}

		boolean mergedRequireBracesForSingleStatements;
		if (other.requireBracesForSingleStatements)
		{
			mergedRequireBracesForSingleStatements = this.requireBracesForSingleStatements;
		}
		else
		{
			mergedRequireBracesForSingleStatements = other.requireBracesForSingleStatements;
		}

		boolean mergedAllowSingleLineBlocks;
		if (other.allowSingleLineBlocks)
		{
			mergedAllowSingleLineBlocks = other.allowSingleLineBlocks;
		}
		else
		{
			mergedAllowSingleLineBlocks = this.allowSingleLineBlocks;
		}

		return new BraceFormatterRuleConfiguration(
			mergedBraceStyle,
			mergedClassBraceStyle,
			mergedMethodBraceStyle,
			mergedControlBraceStyle,
			mergedEmptyBlockStyle,
			mergedRequireBracesForSingleStatements,
			mergedAllowSingleLineBlocks);
	}

	@Override
	public String getDescription()
	{
		return String.format(
			"Brace Formatter Rule: style=%s, empty=%s, requireBraces=%s, singleLine=%s",
			getEffectiveBraceStyle("general"),
			emptyBlockStyle,
			requireBracesForSingleStatements,
			allowSingleLineBlocks);
	}

	/**
	 * Returns the effective brace style for a specific construct type.
	 *
	 * <p>If a construct-specific override is configured, returns that style. Otherwise, returns
	 * the general {@code braceStyle}.
	 *
	 * @param constructType the type of construct ("class", "method", or "control")
	 * @return the effective brace style for the specified construct type
	 * @throws IllegalArgumentException if {@code constructType} is {@code null}
	 * @throws IllegalArgumentException if {@code constructType} is not a recognized construct type
	 */
	public BraceStyle getEffectiveBraceStyle(String constructType)
	{
		if (constructType == null)
		{
			throw new IllegalArgumentException("Construct type cannot be null when determining effective brace style");
		}

		return switch (constructType.toLowerCase())
		{
			case "class", "interface", "enum", "record" ->
				classBraceStyle != null ? classBraceStyle : braceStyle;
			case "method", "constructor" ->
				methodBraceStyle != null ? methodBraceStyle : braceStyle;
			case "control", "if", "else", "for", "while", "do", "try", "catch", "finally" ->
				controlBraceStyle != null ? controlBraceStyle : braceStyle;
			case "general", "default" -> braceStyle;
			default -> throw new IllegalArgumentException(
				"Unknown construct type: " + constructType +
				" (expected: class, method, control, or general)");
		};
	}

	/**
	 * Returns the default brace style applied to all constructs.
	 *
	 * <p>This is the general brace style used when no construct-specific override is configured
	 * via {@link #getClassBraceStyle()}, {@link #getMethodBraceStyle()}, or
	 * {@link #getControlBraceStyle()}.
	 *
	 * @return the default brace style, never {@code null}
	 */
	public BraceStyle getBraceStyle()
	{
		return braceStyle;
	}

	/**
	 * Returns the brace style override for class declarations.
	 *
	 * <p>If {@code null}, the general {@link #getBraceStyle() braceStyle} is used for classes,
	 * interfaces, enums, and records.
	 *
	 * @return the class-specific brace style override, or {@code null} if not configured
	 */
	public BraceStyle getClassBraceStyle()
	{
		return classBraceStyle;
	}

	/**
	 * Returns the brace style override for method declarations.
	 *
	 * <p>If {@code null}, the general {@link #getBraceStyle() braceStyle} is used for methods
	 * and constructors.
	 *
	 * @return the method-specific brace style override, or {@code null} if not configured
	 */
	public BraceStyle getMethodBraceStyle()
	{
		return methodBraceStyle;
	}

	/**
	 * Returns the brace style override for control structures.
	 *
	 * <p>If {@code null}, the general {@link #getBraceStyle() braceStyle} is used for control
	 * structures including if/else, loops (for, while, do-while), try/catch/finally, and
	 * synchronized blocks.
	 *
	 * @return the control-structure-specific brace style override, or {@code null} if not
	 *         configured
	 */
	public BraceStyle getControlBraceStyle()
	{
		return controlBraceStyle;
	}

	/**
	 * Returns the formatting style for empty blocks.
	 *
	 * <p>Determines whether empty blocks use same-line braces {@code {}}, new-line braces,
	 * or preserve their existing formatting. This applies to empty method bodies, empty
	 * control structure blocks, and other empty block constructs.
	 *
	 * @return the empty block formatting style, never {@code null}
	 */
	public EmptyBlockStyle getEmptyBlockStyle()
	{
		return emptyBlockStyle;
	}

	/**
	 * Returns whether braces are required for single-statement control structures.
	 *
	 * <p>When {@code true}, enforces braces even for single-statement bodies:
	 * <pre>{@code
	 * if (condition)
	 * {
	 *     statement;
	 * }
	 * }</pre>
	 *
	 * <p>When {@code false}, allows brace-less single statements:
	 * <pre>{@code
	 * if (condition)
	 *     statement;
	 * }</pre>
	 *
	 * @return {@code true} if braces are required for single statements, {@code false} otherwise
	 */
	public boolean isRequireBracesForSingleStatements()
	{
		return requireBracesForSingleStatements;
	}

	/**
	 * Returns whether opening and closing braces can appear on the same line for short blocks.
	 *
	 * <p>When {@code true}, allows compact single-line formatting for brief method bodies or
	 * control structures:
	 * <pre>{@code
	 * public void shortMethod() { return; }
	 * if (flag) { doSomething(); }
	 * }</pre>
	 *
	 * <p>When {@code false}, requires opening and closing braces on separate lines regardless
	 * of block length, following the configured {@link BraceStyle}.
	 *
	 * @return {@code true} if single-line blocks are allowed, {@code false} otherwise
	 */
	public boolean isAllowSingleLineBlocks()
	{
		return allowSingleLineBlocks;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || getClass() != obj.getClass())
		{
			return false;
		}

		BraceFormatterRuleConfiguration that = (BraceFormatterRuleConfiguration) obj;
		return requireBracesForSingleStatements == that.requireBracesForSingleStatements &&
		       allowSingleLineBlocks == that.allowSingleLineBlocks &&
		       braceStyle == that.braceStyle &&
		       classBraceStyle == that.classBraceStyle &&
		       methodBraceStyle == that.methodBraceStyle &&
		       controlBraceStyle == that.controlBraceStyle &&
		       emptyBlockStyle == that.emptyBlockStyle;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(braceStyle, classBraceStyle, methodBraceStyle, controlBraceStyle,
		                   emptyBlockStyle, requireBracesForSingleStatements, allowSingleLineBlocks);
	}

	@Override
	public String toString()
	{
		return "BraceFormatterRuleConfiguration{" +
		       "braceStyle=" + braceStyle +
		       ", classBraceStyle=" + classBraceStyle +
		       ", methodBraceStyle=" + methodBraceStyle +
		       ", controlBraceStyle=" + controlBraceStyle +
		       ", emptyBlockStyle=" + emptyBlockStyle +
		       ", requireBracesForSingleStatements=" + requireBracesForSingleStatements +
		       ", allowSingleLineBlocks=" + allowSingleLineBlocks +
		       '}';
	}
}
