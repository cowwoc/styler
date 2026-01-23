# State

- **Status:** completed
- **Progress:** 100%
- **Dependencies:** [fix-contextual-keywords-as-identifiers]
- **Resolution:** implemented
- **Completed:** 2026-01-14
- **Last Updated:** 2026-01-14

## Solution

Changed `parseDotExpression()` to use `isIdentifierOrContextualKeyword()` instead of direct
`TokenType.IDENTIFIER` check. This allows contextual keywords like `with`, `to`, `requires` to be
used as method names after the dot operator.

## Tests Added

`ContextualKeywordMethodCallTest` with 4 test cases:
- `testWithMethodCall` - `.with()` method call
- `testToMethodCall` - `.to()` method call
- `testRequiresMethodCall` - `.requires()` method call
- `testChainedContextualKeywordMethods` - chained contextual keyword methods
