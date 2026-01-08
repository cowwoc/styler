# Plan: implement-ast-diff

## Objective
Semantic AST diff ignoring whitespace differences.

## Tasks
1. Design AstDiff and AstChange record structures
2. Implement parallel tree walking algorithm
3. Compare nodes by semantic content
4. Track UNCHANGED, MODIFIED, ADDED, DELETED nodes
5. Make comment comparison configurable

## Dependencies
- Parser (complete)

## Verification
- [ ] All change types detected correctly
- [ ] Complex refactoring scenarios handled
