# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** implemented
- **Dependencies:** integrate-expression-parser
- **Completed:** 2026-01-16

## Description

Fix parser errors where `CASE`, `DEFAULT`, `BREAK`, `THROW`, and `WHILE` keywords appear unexpectedly
in expression context. These occur in old-style switch statements where `case X: statement;` patterns
are being parsed incorrectly as expressions.

**Error patterns (14 files):**
- Unexpected CASE in expression (5 files) - e.g., CodeEmitter.java
- Unexpected DEFAULT in expression (3 files) - e.g., SecondMsg.java
- Unexpected BREAK in expression (3 files) - e.g., Tokenizer.java
- Unexpected THROW in expression (2 files) - e.g., ViewControllerBeanDefinitionParser.java
- Unexpected WHILE in expression (1 file) - e.g., ClassReader.java

## Analysis

The parser is treating `case X:` as an expression label context. When a statement follows (like
`break`, `throw`, or another `case`), the parser fails because these keywords cannot start an
expression.

**Root cause:** Expression parser encounters a colon after `case X` or `default` and tries to parse
what follows as a conditional expression continuation, but these are actually labeled statements in
switch blocks.

## Acceptance Criteria

- [x] All 14 affected Spring Framework files parse successfully
- [x] No regression in other Spring Framework files
- [x] Tests added for old-style switch case patterns
