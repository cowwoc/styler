# Task Plan: fix-contextual-keywords-as-identifiers

## Objective

Allow contextual keywords to be used as identifiers outside their special contexts.

## Problem Analysis

**Errors** (35+ occurrences in Spring Framework):
- "Expected identifier... but found WITH" (14 errors)
- "Expected IDENTIFIER but found MODULE" (7 errors)
- "Expected SEMICOLON but found WITH" (6 errors)
- "Expected IDENTIFIER but found VAR" (5+ errors)
- "Expected IDENTIFIER but found TO" (4 errors)
- "Expected IDENTIFIER but found OPEN" (1 error)

These are contextual keywords that should be valid identifiers in non-keyword contexts:
- `with` - only keyword in record patterns
- `module` - only keyword in module-info.java
- `to`, `open`, `requires`, `exports` - only in module declarations
- `var` - only in local variable type inference

## Example Failing Code

```java
// "with" as method name
public Builder with(String value) { return this; }

// "module" as variable name
String module = getModuleName();

// "var" as method parameter (not LVTI context)
public void process(String var) { }
```

## Tasks

1. [ ] Create `isContextualKeyword(TokenType)` helper method
2. [ ] Modify `expectIdentifier()` to accept contextual keywords as identifiers
3. [ ] Ensure contextual keywords still work in their special contexts
4. [ ] Add tests for each contextual keyword used as identifier
5. [ ] Verify module parsing still works correctly

## Contextual Keywords List

| Keyword | Special Context | Valid as Identifier Elsewhere |
|---------|-----------------|------------------------------|
| `var` | Local variable type inference | Yes |
| `with` | Record patterns | Yes |
| `module` | module-info.java | Yes |
| `open` | module-info.java | Yes |
| `to` | module exports/opens | Yes |
| `requires` | module-info.java | Yes |
| `exports` | module-info.java | Yes |
| `opens` | module-info.java | Yes |
| `uses` | module-info.java | Yes |
| `provides` | module-info.java | Yes |
| `transitive` | module requires | Yes |

## Technical Approach

1. Add `isContextualKeyword()` method that returns true for the above tokens
2. In `expectIdentifier()` and similar methods, check for contextual keywords
3. When not in special context, treat contextual keyword as identifier

## Verification

- [ ] `with` as method name works
- [ ] `module` as variable name works
- [ ] `var` as parameter name works
- [ ] Module declarations still parse correctly
- [ ] Record patterns still parse correctly
- [ ] Spring Framework error count reduced

