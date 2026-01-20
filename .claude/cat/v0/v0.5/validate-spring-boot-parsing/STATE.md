# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** []
- **Last Updated:** 2026-01-19

## Acceptance Criteria

**MANDATORY: Zero parsing errors required.**

This task is complete ONLY when `parser:check` passes on the entire Spring Boot codebase
with **0 failures**.

```bash
# Acceptance test command
./mvnw exec:java -pl parser -Dexec.mainClass=com.stazsoftware.styler.parser.ParserCli \
  -Dexec.args="check ~/spring-boot"

# Required output
# Failed: 0
```

Until all files parse successfully, this task remains in-progress and blocks v0.5 completion.
