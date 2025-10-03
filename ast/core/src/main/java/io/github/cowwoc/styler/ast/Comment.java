package io.github.cowwoc.styler.ast;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a comment in source code with type, content, and position information.
 * Comments are preserved as metadata to maintain developer intent during formatting.
 *
 * @param type the type of comment (line, block, or Javadoc)
 * @param content the comment text content
 * @param position the source position of the comment
 * @param preserveFormatting whether to preserve original formatting
 */
public record Comment(Type type, String content, SourcePosition position, boolean preserveFormatting)
	{
	/**
	 * Enumeration of comment types supported in Java source code.
	 */
	public enum Type
		{
		/**
		 * Single-line comment starting with {@code //}.
		 */
		LINE,
		/**
		 * Multi-line comment enclosed in block delimiters.
		 */
		BLOCK,
		/**
		 * Javadoc comment starting with {@code /**} delimiters.
		 */
		JAVADOC
	}

	/**
	 * Compact constructor validating comment fields are not null.
	 *
	 * @throws NullPointerException if {@code type}, {@code content}, or {@code position} is null
	 */
	public Comment
		{
		requireThat(type, "type").isNotNull();
		requireThat(content, "content").isNotNull();
		requireThat(position, "position").isNotNull();
	}

	/**
	 * Creates a line comment at the specified position.
	 *
	 * @param content the comment text (without {@code //} prefix)
	 * @param position the source position
	 * @return a new line {@code Comment}
	 */
	public static Comment line(String content, SourcePosition position)
		{
		return new Comment(Type.LINE, content, position, false);
	}

	/**
	 * Creates a block comment at the specified position.
	 *
	 * @param content the comment text (without block comment delimiters)
	 * @param position the source position
	 * @return a new block {@code Comment}
	 */
	public static Comment block(String content, SourcePosition position)
		{
		return new Comment(Type.BLOCK, content, position, false);
	}

	/**
	 * Creates a Javadoc comment at the specified position.
	 *
	 * @param content the Javadoc text (without Javadoc delimiters)
	 * @param position the source position
	 * @return a new Javadoc {@code Comment}
	 */
	public static Comment javadoc(String content, SourcePosition position)
		{
		return new Comment(Type.JAVADOC, content, position, true);
	}

	/**
	 * Creates a copy of this comment with preserve formatting enabled.
	 *
	 * @return a new {@code Comment} with {@code preserveFormatting} set to {@code true}
	 */
	public Comment withPreserveFormatting()
		{
		return new Comment(type, content, position, true);
	}

	/**
	 * Gets the rendered comment text including delimiters.
	 *
	 * @return the complete comment text as it appears in source code
	 */
	public String getRenderedText()
		{
		return switch (type)
			{
			case LINE -> "//" + content;
			case BLOCK -> "/*" + content + "*/";
			case JAVADOC -> "/**" + content + "*/";
		};
	}
}
