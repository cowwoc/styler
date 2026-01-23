# State

- **Status:** completed
- **Progress:** 100%
- **Dependencies:** integrate-expression-parser
- **Last Updated:** 2026-01-20
- **Resolution:** implemented
- **Completed:** 2026-01-20

## Description

Fix parser errors where `ARROW` (`->`) is found when `RIGHT_PARENTHESIS` was expected. These occur
in lambda expressions within method arguments where the parser incorrectly interprets the context.

**Error pattern (4 files):**
- Expected RIGHT_PARENTHESIS but found ARROW - e.g., PersistenceManagedTypesScanner.java

## Analysis

The parser expects a closing parenthesis but encounters a lambda arrow. This likely occurs when:
1. Lambda parameters look like a cast expression `(Type name)`
2. Parser commits to cast interpretation and fails when it sees `->` instead of `)`

The existing `fix-lambda-arrow-edge-cases` and `fix-lambda-typed-parameters-in-args` tasks resolved
most cases, but these 4 files contain edge cases not covered.

## Solution

Two issues were fixed:

1. **Asymmetric parenthesis tracking in `isLambdaExpression()`:**
   - `LEFT_PARENTHESIS` always incremented `parenthesisDepth`
   - `RIGHT_PARENTHESIS` only decremented when `angleBracketDepth == 0`

   Fix: Make both `LEFT_PARENTHESIS` and `RIGHT_PARENTHESIS` only track when `angleBracketDepth == 0`.
   This fixes lambdas with annotations containing element-value pairs inside generic type parameters.

2. **Missing comment consumption in `parseExpression()`:**
   - The lambda detection logic at the start of `parseExpression()` checked for `IDENTIFIER` token
   - But comments before the identifier weren't consumed first
   - When a lambda argument follows a method reference with trailing comments (`this::populate, //`),
     the identifier check failed and fell through to non-lambda parsing

   Fix: Add `parser.parseComments()` at the start of `parseExpression()` before checking for lambda.

## Acceptance Criteria

- [x] Failing test `shouldParseLambdaAfterMethodReferenceWithTrailingComments` passes
- [x] All 908 parser tests pass with no regressions
