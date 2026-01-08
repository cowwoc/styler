# Summary: add-multi-catch-support

## Status: COMPLETE
**Completed**: 2025-12-29

## What Was Built
- Added `UNION_TYPE` to NodeType enum (after WILDCARD_TYPE)
- Created dedicated `parseCatchParameter()` method for catch clause parameters
- Modified `parseCatchClause()` to call parseCatchParameter()
- Union type parsing handles `|` operator between exception types

## Files Modified
- `ast/core/src/main/java/.../ast/core/NodeType.java`
- `parser/src/main/java/.../parser/Parser.java`
- `formatter/src/main/java/.../linelength/internal/ContextDetector.java`

## Files Created
- `parser/src/test/java/.../parser/test/MultiCatchParserTest.java` - 5 tests

## Test Coverage
- Two-exception union: `catch (IOException | SQLException e)`
- Three-exception union: `catch (IOException | SQLException | TimeoutException e)`
- Simple catch regression
- Final modifier: `catch (final IOException | SQLException e)`
- Fully qualified names

## Quality
- All tests passing
- Zero Checkstyle/PMD violations
