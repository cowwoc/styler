# Styler - Your style. Their style. Same codebase.

View GitHub PRs in your preferred code style. Work locally in your format while the repo maintains team
standards. Built for developers who think better in their own style.

## 🚀 Quick Start

- **Build & Run**: See [docs/project/build-system.md](docs/project/build-system.md)
- **Development Workflow**: See [docs/project/task-protocol-core.md](docs/project/task-protocol-core.md)
- **Code Style**: See [docs/code-style-human.md](docs/code-style-human.md)

## 📁 Repository Structure

```
.
├── README.md                     # This file
├── CLAUDE.md                     # Claude Code configuration
├── todo.md                       # Active task list
├── docs/                         # All documentation
│   ├── code-style-human.md       # Master human style guide
│   ├── project/                  # Core project documentation
│   │   ├── scope.md              # Project scope & architecture
│   │   ├── task-protocol-core.md # Development workflow (states & agents)
│   │   ├── task-protocol-operations.md # Protocol procedures
│   │   ├── build-system.md       # Build configuration
│   │   └── architecture.md       # Technical architecture
│   └── code-style/               # Code style documentation
│       ├── common-claude.md      # Universal rules - Claude patterns
│       ├── common-human.md       # Universal explanations
│       ├── java-claude.md        # Java-specific - Claude patterns
│       └── java-human.md         # Java explanations & context
├── styler-ast-core/       # AST node hierarchy and visitor pattern
├── styler-parser/         # Custom recursive descent parser for JDK 25
├── styler-formatter-api/  # Formatter plugin interfaces
├── styler-formatter-impl/ # Built-in formatter implementations
├── config/                # TOML configuration system
├── styler-engine/         # Parallel processing engine
├── styler-security/       # Security controls and validation
└── cli/                   # Command-line interface
```

## 📖 Documentation

### Essential Reading
- [docs/project/scope.md](docs/project/scope.md) - Project objectives and architecture
- [docs/code-style-human.md](docs/code-style-human.md) - Coding standards and best practices
- [docs/project/task-protocol-core.md](docs/project/task-protocol-core.md) - Development process

### Development Guides
- [docs/code-style/java-human.md](docs/code-style/java-human.md) - Java best practices
- [docs/code-style/common-human.md](docs/code-style/common-human.md) - Common formatting and validation
- [docs/project/architecture.md](docs/project/architecture.md) - Technical architecture details

## 🎯 Why Styler?

**End style wars forever.** Every developer sees code in their preferred format while the repository
maintains a consistent standard.

- **GitHub PR Extension** - View any PR in your preferred style with smart comment translation
- **AI-Agent Integration** - Structured output that AI coding assistants can understand and act on
- **100+ files/second** - Parallel processing for large codebases
- **100% JDK 25 support** - Pattern matching, records, sealed classes, and more
- **VCS Format Filters** *(coming soon)* - Work in your style locally, commit in repo style

## ⚡ Key Features

### Multi-threaded Processing
- **Performance**: 100-150 files/second on modern multi-core systems
- **Scalability**: Linear performance scaling up to 32 CPU cores
- **Memory Efficiency**: <512MB heap per 1000 files processed
- **Work-Stealing**: Dynamic load balancing across available CPU cores

### AST-Based Formatting
- **Complete Language Support**: All Java language constructs from Java 8 to JDK 25
- **Comment Preservation**: Maintain all comments, whitespace, and formatting hints
- **Incremental Formatting**: Format only changed sections of files
- **Error Recovery**: Graceful handling of malformed or partial Java files

### Security and Safety
- **Sandboxed Execution**: AST-only parsing without code execution capabilities
- **Path Validation**: Comprehensive input validation for all file operations
- **Resource Limits**: File size limits (10MB), memory bounds, and processing timeouts
- **Error Isolation**: Individual file failures don't affect overall processing

### Plugin Architecture
- **Configurable Rules**: Enable/disable specific formatting rules through YAML configuration
- **Custom Extensions**: Support for user-defined formatting rules and auto-fixers
- **Conflict Resolution**: Handle conflicts between competing formatting rules
- **Performance Optimization**: Efficient plugin loading and rule application

## 🔧 Usage

### Command-Line Interface
```bash
# Format all Java files in current directory
styler format .

# Check formatting without making changes
styler check src/

# Format with specific configuration
styler format --config .styler.yml src/

# Format using predefined style profile
styler format --profile google src/
```

### Configuration

Styler uses [TOML](https://toml.io/) configuration files for formatting rules. Create a `.styler.toml` file in
your project root:

```toml
# Maximum line length before wrapping
maxLineLength = 120
```

Configuration files are discovered hierarchically:
1. Current directory and parents (up to .git boundary)
2. User home directory (`~/.styler.toml`)
3. System-wide (`/etc/.styler.toml`)

Multiple config files are merged with **field-level precedence** - each field uses the value from the nearest
config file that specifies it.

**For complete configuration documentation**, see [docs/configuration.md](docs/configuration.md).

## 🚀 Getting Started

1. **Read the scope**: [docs/project/scope.md](docs/project/scope.md)
2. **Follow the workflow**: [docs/project/task-protocol-core.md](docs/project/task-protocol-core.md)
3. **Setup your environment**: [docs/project/build-system.md](docs/project/build-system.md)
4. **Review coding standards**: [docs/code-style-human.md](docs/code-style-human.md)

## 📋 Active Development

Current tasks are tracked in [todo.md](todo.md). Development follows a strict 7-phase workflow process
documented in [docs/project/task-protocol-core.md](docs/project/task-protocol-core.md).

## 🏗️ Architecture

Styler uses a modular architecture with the following components:

- **styler-ast-core**: Immutable AST node hierarchy with visitor pattern
- **styler-parser**: Custom recursive descent parser with JDK 25 support
- **styler-formatter-api**: Plugin interfaces and configuration schema
- **styler-formatter-impl**: Built-in formatter implementations
- **config**: TOML configuration system with hierarchical discovery
- **styler-engine**: Multi-threaded processing with work-stealing
- **styler-security**: Comprehensive security controls and input validation
- **cli**: Command-line interface and main entry point

For detailed technical architecture, see [docs/project/architecture.md](docs/project/architecture.md).