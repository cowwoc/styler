# Task Plan: migrate-parser-tests-to-nodearena

## Objective

Replace SemanticNode wrapper with direct NodeArena comparison.

## Tasks

1. Migrate all test files from parseSemanticAst() to parse()
2. Replace Set<SemanticNode> with NodeArena comparison
3. Remove SemanticNode class and factory methods
4. Reduce ParserTestUtils to essential methods

## Verification

- [ ] All parser tests pass with new pattern
- [ ] ParserTestUtils significantly reduced
- [ ] Tests use try-with-resources for NodeArena

