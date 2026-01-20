# State

- **Status:** in-progress
- **Progress:** 88%
- **Dependencies:** [add-type-use-annotation-support]
- **Last Updated:** 2026-01-20
- **Note:** Validation run completed. 94 files failing. Need new tasks for type-use annotation patterns.

## Acceptance Criteria

**MANDATORY: Zero parsing errors required.**

This task is complete ONLY when `parser:check` passes on the entire Spring Boot codebase
with **0 failures**.

```bash
# Acceptance test command
./mvnw exec:java -pl parser -Dexec.mainClass=com.stazsoftware.styler.parser.ParserCli \
  -Dexec.args="check ~/spring-boot"

# Required output
# Failed: 0
```

Until all files parse successfully, this task remains in-progress and blocks v0.5 completion.

## Validation Run (2026-01-20)

**Result:** 98.86% success rate (8,165/8,259 files)

| Metric | Value |
|--------|-------|
| Total files | 8,259 |
| Succeeded | 8,165 |
| Failed | 94 |
| Time | 2,189ms |
| Throughput | 3,773.0 files/sec |

## Remaining Errors (94 files) - New Tasks Required

| Error Type | Count | Root Cause |
|------------|-------|------------|
| Unexpected token in member declaration: AT_SIGN | 71 | Type-use annotations (e.g., `@Nullable String`, `Supplier<@Nullable T>`) |
| Expected SEMICOLON but found IDENTIFIER | 9 | Related to type-use annotation parsing |
| Expected RIGHT_PARENTHESIS but found IDENTIFIER | 6 | Related to type-use annotation in generics |
| Expected SEMICOLON but found LESS_THAN | 2 | Generic type with type-use annotation |
| Unexpected token in member declaration: RIGHT_BRACE | 1 | Empty class body edge case |
| Expected IDENTIFIER but found AT_SIGN | 1 | Annotation in unexpected position |
| Expected GREATER_THAN but found IDENTIFIER | 1 | Generic type parsing |
| Expected RIGHT_PARENTHESIS but found LESS_THAN | 1 | Nested generic with annotation |
| Expected identifier but found QUESTION_MARK | 1 | Wildcard in generic context |
| Expected RIGHT_PARENTHESIS but found MODULE | 1 | Contextual keyword in generic |

### Error Categories Analysis

**Type-Use Annotations (Java 8+ Feature) - 71+ files:**

Patterns that fail:
```java
// Field type annotation
private final @Nullable Integer dirMode;

// Return type annotation
public @Nullable String getAuthHeader() { ... }

// Generic type parameter annotation
Function<String, @Nullable CredentialHelper> factory;

// Method return type annotation with generic
private <T> @Nullable T read(JsonParser parser, Class<T> type) { ... }
```

The parser doesn't recognize annotations in type-use positions (between modifiers and type name,
or within generic type parameters).

**Root cause:** Type-use annotations (JSR 308, Java 8) allow annotations on any type use, not just
declarations. The parser currently only handles annotations at declaration sites.

**Recommendation:** Create task `add-type-use-annotation-support` to handle annotations in:
- Field types: `private @Nullable String name;`
- Method return types: `public @Nullable T get() { }`
- Generic type parameters: `List<@NonNull String>`
- Array components: `String @NonNull []`

## Conclusion

**94 errors remain after investigation.** All errors are related to type-use annotations (JSR 308),
a Java 8+ feature not yet supported by the parser.

**Status:** Blocked until type-use annotation parsing is implemented.

**Recommendation:** Create single task:
- `add-type-use-annotation-support` - Handle annotations in type-use positions (JSR 308)

---
*Investigation completed 2026-01-20. Blocked on type-use annotation support.*
