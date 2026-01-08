# Summary: refactor-parser-depth-limiting

## Status: COMPLETE
**Completed**: 2026-01-02

## What Was Built
- Renamed `MAX_PARSE_DEPTH` → `MAX_NODE_DEPTH` (30 → 100)
  - Describes maximum nesting depth of nodes in the AST
  - 100 provides margin for legitimate nesting while preventing stack overflow
- Lowered `MAX_ARENA_CAPACITY` from 10M → 100K nodes
  - More appropriate limit for single-file parsing (~1.6MB)
  - Typical ASTs have 1K-10K nodes; 100K provides 10x safety margin

## Files Modified
- `ast/core/src/main/java/.../ast/core/SecurityConfig.java` - Updated constants and JavaDoc
- `parser/src/main/java/.../parser/Parser.java` - Updated constant reference
- `parser/src/test/java/.../parser/test/ParserTest.java` - Updated constant reference
- `parser/src/test/java/.../parser/test/SecurityTest.java` - Updated constant reference

## Quality
- All tests passing
- Zero Checkstyle/PMD violations
- Build successful
