# Summary: collapse-import-node-types

## Status: COMPLETE
**Completed**: 2026-01-02

## What Was Built
- Added `boolean isStatic` component to `ImportAttribute` record
- Updated Parser to pass `isStatic` flag when creating import nodes
- Simplified `ImportExtractor` to use single `findNodesByType()` call
- Removed `STATIC_IMPORT_DECLARATION` from `NodeType` enum
- Removed `allocateStaticImportDeclaration()` from `NodeArena`

## Files Modified
- `ast/core/src/main/java/.../ImportAttribute.java` - Added `isStatic` component
- `ast/core/src/main/java/.../NodeType.java` - Removed `STATIC_IMPORT_DECLARATION`
- `ast/core/src/main/java/.../NodeArena.java` - Removed static import allocation method
- `parser/src/main/java/.../Parser.java` - Use single allocation for all imports
- `formatter/src/main/java/.../ImportExtractor.java` - Single query approach
- `formatter/src/main/java/.../ContextDetector.java` - Removed switch case
- Multiple test files updated for new attribute structure

## Test Infrastructure Improvements
- `ParserTestUtils.SemanticNode` refactored with compile-time safety
- Added 72 type-specific factory methods for all non-attribute NodeTypes
- Updated 35 test files to use type-safe factory methods

## Benefits
- Single query returns all imports in position order
- Cleaner semantic model (static is a modifier)
- Simpler consumer code

## Quality
- All tests passing
- Zero Checkstyle/PMD violations
