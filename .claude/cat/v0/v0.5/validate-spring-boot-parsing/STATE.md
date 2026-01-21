# State

- **Status:** in-progress
- **Progress:** 99%
- **Dependencies:** [add-type-use-annotation-support]
- **Last Updated:** 2026-01-20
- **Note:** Type-use annotation fix reduced failures from 94 to 22. Remaining issues are advanced patterns.

## Acceptance Criteria

**MANDATORY: Zero parsing errors required.**

This task is complete ONLY when `parser:check` passes on the entire Spring Boot codebase
with **0 failures**.

Until all files parse successfully, this task remains in-progress and blocks v0.5 completion.

## Validation Run (2026-01-20, post type-use fix)

**Result:** 99.73% success rate (8,237/8,259 files)

| Metric | Value |
|--------|-------|
| Total files | 8,259 |
| Succeeded | 8,237 |
| Failed | 22 |
| Time | 2,860ms |
| Throughput | 2,887.8 files/sec |

**Improvement:** Type-use annotation support fixed 72 files (94 → 22 failures).

## Remaining Errors (22 files) - New Tasks Required

| Error Pattern | Count | Example | Root Cause |
|---------------|-------|---------|------------|
| Type-use annotation in wildcard | ~7 | `Consumer<@Nullable ? super T>` | Annotation before `?` wildcard |
| Type-use annotation after qualified type | ~5 | `java.security.@Nullable Principal` | Annotation after package path |
| Pattern matching instanceof | ~4 | `input instanceof Module module` | Java 16+ feature not supported |
| Type-use annotation with bounds | ~3 | `@Nullable P extends @Nullable Object` | Annotation in type parameter bounds |
| Unexpected RIGHT_BRACE | ~1 | Empty anonymous class with comment | Edge case in class body parsing |
| Generic type with annotation in multiple positions | ~2 | Complex nested generics | Multiple annotation positions |

### Example Files and Patterns

**1. Wildcard type-use annotation:**
```java
// PropertyMapper.java:433
public void to(Consumer<@Nullable ? super T> consumer) { ... }
```

**2. Qualified type annotation:**
```java
// HttpExchange.java:172
Supplier<java.security.@Nullable Principal> principalSupplier
```

**3. Pattern matching instanceof (Java 16+):**
```java
// BomExtension.java:338
(input instanceof Module module) ? module : new Module((String) input)
```

**4. Type parameter bounds with annotation:**
```java
// KotlinxSerializationJsonPropertiesTests.java:91
interface Accessor<S, @Nullable P extends @Nullable Object> { ... }
```

**5. Multiple annotation positions in qualified type:**
```java
// BootZipCopyAction.java:194
private LoaderZipEntries.@Nullable WrittenEntries writtenLoaderEntries;
```

## Recommended New Tasks

Based on the remaining 22 failures, these new parser features are needed:

1. **add-wildcard-type-annotation-support** - Handle `@Annotation ?` in generic wildcards
2. **add-qualified-type-annotation-support** - Handle `pkg.@Annotation Type` pattern
3. **add-pattern-matching-instanceof-support** - Handle `expr instanceof Type name` (Java 16+)
4. **add-type-parameter-bound-annotations** - Handle annotations on type bounds

## Previous Validation (before type-use fix)

| Metric | Value |
|--------|-------|
| Total files | 8,259 |
| Succeeded | 8,165 |
| Failed | 94 |
| Success rate | 98.86% |

---
*Updated 2026-01-20 after add-type-use-annotation-support merge.*
