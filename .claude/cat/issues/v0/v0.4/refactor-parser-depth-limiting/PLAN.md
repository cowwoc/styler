# Task Plan: refactor-parser-depth-limiting

## Objective

Rename parser limits to describe AST properties, not implementation details.

## Tasks

1. Rename MAX_PARSE_DEPTH to MAX_NODE_DEPTH (30 -> 100)
2. Lower MAX_ARENA_CAPACITY from 10M to 100K nodes
3. Update all references in Parser and tests

## Verification

- [ ] Constants renamed and limits adjusted
- [ ] All tests pass with new limits
- [ ] JavaDoc updated to describe AST semantics

