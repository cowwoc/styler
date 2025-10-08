# 🚫 OUT OF SCOPE - Java Code Formatter

**The following features are explicitly out of scope for the Styler Java code formatter:**

## Architecture & Communication Patterns
- **Complex state management frameworks** - Keep architecture simple, avoid over-engineering
- **Multi-user coordination** - Single-user CLI tool, no multi-user features needed

## External Dependencies & Infrastructure
- **Redis integration** - No external caching systems
- **Database systems** - No external databases (PostgreSQL, MySQL, etc.)
- **Message queues** - No RabbitMQ, Kafka, or similar systems
- **Container orchestration** - No Kubernetes, Docker Swarm implementations
- **Cloud services** - No AWS, Azure, GCP specific integrations
- **Microservices architecture** - Keep as monolithic application
- **Dependency injection frameworks** - No Guice, Spring DI, or external DI containers
- **JUnit testing framework** - Use TestNG exclusively for consistency and JPMS compatibility
- **Test mocking frameworks** - No Mockito, EasyMock, or other mocking libraries (use real objects or test stubs)
- **GraalVM native image** - No native compilation or startup optimization via GraalVM

## Third-Party Integrations & External Services
- **CRM integrations** - No Salesforce, HubSpot, etc.
- **Payment processing** - No Stripe, PayPal integrations
- **Email services** - No SendGrid, Mailgun integrations
- **Analytics platforms** - No Google Analytics, Mixpanel integrations
- **Social media APIs** - No Facebook, Twitter integrations
- **Authentication providers** - No OAuth, SAML, or external auth systems
- **File storage services** - No S3, Google Drive, Dropbox integrations
- **External API dependencies** - Minimize external service dependencies

## Code Analysis Beyond Formatting
- **Static code analysis** - No bug detection, security vulnerability scanning, or code quality metrics
- **Semantic analysis** - No type checking, unused variable detection, or dead code removal
- **Performance analysis** - No algorithmic complexity analysis or performance bottleneck detection
- **Code coverage analysis** - No test coverage measurement or gap identification
- **Dependency analysis** - No circular dependency detection or architecture compliance checking

## Language Support Beyond Java
- **Multi-language support** - Java only, no Kotlin, Scala, Groovy, or other JVM languages
- **Mixed-language projects** - No JavaScript, TypeScript, Python, or other language formatting
- **Configuration languages** - No XML, YAML formatting (TOML and JSON allowed for styler config files only)
- **Template languages** - No JSP, Thymeleaf, or other template formatting
- **Documentation formats** - No Markdown, AsciiDoc, or other documentation formatting

## Advanced IDE Features
- **Code completion** - No IntelliSense or autocomplete functionality
- **Refactoring tools** - No extract method, rename variable, or structural refactoring
- **Navigation features** - No go-to-definition, find usages, or symbol navigation
- **Debugging support** - No breakpoint integration or debug assistance
- **Code folding** - No collapsible regions or outline views
- **Syntax highlighting** - Output formatted text only, no color/syntax markup

## Version Control Integration
- **Git blame/history** - No integration with version control for blame or history views
- **Diff generation** - No visual diff tools or merge conflict resolution
- **Patch application** - No automatic patch generation or application
- **Branch-aware formatting** - No different formatting rules per git branch
- **Commit hook integration** - Basic CLI only, no sophisticated git hook management

## Enterprise/Team Features
- **User authentication** - No user accounts, permissions, or access control
- **Team collaboration** - No shared configurations, comments, or collaborative editing
- **Code review integration** - No integration with Gerrit, GitHub PR, or other review tools
- **Approval workflows** - No formatting rule approval or governance processes
- **Audit trails** - No detailed user activity logging or compliance reporting
- **Role-based permissions** - All users have same formatting capabilities

## Web/Cloud Services
- **Web-based interface** - Command-line tool only, no web UI
- **Cloud storage** - Local file-based configuration only
- **SaaS deployment** - Standalone CLI tool, not a hosted service

## Real-time/Interactive Features
- **Live formatting** - No real-time formatting during typing (format-on-save only)
- **Interactive configuration** - No GUI for configuration management
- **Preview modes** - No before/after previews or interactive formatting options
- **Undo/redo** - No formatting history or rollback capabilities beyond file backup
- **Incremental formatting** - Full file processing only, no partial/incremental updates

## Complex Build Integration
- **Maven/Gradle plugin complexity** - Basic plugin only, no complex lifecycle integration
- **Build caching** - No sophisticated build cache integration or dependency tracking
- **Incremental builds** - No build system incremental compilation integration
- **Multi-module coordination** - Process files independently, no cross-module formatting rules
- **Custom build phases** - Standard formatting only, no custom build phase integration

## Transformation Context API Simplifications
- **Complex authorization frameworks** - Rules run sequentially and should transform freely
- **Transaction management** - No need for transactions since rules execute sequentially per region
- **Conflict resolution systems** - No parallel rule conflicts in sequential execution model
- **Security audit trails** - No security event logging needed for code formatting operations
- **Resource limit enforcement** - Trust formatting rules to behave appropriately
- **Async execution support** - Sequential rule execution model doesn't require async operations
- **Complex validation frameworks** - Basic structural integrity validation sufficient

## Performance/Scalability Extremes
- **Massive file support** - Reasonable limits on file size (e.g., 10MB max per file)
- **Extreme concurrency** - Reasonable thread limits based on available CPU cores
- **Memory optimization** - Good performance but not extreme memory constraints
- **Distributed processing** - Single-machine processing only, no distributed computing
- **Real-time constraints** - Batch processing acceptable, no hard real-time requirements

## Legacy Java Support
- **Java 8 and below** - Java 25+ only for runtime, can format older source syntax
- **Legacy build tools** - Maven 3.6+ and Gradle 6+ only
- **Deprecated APIs** - No support for deprecated Java features in generated code
- **Binary compatibility** - Source code formatting only, no bytecode manipulation
- **Class file analysis** - Source code only, no .class file inspection or modification

## Java Preview Features
- **Preview APIs** - No use of Java preview features (e.g., StructuredTaskScope in Java 25)
- **Incubating APIs** - No use of incubating APIs that require `--enable-preview` flags
- **Experimental features** - Only stable, finalized Java APIs may be used in production code
- **Rationale**: Preview features can change between Java versions and require special compiler/runtime flags, making them unsuitable for production use
- **Alternative**: Use stable equivalents (e.g., ExecutorService with virtual threads instead of StructuredTaskScope)

## Enforcement

**CRITICAL**: OUT OF SCOPE FEATURES MUST NEVER BE ADDED TO TODO.MD OR IMPLEMENTED. If a requested feature involves any of the above areas, **REJECT** it completely and suggest alternatives within scope.

**Alternative Approaches**: When functionality is needed, prefer:
- **Simple file-based solutions** over complex database or cloud storage
- **Command-line interfaces** over web UIs or complex interactive features
- **Direct AST manipulation** over complex transformation frameworks
- **Sequential processing** over complex parallel or async architectures
- **Configuration files** over user management or authentication systems
- **Standard libraries** over external services or complex dependencies

**Focus**: Keep Styler as a focused, efficient Java code formatter that does one thing exceptionally well: formatting Java source code according to configurable style rules.