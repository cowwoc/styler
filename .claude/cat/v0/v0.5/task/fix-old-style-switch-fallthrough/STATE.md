# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** duplicate
- **Duplicate Of:** v0.5-fix-switch-case-in-expression-context
- **Dependencies:** none
- **Completed:** 2026-01-17

## Context

Created from validate-spring-framework-parsing findings. 14 files fail with unexpected
CASE/DEFAULT/BREAK/THROW/WHILE tokens in expression context.

**Resolution:** This task is a duplicate of `fix-switch-case-in-expression-context` (commit 4d5b77b,
completed 2026-01-16). The same fix was already implemented: `parseCaseLabelExpression()` calls
`parseLogicalOr()` instead of `parseAssignment()` to prevent the case label COLON from being
interpreted as a ternary operator.

Tests exist and pass: `OldStyleSwitchCaseParserTest.java` (7 tests covering CASE, DEFAULT, BREAK,
THROW, WHILE keywords after case label colon).

## Files Affected

- spring-core/src/main/java/org/springframework/cglib/core/CodeEmitter.java
- spring-core/src/main/java/org/springframework/cglib/core/EmitUtils.java
- spring-core/src/main/java/org/springframework/asm/ClassReader.java
- spring-expression/src/main/java/org/springframework/expression/spel/standard/Tokenizer.java
- spring-expression/src/main/java/org/springframework/expression/spel/ast/Operator.java
- spring-web/src/main/java/org/springframework/web/util/RfcUriParser.java
- spring-webmvc/.../ViewControllerBeanDefinitionParser.java
- spring-websocket/.../ConcurrentWebSocketSessionDecorator.java
- spring-messaging/src/test/.../Msg.java (protobuf generated)
- spring-messaging/src/test/.../SecondMsg.java (protobuf generated)
- (and 4 more similar files)
