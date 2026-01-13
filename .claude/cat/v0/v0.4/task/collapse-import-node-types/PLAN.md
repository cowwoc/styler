# Task Plan: collapse-import-node-types

## Objective

Simplify AST by treating static as a modifier on imports, not a distinct node type.

## Tasks

1. Add `boolean isStatic` component to ImportAttribute
2. Update Parser to pass isStatic flag
3. Simplify ImportExtractor to single findNodesByType() call
4. Remove STATIC_IMPORT_DECLARATION from NodeType
5. Update all affected tests

## Verification

- [ ] Single query returns all imports in position order
- [ ] Static flag preserved in ImportAttribute
- [ ] No regression in import handling

