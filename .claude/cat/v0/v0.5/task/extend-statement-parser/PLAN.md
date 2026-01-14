# Plan: extend-statement-parser

## Objective
Complete StatementParser by adding all remaining statement parsing methods.
Currently only has try-catch methods (207 lines), needs ~500 lines total.

## Approach
Add remaining statement methods to existing StatementParser.java.
Update to use ParserAccess instead of Parser.

## Files to Modify

### Modify: `parser/src/main/java/io/github/cowwoc/styler/parser/internal/StatementParser.java`

Update constructor to take ParserAccess:
```java
public StatementParser(ParserAccess parser)
{
    this.parser = parser;
}
```

### Methods to Add (~300 lines)

**Statement dispatch:**
- parseStatement, parseLabeledStatement

**Local declarations:**
- isLocalTypeDeclarationStart, parseLocalTypeDeclaration
- skipBalancedParens, skipMemberModifiers

**Control flow:**
- parseBreakStatement, parseContinueStatement
- parseIfStatement, parseForStatement, tryParseEnhancedForHeader
- parseWhileStatement, parseDoWhileStatement

**Switch:**
- parseSwitchStatement, parseCaseLabelElement, parseCaseLabelExpression

**Pattern matching:**
- tryParsePrimitiveTypePattern, tryParseTypePattern
- parseRecordPattern, parseRecordPatternComponents
- parseComponentPattern, parseGuardExpression

**Return/throw/yield:**
- parseReturnStatement, parseThrowStatement, parseYieldStatement

**Other statements:**
- parseSynchronizedStatement, parseAssertStatement
- parseExpressionOrVariableStatement, tryParseVariableDeclaration
- parseAdditionalDeclarators

**Helper methods:**
- looksLikeTypeStart, isContextualKeyword(String)

### Modify: Parser.java

1. Update StatementParser initialization to pass `this` (ParserAccess)
2. Add delegation methods as needed
3. Remove statement parsing method bodies from Parser

## Verification

```bash
./mvnw compile -pl parser -q
./mvnw test -pl parser -q
```

## Acceptance Criteria
- [ ] StatementParser extended with all statement methods
- [ ] StatementParser uses ParserAccess interface
- [ ] Parser delegates to StatementParser
- [ ] All existing tests pass
- [ ] No public API changes
