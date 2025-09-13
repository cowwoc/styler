# 🚫 OUT OF SCOPE TASKS

**The following types of tasks are explicitly out of scope and should not be implemented:**

## Architecture & Communication Patterns
- **WebSocket implementations** - Use HTTP-only communication for progressive updates
- **React architecture** - Keep frontend simple, avoid complex state management frameworks
- **Real-time communication** - No persistent connections, use request/response only
- **Server-side state management** - Server must remain stateless (progressive data managed client-side)
- **Session storage** - No server-side session management or user data persistence
- **Multi-client support** - Only browser client needs to be supported
- **REST API design** - No need for generic REST patterns for third-party clients
- **API versioning** - No backward compatibility requirements for external consumers
- **Push notifications** - No server-initiated updates, client polls as needed for progressive updates

## External Dependencies & Infrastructure  
- **Redis integration** - No external caching systems
- **Database systems** - No external databases (PostgreSQL, MySQL, etc.)
- **Message queues** - No RabbitMQ, Kafka, or similar systems
- **Container orchestration** - No Kubernetes, Docker Swarm implementations
- **Cloud services** - No AWS, Azure, GCP specific integrations
- **Microservices architecture** - Keep as monolithic application
- **Dependency injection frameworks** - No Guice, Spring DI, or external DI containers

## Third-Party Integrations & External Services
- **CRM integrations** - No Salesforce, HubSpot, etc.
- **Payment processing** - No Stripe, PayPal integrations
- **Email services** - No SendGrid, Mailgun integrations
- **Analytics platforms** - No Google Analytics, Mixpanel integrations
- **Social media APIs** - No Facebook, Twitter integrations
- **External tax services** - Tax engine must be self-contained, no external tax APIs
- **Financial data providers** - No integration with banks, investment platforms, or financial APIs
- **Government tax APIs** - No direct integration with CRA or Revenu Québec systems

## Advanced Infrastructure
- **Service mesh** - No Istio, Linkerd implementations
- **API gateways** - No Kong, Ambassador setups
- **Distributed tracing** - No Jaeger, Zipkin integrations
- **External monitoring** - No Prometheus, Grafana unless lightweight
- **Load balancers** - No HAProxy, nginx configurations
- **Cloud deployment** - No cloud-specific deployment configurations
- **Rate limiting** - No external rate limiting systems or frameworks
- **Correlation IDs** - No distributed correlation ID tracking systems
- **Structured logging frameworks** - No structured logging implementations beyond basic SLF4J
- **Domain events architecture** - No event-driven architecture or event sourcing patterns
- **Health check endpoints** - No HTTP health check or containerized deployment endpoints
- **YAML configuration files** - No YAML configuration (except JSON files for tax constants are in scope)

## Code Compatibility & Legacy Support
- **Backward compatibility methods** - No deprecated method retention for internal code
- **Compatibility layers** - No compatibility interfaces for internal refactoring
- **Legacy API support** - No support for outdated internal interfaces
- **Method deprecation** - Remove old methods directly rather than deprecating them
- **Third-party client compatibility** - Only browser client needs support

## Test Development Approach
- **Anticipatory test creation** - Do not write tests for functionality that doesn't exist yet
- **Test-first development** - Tests must be written AFTER implementation, not before
- **Future functionality tests** - No placeholder or skeleton tests for planned features
- **Broken build tolerance** - Build and tests must remain passing at all times
- **Speculative testing** - No tests based on anticipated API changes or future requirements

## Enforcement

**CRITICAL**: OUT OF SCOPE TASKS MUST NEVER BE ADDED TO TODO.MD OR WORKED ON. If a task involves any of the above technologies, **REJECT** it completely.

**Alternative Approaches**: If functionality is needed, use:
- Pure Java libraries
- Lightweight embedded solutions
- In-memory implementations
- File-based storage where needed
- Simple HTTP endpoints for browser communication
- Client-side state management in browser for progressive input
- Self-contained tax calculation implementations
- JSON configuration files for tax constants and temporal rules
- Direct method replacement instead of deprecation for internal code
- **Implementation-first testing**: Write functionality first, then add corresponding tests to maintain build integrity

## Tax Engine Configuration Guidelines

**IN SCOPE**: Tax rule configuration using:
- JSON configuration files for tax constants and rules
- Date-based rule application for temporal tax changes
- Jurisdiction-specific configuration file organization
- Runtime loading of tax configuration data
- File-based storage for tax rule versioning

**OUT OF SCOPE**: Complex tax rule management:
- **External tax service integration** - Tax calculations must be self-contained
- **Database-driven tax rules** - Use JSON file configuration only
- **Real-time tax rule updates** - Configuration loaded at startup/runtime as needed
- **Distributed tax rule synchronization** - Single application instance tax rule management

## Progressive Input Implementation Guidelines

**IN SCOPE**: Simple progressive input using:
- Client-side state management for incremental data
- HTTP requests triggered by client-side data changes
- Browser local storage for data persistence
- Simple form validation and user feedback

**OUT OF SCOPE**: Complex progressive input patterns:
- **Server-side session tracking** - All progressive state managed client-side
- **Real-time synchronization** - Use simple request/response for updates
- **Complex state management frameworks** - Keep progressive updates simple
- **Database persistence** - Progressive data stored client-side only
- **Multi-user collaborative input** - Single user progressive input only