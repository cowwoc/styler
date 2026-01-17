# Styler - Your style. Their style. Same codebase.

View GitHub PRs in your preferred code style. Work locally in your format while the repo maintains team
standards. Built for developers who think better in their own style.

## 🚀 Quick Start

- **Build & Run**: See [docs/project/build-system.md](docs/project/build-system.md)
- **Development Workflow**: See [docs/project/scope.md](docs/project/scope.md)
- **Code Style**: See [docs/code-style-human.md](docs/code-style-human.md)

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
2. **Setup your environment**: [docs/project/build-system.md](docs/project/build-system.md)
3. **Review coding standards**: [docs/code-style-human.md](docs/code-style-human.md)

## 📋 Active Development

Development progress is tracked in the [.claude/cat](.claude/cat) directory using the CAT (Claude
Agentic Tasks) planning framework.

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

## 📄 License

Styler is source-available software under a custom commercial license.

**Personal Use**: Free for personal, educational, research, and hobby projects.

**Commercial Use**: Requires a paid license. This includes use by for-profit companies, use in commercial
products/services, and use by organizations with annual revenue exceeding $100,000 USD.

**Runtime Redistribution**: If your product or service invokes Styler at runtime for end users (e.g., a
SaaS formatting service, hosted IDE), each end user needs their own license. Internal tooling that doesn't
expose Styler to end users does not require licenses for your end users.

**Derivative Works**: Commercial use of forks or derivatives also requires a license from the original
author (not the fork maintainer).

See [LICENSE.md](LICENSE.md) for complete terms. For commercial licensing inquiries, contact the project
maintainers.