# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** []
- **Last Updated:** 2026-01-14

## Error Pattern

**2 occurrences** in Spring Framework 6.2.1

Error: `Expected RIGHT_PARENTHESIS but found INTEGER_LITERAL`

Example code:
```java
sb.append('\013');  // vertical tab (octal)
if (c == '\013') {  // vertical tab check
```

The lexer doesn't handle octal escape sequences in character literals.
It sees `\0` followed by `13` which becomes INTEGER_LITERAL.
