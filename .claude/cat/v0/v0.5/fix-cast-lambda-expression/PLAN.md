# Task Plan: fix-cast-lambda-expression

## Objective

Fix parser to handle cast expressions followed by lambda expressions.

## Problem Analysis

**Error**: "Expected RIGHT_PARENTHESIS but found ARROW" (18 occurrences in Spring Framework)

When a lambda is cast to a functional interface type, the parser fails:
`(FunctionalInterface) x -> body`

The parser sees `(FunctionalInterface)` as a cast, then tries to parse `x` as the
cast operand, and fails when it sees `->`.

## Example Failing Code

```java
return nonNull(executeWithNativeSession((HibernateCallback<List<T>>) session -> {
    Criteria criteria = session.createCriteria(entityClass);
    return criteria.list();
}));
```

## Tasks

1. [ ] After parsing cast, check if result is followed by `->` (lambda)
2. [ ] If lambda detected, reparse as: cast type + lambda expression
3. [ ] Handle generic types in cast: `(Supplier<String>) () -> "value"`
4. [ ] Add tests for cast + lambda patterns
5. [ ] Verify against Spring Framework error cases

## Technical Approach

In cast expression parsing:
1. Parse `(Type)` as normal cast
2. Parse the operand expression
3. If operand is a simple identifier AND next token is `->`:
   - The identifier is actually the lambda parameter
   - Reparse as lambda with the identifier as parameter
4. Return the cast wrapping the lambda expression

Alternative approach:
- After parsing what looks like a cast `(Type)`, check if followed by identifier + `->`
- If so, parse as lambda and wrap result in cast node

## Verification

- [ ] `(Runnable) () -> doSomething()` parses correctly
- [ ] `(Function<String, Integer>) s -> s.length()` parses correctly
- [ ] `(HibernateCallback<List<T>>) session -> { ... }` parses correctly
- [ ] Normal casts still work: `(String) obj`
- [ ] Spring Framework error count reduced

