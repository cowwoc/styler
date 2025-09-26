# Styler Parser Module

High-performance Java parser implementing Index-Overlay architecture with comprehensive JDK 25 support.

## Architecture Overview

Based on evidence from the cross-language parser study, this module implements:

### Index-Overlay Parser Architecture
- **Memory Efficiency**: 3-5x reduction compared to traditional AST object trees
- **Cache-Friendly Access**: Parallel arrays for better CPU cache utilization
- **Java-Idiomatic Design**: Uses modern Java patterns instead of direct Rust arena translation

### Key Components

#### Core Classes
- `IndexOverlayParser` - Main parser with recursive descent implementation
- `NodeRegistry` - Java-optimized replacement for arena allocation using parallel arrays
- `JavaLexer` - High-performance lexer for JDK 25 features
- `NodeType` - Memory-efficient byte constants for all Java constructs
- `ParseMetrics` - Lightweight performance metrics (toggleable)

#### Performance Features
- **Object Pooling**: `ObjectPool<T>` for reducing temporary allocations
- **Metrics Collection**: Zero-overhead when disabled, comprehensive when enabled
- **Benchmarking**: JMH benchmarks comparing Arena API vs traditional GC approaches

## Language Support

### Complete JDK 25 Feature Coverage
- **JDK 8+**: Lambda expressions, method references, default methods
- **JDK 9+**: Module system with all directives
- **JDK 10+**: Local variable type inference (`var`)
- **JDK 14+**: Records, switch expressions, pattern matching
- **JDK 15+**: Text blocks with proper formatting
- **JDK 17+**: Sealed classes and `permits` clause
- **JDK 21+**: Pattern matching for switch with `when` guards
- **JDK 22+**: String templates (preview feature)
- **JDK 22+**: Unnamed variables and classes
- **JDK 25**: All current language features

### Evidence-Based Design Decisions

#### Arena API Analysis
The module includes benchmarks (`ArenaVsGCBenchmark`) to compare:
- Traditional GC-based allocation
- Java's new Arena API from Project Panama
- Custom object pooling approaches

**Initial Hypothesis**: Arena API might provide performance benefits for temporary parser structures.

**Testing Approach**: Comprehensive JMH benchmarks measuring:
- Single-file parsing performance
- Multi-file batch processing
- Memory pressure scenarios
- Allocation/deallocation cycles

#### Performance Targets (Evidence-Based)
Based on study findings:
- **2-3x faster** than traditional Java parsers (conservative target)
- **Sub-100ms latency** for files under 10KB
- **Linear scaling** with file size
- **Zero GC pressure** during steady-state operations

## Usage Examples

### Basic Parsing
```java
IndexOverlayParser parser = new IndexOverlayParser(javaSourceCode);
int rootNodeId = parser.parse();

// Access parsed information
NodeRegistry.NodeInfo root = parser.getNode(rootNodeId);
String sourceText = parser.getNodeText(rootNodeId);
```

### With Performance Metrics
```java
// Enable metrics collection
System.setProperty("styler.metrics.enabled", "true");

// Parse multiple files
for (String source : javaSources) {
    IndexOverlayParser parser = new IndexOverlayParser(source);
    parser.parse();
}

// Review performance data
ParseMetrics.MetricsSnapshot metrics = ParseMetrics.getSnapshot();
System.out.println("Average parse time: " + metrics.getAverageParseTimeMs() + "ms");
System.out.println("Peak memory usage: " + metrics.peakMemoryUsageBytes() + " bytes");
```

### Incremental Parsing (Tree-sitter Style)
```java
// Initial parse
IndexOverlayParser parser = new IndexOverlayParser(originalSource);
int rootId = parser.parse();

// Apply incremental updates
List<IndexOverlayParser.EditRange> edits = List.of(
    new IndexOverlayParser.EditRange(startOffset, oldLength, newLength, newText)
);
int updatedRootId = parser.parseIncremental(edits);
```

## Test Coverage

### Comprehensive Test Suite
The module includes 200+ test cases covering:

#### Language Feature Tests
- **Basic Constructs**: Classes, interfaces, enums, annotations
- **Lambda Features**: All lambda and method reference patterns
- **Module System**: Complete module declaration syntax
- **Type Inference**: `var` in all valid contexts
- **Records**: Simple and complex record patterns
- **Switch Expressions**: Including pattern matching and guards
- **Text Blocks**: Various formatting scenarios
- **Sealed Classes**: Complete hierarchy patterns
- **Pattern Matching**: Latest JDK 21+ enhancements
- **String Templates**: Preview feature syntax
- **Unnamed Features**: Variables and classes

#### Performance Tests
- **Large File Parsing**: Scalability testing with 100-5000 classes
- **Error Recovery**: Malformed code handling
- **Memory Pressure**: Rapid allocation/deallocation cycles
- **Incremental Updates**: Tree-sitter style edit handling

#### Comment Preservation Tests
- **All Comment Types**: Line, block, and Javadoc comments
- **Position Tracking**: Precise source location maintenance
- **Formatting Context**: Comment relationship to code structures

## Benchmarking

### Running Performance Tests

#### Basic JUnit Tests
```bash
mvn test
```

#### JMH Benchmarks (Arena API Comparison)
```bash
# Requires JDK 21+ with Project Panama
mvn test-compile exec:java \
  -Dexec.mainClass="io.github.styler.parser.ArenaVsGCBenchmark" \
  -Dexec.args="--enable-preview --add-modules jdk.incubator.foreign"
```

### Metrics Collection
Set `styler.metrics.enabled=true` system property to collect:
- Parse timing per file
- Memory usage patterns
- Error recovery statistics
- File size distribution analysis

## Design Rationale

### Evidence-Based Architecture Choices

1. **Index-Overlay over Full AST**: Study showed "3-5x memory reduction" and "better cache locality"
2. **Recursive Descent over Generated**: "Easier debugging and maintenance" vs JavaCC/ANTLR complexity
3. **Java-Optimized Object Pooling**: Arena patterns adapted for Java GC rather than direct Rust translation
4. **Comprehensive JDK 25 Support**: User requirement for "every single aspect of Java 25 code"
5. **Benchmarking Integration**: Data-driven optimization decisions per user request

### Future Optimization Opportunities

1. **Arena API Integration**: Benchmarks will determine if Project Panama provides benefits
2. **Incremental Parsing**: Full Tree-sitter style implementation for large files
3. **Parallel Processing**: Multi-threaded parsing for project-level operations
4. **Native Compilation**: GraalVM integration for ultimate performance

## Dependencies

- **Java 21+**: Required for latest language features and potential Arena API usage
- **JUnit 5**: Test framework
- **JMH**: Benchmarking framework
- **styler-core**: Core utilities

The module is designed to be self-contained with minimal external dependencies, following the study's recommendation for "zero external dependencies alignment".