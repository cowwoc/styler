# Summary: fix-array-creation-expression-parsing

## Status: COMPLETE
**Completed**: 2026-01-06

## What Was Built
- **Root Cause**: `parseNewExpression()` called `parseType()` which consumed `[]` brackets before array dimension expressions could be parsed
- Created `parseTypeWithoutArrayDimensions()` helper method
- Modified `parseNewExpression()` to use the new helper

## Files Modified
- `parser/src/main/java/.../Parser.java` - Added `parseTypeWithoutArrayDimensions()`, refactored `parseNewExpression()`
- `parser/src/test/java/.../ArrayCreationParserTest.java` - 20 tests (+568 lines)

## Quality
- All 771 parser tests pass (751 existing + 20 new)
- Zero Checkstyle/PMD violations
- Enables parsing of Spring Framework 6.2.1 sources (fixes ~10 previously failing files)
