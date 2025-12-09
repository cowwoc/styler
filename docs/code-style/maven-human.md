# Maven POM Style Guide - Human Understanding

**Purpose**: Conceptual understanding and rationale for Maven POM.xml formatting standards
**Companion**: See `maven-claude.md` for optimized detection patterns and commands

## 📋 Table of Contents

- [🧠 Why These Rules Matter](#why-these-rules-matter)
- [🚨 TIER 1 CRITICAL - Build Blockers](#tier-1-critical---build-blockers)
- [📚 Integration with Detection](#integration-with-detection)

---

## 🧠 Why These Rules Matter

Maven POM files define project structure, dependencies, and build configuration. Clear organization is
critical because:
-  **Dependency management**: Developers need to quickly understand project dependencies and their
  relationships
- **Merge conflict reduction**: Consistent formatting minimizes git conflicts during dependency updates
- **Maintainability**: Clear grouping makes it easier to identify missing dependencies or version conflicts

## 🚨 TIER 1 CRITICAL - Build Blockers

### Avoid relativePath in Parent References
**Why avoid explicit relativePath**: Maven automatically resolves parent POMs from the local repository or
the reactor (multi-module build). Adding explicit `<relativePath>` elements creates unnecessary coupling
between module locations and filesystem structure.

**Problems with relativePath**:
- **Brittle builds**: Moving modules or changing directory structure breaks builds
- **Unnecessary coupling**: Modules shouldn't know about filesystem layout beyond their own structure
- **Redundant**: Maven's default behavior already handles parent resolution correctly

**Correct approach**:
```xml
<parent>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler</artifactId>
	<version>1.0-SNAPSHOT</version>
	<!-- Let Maven resolve parent automatically - no relativePath needed -->
</parent>
```

### Dependency Grouping - Inconsistent Organization
**Why dependency grouping matters**: Dependencies should be organized by type and scope to make the POM file
scannable and maintainable. This organization helps developers quickly understand which dependencies are
internal project modules versus external libraries.

**Grouping rules**:
1.  **Group by type first**: All project dependencies (`groupId` starting with project groupId) are grouped
   together at the top
2. **Group by scope second**: External dependencies are grouped by scope (compile, provided, test)
3. **No blank lines within groups**: Dependencies of the same type and scope should have no blank lines between `</dependency>` and `<dependency>`
4.  **Exactly one blank line between groups**: Groups with different type or scope should be separated by
   exactly one blank line

**Practical example**:
```xml
<dependencies>
	<!-- Project dependencies - no blank lines within group -->
	<dependency>
		<groupId>io.github.cowwoc.styler</groupId>
		<artifactId>styler-core</artifactId>
		<version>${project.version}</version>
	</dependency>
	<dependency>
		<groupId>io.github.cowwoc.styler</groupId>
		<artifactId>styler-parser</artifactId>
		<version>${project.version}</version>
	</dependency>

	<!-- External compile dependencies - one blank line before group -->
	<dependency>
		<groupId>com.google.guava</groupId>
		<artifactId>guava</artifactId>
		<version>32.1.3-jre</version>
	</dependency>
	<dependency>
		<groupId>org.slf4j</groupId>
		<artifactId>slf4j-api</artifactId>
		<version>2.0.9</version>
	</dependency>

	<!-- External test dependencies - one blank line before group -->
	<dependency>
		<groupId>org.testng</groupId>
		<artifactId>testng</artifactId>
		<version>7.8.0</version>
		<scope>test</scope>
	</dependency>
</dependencies>
```

**Why this organization helps**:
-  **Project vs external separation**: Immediately see internal module dependencies versus third-party
  libraries
- **Scope visibility**: Test dependencies are visually separated from runtime dependencies
-  **Merge conflict reduction**: Consistent spacing reduces unnecessary conflicts when adding dependencies in
  different branches
- **Dependency audit**: Quickly scan for test-only dependencies that shouldn't be in compile scope

## 📚 Navigation

### Related Documentation
- **[Common Practices](common-human.md)**: Universal principles applying to all languages
- **[Java Style Guide](java-human.md)**: Java-specific coding standards
- **[Master Style Guide](../code-style-human.md)**: Complete overview and philosophy

### Claude Detection Patterns
- **[Maven Detection Patterns](maven-claude.md)**: Automated rule detection patterns

This human guide provides the conceptual foundation. For specific violation patterns and systematic checking,
Claude uses the companion detection file. Together, they ensure both understanding and consistent enforcement
of Maven POM quality standards.
