# Plan: integrate-expression-parser

## Objective
Complete ExpressionParser integration: wire Parser.java delegation, run full tests, cleanup.

## Parent Task
Decomposed from: extract-expression-parser (sequence 6 of 6)

## Scope

### Final Integration Steps

1. **Add ExpressionParser field to Parser.java**
   ```java
   private final ExpressionParser expressionParser;
   ```

2. **Initialize in Parser constructor**
   ```java
   this.expressionParser = new ExpressionParser(parserAccess);
   ```

3. **Implement ParserAccess methods for ExpressionParser needs**
   - Verify all methods used by ExpressionParser exist in ParserAccess
   - Add any missing method implementations to anonymous ParserAccess class

4. **Remove extracted methods from Parser.java**
   - Delete all method bodies that were extracted
   - Keep only parseExpression() as delegation to ExpressionParser

5. **Run full test suite**
   ```bash
   ./mvnw test -pl parser -q
   ```

### Prerequisites
- All 5 extraction subtasks completed
- ExpressionParser has all 48 methods

### Verification

```bash
# Compilation
./mvnw compile -pl parser -q

# Full tests
./mvnw test -pl parser -q

# Verify line counts
wc -l parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java
wc -l parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java
```

Expected:
- Parser.java: ~1400 lines (reduced from ~3850)
- ExpressionParser.java: ~2500 lines

## Acceptance Criteria
- [ ] All parser tests pass
- [ ] Parser.java delegates parseExpression() to ExpressionParser
- [ ] No public API changes
- [ ] Parser.java significantly reduced in size
