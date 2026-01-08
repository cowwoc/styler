# Summary: comment-parsing-fixes

## Status: COMPLETE
**Completed**: 2026-01-06

## What Was Built

### Part A: Remaining Comment Gaps (2026-01-03)
- Added ~36 `parseComments()` calls across 15 parser methods
- Covered: array initializers, lambda expressions, control flow (if/for/while/switch), blocks
- Note: Method/record parameters and type parameters deferred (parser architecture limitation)

### Part B: Block Comments in Member Declarations (2026-01-06)
- **Root Cause**: `parseMemberDeclaration()` didn't call `parseComments()` at its start
- Some call sites (class body loop) correctly called `parseComments()` first
- Other call sites (enum body after semicolon, anonymous class bodies) did not
- Added `parseComments()` call at start of `parseMemberDeclaration()` (Parser.java:1137)

## Files Modified
- `parser/src/main/java/.../parser/Parser.java`

## Files Created/Updated
- `parser/src/test/java/.../parser/test/ArrayInitializerCommentParserTest.java`
- `parser/src/test/java/.../parser/test/BlockCommentParserTest.java`
- `parser/src/test/java/.../parser/test/ControlFlowCommentParserTest.java`
- `parser/src/test/java/.../parser/test/LambdaCommentParserTest.java`

## Quality
- All parser tests pass (55 test classes)
- Full project build passes
- Zero Checkstyle/PMD violations
