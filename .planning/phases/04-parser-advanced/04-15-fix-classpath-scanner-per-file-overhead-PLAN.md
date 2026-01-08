# Plan: fix-classpath-scanner-per-file-overhead

## Objective
Eliminate per-file ClasspathScanner creation overhead.

## Tasks
1. Add early return in ClasspathScanner.create() for empty config
2. Move ClasspathScanner to public package
3. Add TransformationContext.classpathScanner() method
4. Add ProcessingContext.classpathScanner field
5. Make FileProcessingPipeline implement AutoCloseable
6. Update ImportOrganizerFormattingRule to use context scanner

## Verification
- [ ] Per-file overhead eliminated (~13.6ms → ~0ms)
- [ ] Scanner lifecycle managed properly
