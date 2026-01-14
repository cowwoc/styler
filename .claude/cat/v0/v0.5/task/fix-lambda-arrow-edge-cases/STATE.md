# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** [fix-lambda-parameter-parsing, fix-cast-lambda-expression]
- **Last Updated:** 2026-01-14

## Error Pattern

**16 occurrences** in Spring Framework 6.2.1

Error: `Expected SEMICOLON but found ARROW`

## Root Cause

Parser fails on certain lambda expression patterns where the arrow (`->`) appears
in an unexpected context. This may relate to:
- Lambda expressions in complex initializers
- Nested lambdas
- Lambdas in method chains

Example files:
- MockServerWebExchange.java
- MockClientHttpRequest.java
- MockServerHttpResponse.java
