# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** implemented
- **Dependencies:** []
- **Last Updated:** 2026-01-20

## Implementation Summary

Fixed parser failure "Unexpected token: THROW" when comments appear between `->` and `throw`
in switch expression arms.

### Changes Made
1. **ExpressionParser.java** - Added `parser.parseComments()` after matching ARROW in switch arm
2. **StatementParser.java** - Added `parser.parseComments()` after matching ARROW in switch arm
3. **SwitchExpressionParserTest.java** - Added test verifying AST structure for affected pattern

### Verification
- All parser tests pass (no regressions)
- New test validates complete AST structure using `isEqualTo(expected)` pattern
