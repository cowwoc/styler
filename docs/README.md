# Documentation Directory Structure

This directory contains all project documentation organized hierarchically for efficient navigation.

## 📁 Directory Structure

```
.
├── code-style-human.md                    # Master human style guide
├── README.md                              # This file
├── project/                               # Core project documentation
│   ├── scope.md                           # Project scope & constraints
│   ├── task-protocol-core.md              # State machine protocol (states & agents)
│   ├── task-protocol-operations.md        # Protocol procedures & operations
│   ├── build-system.md                    # Build configuration
│   ├── architecture.md                    # Application architecture
│   ├── critical-rules.md                  # Build integrity rules
│   └── scope/out-of-scope.md              # Prohibited technologies
└── code-style/                            # Code style documentation (flatter structure)
    ├── common-claude.md                   # Universal rules - Claude detection patterns
    ├── common-human.md                    # Universal explanations - Human context
    ├── java-claude.md                     # Java-specific rules - Claude detection patterns
    ├── java-human.md                      # Java explanations & parser context
    ├── typescript-claude.md               # TypeScript-specific rules - Claude detection
    └── typescript-human.md                # TypeScript explanations & type safety
```

## 🎯 Navigation Guide

### Start Here
- **[code-style-human.md](code-style-human.md)** - Main human-readable style guide with navigation

### By Category
-  **Common Rules**: [code-style/common-human.md](code-style/common-human.md) (human) /
  [code-style/common-claude.md](code-style/common-claude.md) (Claude)
-  **Java**: [code-style/java-human.md](code-style/java-human.md) (human) /
  [code-style/java-claude.md](code-style/java-claude.md) (Claude)
-  **TypeScript**: [code-style/typescript-human.md](code-style/typescript-human.md) (human) /
  [code-style/typescript-claude.md](code-style/typescript-claude.md) (Claude)

### By Task
-  **TypeScript development** → [code-style/typescript-human.md](code-style/typescript-human.md) (human) /
  [code-style/typescript-claude.md](code-style/typescript-claude.md) (Claude)
-  **Java development** → [code-style/java-human.md](code-style/java-human.md) (human) /
  [code-style/java-claude.md](code-style/java-claude.md) (Claude)
-  **Common formatting & validation** → [code-style/common-human.md](code-style/common-human.md) (human) /
  [code-style/common-claude.md](code-style/common-claude.md) (Claude)

## 🔗 Cross-References

All documents contain "See also" sections linking to related guidelines using relative paths within this
hierarchy.
