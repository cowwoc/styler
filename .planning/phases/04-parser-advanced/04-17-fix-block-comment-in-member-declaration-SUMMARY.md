# Summary: fix-block-comment-in-member-declaration

## Status: COMPLETE
**Completed**: 2026-01-06

## What Was Built
- **Root Cause**: `parseMemberDeclaration()` didn't call `parseComments()` at its start
- Some call sites (class body loop) correctly called `parseComments()` first
- Other call sites (enum body after semicolon, anonymous class bodies) did not
- Added `parseComments()` call at start of `parseMemberDeclaration()` (Parser.java:1137)

## Files Modified
- `parser/src/main/java/.../parser/Parser.java` - Added parseComments() call (+1 line)
- `parser/src/test/java/.../BlockCommentParserTest.java` - Added test (+27 lines)

## Quality
- All parser tests pass (55 test classes)
- Full project build passes
- Zero Checkstyle/PMD violations
