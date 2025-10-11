# Claude POM.xml Style Guide - Detection Patterns

**File Scope**: `pom.xml` files only
**Purpose**: Systematic violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### Dependency Grouping - Inconsistent Organization
**Key Rules**: Project dependencies (same groupId) first | External dependencies grouped by scope | No blank lines within group | Exactly one blank line between groups
**Detection**: Manual analysis required - Extract dependencies, verify grouping/spacing
**Step 1**: `grep -A 10 '<dependency>' pom.xml | grep -B 1 -A 8 '<groupId>'`
**Step 2 - Analyze violations**:

**Violation Patterns**:
1. **Project deps not first**: External before project groupId
2. **Missing blank line**: No blank between groups
3. **Blank within group**: Blank line between same type/scope deps
4. **Scope not grouped**: Test/compile mixed

**Complete Correct Example**:
```xml
<dependencies>
	<!-- Project dependencies -->
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

	<!-- External compile dependencies -->
	<dependency>
		<groupId>com.google.guava</groupId>
		<artifactId>guava</artifactId>
		<version>32.1.3-jre</version>
	</dependency>

	<!-- External test dependencies -->
	<dependency>
		<groupId>org.testng</groupId>
		<artifactId>testng</artifactId>
		<version>7.8.0</version>
		<scope>test</scope>
	</dependency>
</dependencies>
```

**Detection Commands**:
```bash
# Check if project dependencies first
awk '/<groupId>/ {print NR, $0}' pom.xml | head -20

# Check blank line patterns
grep -n -A 1 '</dependency>' pom.xml | grep -B 1 '<dependency>'

# Extract groupIds in order
grep -o '<groupId>[^<]*</groupId>' pom.xml | sed 's/<[^>]*>//g'
```

**Manual Verification**: Identify project groupId | Group dependencies by (type, scope) | Verify ordering (project first, external by scope) | Verify spacing (no blanks within groups, one between groups)
**Rationale**: Consistent organization improves readability, reduces merge conflicts, easier dependency audits
