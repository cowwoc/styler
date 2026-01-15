---
paths:
  - "**/*.java"
---

# Requirements API Usage Guide

This project uses the [cowwoc/requirements](https://github.com/cowwoc/requirements.java) library for
validation. This guide covers correct API usage patterns.

## Entry Points: requireThat vs assert that

```java
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;
```

### When to Use Which

| Context | Entry Point | Behavior |
|---------|-------------|----------|
| Public API (constructors, methods) | `requireThat()` | Always validates, throws on failure |
| Internal types (package-private, records) | `assert that().elseThrow()` | Only validates with `-ea` flag |
| Test assertions | `requireThat()` | Better error messages than TestNG assertions |

### Examples

```java
// Public API - always validates
public void process(String input)
{
    requireThat(input, "input").isNotNull();
}

// Internal record - validates only with -ea
record InternalData(String value)
{
    InternalData
    {
        assert that(value, "value").isNotNull().elseThrow();
    }
}

// Test assertion - replaces manual if-throw
@Test
public void shouldParseValidInput()
{
    Result result = parser.parse(input);
    requireThat(result.isSuccess(), "result.isSuccess()").isTrue();
}
```

## Common Validator Methods

### Object Validators
| Method | Description |
|--------|-------------|
| `isNull()` | Value must be null |
| `isNotNull()` | Value must not be null |
| `isEqualTo(expected)` | Value must equal expected (using `.equals()`) |
| `isNotEqualTo(other)` | Value must not equal other |
| `isSameReferenceAs(expected)` | Value must be same reference (`==`) |
| `isNotSameReferenceAs(other)` | Value must not be same reference |
| `isInstanceOf(type)` | Value must be instance of type |

### String Validators
| Method | Description | Implies |
|--------|-------------|---------|
| `isNotNull()` | String must not be null | - |
| `isNotEmpty()` | String must not be null or empty (`""`) | `isNotNull()` |
| `isNotBlank()` | String must not be null, empty, or whitespace-only | `isNotEmpty()` |
| `isEmpty()` | String must be empty (`""`) | - |
| `isBlank()` | String must be null, empty, or whitespace-only | - |
| `startsWith(prefix)` | String must start with prefix | - |
| `endsWith(suffix)` | String must end with suffix | - |
| `contains(substring)` | String must contain substring | - |
| `doesNotContain(substring)` | String must not contain substring | - |
| `matches(regex)` | String must match regex pattern | - |
| `length()` | Returns validator for string length | - |

### Number Validators (Integer, Long, Double, etc.)
| Method | Description |
|--------|-------------|
| `isPositive()` | Value must be > 0 |
| `isNotPositive()` | Value must be <= 0 |
| `isNegative()` | Value must be < 0 |
| `isNotNegative()` | Value must be >= 0 |
| `isZero()` | Value must be 0 |
| `isNotZero()` | Value must not be 0 |
| `isLessThan(value)` | Value must be < value |
| `isLessThanOrEqualTo(value)` | Value must be <= value |
| `isGreaterThan(value)` | Value must be > value |
| `isGreaterThanOrEqualTo(value)` | Value must be >= value |
| `isBetween(min, max)` | Value must be in range [min, max] |

### Collection Validators (List, Set, Collection)
| Method | Description | Implies |
|--------|-------------|---------|
| `isNotNull()` | Collection must not be null | - |
| `isNotEmpty()` | Collection must not be null or empty | `isNotNull()` |
| `isEmpty()` | Collection must be empty | - |
| `contains(element)` | Collection must contain element | - |
| `containsAll(elements)` | Collection must contain all elements | - |
| `doesNotContain(element)` | Collection must not contain element | - |
| `size()` | Returns validator for collection size | - |

### Comparable Validators (Duration, Instant, etc.)
| Method | Description |
|--------|-------------|
| `isLessThan(value)` | Value must be < value |
| `isLessThanOrEqualTo(value)` | Value must be <= value |
| `isGreaterThan(value)` | Value must be > value |
| `isGreaterThanOrEqualTo(value)` | Value must be >= value |
| `isBetween(min, max)` | Value must be in range [min, max] |

### Boolean Validators
| Method | Description |
|--------|-------------|
| `isTrue()` | Value must be true |
| `isFalse()` | Value must be false |

## Validation Patterns

### Chain Validators (Don't Repeat)
```java
// ❌ WRONG - Separate calls for same parameter
requireThat(x, "x").isPositive();
requireThat(x, "x").isLessThan(100);

// ✅ CORRECT - Single chain
requireThat(x, "x").isPositive().isLessThan(100);
```

### Don't Chain Redundant Validators
Some validators imply others - don't chain both:
```java
// ❌ WRONG - isNotEmpty() already implies isNotNull()
requireThat(name, "name").isNotNull().isNotEmpty();

// ✅ CORRECT - isNotEmpty() is sufficient
requireThat(name, "name").isNotEmpty();

// ❌ WRONG - isNotBlank() already implies isNotEmpty()
requireThat(name, "name").isNotEmpty().isNotBlank();

// ✅ CORRECT - isNotBlank() is sufficient
requireThat(name, "name").isNotBlank();
```

### Don't Validate Before Delegating
If a method you're calling already validates the parameter, don't validate it again:
```java
// ❌ WRONG - validateIndex() already checks isNotNull()
requireThat(index, "index").isNotNull();
validateIndex(index);

// ✅ CORRECT - Let validateIndex() handle validation
validateIndex(index);
```

### Trust Natural NPE
Don't check null before calling methods that would throw NPE anyway:
```java
// ❌ WRONG - Unnecessary null check
requireThat(token, "token").isNotNull();
int length = token.length();  // Would NPE naturally

// ✅ CORRECT - Let method call throw NPE if null
int length = token.length();
```

### Add Context for Derived Values
When validating method results or computed values, add context for better error messages:
```java
// ❌ WRONG - Unhelpful error: "root.isValid() must be true"
requireThat(success.rootNode().isValid(), "root.isValid()").isTrue();

// ✅ CORRECT - Includes diagnostic context
requireThat(success.rootNode().isValid(), "root.isValid()").
    withContext(success.rootNode(), "rootNode").
    isTrue();
```

**When to use `withContext()`:**
- Validating method return values (e.g., `x.isValid()`, `list.isEmpty()`)
- Validating computed/derived values
- When the parameter name alone won't help diagnose failures

**What to include in context:**
- The object being validated (for inspecting its state)
- Related values that help understand the failure
- Source code or input that led to the value

## Test Assertions

### Use requireThat() Instead of TestNG Assertions
```java
// ❌ WRONG - TestNG assertion with poor error message
assertEquals(actual.size(), 3);

// ✅ CORRECT - requireThat with descriptive parameter name
requireThat(actual.size(), "actual.size()").isEqualTo(3);
```

### Parser Test Pattern
Parser tests MUST compare actual AST to expected AST:
```java
// ❌ WRONG - Only verifies parsing succeeded, NOT AST correctness
requireThat(actual, "actual").isNotNull();

// ❌ WRONG - Tests nothing about specific node types
requireThat(actual.getNodes(), "actual.getNodes()").isNotEmpty();

// ✅ CORRECT - Compares full AST structure
requireThat(actual, "actual").isEqualTo(expected);
```

### Duration Comparisons
```java
Instant startTime = Instant.now();
doWork();
Duration elapsed = Duration.between(startTime, Instant.now());

// ✅ CORRECT - requireThat with Duration
requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(5));
```

## Documentation Terminology

When documenting validation in JavaDoc, use "empty" not "blank":
```java
// ❌ WRONG - Uses "blank" in documentation
/**
 * @throws IllegalArgumentException if {@code name} is blank
 */

// ✅ CORRECT - Uses "empty" for user-facing documentation
/**
 * @throws IllegalArgumentException if {@code name} is empty
 */
```

**Note**: The method call `isNotBlank()` remains unchanged - only the documentation terminology changes.
