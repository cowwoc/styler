# Styler - Java Code Formatter

An unopinionated Java code formatter that supports 100% of JDK 25's features with multi-threaded file processing capabilities.

## 🚀 Quick Start

- **Build & Run**: See [docs/project/build-system.md](docs/project/build-system.md)
- **Development Workflow**: See [docs/project/task-protocol.md](docs/project/task-protocol.md)
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
│   │   ├── task-protocol.md      # Development workflow
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
├── styler-config/         # YAML configuration system
├── styler-engine/         # Parallel processing engine
├── styler-security/       # Security controls and validation
└── styler-cli/            # Command-line interface
```

## 📖 Documentation

### Essential Reading
- [docs/project/scope.md](docs/project/scope.md) - Project objectives and architecture
- [docs/code-style-human.md](docs/code-style-human.md) - Coding standards and best practices
- [docs/project/task-protocol.md](docs/project/task-protocol.md) - Development process

### Development Guides
- [docs/code-style/java-human.md](docs/code-style/java-human.md) - Java best practices
- [docs/code-style/common-human.md](docs/code-style/common-human.md) - Common formatting and validation
- [docs/project/architecture.md](docs/project/architecture.md) - Technical architecture details

## 🎯 Project Focus

**Unopinionated Java Code Formatting** with:
- 100% JDK 25 feature support (pattern matching, string templates, unnamed classes)
- Multi-threaded file processing for large codebases
- Plugin architecture for configurable formatting rules
- Auto-fixers for common code style violations
- Preservation of developer intent and comment formatting

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
Create a `.styler.yml` file in your project root:

```yaml
# Core settings
language_version: JDK_25
thread_pool_size: auto  # Auto-detect CPU cores
memory_limit: 512MB     # Per 1000 files

# Formatting rules
line_length: 120
indentation:
  type: spaces
  size: 4
braces:
  classes: next_line
  methods: next_line
  control: same_line

# Auto-fixer settings
auto_fixes:
  enabled: true
  line_length: true
  import_organization: true
  whitespace: true

# Plugin system
plugins:
  - name: line-length-formatter
    enabled: true
  - name: import-organizer
    enabled: true
```

## 🚀 Getting Started

1. **Read the scope**: [docs/project/scope.md](docs/project/scope.md)
2. **Follow the workflow**: [docs/project/task-protocol.md](docs/project/task-protocol.md)
3. **Setup your environment**: [docs/project/build-system.md](docs/project/build-system.md)
4. **Review coding standards**: [docs/code-style-human.md](docs/code-style-human.md)

## 📋 Active Development

Current tasks are tracked in [todo.md](todo.md). Development follows a strict 7-phase workflow process documented in [docs/project/task-protocol.md](docs/project/task-protocol.md).

## 🏗️ Architecture

Styler uses a modular architecture with the following components:

- **styler-ast-core**: Immutable AST node hierarchy with visitor pattern
- **styler-parser**: Custom recursive descent parser with JDK 25 support
- **styler-formatter-api**: Plugin interfaces and configuration schema
- **styler-formatter-impl**: Built-in formatter implementations
- **styler-config**: YAML configuration system with inheritance
- **styler-engine**: Multi-threaded processing with work-stealing
- **styler-security**: Comprehensive security controls and input validation
- **styler-cli**: Command-line interface and main entry point

For detailed technical architecture, see [docs/project/architecture.md](docs/project/architecture.md).