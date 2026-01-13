# Task Plan: implement-security-framework

## Objective

Build security framework with resource limits and input validation.

## Context

Formatter must handle untrusted input safely. Need limits on file size,
nesting depth, and processing time to prevent DoS attacks.

## Tasks

1. Create SecurityConfig with configurable limits
2. Implement SecurityValidator for input validation
3. Add ExecutionTimeoutException for time limit enforcement
4. Integrate with Parser for depth limiting

## Verification

- [ ] File size limits enforced
- [ ] Nesting depth limits enforced
- [ ] Processing timeout works
- [ ] Malformed input rejected gracefully

## Files

- `security/src/main/java/.../security/SecurityConfig.java`
- `security/src/main/java/.../security/SecurityValidator.java`
- `security/src/main/java/.../security/ExecutionTimeoutException.java`

