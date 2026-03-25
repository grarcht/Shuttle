# Contributing to Shuttle

Shuttle is an open source project and contributions are welcome. This guide covers everything you need to get a pull request merged cleanly and efficiently.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Reporting Issues](#reporting-issues)
- [Requesting Features](#requesting-features)
- [Getting Started](#getting-started)
- [Branching Strategy](#branching-strategy)
- [Making Changes](#making-changes)
- [Pull Request Checklist](#pull-request-checklist)
- [Code Standards](#code-standards)
- [Commit Messages](#commit-messages)

---

## Code of Conduct

By contributing, you agree to keep this project a respectful and welcoming space for everyone. Harassment, discrimination, or hostile behavior of any kind will not be tolerated.

---

## Reporting Issues

Issues are tracked on [GitHub](https://github.com/grarcht/Shuttle/issues). Use the issue tracker to report bugs, ask questions, or request features.

Before filing a new issue:

1. **Search first.** Check the existing issues to see if the problem is already reported. Avoiding duplicates helps maximize the time available for fixes and new features.
2. **Use the issue template.** Fill in all requested fields. The more context you provide, the faster the issue can be diagnosed and reproduced.
3. **Include a minimal reproduction.** A focused code snippet or sample project that demonstrates the problem is far more useful than a long description alone.

A good bug report includes:
- Shuttle version
- Android API level and device/emulator details
- Steps to reproduce
- Expected vs. actual behavior
- Relevant stack traces or logs

---

## Requesting Features

Feature requests are welcome via the [issue tracker](https://github.com/grarcht/Shuttle/issues). When requesting a feature, describe:

- The problem you are trying to solve
- Why you think it belongs in Shuttle rather than in the consuming app
- Any alternative approaches you have considered

---

## Getting Started

1. **Fork** the repository on GitHub
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/<your-username>/Shuttle.git
   cd Shuttle
   ```
3. **Set the upstream remote** so you can pull in future changes:
   ```bash
   git remote add upstream https://github.com/grarcht/Shuttle.git
   ```
4. **Build the project** to confirm everything works before making changes:
   ```bash
   ./gradlew build
   ```

---

## Branching Strategy

All pull requests must target the **`develop`** branch. PRs targeting `main` will not be accepted.

| Branch | Purpose |
|---|---|
| `main` | Stable, released code |
| `develop` | Active development, target for all PRs |
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `chore/<name>` | Dependency updates, build changes, cleanup |

**Create a branch from `develop`:**
```bash
git checkout develop
git pull upstream develop
git checkout -b feature/my-feature
```

---

## Making Changes

- Keep changes focused. One PR should do one thing.
- If you are fixing a bug, include a test that would have caught it.
- If you are adding a feature, include tests covering the new behavior.
- Run the full test suite before submitting:
  ```bash
  ./gradlew test
  ```
- Run static analysis and confirm there are no new violations:
  ```bash
  ./gradlew detekt
  ```

---

## Pull Request Checklist

Before opening a PR, confirm the following:

- [ ] PR targets the `develop` branch
- [ ] Code builds cleanly with no errors or warnings
- [ ] All existing tests pass
- [ ] New behavior is covered by tests
- [ ] Detekt reports no new violations
- [ ] Code follows the standards described below
- [ ] Commit messages follow the format described below
- [ ] PR description explains what changed and why

---

## Code Standards

Shuttle's architecture is built around quality attributes: **readability, maintainability, recognizability, reusability, and usability.** Contributions should reflect these same values.

Best practices for **solution and software architecture, object-oriented programming, Kotlin, and Android development** must be followed. If you are unsure whether an approach aligns with these standards, open an issue or discussion before writing the code.

**General guidelines:**

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Keep functions and classes small and focused on a single responsibility
- Avoid unnecessary abstractions, but respect the existing layering of the framework
- Prefer explicit over clever; code is read far more often than it is written
- Document public API with KDoc where the intent is not obvious from the signature alone
- Do not introduce large transitive dependencies; Shuttle deliberately keeps its footprint lean

**Architecture alignment:**

Shuttle is structured as a layered Solution Building Block (SBB) framework. When contributing:

- Framework core changes belong in the `framework` module
- New persistence integrations belong in `framework-integrations` and `framework-integrations-extensions`
- New addons belong in `framework-addons`
- Changes that cross module boundaries should be discussed in an issue first

---

## Commit Messages

Use a short, descriptive prefix to categorize commits:

| Prefix | Use for |
|---|---|
| `Add:` | New functionality |
| `Fix:` | Bug fixes |
| `Change:` | Updates to existing behavior |
| `Remove:` | Removing code or dependencies |
| `Chore:` | Build scripts, dependencies, tooling |
| `Docs:` | Documentation only |
| `Test:` | Test additions or corrections |

**Format:**
```
Prefix: Short description in present tense

Optional longer explanation if the change needs context.
Reference any related issues: Closes #123
```

**Examples:**
```
Add: ShuttleService support for remote and local Android services

Fix: Cargo not cleaned up when navigating back across process boundary
Closes #42

Chore: Update AGP to 8.10.1 and Gradle wrapper to 8.14.1
```

---

Thank you for taking the time to contribute. Every issue filed, bug fixed, and improvement made helps the project get better for everyone using it.