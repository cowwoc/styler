# Task Plan: fix-contextual-keyword-method-call

## Objective

Extend contextual keyword handling to method call contexts after the `.` operator.

## Problem Analysis

**Error:** `Expected identifier, 'class', 'this', 'super', or 'new' after '.' but found WITH`
**Occurrences:** 19 in Spring Framework 6.2.1

Also occurs with:
- `TO` - `.to()` method calls
- `REQUIRES` - `.requires()` method calls

Examples:
```java
TestCompiler.forSystem().with(CompilerFiles.from(generatedFiles))
message.to(recipients)
module.requires(dependency)
```

The fix-contextual-keywords-as-identifiers addressed identifier contexts but
the dot operator parsing still rejects contextual keywords as method names.

## Approach

1. Review existing contextual keyword fix (expectIdentifierOrContextualKeyword)
2. Find method call parsing after `.` operator
3. Apply same contextual keyword acceptance
4. Add test cases

## Execution Steps

1. Locate postfix expression parsing (`.` handling)
2. Identify where identifier is expected after dot
3. Use `expectIdentifierOrContextualKeyword()` instead of `expect(IDENTIFIER)`
4. Add tests for `.with()`, `.to()`, `.requires()` patterns
5. Verify all tests pass

## Success Criteria

- [ ] Parser accepts `.with()` method calls
- [ ] Parser accepts `.to()` method calls
- [ ] Parser accepts `.requires()` method calls
- [ ] All existing tests pass
- [ ] New test cases cover method call patterns
