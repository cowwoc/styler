#!/bin/bash
# detect-test-antipatterns.sh
# Scan test files for common thread-safety anti-patterns

echo "======================================"
echo "Test Thread-Safety Anti-Pattern Scanner"
echo "======================================"

echo -e "\n[1] Scanning for @BeforeMethod/@AfterMethod usage..."
BEFORE_METHOD_COUNT=$(grep -r "@BeforeMethod\|@AfterMethod" src/test/java/ --include="*.java" 2>/dev/null | wc -l)
if [ "$BEFORE_METHOD_COUNT" -gt 0 ]; then
    echo "⚠️  Found $BEFORE_METHOD_COUNT occurrences:"
    grep -rn "@BeforeMethod\|@AfterMethod" src/test/java/ --include="*.java" 2>/dev/null
else
    echo "✅ No @BeforeMethod/@AfterMethod found"
fi

echo -e "\n[2] Scanning for mutable static fields in tests..."
STATIC_FIELD_COUNT=$(grep -r "private static [^f]" src/test/java/ --include="*.java" 2>/dev/null | grep -v "private static final" | wc -l)
if [ "$STATIC_FIELD_COUNT" -gt 0 ]; then
    echo "⚠️  Found $STATIC_FIELD_COUNT mutable static fields:"
    grep -rn "private static [^f]" src/test/java/ --include="*.java" 2>/dev/null | grep -v "private static final"
else
    echo "✅ No mutable static fields found"
fi

echo -e "\n[3] Scanning for temp file operations without UUID..."
TEMP_FILE_COUNT=$(grep -r "createTempFile\|createTempDirectory" src/test/java/ --include="*.java" 2>/dev/null | grep -v "UUID" | wc -l)
if [ "$TEMP_FILE_COUNT" -gt 0 ]; then
    echo "⚠️  Found $TEMP_FILE_COUNT temp file operations without UUID:"
    grep -rn "createTempFile\|createTempDirectory" src/test/java/ --include="*.java" 2>/dev/null | grep -v "UUID"
else
    echo "✅ All temp file operations use UUID isolation"
fi

echo -e "\n[4] Scanning for System.setProperty usage..."
SYSTEM_PROP_COUNT=$(grep -r "System.setProperty" src/test/java/ --include="*.java" 2>/dev/null | wc -l)
if [ "$SYSTEM_PROP_COUNT" -gt 0 ]; then
    echo "⚠️  Found $SYSTEM_PROP_COUNT System.setProperty calls:"
    grep -rn "System.setProperty" src/test/java/ --include="*.java" 2>/dev/null
else
    echo "✅ No System.setProperty usage found"
fi

echo -e "\n[5] Scanning for static {} initialization blocks in tests..."
STATIC_INIT_COUNT=$(grep -r "static {" src/test/java/ --include="*.java" 2>/dev/null | wc -l)
if [ "$STATIC_INIT_COUNT" -gt 0 ]; then
    echo "⚠️  Found $STATIC_INIT_COUNT static initialization blocks:"
    grep -rn "static {" src/test/java/ --include="*.java" 2>/dev/null
else
    echo "✅ No static initialization blocks found"
fi

echo -e "\n======================================"
echo "Summary:"
echo "  @BeforeMethod/@AfterMethod: $BEFORE_METHOD_COUNT"
echo "  Mutable static fields: $STATIC_FIELD_COUNT"
echo "  Temp files without UUID: $TEMP_FILE_COUNT"
echo "  System.setProperty calls: $SYSTEM_PROP_COUNT"
echo "  Static init blocks: $STATIC_INIT_COUNT"
echo "======================================"

TOTAL_ISSUES=$((BEFORE_METHOD_COUNT + STATIC_FIELD_COUNT + TEMP_FILE_COUNT + SYSTEM_PROP_COUNT + STATIC_INIT_COUNT))
if [ "$TOTAL_ISSUES" -eq 0 ]; then
    echo "✅ No anti-patterns found - tests appear thread-safe"
    exit 0
else
    echo "⚠️  Total issues found: $TOTAL_ISSUES"
    exit 1
fi
