# Summary: fix-classpath-scanner-per-file-overhead

## Status: COMPLETE
**Completed**: 2026-01-06

## What Was Built
- Added early return in `ClasspathScanner.create()` for empty config (avoids system classpath scan)
- Moved `ClasspathScanner` from internal package to public package
- Added `TransformationContext.classpathScanner()` method for shared scanner access
- Created `ProcessingContext.classpathScanner` field for pipeline-level sharing
- Made `FileProcessingPipeline` implement `AutoCloseable` to manage scanner lifecycle
- Updated `ImportOrganizerFormattingRule` to use `context.classpathScanner()` instead of creating per-file

## Files Modified
- `formatter/src/main/java/.../formatter/ClasspathScanner.java` - Moved from internal, added early return
- `formatter/src/main/java/.../formatter/TransformationContext.java` - Added classpathScanner() method
- `formatter/src/main/java/.../importorg/ImportOrganizerFormattingRule.java` - Use context scanner
- `pipeline/src/main/java/.../pipeline/FileProcessingPipeline.java` - AutoCloseable, owns scanner
- `pipeline/src/main/java/.../pipeline/ProcessingContext.java` - Added classpathScanner field
- `pipeline/src/main/java/.../pipeline/internal/DefaultTransformationContext.java` - Added scanner field

## Performance Impact
- Per-file overhead: 13.6ms → ~0ms
- Spring Framework (1691 files): ~23 seconds saved

## Quality
- All tests passing (1532 tests)
- Zero Checkstyle/PMD violations
