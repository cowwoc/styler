<h1>
  <img src="docs/logo.svg" alt="" width="32" height="32" valign="middle"/>
  Styler
</h1>

[![Supports JDK 25](https://img.shields.io/badge/Supports-JDK%2025-blue)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Source%20Available-green)](LICENSE.md)

**Your style. Their style. Same codebase.**

I like Allman braces. You like K&R. Our team's repo uses whatever the tech lead decided in 2019.

Styler lets everyone see code in their preferred format while the repository stays consistent. No more style wars. No more "fixing" formatting in PRs. Just code.

---

**Building this in public** - [Follow along on LinkedIn](https://www.linkedin.com/search/results/content/?fromMember=%5B%22ACoAAABwrqsBAtWMgtLt3YTqxSfbkvIK9tOoks8%22%5D&keywords=%23buildinpublic) as I ship v1.0 in 18 days.

If this solves a problem you have, [star the repo](https://github.com/cowwoc/styler) - it helps others find it.

---

## Try It

```bash
mvn clean install
java -jar cli/target/styler-cli-*.jar format src/
```

## Configure

Create `.styler.toml` in your project root:

```toml
maxLineLength = 120
```

See [docs/configuration.md](docs/configuration.md) for all options.

## Features

- **See code your way** - Personal style locally, team style in the repo
- **400 files/second** - Virtual threads for large codebases
- **JDK 25 ready** - Pattern matching, records, sealed classes, switch expressions
- **AI-friendly** - Structured output for GitHub Copilot, Claude Code, and others

## Roadmap

Launching January 24, 2026.

| Phase | Status |
|-------|--------|
| Core (parser, CLI, Maven plugin) | ✅ Done |
| Parser compatibility | 🔄 Now |
| GitHub PR extension | Next |
| Launch | Jan 24 |

## About

Built by [Gili Tzabari](https://github.com/cowwoc), author of [requirements.java](https://github.com/cowwoc/requirements.java) and [cmake-maven-plugin](https://github.com/cmake-maven-plugin/cmake-maven-plugin).

## License

Source-available. Free for personal use. [Details](LICENSE.md).
