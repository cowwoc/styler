# Configuration Guide

Styler uses [TOML](https://toml.io/en/v1.0.0) configuration files to customize formatting rules. Configuration
files are discovered hierarchically and merged with field-level precedence.

## Configuration Files

Styler looks for `.styler.toml` files in the following locations (in order of precedence):

1. **Current directory and parents** (up to git repository boundary)
2. **User home directory** (`~/.styler.toml`)
3. **System-wide** (`/etc/.styler.toml`)

### Discovery Process

When Styler starts, it searches for configuration files in this order:

```
1. Current directory: /path/to/your/project/.styler.toml
2. Parent directories: /path/to/your/.styler.toml
                       /path/to/.styler.toml
                       /path/.styler.toml
   (stops at .git boundary)
3. User home:        ~/.styler.toml
4. System-wide:      /etc/.styler.toml
```

**Git Boundary Protection**: The search stops when it encounters a `.git` directory. This prevents
configuration from leaking across repository boundaries. For example, if you're working in a nested git
repository, Styler won't read the parent repository's configuration.

## Merging Strategy

Styler uses **field-level precedence** when merging multiple configuration files. This means:

- Each configuration field is resolved independently
- For each field, the value from the **nearest** (highest precedence) config file wins
- Files are never merged wholesale - only individual fields

### Example

Given these configuration files:

**~/.styler.toml** (user home):
```toml
maxLineLength = 100
```

**project/.styler.toml** (project root):
```toml
# maxLineLength not specified - will use user's preference
indentSize = 4
```

The **merged configuration** will be:
- `maxLineLength = 100` (from `~/.styler.toml`, since project config doesn't specify it)
- `indentSize = 4` (from `project/.styler.toml`)

This allows project-specific overrides while preserving user preferences for unspecified fields.

### Precedence Rules

1. **Project config beats user config**: If both files specify `maxLineLength`, the project value wins
2. **User config beats system config**: User preferences override system defaults
3. **Nearest config wins**: `/path/to/project/.styler.toml` beats `/path/to/.styler.toml`

## Configuration Options

### Formatting Rules

#### maxLineLength
Maximum line length before wrapping.

- **Type**: Integer
- **Default**: 120
- **Constraint**: Must be positive

**Example**:
```toml
maxLineLength = 100
```

## Common Scenarios

### Project-Specific Formatting
Create `.styler.toml` in your project root:
```toml
maxLineLength = 80  # Override for this project only
```

### User-Wide Preferences
Create `~/.styler.toml`:
```toml
maxLineLength = 120  # Your personal preference
```

Projects without `.styler.toml` will use your user settings.

### System Defaults
System administrators can create `/etc/.styler.toml`:
```toml
maxLineLength = 100  # Organization standard
```

This applies when no user or project config exists.

## Validation

Configuration values are validated when loaded:

- **Syntax errors**: Invalid TOML syntax throws `ConfigurationSyntaxException` with line/column information
-  **Validation errors**: Values violating business rules (e.g., negative line length) throw
  `ConfigurationValidationException`

Both exceptions include actionable error messages pointing to the problem.

## Load-Once Pattern

Styler loads configuration **once** at startup and uses the same immutable `Config` instance throughout
execution. There is no configuration reloading or file watching.

This ensures:
- **Consistent behavior**: All files formatted in a session use the same settings
- **Performance**: No I/O overhead during formatting
- **Thread safety**: Immutable configuration is safe to share across threads
