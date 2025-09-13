# Encoding Strategy Documentation

## UTF-8 Enforcement Approach

This project uses a **layered approach** to ensure consistent UTF-8 encoding:

### 1. Git-Level Enforcement (Primary)
- **`.gitattributes`** automatically normalizes all text files to UTF-8 with LF line endings
- **Proactive**: Prevents encoding issues before they reach the codebase
- **Universal**: Applies to all contributors regardless of IDE settings

### 2. Maven Project Configuration (Secondary)
- **`project.build.sourceEncoding=UTF-8`** ensures Maven plugins use UTF-8
- **Maven Enforcer Plugin** validates this property is set correctly
- **Build consistency**: Ensures all Maven operations use UTF-8

### 3. Natural JVM Validation (Tertiary)
- **Java compiler fails** on encoding mismatches during compilation
- **Immediate feedback**: Build breaks prevent encoding issues from advancing

## Why File-Level Detection Is Unnecessary

Build-time file encoding validation was considered but **rejected** because:

1. **Git normalization is superior** - fixes issues before they enter the repository
2. **Compilation provides natural validation** - encoding mismatches cause build failures  
3. **Modern development defaults to UTF-8** - encoding issues are increasingly rare
4. **Checkstyle charset property doesn't validate files** - only sets parser encoding
5. **Performance impact** - file encoding detection adds build overhead

## Validation Commands

```bash
# Verify Git normalization is working
git check-attr text eol *.java

# Check project encoding setting
mvn help:evaluate -Dexpression=project.build.sourceEncoding

# Manual encoding verification (if needed)
file --mime-encoding src/**/*.java
```

This layered approach provides robust UTF-8 enforcement without the complexity and performance overhead of build-time file scanning.