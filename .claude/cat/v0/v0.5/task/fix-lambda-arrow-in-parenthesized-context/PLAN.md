# Plan: fix-lambda-arrow-in-parenthesized-context

## Problem

Lambda expressions after method references with trailing comments cause "Expected RIGHT_PARENTHESIS but
found ARROW" errors. This affects DatabasePopulator.java and Jackson2ObjectMapperBuilder.java.

## Failing Tests (Created During Task Creation)

**Test file:** `parser/src/test/java/io/github/cowwoc/styler/parser/test/LambdaArrowEdgeCaseParserTest.java`
**Test method:** `shouldParseLambdaAfterMethodReferenceWithTrailingComments`
**Commit:** ac4e052 - test: add failing tests for switch fallthrough and lambda arrow bugs

```java
String source = """
    class Test
    {
        void foo()
        {
            Mono.usingWhen(getConnection(), //
                this::populate, //
                connection -> release(connection));
        }
    }
    """;
```

**Verification run (from task creation):**
```bash
./mvnw test -pl parser -Dtest=LambdaArrowEdgeCaseParserTest#shouldParseLambdaAfterMethodReferenceWithTrailingComments
# Result: FAILED with error: "Expected RIGHT_PARENTHESIS but found ARROW at position 101"
```

## Goal

Fix `isLambdaExpression()` asymmetric parenthesis tracking that causes false negatives for lambdas with
annotated generic type parameters containing element-value pairs.

## Approach

Make parenthesis tracking symmetric: both `(` and `)` should only be counted when outside generic type
arguments (`angleBracketDepth == 0`). Parentheses inside generics (from annotations with element-value
pairs) don't affect the lambda structure.

## Files to Modify

| File | Change |
|------|--------|
| `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java` | Fix symmetric tracking |
| `parser/src/test/java/io/github/cowwoc/styler/parser/test/LambdaAnnotatedParameterParserTest.java` | New test file |

## Execution Steps

### Step 1: Fix isLambdaExpression() in ExpressionParser.java

**File:** `parser/src/main/java/io/github/cowwoc/styler/parser/internal/ExpressionParser.java`

**Location:** Lines 283-288 in `isLambdaExpression()` method

**Before:**
```java
case LEFT_PARENTHESIS -> ++parenthesisDepth;
case RIGHT_PARENTHESIS ->
{
    if (angleBracketDepth == 0)
        --parenthesisDepth;
}
```

**After:**
```java
case LEFT_PARENTHESIS ->
{
    if (angleBracketDepth == 0)
        ++parenthesisDepth;
}
case RIGHT_PARENTHESIS ->
{
    if (angleBracketDepth == 0)
        --parenthesisDepth;
}
```

### Step 2: Add tests for annotated generic lambda parameters

**File:** Create `parser/src/test/java/io/github/cowwoc/styler/parser/test/LambdaAnnotatedParameterParserTest.java`

**Test cases:**

1. `shouldParseLambdaWithAnnotatedGenericParameter` - Single annotated type with element-value:
   ```java
   handle((List<@NonNull(when=MAYBE) String> items) -> items.size());
   ```

2. `shouldParseLambdaWithMultipleAnnotatedGenericParameters` - Map with annotated key/value:
   ```java
   process((Map<@Key String, @Value(priority=1) Integer> map) -> map.size());
   ```

3. `shouldParseLambdaWithNestedAnnotatedGenerics` - Nested generics with annotations:
   ```java
   transform((Map<String, List<@Valid Item>> data) -> data.get("key"));
   ```

4. `shouldParseLambdaWithArrayAnnotationElement` - Annotation with array value:
   ```java
   validate((List<@Constraint(groups={A.class, B.class}) Item> items) -> items.isEmpty());
   ```

### Step 3: Run regression tests

Verify existing lambda tests still pass:

```bash
./mvnw -pl parser test -Dtest=TypedLambdaParameterParserTest,CastLambdaExpressionParserTest,MultiParamLambdaParserTest,LambdaArrowEdgeCaseParserTest
```

## Verification

```bash
# Run all parser tests
./mvnw -pl parser test

# Run specific new test class
./mvnw -pl parser test -Dtest=LambdaAnnotatedParameterParserTest

# Run existing lambda tests for regression
./mvnw -pl parser test -Dtest="*Lambda*"

# Full build
./mvnw verify
```

## Risk Assessment

**Risk: LOW**

- Change is minimal (2 lines)
- Logic is clearly symmetric now (was asymmetric before)
- Comprehensive regression tests exist for lambda parsing
- New tests verify the specific fix

## Commit Message

```
bugfix: fix lambda detection with annotated generic type parameters

The isLambdaExpression() method had asymmetric parenthesis tracking:
- LEFT_PARENTHESIS always incremented parenthesisDepth
- RIGHT_PARENTHESIS only decremented when angleBracketDepth == 0

This caused false negatives for lambdas with annotations containing
element-value pairs inside generic type parameters, like:
  (List<@NonNull(when=MAYBE) String> items) -> items.size()

The annotation's parentheses incremented the counter but never
decremented, causing the method to never reach parenthesisDepth == 0.

Fix: Make both LEFT_PARENTHESIS and RIGHT_PARENTHESIS only track
when angleBracketDepth == 0. Parentheses inside generic arguments
(from annotations) don't affect the lambda structure.

Task ID: v0.5-fix-lambda-arrow-in-parenthesized-context
```
