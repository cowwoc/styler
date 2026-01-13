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

## Architecture Pattern (Internal Package)

**Use non-exported internal package**, not package-private in same package:

```java
// In io.github.cowwoc.styler.parser.internal package
package io.github.cowwoc.styler.parser.internal;

public final class TypeParser
{
    private final Parser parser;

    public TypeParser(Parser parser)
    {
        this.parser = parser;
    }

    public NodeIndex parseClassDeclaration()
    {
        // Public methods - cleaner API, directly testable
    }
}
```

**In module-info.java:**
```java
module io.github.cowwoc.styler.parser {
    exports io.github.cowwoc.styler.parser;
    // io.github.cowwoc.styler.parser.internal NOT exported
}
```

**Benefits over package-private:**
- Public classes/methods are directly testable
- Module system prevents external access (stronger than package-private)
- Cleaner internal API design
- IDE autocomplete works within module

Parser exposes public accessors for internal package use:
- `getTokens()` - returns token list
- `getPosition()` / `setPosition(int)` - position management
- `currentToken()` / `previousToken()` - token access
- `consume()` / `match(TokenType)` / `expect(TokenType)` - token consumption
- `getArena()` - returns NodeArena

## Dependencies
None - can be executed independently.

## Execution Steps

### Step 1: Extract ExpressionParser (largest group ~800 lines)
**Files:**
- parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java (NEW)
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
- parser/src/main/java/io/github/cowwoc/styler/parser/internal/StatementParser.java (NEW)
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
- parser/src/main/java/io/github/cowwoc/styler/parser/internal/TypeParser.java (NEW)
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

**Action:** Make accessors public for use by internal package classes.
Likely needed:
- `setPosition(int)` for backtracking
- `enterDepth()` / `exitDepth()` for depth tracking

**Verify:** All tests pass
**Done when:** All accessors needed by internal classes are public

### Step 5: Update module-info.java
**Files:** parser/src/main/java/module-info.java

**Action:** Ensure internal package is NOT exported:
```java
module io.github.cowwoc.styler.parser {
    exports io.github.cowwoc.styler.parser;
    // io.github.cowwoc.styler.parser.internal NOT exported
}
```

**Verify:** External code cannot access internal classes
**Done when:** Module properly configured

### Step 6: Verify size constraints and PMD
**Action:** Check each class size and run PMD
**Verify:**
```bash
wc -l parser/src/main/java/io/github/cowwoc/styler/parser/*.java
wc -l parser/src/main/java/io/github/cowwoc/styler/parser/internal/*.java
./mvnw pmd:check -pl parser
```
**Done when:** All classes under threshold, PMD passes

## Acceptance Criteria
- [ ] Parser.java under 1500 NCSS lines (PMD threshold)
- [ ] All internal classes under 1500 NCSS lines
- [ ] PMD check passes (no NcssCount violations)
- [ ] All existing tests pass
- [ ] No public API changes
- [ ] Internal classes in non-exported internal package (not package-private)
