# Plan: split-parser-into-multiple-classes

## Current State
Parser.java has grown to 1525 NCSS lines, exceeding PMD's 1500 line threshold. ModuleParser already
exists and demonstrates the extraction pattern (takes Parser reference, uses package-private accessors).

## Target State
Parser.java split into multiple focused classes, each under 1000 lines:
- Parser.java - Core parsing orchestration and state management
- ModuleParser.java - Module declaration parsing (ALREADY EXISTS)
- TypeParser.java - Class/interface/enum/record declarations (NEW)
- StatementParser.java - Statement parsing, blocks, control flow (NEW)
- ExpressionParser.java - Expression parsing (NEW)

## Architecture Pattern (Follow Existing ModuleParser)

**DO NOT use SharedSecrets.** Follow the existing ModuleParser pattern:

```java
// Helper class takes Parser reference
final class TypeParser
{
    private final Parser parser;

    TypeParser(Parser parser)
    {
        this.parser = parser;
    }

    // Methods access parser state via package-private accessors
    NodeIndex parseClassDeclaration()
    {
        List<Token> tokens = parser.getTokens();
        int position = parser.getPosition();
        // ...
    }
}
```

Parser exposes these package-private accessors (already exist):
- `getTokens()` - returns token list
- `getPosition()` - returns current position
- `setPosition(int)` - sets position (may need to add)
- `currentToken()` - returns token at current position
- `previousToken()` - returns previous token
- `consume()` - advances and returns consumed token
- `match(TokenType)` - matches and consumes if matches
- `expect(TokenType)` - expects token or throws
- `getArena()` - returns NodeArena

## Dependencies
None - can be executed independently.

## Execution Steps

### Step 1: Extract ExpressionParser (largest group ~800 lines)
**Files:**
- parser/src/main/java/io/github/cowwoc/styler/parser/ExpressionParser.java (NEW)
- parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java (modify)

**Methods to move:**
- parseExpression, parseLambdaBody, parseParenthesizedOrLambda
- parseAssignment, parseTernary, parseBinaryExpression
- parseLogicalOr/And, parseBitwiseOr/Xor/And
- parseEquality, parseRelational, parseShift
- parseAdditive, parseMultiplicative
- parseUnary, parsePostfix, parseDotExpression
- parseArrayAccessOrClassLiteral, parsePrimary
- parseLiteralExpression, parsePrimitiveClassLiteral
- parseNewExpression, parseArrayCreation, parseObjectCreation
- parseArrayInitializer, tryCastExpression, parseCastOperand

**Action:**
1. Create ExpressionParser class following ModuleParser pattern
2. Move expression parsing methods
3. Add field `private final ExpressionParser expressionParser` to Parser
4. Delegate from Parser to ExpressionParser

**Verify:** `./mvnw test -pl parser` - all tests pass
**Done when:** Expression parsing delegated, tests pass

### Step 2: Extract StatementParser (~500 lines)
**Files:**
- parser/src/main/java/io/github/cowwoc/styler/parser/StatementParser.java (NEW)
- parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java (modify)

**Methods to move:**
- parseBlock, parseStatement, parseLabeledStatement
- parseLocalTypeDeclaration, parseBreakStatement, parseContinueStatement
- parseIfStatement, parseForStatement, parseWhileStatement, parseDoWhileStatement
- parseSwitchStatement, parseSwitchExpression, parseCaseLabelElement
- tryParsePrimitiveTypePattern, tryParseTypePattern, parseRecordPattern
- parseReturnStatement, parseThrowStatement, parseYieldStatement
- parseTryStatement, parseCatchClause, parseFinallyClause
- parseResource, parseSynchronizedStatement, parseAssertStatement
- tryParseVariableDeclaration, parseExpressionOrVariableStatement

**Verify:** `./mvnw test -pl parser` - all tests pass
**Done when:** Statement parsing delegated, tests pass

### Step 3: Extract TypeParser (~400 lines)
**Files:**
- parser/src/main/java/io/github/cowwoc/styler/parser/TypeParser.java (NEW)
- parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java (modify)

**Methods to move:**
- parseClassDeclaration, parseInterfaceDeclaration, parseEnumDeclaration
- parseAnnotationDeclaration, parseRecordDeclaration, parseImplicitClassDeclaration
- parseTypeParameters, parseTypeParameter
- parseClassBody, parseEnumBody, parseEnumConstant
- parseMemberDeclaration, parseNestedTypeDeclaration, parseMemberBody
- parseIdentifierMember, parsePrimitiveTypedMember
- parseMethodRest, parseParameter, parseCatchParameter, parseFieldRest

**Verify:** `./mvnw test -pl parser` - all tests pass
**Done when:** Type parsing delegated, tests pass

### Step 4: Add any missing accessors to Parser
**Files:** parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java

**Action:** If any helper class needs an accessor not currently exposed, add it as package-private.
Likely needed:
- `setPosition(int)` for backtracking
- `enterDepth()` / `exitDepth()` for depth tracking

**Verify:** All tests pass
**Done when:** All accessors needed by helper classes are exposed

### Step 5: Verify size constraints and PMD
**Action:** Check each class size and run PMD
**Verify:**
```bash
wc -l parser/src/main/java/io/github/cowwoc/styler/parser/*.java
./mvnw pmd:check -pl parser
```
**Done when:** All classes under threshold, PMD passes

## Acceptance Criteria
- [ ] Parser.java under 1500 NCSS lines (PMD threshold)
- [ ] All helper classes under 1500 NCSS lines
- [ ] PMD check passes (no NcssCount violations)
- [ ] All existing tests pass
- [ ] No public API changes
- [ ] Follows existing ModuleParser pattern (no SharedSecrets)
