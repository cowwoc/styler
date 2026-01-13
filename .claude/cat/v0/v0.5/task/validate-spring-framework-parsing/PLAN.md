# Task Plan: validate-spring-framework-parsing

## Objective

Validate that the parser handles all Spring Framework 6.2.1 Java source files without errors.
This is a manual gate task - run before marking v0.5 complete.

## Problem Analysis

v0.5 tasks fix parser edge cases identified from Spring Framework parsing errors:
- fix-switch-expression-case-parsing
- fix-lambda-parameter-parsing
- fix-comment-in-member-declaration
- add-nested-annotation-type-support
- fix-contextual-keywords-as-identifiers
- fix-cast-lambda-expression
- add-array-initializer-in-annotation-support

Each task has unit tests, but we need end-to-end validation against the actual codebase.

## Approach

**On-the-fly validation** - create temporary validation tool, run it, then delete. Nothing committed.

The validation tool:
- Parses all `.java` files in the target codebase
- Reports errors grouped by type
- Outputs success/failure counts and throughput

## Execution Steps

1. Clone Spring Framework 6.2.1 to `/tmp/spring-framework`
2. Build parser module: `./mvnw install -DskipTests -pl parser -am`
3. Create temporary `tools/` module with `ValidateCodebase.java`
4. Build and run validation: `./mvnw -f tools/pom.xml compile exec:java -Dexec.args="/tmp/spring-framework"`
5. Record results in STATE.md
6. Delete temporary `tools/` module (not committed)

## ValidateCodebase.java (created on-the-fly)

```java
package io.github.cowwoc.styler.tools;

import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class ValidateCodebase
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("Usage: ValidateCodebase <path-to-codebase>");
            System.exit(1);
        }

        Path codebasePath = Path.of(args[0]);
        if (!Files.isDirectory(codebasePath))
        {
            System.err.println("Error: Not a directory: " + codebasePath);
            System.exit(1);
        }

        System.out.println("Scanning for Java files in: " + codebasePath);

        List<Path> javaFiles = new ArrayList<>();
        Files.walkFileTree(codebasePath, new SimpleFileVisitor<>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            {
                if (file.toString().endsWith(".java"))
                    javaFiles.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
            {
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("Found " + javaFiles.size() + " Java files");
        System.out.println("Parsing...");

        Map<Path, String> errors = new LinkedHashMap<>();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger processedCount = new AtomicInteger();

        long startTime = System.currentTimeMillis();

        for (Path file : javaFiles)
        {
            try (Parser parser = Parser.fromPath(file))
            {
                ParseResult result = parser.parse();
                if (result instanceof ParseResult.Success)
                    successCount.incrementAndGet();
                else if (result instanceof ParseResult.Failure failure)
                {
                    String errorMsg = failure.errors().stream()
                        .map(e -> e.message() + " at position " + e.position())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Unknown error");
                    errors.put(file, errorMsg);
                }
            }
            catch (Exception e)
            {
                errors.put(file, e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            int count = processedCount.incrementAndGet();
            if (count % 500 == 0)
                System.out.println("Processed " + count + "/" + javaFiles.size() + " files...");
        }

        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println();
        System.out.println("=== VALIDATION RESULTS ===");
        System.out.println("Total files:  " + javaFiles.size());
        System.out.println("Succeeded:    " + successCount.get());
        System.out.println("Failed:       " + errors.size());
        System.out.println("Time:         " + elapsed + "ms");
        System.out.println("Throughput:   " + String.format("%.1f", javaFiles.size() * 1000.0 / elapsed) +
            " files/sec");

        if (!errors.isEmpty())
        {
            System.out.println();
            System.out.println("=== ERRORS BY TYPE ===");

            Map<String, List<Path>> errorsByType = new LinkedHashMap<>();
            for (Map.Entry<Path, String> entry : errors.entrySet())
            {
                String errorType = extractErrorType(entry.getValue());
                errorsByType.computeIfAbsent(errorType, k -> new ArrayList<>()).add(entry.getKey());
            }

            errorsByType.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .forEach(entry ->
                {
                    System.out.println();
                    System.out.println(entry.getKey() + " (" + entry.getValue().size() + " occurrences):");
                    entry.getValue().stream()
                        .limit(3)
                        .forEach(path -> System.out.println("  - " + path));
                    if (entry.getValue().size() > 3)
                        System.out.println("  ... and " + (entry.getValue().size() - 3) + " more");
                });
        }

        System.out.println();
        if (errors.isEmpty())
        {
            System.out.println("SUCCESS: All files parsed without errors!");
            System.exit(0);
        }
        else
        {
            System.out.println("FAILED: " + errors.size() + " files had parsing errors");
            System.exit(1);
        }
    }

    private static String extractErrorType(String errorMessage)
    {
        if (errorMessage.contains("Expected"))
        {
            int start = errorMessage.indexOf("Expected");
            int end = errorMessage.indexOf(" at position");
            if (end > start)
                return errorMessage.substring(start, end);
        }
        if (errorMessage.contains("Unexpected"))
        {
            int start = errorMessage.indexOf("Unexpected");
            int end = errorMessage.indexOf(" at position");
            if (end > start)
                return errorMessage.substring(start, end);
        }
        if (errorMessage.length() > 60)
            return errorMessage.substring(0, 60) + "...";
        return errorMessage;
    }
}
```

## tools/pom.xml (created on-the-fly)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>io.github.cowwoc.styler</groupId>
        <artifactId>styler</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>styler-tools</artifactId>
    <name>Styler Tools</name>
    <description>Temporary validation tool (not committed)</description>

    <properties>
        <project.root.basedir>${project.parent.basedir}</project.root.basedir>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.github.cowwoc.styler</groupId>
            <artifactId>styler-parser</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>io.github.cowwoc.styler.tools.ValidateCodebase</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Success Criteria

- [ ] Parser runs on all Spring Framework files
- [ ] Error count is 0 (or documented exceptions with new tasks created)
- [ ] Results recorded in STATE.md
- [ ] Temporary tools/ module deleted after validation

## Note

This is a **manual validation gate**, not an automated test. Run before marking v0.5 complete.
The validation tooling is created on-the-fly and NOT committed to the repository.
