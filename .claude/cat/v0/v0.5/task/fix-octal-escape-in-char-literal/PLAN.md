# Task Plan: fix-octal-escape-in-char-literal

## Objective

Fix parsing of octal escape sequences in character literals.

## Problem Analysis

Java supports octal escapes in character and string literals:
```java
'\0'      // null character
'\7'      // bell
'\12'     // newline
'\013'    // vertical tab (3-digit octal)
'\377'    // max octal (255)
```

The lexer doesn't handle multi-digit octal escapes correctly.
It sees `\0` but then treats the following digits as a new token.

## Affected Files

- `spring-web/.../JavaScriptUtilsTests.java` - `'\013'`
- `spring-web/.../JavaScriptUtils.java` - `'\013'`

## Approach

Update character literal lexing to handle octal escape sequences with
up to 3 octal digits after backslash.

## Execution Steps

1. Add test cases for octal escapes: `'\0'`, `'\12'`, `'\013'`, `'\377'`
2. Locate character literal lexing in Lexer.java
3. Add octal escape handling (1-3 octal digits)
4. Verify both affected files parse correctly

## Success Criteria

- [ ] `'\0'` through `'\377'` parse as character literals
- [ ] Both Spring Framework files parse correctly
