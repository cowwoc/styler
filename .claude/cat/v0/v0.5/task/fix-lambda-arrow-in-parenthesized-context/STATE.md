# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** integrate-expression-parser
- **Last Updated:** 2026-01-17
- **Note:** Reopened - previous fix didn't address lambda after method reference with trailing comments

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

The root cause was asymmetric parenthesis tracking in `isLambdaExpression()`:
- `LEFT_PARENTHESIS` always incremented `parenthesisDepth`
- `RIGHT_PARENTHESIS` only decremented when `angleBracketDepth == 0`

This caused false negatives for lambdas with annotations containing element-value pairs inside generic
type parameters (e.g., `List<@NonNull(when=MAYBE) String>`). The parentheses inside the annotation
were counted on open but not on close, leading to incorrect depth tracking.

Fix: Make both `LEFT_PARENTHESIS` and `RIGHT_PARENTHESIS` only track when `angleBracketDepth == 0`.

## Acceptance Criteria

- [ ] Failing test `shouldParseLambdaAfterMethodReferenceWithTrailingComments` passes
- [ ] DatabasePopulator.java and Jackson2ObjectMapperBuilder.java parse successfully
- [ ] No regression in other Spring Framework files
