# Task Plan: fix-misc-parsing-edge-cases

## Objective

Fix remaining miscellaneous parsing edge cases in Spring Framework.

## Problem Analysis

Several unique patterns cause parsing failures:

1. **Block comment in field declaration:**
   ```java
   private final Map/* <Class, Set<Sig>> */declToBridge;
   ```

2. **Multiple top-level classes:**
   ```java
   class Tests { }
   @Configuration
   class Config { }  // AT_SIGN at top level
   ```

3. **Complex generic patterns:**
   ```java
   Map<String,Long> record = new HashMap<>();  // RECORD as identifier
   public Mono<ServerResponse> proxy(Request r) { }  // generic return
   ```

4. **Typed lambda patterns:**
   ```java
   Flux.from(stream).map((byte[] bytes) -> expr)
   ```

## Affected Files

- `spring-core/.../BridgeMethodResolver.java` - block comment
- `spring-context/.../Spr8808Tests.java` - multiple classes
- `spring-core/.../AnnotationMetadataTests.java` - member declaration
- Plus ~7 more files

## Approach

After fixing the main task issues, investigate remaining failures.
Some may be resolved by other tasks' fixes.

## Execution Steps

1. Re-run validation after main tasks complete
2. Identify remaining failures
3. Add specific fixes for each pattern
4. Verify all files parse correctly

## Success Criteria

- [ ] All 8,817 Spring Framework files parse successfully
- [ ] 100% success rate achieved
