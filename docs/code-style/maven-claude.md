# Claude POM.xml Style Guide - Detection Patterns

**File Scope**: `pom.xml` files only
**Purpose**: Systematic violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### Dependency Grouping - Inconsistent Organization
**Detection Pattern**: Dependencies not grouped by type and scope with correct spacing
**Key Rules**:
1. Project dependencies (same groupId as project) must be grouped together first
2. External dependencies must be grouped by scope
3. No blank lines within same type/scope group
4. Exactly one blank line between different type/scope groups

**Detection Strategy**: Multi-step manual analysis required

**Step 1 - Find all dependency blocks**:
```bash
# Extract all dependencies with context
grep -A 10 '<dependency>' pom.xml | grep -B 1 -A 8 '<groupId>'
```

**Step 2 - Analyze grouping violations**:

**Violation Pattern 1 - Project dependencies not grouped first**:
```xml
<!-- VIOLATION - External dependency before project dependency -->
<dependency>
	<groupId>com.google.guava</groupId>
	<artifactId>guava</artifactId>
</dependency>
<dependency>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler-core</artifactId>
</dependency>

<!-- CORRECT - Project dependencies first -->
<dependency>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler-core</artifactId>
</dependency>

<dependency>
	<groupId>com.google.guava</groupId>
	<artifactId>guava</artifactId>
</dependency>
```

**Violation Pattern 2 - Missing blank line between groups**:
```xml
<!-- VIOLATION - No blank line between project and external dependencies -->
<dependency>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler-core</artifactId>
</dependency>
<dependency>
	<groupId>com.google.guava</groupId>
	<artifactId>guava</artifactId>
</dependency>

<!-- CORRECT - One blank line separates groups -->
<dependency>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler-core</artifactId>
</dependency>

<dependency>
	<groupId>com.google.guava</groupId>
	<artifactId>guava</artifactId>
</dependency>
```

**Violation Pattern 3 - Blank line within same group**:
```xml
<!-- VIOLATION - Blank line between dependencies of same type/scope -->
<dependency>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler-core</artifactId>
</dependency>

<dependency>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler-parser</artifactId>
</dependency>

<!-- CORRECT - No blank lines within same group -->
<dependency>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler-core</artifactId>
</dependency>
<dependency>
	<groupId>io.github.cowwoc.styler</groupId>
	<artifactId>styler-parser</artifactId>
</dependency>
```

**Violation Pattern 4 - Scope not grouped correctly**:
```xml
<!-- VIOLATION - Test dependencies mixed with compile dependencies -->
<dependency>
	<groupId>com.google.guava</groupId>
	<artifactId>guava</artifactId>
</dependency>
<dependency>
	<groupId>org.testng</groupId>
	<artifactId>testng</artifactId>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.slf4j</groupId>
	<artifactId>slf4j-api</artifactId>
</dependency>

<!-- CORRECT - Dependencies grouped by scope -->
<dependency>
	<groupId>com.google.guava</groupId>
	<artifactId>guava</artifactId>
</dependency>
<dependency>
	<groupId>org.slf4j</groupId>
	<artifactId>slf4j-api</artifactId>
</dependency>

<dependency>
	<groupId>org.testng</groupId>
	<artifactId>testng</artifactId>
	<scope>test</scope>
</dependency>
```

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
# Step 1: Check if project dependencies are first
# Extract all groupIds and check if project groupIds come before external ones
awk '/<groupId>/ {print NR, $0}' pom.xml | head -20

# Step 2: Check blank line patterns
# Look for consecutive </dependency><dependency> without blank line (potential within-group spacing)
grep -n -A 1 '</dependency>' pom.xml | grep -B 1 '<dependency>'

# Step 3: Check for blank lines between same-group dependencies (violation)
# Find all </dependency> followed by blank line followed by <dependency> with same groupId

# Step 4: Manual verification required
# 1. Identify project groupId from <project><groupId>
# 2. Group all dependencies by (type, scope) tuples
# 3. Verify ordering: project deps first, then external grouped by scope
# 4. Verify spacing: no blanks within groups, exactly one blank between groups
```

**Rationale**: Consistent dependency organization improves POM readability, reduces merge conflicts, and makes dependency audits easier. Grouping by type (project vs external) and scope (compile vs test) creates logical sections that developers can quickly scan and understand.

## Optimized Detection Commands

**Performance Strategy**: Manual analysis required due to semantic grouping rules

```bash
# Extract dependency structure for manual review
echo "=== DEPENDENCY GROUPING ANALYSIS ==="

# Show all dependencies with line numbers
grep -n '<dependency>' pom.xml

# Show blank lines between dependencies
grep -n -B 1 -A 1 '</dependency>' pom.xml | grep -E '(^[0-9]+-$|dependency)'

# Extract all groupIds in order (for grouping analysis)
grep -o '<groupId>[^<]*</groupId>' pom.xml | sed 's/<[^>]*>//g'
```

**Usage for Style Auditor**:
1. Extract dependency list with groupIds and scopes
2. Identify project groupId
3. Verify project dependencies appear first
4. Verify external dependencies are grouped by scope
5. Verify blank line rules (none within groups, exactly one between groups)
6. Generate detailed violation report with specific line numbers and corrections
