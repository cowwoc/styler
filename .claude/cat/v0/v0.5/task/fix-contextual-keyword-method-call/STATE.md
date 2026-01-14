# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** [fix-contextual-keywords-as-identifiers]
- **Last Updated:** 2026-01-14

## Error Pattern

**19 occurrences** in Spring Framework 6.2.1

Error: `Expected identifier, 'class', 'this', 'super', or 'new' after '.' but found WITH`

## Root Cause

Parser fails when contextual keywords (with, to, requires, etc.) are used as
method names in method invocations:

```java
TestCompiler.forSystem().with(CompilerFiles.from(generatedFiles))
//                       ^^^^ 'with' is a contextual keyword
```

The fix-contextual-keywords-as-identifiers task addressed variable/field names
but did not extend to method call contexts after `.` operator.
