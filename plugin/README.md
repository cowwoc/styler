# Styler Maven Plugin

Maven plugin for integrating Styler Java code formatter into your build process.

## Current Status

**MVP Implementation** - Plugin structure is complete and functional, but formatting logic is placeholder. The plugin validates configuration and identifies source files correctly.

## Usage

### Two-Phase Workflow (Current Requirement)

Due to architectural constraints (plugin depends on CLI which depends on formatter modules), the plugin must be installed before it can run:

```bash
# Phase 1: Build and install the plugin
./mvnw clean install

# Phase 2: Run styler on your project
./mvnw io.github.cowwoc.styler:styler-maven-plugin:1.0-SNAPSHOT:check
```

### Running on Specific Modules

To run on modules with source code only (skipping parent POMs):

```bash
./mvnw styler:check -pl ast/core,core,parser,formatter/api,formatter/rules,cli,plugin
```

### Available Goals

- **`styler:check`** - Validates source code formatting without modifying files
- **`styler:format`** - Formats source code in-place (placeholder implementation)

### Configuration

Plugin respects Maven standard properties:

- `${project.build.sourceDirectory}` - Main source directory (default: `src/main/java`)
- `${project.build.testSourceDirectory}` - Test source directory (default: `src/test/java`)
- `${project.build.sourceEncoding}` - File encoding (default: `UTF-8`)

#### Skip Execution

```xml
<properties>
    <styler.skip>true</styler.skip>
</properties>
```

Or via command line:
```bash
mvn verify -Dstyler.skip=true
```

## Known Limitations

1. **Cyclic Dependency**: Cannot be used in Maven profiles (`-Pstyler`) because plugin → CLI → formatter creates a reactor cycle
2. **POM-Only Modules**: Fails on modules without `src/main/java` - use `-pl` to select specific modules
3. **Placeholder Implementation**: Currently validates configuration but does not perform actual formatting

## Planned Improvements

See `todo.md` task: `refactor-maven-plugin-dependencies`

- Remove CLI dependency and use formatter API directly
- Eliminate cyclic dependency to enable profile-based usage
- Implement actual formatting logic using FileProcessorPipeline

## Example Output

```
[INFO] --- styler:1.0-SNAPSHOT:check (default-cli) @ styler-core ---
[INFO] Checking source formatting in: /workspace/project/src/main/java
[INFO] Maven plugin structure created successfully
[INFO] Configuration:
[INFO]   - Source directory: /workspace/project/src/main/java
[INFO]   - Encoding: UTF-8
[INFO]   - Fail on violation: true
[INFO] Formatting check complete (MVP implementation)
```
