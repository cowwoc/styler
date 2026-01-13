# Plan: split-parser-into-multiple-classes

## Current State
Parser.java has grown to 1519 NCSS lines, exceeding PMD's 1500 line threshold. This causes PMD
violations and indicates the class has too many responsibilities.

## Target State
Parser.java split into multiple focused classes, each under 1000 lines:
- Parser.java - Core parsing orchestration and state management
- ModuleParser.java - Module declaration parsing (module-info.java)
- TypeParser.java - Class/interface/enum/record declarations
- StatementParser.java - Statement parsing (blocks, control flow)
- ExpressionParser.java - Expression parsing

## Rationale
- PMD NcssCount violation blocks clean builds
- Large class is difficult to maintain and understand
- Logical groupings already exist within the code
- SharedSecrets pattern allows helper classes to access package-private state

## Risk Assessment
- **Risk Level:** MEDIUM
- **Breaking Changes:** None - public API unchanged, internal restructure only
- **Mitigation:** Incremental extraction with tests after each step

## SharedSecrets Pattern

Use JDK's SharedSecrets pattern for cross-package access to parser internals:

```java
// In parser package
public final class ParserAccess {
    private static volatile ParserAccessor accessor;

    public interface ParserAccessor {
        NodeArena getArena(Parser parser);
        List<Token> getTokens(Parser parser);
        int getPosition(Parser parser);
        void setPosition(Parser parser, int position);
        // ... other accessors as needed
    }

    public static void setAccessor(ParserAccessor acc) {
        accessor = acc;
    }

    public static ParserAccessor getAccessor() {
        return accessor;
    }
}

// Parser registers its accessor in static initializer
public class Parser {
    static {
        ParserAccess.setAccessor(new ParserAccess.ParserAccessor() {
            @Override
            public NodeArena getArena(Parser p) { return p.arena; }
            // ... implement other methods
        });
    }
}
```

## Dependencies
None - can be executed independently.

## Execution Steps

### Step 1: Create ParserAccess infrastructure
**Files:** parser/src/main/java/io/github/cowwoc/styler/parser/ParserAccess.java
**Action:** Create SharedSecrets accessor interface and registration mechanism
**Verify:** Build compiles: `./mvnw compile -pl parser`
**Done:** ParserAccess class exists with accessor interface

### Step 2: Register accessor in Parser
**Files:** parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java
**Action:** Add static initializer that registers ParserAccessor implementation
**Verify:** Build compiles, tests pass
**Done:** Parser registers its accessor

### Step 3: Extract ModuleParser
**Files:**
- parser/src/main/java/io/github/cowwoc/styler/parser/internal/ModuleParser.java
- parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java
**Action:**
- Create ModuleParser class in internal package
- Move module-related parsing methods
- Use ParserAccess to access Parser state
- Delegate from Parser to ModuleParser
**Verify:** `./mvnw test -pl parser` - all tests pass
**Done:** Module parsing delegated to ModuleParser

### Step 4: Extract TypeParser
**Files:**
- parser/src/main/java/io/github/cowwoc/styler/parser/internal/TypeParser.java
- parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java
**Action:**
- Create TypeParser for class/interface/enum/record declarations
- Move type declaration parsing methods
- Delegate from Parser
**Verify:** All tests pass
**Done:** Type parsing delegated to TypeParser

### Step 5: Extract StatementParser
**Files:**
- parser/src/main/java/io/github/cowwoc/styler/parser/internal/StatementParser.java
- parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java
**Action:**
- Create StatementParser for block and control flow statements
- Move statement parsing methods
- Delegate from Parser
**Verify:** All tests pass
**Done:** Statement parsing delegated to StatementParser

### Step 6: Extract ExpressionParser
**Files:**
- parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java
- parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java
**Action:**
- Create ExpressionParser for expression parsing
- Move expression-related methods
- Delegate from Parser
**Verify:** All tests pass
**Done:** Expression parsing delegated to ExpressionParser

### Step 7: Verify size constraints
**Files:** All parser files
**Action:** Verify each class is under 1000 lines
**Verify:** `./mvnw pmd:check -pl parser` - no NcssCount violations
**Done:** All classes under size threshold, PMD passes

## Acceptance Criteria
- [ ] Parser.java under 1000 NCSS lines
- [ ] All helper classes under 1000 NCSS lines
- [ ] PMD check passes (no NcssCount violations)
- [ ] All existing tests pass
- [ ] No public API changes
- [ ] SharedSecrets pattern correctly implemented
