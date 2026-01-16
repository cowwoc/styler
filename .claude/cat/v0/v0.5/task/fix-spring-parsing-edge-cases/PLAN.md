# Plan: fix-spring-parsing-edge-cases

## Problem

Parser fails on 4 Spring Framework files with unique edge case patterns not covered by previous fixes.

## Reproduction Code

### 1. Block comment in type declaration (BridgeMethodResolver.java)

```java
// Error: Expected SEMICOLON but found BLOCK_COMMENT
private final Map/* <Class, Set<Signature> */declToBridge;
public Map/*<Signature, Signature>*/resolveAll() { }
```

### 2. Array initialization in class body (SpringJUnit4ConcurrencyTests.java)

```java
// Error: Unexpected RIGHT_BRACE in expression
private final Class<?>[] testClasses = new Class<?>[] {
    SpringJUnit4ClassRunnerAppCtxTests.class,
    InheritedConfigSpringJUnit4ClassRunnerAppCtxTests.class,
};  // trailing comma before }
```

### 3. Lambda in generic method call (RouterFunctionsTests.java)

```java
// Error: Expected SEMICOLON but found ARROW
RouterFunction<ServerResponse> routerFunction = request -> Mono.just(handlerFunction);
```

### 4. Generic type in complex expression (SpelCompilationCoverageTests.java)

```java
// Error: Expected SEMICOLON but found LESS_THAN
Map<String, Integer> map = Map.of("a", 13, "b", 42);
expression.getValue(map, Integer.class);
```

## Expected vs Actual

- **Expected:** All 4 files parse successfully
- **Actual:** Parser errors at specific patterns shown above

## Root Cause

These are edge cases in specific parsing contexts:
1. Block comments between type and identifier
2. Array initializer with trailing comma in field declaration
3. Lambda in variable declaration context
4. Generic type after method call chain

## Risk Assessment

- **Risk Level:** MEDIUM
- **Regression Risk:** May affect valid code patterns if fixes are too broad
- **Mitigation:** Add specific tests for each pattern

## Files to Modify

- `parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java` - Handle block comments in types
- `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java` - Fix lambda/generic cases

## Test Cases

- [ ] Block comment in type: `Map/* comment */var;` parses
- [ ] Array with trailing comma: `new T[] { a, b, }` parses
- [ ] Lambda variable: `Func f = x -> y;` parses
- [ ] Generic method chain: `Map.of().get()` parses

## Execution Steps

1. **Add test cases:** Create parser tests for each reproduction pattern
2. **Fix block comment handling:** Allow `/*...*/` between type and identifier
3. **Verify array initializer:** Ensure trailing comma works in field context
4. **Verify lambda/generic:** Confirm recent lambda fixes cover these cases
5. **Run validation:** Test against full Spring Framework
