# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** implemented
- **Completed:** 2026-01-15
- **Dependencies:** []
- **Last Updated:** 2026-01-15

## Error Pattern

**~10 occurrences** in Spring Framework 6.2.1

Errors:
- `Expected RIGHT_PARENTHESIS but found IDENTIFIER`
- `Expected RIGHT_PARENTHESIS but found ARROW`

Example code:
```java
context.addApplicationListener((MyEvent event) -> seenEvents.add(event));
connection -> ConnectionFactoryUtils.releaseConnection(connection, connectionFactory)
return (S s) -> { ... };
```

Lambda expressions with explicit type parameters aren't parsing correctly when
passed as method arguments.

## Implementation

Added comprehensive lambda detection in `parseParenthesizedOrLambda()` that scans for
the `) ->` pattern BEFORE trying cast parsing. Key changes:

- Added `isLambdaExpression()` lookahead that handles nested parens, generics, and shift operators
- Added `parseLambdaParameters()` with typed/untyped detection via `isTypedLambdaParameters()`
- Reordered decision logic: lambda check now precedes cast attempt
- Removed obsolete `isMultiParamLambda()` and `parseMultiParamLambda()` methods

Supported patterns:
- `(MyEvent event) -> handle(event)` - single typed param
- `(String a, int b) -> a + b` - multi typed params
- `(List<String> items) -> items.size()` - generic type param
- `(String[] args) -> args.length` - array type param
- `(final String s) -> s.length()` - final modifier
- `(@NonNull String s) -> s.length()` - annotation
