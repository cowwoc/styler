package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.parser.ParseError;
import io.github.cowwoc.styler.parser.ParseResult;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link ParseResult} sealed interface and its implementations.
 * <p>
 * Validates that Success requires a valid root node and Failure requires a non-empty list of errors.
 * Also verifies that Failure defensively copies its error list to ensure immutability.
 */
public class ParseResultTest
{
	/**
	 * Verifies that Success rejects null root node.
	 * A successful parse result must always contain a valid AST root reference.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void successShouldRejectNullRootNode()
	{
		new ParseResult.Success(null);
	}

	/**
	 * Verifies that Success rejects invalid root node (NodeIndex.NULL).
	 * The root node must reference an actual node in the AST, not the null sentinel.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void successShouldRejectInvalidRootNode()
	{
		new ParseResult.Success(NodeIndex.NULL);
	}

	/**
	 * Verifies that Success accepts a valid root node and stores it correctly.
	 * A valid NodeIndex (index >= 0) represents a real node in the AST.
	 */
	@Test
	public void successShouldAcceptValidRootNode()
	{
		NodeIndex validNode = new NodeIndex(0);

		ParseResult.Success success = new ParseResult.Success(validNode);

		requireThat(success.rootNode(), "rootNode").isEqualTo(validNode);
	}

	/**
	 * Verifies that Failure rejects null error list.
	 * A failed parse result must contain at least one error explaining why parsing failed.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void failureShouldRejectNullErrors()
	{
		new ParseResult.Failure(null);
	}

	/**
	 * Verifies that Failure rejects empty error list.
	 * A parse failure without any errors would leave developers without diagnostic information.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void failureShouldRejectEmptyErrors()
	{
		new ParseResult.Failure(List.of());
	}

	/**
	 * Verifies that Failure accepts a non-empty list of errors and stores it correctly.
	 * Multiple errors may be reported when the parser encounters several issues.
	 */
	@Test
	public void failureShouldAcceptValidErrors()
	{
		ParseError error1 = new ParseError(0, 1, 1, "First error");
		ParseError error2 = new ParseError(10, 2, 5, "Second error");
		List<ParseError> errors = List.of(error1, error2);

		ParseResult.Failure failure = new ParseResult.Failure(errors);

		requireThat(failure.errors().size(), "errors.size()").isEqualTo(2);
		requireThat(failure.errors().get(0), "errors.get(0)").isEqualTo(error1);
		requireThat(failure.errors().get(1), "errors.get(1)").isEqualTo(error2);
	}

	/**
	 * Verifies that Failure defensively copies the error list.
	 * Modifying the original list after construction must not affect the Failure instance,
	 * ensuring immutability as documented in the thread-safety note.
	 */
	@Test
	public void failureShouldDefensivelyCopyErrors()
	{
		ParseError error = new ParseError(0, 1, 1, "Error message");
		List<ParseError> mutableList = new ArrayList<>();
		mutableList.add(error);

		ParseResult.Failure failure = new ParseResult.Failure(mutableList);

		// Modify the original list
		mutableList.add(new ParseError(5, 2, 1, "Another error"));

		// Failure should still have only one error
		requireThat(failure.errors().size(), "errors.size()").isEqualTo(1);
	}
}
