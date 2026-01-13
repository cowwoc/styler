# Task Plan: implement-core-parser

## Objective

Build the Java parser with lexer, token types, and recursive descent parsing for JDK 25 syntax.

## Context

Foundation for all formatting operations. Parser must handle 100% of JDK 25 language features.

## Tasks

1. Implement Lexer with all Java token types
2. Implement recursive descent Parser
3. Create ParseResult sealed type for success/failure handling

## Verification

- [ ] All Java keywords recognized
- [ ] All operators tokenized correctly
- [ ] Basic Java files parse successfully

## Files

- `parser/src/main/java/.../parser/Lexer.java`
- `parser/src/main/java/.../parser/Parser.java`
- `parser/src/main/java/.../parser/Token.java`
- `parser/src/main/java/.../parser/TokenType.java`

