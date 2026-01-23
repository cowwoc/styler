# Task Plan: fix-contextual-keywords-in-expressions

## Objective

Allow contextual keywords (var, module, record, etc.) to be used as identifiers in all
expression contexts, not just where explicitly handled.

## Problem Analysis

Contextual keywords are listed in `isContextualKeyword()`:
- VAR, YIELD, MODULE, OPEN, TO, REQUIRES, EXPORTS, OPENS, USES, PROVIDES, WITH, TRANSITIVE

However, `parsePrimary()` and other expression parsing methods don't recognize these
as valid expression starters. When `this.var` is parsed, the parser sees VAR token and
doesn't know it can be an identifier in this context.

Also need to add RECORD and SEALED to the contextual keywords list.

## Affected Code Patterns

```java
this.var = var;           // field access and assignment
module.getResourceAsStream()  // method call on 'module' variable
return with(delegates);   // method call named 'with'
RfcUriParser.UriRecord record  // local variable named 'record'
```

## Approach

1. Add RECORD and SEALED to `isContextualKeyword()` if missing
2. Modify `parsePrimary()` to recognize contextual keywords as expression starters
3. Ensure `parsePostfix()` allows contextual keywords after DOT
4. Check assignment targets allow contextual keywords

## Execution Steps

1. Add test cases for each contextual keyword pattern
2. Update `isContextualKeyword()` to include RECORD, SEALED
3. Update `parsePrimary()` to handle contextual keywords
4. Verify all ~30 affected files parse correctly

## Success Criteria

- [ ] `this.var = var;` parses correctly
- [ ] `module.method()` parses correctly
- [ ] `Type record = value;` parses correctly
- [ ] All 30 Spring Framework files with contextual keyword issues parse
