# Build System & Project Structure

## Environment Setup {#environment-setup}

```bash
export JAVA_HOME="/home/node/.sdkman/candidates/java/current"
```

**Note**: On Windows, convert paths to WSL2 mount points (/mnt/c/...)

## Test Module Structure (JPMS Compliance) {#test-module-structure-jpms-compliance}

**CRITICAL**: For Java modules using TestNG, tests MUST follow this pattern:

1. **Package Structure**: Test classes must be in separate packages from main code:
   - Main: `io.github.styler.parser` or `io.github.styler.formatter`
   - Test: `io.github.styler.parser.test` or `io.github.styler.formatter.test` (different base package)

2. **Test Module Descriptor** (`src/test/java/module-info.java`):
   ```java
   module your.module.name.test
   {
	   requires your.main.module;
	   requires org.testng;

	   opens your.test.package to org.testng;
	   opens your.test.package.subpackage to org.testng;
   }
   ```

3. **Key Requirements**:
   - Use `opens X to org.testng` NOT `exports X to org.testng` (TestNG needs reflection access)
   - Test classes need explicit imports from main module: `import io.github.styler.parser.*;`
   - Each test package must be explicitly exported to TestNG

**Example Structure**:
```
src/test/java/
  ├── module-info.java
  └── io/github/styler/parser/test/
      ├── JavaParserTest.java
      └── ast/ASTNodeTest.java
```

This resolves TestNG module export warnings with `-Werror` enabled.

### Test Module Name Verification {#test-module-name-verification}

**CRITICAL REQUIREMENT**: Test modules MUST use a different module name than the main module to avoid conflicts.

**Correct Pattern**:
```java
// src/main/java/module-info.java
module io.github.styler.parser { }

// src/test/java/module-info.java
module io.github.styler.parser.test {  // ✅ Different name with .test suffix
    requires io.github.styler.parser;
    requires org.testng;
    opens io.github.styler.parser.test to org.testng;
}
```

**Incorrect Pattern (CAUSES BUILD FAILURES)**:
```java
// src/main/java/module-info.java
module io.github.styler.parser { }

// src/test/java/module-info.java
module io.github.styler.parser {  // ❌ SAME NAME - module conflict
    requires org.testng;
    opens io.github.styler.parser.test to org.testng;
}
```

**Symptoms of Module Name Conflict**:
- Build succeeds with `./mvnw compile` but fails with `./mvnw verify`
- Error: "module not found: io.github.styler.parser" despite module being present
- Intermittent failures depending on Maven build cache state
- Clean build required to see actual error

**Verification Command**:
```bash
# Check both module descriptors have different names
diff <(grep "^module " src/main/java/module-info.java) \
     <(grep "^module " src/test/java/module-info.java)
# Should show difference (e.g., "parser" vs "parser.test")
```

**Enforcement**: Always use `./mvnw clean verify` before merging to detect module name conflicts that might be masked by stale build cache.

## Build & Validation Commands {#build-validation-commands}

**⚠️ CRITICAL**: Always use `./mvnw` (Maven Wrapper) instead of `mvn` to ensure consistent Maven version
across environments.

**Core Build Commands**:
- **Full Validation**: `./mvnw verify` (PREFERRED - compile + test + package in optimized sequence)
- **Clean Build**: `./mvnw clean verify` (when dependency changes or first build, REQUIRED for JPMS projects)
- **Compilation Only**: `./mvnw compile` (main source only - does NOT compile tests, use for quick syntax checking)
- **Test Compilation**: `./mvnw test-compile` (compiles both main + test sources, use for test syntax verification)
- **Selective Testing**: `./mvnw test -Dtest=ClassName` (targeted investigation)

**IMPORTANT - Maven Lifecycle Phases**:
- `compile` - Compiles ONLY main sources (`src/main/java/`)
- `test-compile` - Compiles main sources AND test sources (`src/test/java/`)
- `test` - Runs test-compile + executes tests
- `package` - Runs tests + packages JAR
- `verify` - Runs package + integration tests + validation

**For JPMS projects**: Always use `./mvnw clean verify` for final validation. The `compile` phase alone does NOT include test module descriptors, which can cause stale module-info.class in build cache.

### Build Output Efficiency {#build-output-efficiency}

**Purpose**: Reduce token usage during validation builds by filtering output to errors/warnings only.

**When to Use**:
- Validation builds where you only need to know if build passed/failed
- Checking for violations after fixes
- Iterative fix-verify cycles where success output is not informative

**Efficiency Pattern**:
```bash
# Filter to errors/warnings only (~70% token reduction per build)
./mvnw -q clean verify 2>&1 | grep -E "(ERROR|WARN|FAIL|BUILD FAILURE|BUILD SUCCESS)"

# Or for checkstyle/PMD specific validation
./mvnw -q checkstyle:check pmd:check 2>&1 | grep -E "(ERROR|WARN|violation)"

# Module-specific validation
./mvnw -q verify -pl :module-name 2>&1 | grep -E "(ERROR|WARN|FAIL)"
```

**When NOT to Use**:
- Debugging build failures (need full output to diagnose)
- First build of new module (want to see all compilation steps)
- Build performance analysis (need timing information)

**Token Impact**:
- Full build output: ~5000-8000 tokens
- Filtered output: ~1000-2000 tokens (errors/warnings only)
- **Savings**: ~70% token reduction per validation build

**Note**: For successful builds with no violations, filtered output will only show `BUILD SUCCESS`.

## Maven Multi-Module Setup {#maven-multi-module-setup}

### pom.xml Style Guidelines {#pomxml-style-guidelines}

**RULE**: Do not add explanatory comments above dependencies or plugins that simply describe what they are.

**❌ PROHIBITED**:
```xml
<!-- Maven Plugin Development -->
<dependency>
    <groupId>org.apache.maven</groupId>
    <artifactId>maven-plugin-api</artifactId>
</dependency>

<!-- Checkstyle for maven-checkstyle-plugin -->
<dependency>
    <groupId>com.puppycrawl.tools</groupId>
    <artifactId>checkstyle</artifactId>
</dependency>
```

**✅ ALLOWED**:
```xml
<dependency>
    <groupId>org.apache.maven</groupId>
    <artifactId>maven-plugin-api</artifactId>
</dependency>

<dependency>
    <groupId>com.puppycrawl.tools</groupId>
    <artifactId>checkstyle</artifactId>
</dependency>
```

**✅ ALLOWED** (comments providing non-obvious context):
```xml
<!-- ✅ **REQUIRED** - Root pom.xml dependency version management -->
<dependencyManagement>...</dependencyManagement>

<!-- Workaround for JDK 25 preview feature compatibility -->
<dependency>...</dependency>
```

### Maven Dependency Management Standards 🔴 CRITICAL {#maven-dependency-management-standards-critical}

**RULE**: Centralize all plugin and dependency versions in the root pom.xml using `dependencyManagement` and
`pluginManagement`.

**Root POM Configuration**:
```xml
<!-- ✅ **REQUIRED** - Root pom.xml dependency version management -->
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.testng</groupId>
			<artifactId>testng</artifactId>
			<version>7.8.0</version>
		</dependency>
		<dependency>
			<groupId>io.github.cowwoc</groupId>
			<artifactId>requirements-java</artifactId>
			<version>12.1</version>
		</dependency>
	</dependencies>
</dependencyManagement>

<build>
	<pluginManagement>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
        <version>3.14.1</version>
        <dependencies>
          <dependency>
            <groupId>org.ow2.asm</groupId>
            <artifactId>asm</artifactId>
            <version>9.8</version>
          </dependency>
        </dependencies>
				<configuration>
					<release>25</release>
				</configuration>
			</plugin>
		</plugins>
    </pluginManagement>
</build>
```

**Child Module Configuration**:
```xml
<!-- ✅ **REQUIRED** - Child pom.xml references without versions -->
<dependencies>
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <scope>test</scope>
        <!-- NO VERSION - inherited from parent dependencyManagement -->
    </dependency>
</dependencies>
```

**🚨 CRITICAL VIOLATIONS**:
```xml
<!-- ❌ **FORBIDDEN** - Version specified in child module -->
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.8.0</version>  <!-- REMOVE - should be in parent dependencyManagement -->
</dependency>
```

## 🚨 CRITICAL BUILD VALIDATION WORKFLOW {#critical-build-validation-workflow}

**Build Validation Required for Every Task**:
- **INITIAL**: `./mvnw clean` when beginning a new task (fresh compilation state)  
- **ONGOING**: `./mvnw verify` for subsequent validation during task work
- **FINAL**: build-validator after all changes using `./mvnw verify`
- **MANDATORY**: Build must pass with zero warnings/failures before task completion

## Timeout Configuration {#timeout-configuration}

**⚠️ CRITICAL**: Build operations must use adequate timeouts to prevent premature termination:

**Standard Timeouts** (for Bash tool integration):
- **./mvnw verify**: 300,000ms (5 minutes) - Full validation
- **./mvnw clean verify**: 300,000ms (5 minutes) - Clean builds
- **./mvnw exec:exec**: 1,800,000ms (30 minutes) - Simulation execution
- **./mvnw compile**: 300,000ms (5 minutes) - Compilation only

## Formatter Execution Modes {#formatter-execution-modes}

- **Format single file**: `java -jar styler-cli.jar format MyFile.java`
- **Format directory**: `java -jar styler-cli.jar format src/main/java/`
- **Check mode (CI/CD)**: `java -jar styler-cli.jar check src/`
- **Custom configuration**: `java -jar styler-cli.jar format --config .styler.yml src/`

## Project Structure {#project-structure}

### Core Module (`styler-ast-core/src/main/java/io/github/styler/ast/`) {#core-module-styler-ast-coresrcmainjavaiogithubstylerast}

- **Entry Point**: AST node hierarchy and visitor pattern implementation

**Core Packages**:
- `nodes/` - AST node implementations (ClassDeclaration, MethodDeclaration, etc.)
- `visitors/` - Visitor pattern implementations for AST traversal
- `builders/` - Builder pattern for immutable AST construction
- `metadata/` - Source position tracking and formatting metadata
- `transformations/` - AST transformation utilities

### Parser Module (`styler-parser/src/main/java/io/github/styler/parser/`) {#parser-module-styler-parsersrcmainjavaiogithubstylerparser}

- **Entry Point**: `JavaParser.java` - Main parser implementation

**Core Packages**:
- `lexer/` - Token generation and lexical analysis
- `grammar/` - Java language grammar implementation
- `recovery/` - Error recovery and partial parsing
- `trivia/` - Comment and whitespace preservation
- `incremental/` - Incremental parsing for performance

### Formatter Module (`styler-formatter-impl/src/main/java/io/github/styler/formatter/`) {#formatter-module-styler-formatter-implsrcmainjavaiogithubstylerformatter}

- **Entry Point**: Plugin-based formatting system

**Core Packages**:
- `plugins/` - Built-in formatter implementations
- `rules/` - Formatting rule engine
- `config/` - Configuration management and validation
- `output/` - Code generation and trivia preservation

### CLI Module (`styler-cli/`) {#cli-module-styler-cli}

**Command-Line Interface**: Java application for formatting operations with configuration management.

**Build Process**:
- **Assembly Plugin**: Creates executable JAR with all dependencies
- **Native Image**: GraalVM native compilation for fast startup
- **Distribution**: ZIP/TAR distributions with scripts and documentation
- **Output**: Executable JAR and native binaries

**Code Quality & Documentation**:
- **Testing**: JUnit 5 with comprehensive CLI integration tests
- **Documentation**: JavaDoc for API documentation generation
- **Validation**: Input validation and error handling
- **Configuration**: YAML-based configuration with validation

**Build Commands**:
- `./mvnw compile -pl styler-cli` - Build CLI module only
- `./mvnw package -pl styler-cli` - Create executable JAR
- `./mvnw assembly:single -pl styler-cli` - Create distribution packages
- `./mvnw native:compile -pl styler-cli` - Create native executable (requires GraalVM)

**Key Files**:
- `pom.xml` - Maven configuration with assembly and native plugins
- `src/main/java/io/github/styler/cli/StylerCLI.java` - Main entry point
- `src/main/java/io/github/styler/cli/commands/` - Command implementations
- `src/main/java/io/github/styler/cli/config/` - Configuration management
- `src/main/resources/META-INF/native-image/` - GraalVM configuration
- `target/styler-cli.jar` - Executable JAR output
- `target/distributions/` - Distribution packages

### Engine Module (`styler-engine/`) {#engine-module-styler-engine}

**Processing Engine**: Multi-threaded file processing and coordination system.

**Key Features**:
- **Runtime Dependencies**: Parser and formatter modules
- **Threading**: ForkJoinPool for parallel file processing
- **Performance**: Work-stealing thread pool with progress reporting
- **Security**: Resource limits and sandboxing controls

### Supporting Structure {#supporting-structure}

- `src/test/java/` - Parser, formatter, and integration tests
- `src/main/resources/` - Configuration schemas and logging setup
- `pom.xml` - Maven multi-module configuration with 8 modules:
  - `styler-ast-core` - AST node hierarchy
  - `styler-parser` - Java parsing implementation
  - `styler-formatter-api` - Plugin interfaces
  - `styler-formatter-impl` - Built-in formatters
  - `styler-config` - Configuration system
  - `styler-engine` - Parallel processing
  - `styler-security` - Security controls
  - `styler-cli` - Command-line interface

## Maven Build Cache Compatibility {#maven-build-cache-compatibility}

**STATUS**: ✅ **COMPATIBLE** with JPMS multi-module builds when using correct lifecycle phases

### Requirements {#requirements}

Maven Build Cache Extension v1.2.0 works correctly with Java Platform Module System (JPMS) modules when
following these requirements:

- **Required Goals**: Always use `./mvnw clean verify` (or `package`/`install`/`deploy`)
- **Prohibited Goals**: Never use `./mvnw clean compile` as final build command
- **Cache Location**: `~/.m2/build-cache/`
- **Cache Cleanup**: `rm -rf ~/.m2/build-cache/` if stale entries cause issues

### How It Works {#how-it-works}

The Maven Build Cache Extension activates on `package` or higher lifecycle phases:

1. **With `package`/`verify`/`install`/`deploy`**:
   - Saves compiled classes including module-info.class to cache as ZIP files
   - Restores both JAR artifacts and classes directory on cache hit
   - Full JPMS module support with correct module resolution

2. **With `compile` or `test-compile` only** (⚠️ INCOMPLETE CACHING):
   - Saves build metadata but NOT the classes directory
   - Creates `<final>false</final>` cache entries without attachedOutputs
   - Subsequent `verify` builds fail with "module not found" errors
   - NOTE: `compile` only processes main sources; `test-compile` processes both main and test sources

### Cache Corruption Prevention {#cache-corruption-prevention}

**Symptom**: "module not found: io.github.cowwoc.styler.ast.core" errors despite successful previous builds

**Root Cause**: Stale cache entry from `compile` or `test-compile` build lacking packaged classes

**Solution**:
```bash
# Clear cache to remove stale entries
rm -rf ~/.m2/build-cache/

# Always use verify or package goals
./mvnw clean verify
```

### Cache Configuration {#cache-configuration}

Cache is configured via `.mvn/maven-build-cache-config.xml`:

```xml
<attachedOutputs>
  <dirNames>
    <dirName>classes</dirName>
    <dirName>test-classes</dirName>
  </dirNames>
</attachedOutputs>
```

These directories are saved as ZIP artifacts (e.g., `styler-ast-core-mvn-cache-ext-extra-output-3.zip`)
containing all compiled classes including module-info.class.

### Performance Benefits {#performance-benefits}

Build cache provides significant performance improvements for JPMS projects:

- **Cache Hit**: 2-3 seconds (vs. 20-30 seconds clean build)
- **Partial Restore**: Skips unchanged modules, rebuilds only modified modules
- **Multi-Module Efficiency**: Avoids redundant compilation across reactor
