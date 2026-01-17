# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** none
- **Last Updated:** 2026-01-17

## Context

Created from validate-spring-framework-parsing findings. 14 files fail with unexpected
CASE/DEFAULT/BREAK/THROW/WHILE tokens in expression context.

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
