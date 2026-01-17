# Plan: fix-switch-case-in-expression-context

## Problem

Nested switch statements with fall-through comments cause "Unexpected token in expression: CASE" errors.
This affects 14 Spring Framework files including CodeEmitter.java.

## Failing Tests (Created During Task Creation)

**Test file:** `parser/src/test/java/io/github/cowwoc/styler/parser/test/OldStyleSwitchCaseParserTest.java`
**Test method:** `shouldParseNestedSwitchWithFallthroughComments`
**Commit:** ac4e052 - test: add failing tests for switch fallthrough and lambda arrow bugs

```java
String source = """
    public class Test
    {
        int intOp;
        void swap() {}

        public void foo(int x, int mode)
        {
            switch (x)
            {
                default:
                    switch (mode)
                    {
                        case EQ: intOp = 1; break;
                        case NE: intOp = 2; break;
                        case GE: swap(); /* fall through */
                        case LT: intOp = 3; break;
                        case LE: swap(); /* fall through */
                        case GT: intOp = 4; break;
                    }
            }
        }

        static final int EQ = 0, NE = 1, GE = 2, LT = 3, LE = 4, GT = 5;
    }
    """;
```

**Verification run (from task creation):**
```bash
./mvnw test -pl parser -Dtest=OldStyleSwitchCaseParserTest#shouldParseNestedSwitchWithFallthroughComments
# Result: FAILED with error: "Unexpected token in expression: CASE at position 249"
```

## Goal

Fix parser treating COLON after case expression as ternary operator by calling parseLogicalOr() directly,
bypassing ternary parsing for case label expressions.

## Approach

Expose `parseLogicalOr()` through the `ParserAccess` interface and modify `parseCaseLabelExpression()` in
`StatementParser` to call it instead of `parseAssignment()`. This bypasses `parseTernary()` entirely,
preventing the parser from misinterpreting the case label's COLON as a ternary operator. The change is
semantically correct because ternary expressions are invalid in case labels anyway (they would require a
second colon which would conflict with the case label terminator).

## Files to Modify

| File | Change |
|------|--------|
| `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ParserAccess.java` | Add `parseLogicalOr()` method |
| `parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java` | Add delegate implementation |
| `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java` | Make parseLogicalOr() package-private |
| `parser/src/main/java/io/github/cowwoc/styler/parser/internal/StatementParser.java` | Call parseLogicalOr() |
| `parser/src/test/java/io/github/cowwoc/styler/parser/test/OldStyleSwitchCaseParserTest.java` | New test file |

## Execution Steps

### Step 1: Add parseLogicalOr to ParserAccess interface

**File:** `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ParserAccess.java`

Add after `parseAssignment()` method:

```java
	/**
	 * Parses a logical OR expression (stops at QUESTION_MARK for ternary).
	 * <p>
	 * Used by case label parsing to avoid interpreting COLON as ternary operator.
	 *
	 * @return the expression node index
	 */
	NodeIndex parseLogicalOr();
```

### Step 2: Implement delegate in Parser.java

**File:** `parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java`

Add to the anonymous `ParserAccess` implementation in `createParserAccess()`:

```java
			@Override
			public NodeIndex parseLogicalOr()
			{
				return expressionParser.parseLogicalOr();
			}
```

### Step 3: Change parseLogicalOr visibility in ExpressionParser

**File:** `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java`

Change `private` to package-private and add JavaDoc:

```java
	/**
	 * Parses a logical OR expression ({@code ||}).
	 *
	 * @return the expression node index
	 */
	NodeIndex parseLogicalOr()
	{
		return parseBinaryExpression(this::parseLogicalAnd, TokenType.LOGICAL_OR);
	}
```

### Step 4: Modify parseCaseLabelExpression in StatementParser

**File:** `parser/src/main/java/io/github/cowwoc/styler/parser/internal/StatementParser.java`

**Before:**
```java
	public void parseCaseLabelExpression()
	{
		// Skip lambda lookahead - go directly to assignment parsing
		parser.parseAssignment();
	}
```

**After:**
```java
	public void parseCaseLabelExpression()
	{
		// Use parseLogicalOr() to avoid treating COLON as ternary operator.
		// Ternary expressions are invalid in case labels anyway (would need second COLON
		// that conflicts with the case label terminator).
		parser.parseLogicalOr();
	}
```

### Step 5: Add tests for old-style switch patterns

**File:** Create `parser/src/test/java/io/github/cowwoc/styler/parser/test/OldStyleSwitchCaseParserTest.java`

Tests for: CASE, DEFAULT, BREAK, THROW, WHILE keywords after colon, plus fallthrough and qualified constants.

## Verification

```bash
# Run all parser tests
./mvnw -pl parser test

# Run specific test class
./mvnw -pl parser test -Dtest=OldStyleSwitchCaseParserTest

# Run existing switch tests for regression
./mvnw -pl parser test -Dtest=SwitchCaseConstantParserTest,SwitchExpressionParserTest

# Full build
./mvnw verify
```

## Commit Message

```
bugfix: stop treating case label COLON as ternary operator

When parsing old-style switch statements like `case FOO:`, the parser
incorrectly interpreted COLON as a ternary operator and tried to parse
the following statement (CASE/DEFAULT/BREAK/THROW/WHILE) as an expression.

Root cause: parseCaseLabelExpression() called parseAssignment() which
calls parseTernary(). When parseTernary() saw COLON after the case
expression, it assumed a ternary conditional expression.

Fix: Call parseLogicalOr() directly instead of parseAssignment() in
parseCaseLabelExpression(). This bypasses ternary parsing entirely.
Ternary expressions are semantically invalid in case labels anyway
(the second colon would conflict with the case label terminator).

Fixes 14 Spring Framework files that use old-style switch syntax.

Task ID: v0.5-fix-switch-case-in-expression-context
```
