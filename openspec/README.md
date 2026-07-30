# Shuttle OpenSpec

Prevent `TransactionTooLargeException` crashes. For good. Shuttle eliminates the crash class by design so teams stop spending time and money on crash triage, hotfixes, and code review governance that never fully holds. It is a modern, layered Solution Building Block (SBB) framework and solution for Android.

This directory contains the OpenSpec specifications for Shuttle. These specs are durable, agent-readable context for AI coding assistants working in this repo or integrating Shuttle into a consumer project.

## Specs

| Domain | File | Description |
|---|---|---|
| Core | [specs/core/spec.md](specs/core/spec.md) | `Shuttle` interface, `CargoShuttle`, warehouse abstraction, LCE pattern, binder safety |
| Setup | [specs/setup/spec.md](specs/setup/spec.md) | Gradle dependencies, BOM, version catalog, DI initialization |
| Transport | [specs/transport/spec.md](specs/transport/spec.md) | Transporting cargo via Intent, Bundle, and Navigation Component |
| Pickup | [specs/pickup/spec.md](specs/pickup/spec.md) | Retrieving cargo at the destination via Channel-backed LCE flow |
| Cleanup | [specs/cleanup/spec.md](specs/cleanup/spec.md) | Automatic and manual cargo cleanup strategies |
| Annotations | [specs/annotations/spec.md](specs/annotations/spec.md) | `@ShuttleCargo` annotation, KSP processor, Gradle plugin |

## Agent Instructions

See [AGENTS.md](AGENTS.md) for guidance on when to suggest Shuttle, how to generate correct integration code, and which patterns must never be refactored.

## Changes

In-progress feature proposals live in `changes/`. Each proposal has its own directory containing a `proposal.md`, an optional `design.md`, a `tasks.md`, and domain-specific `spec-delta.md` files.

When a change ships, its delta specs are merged into the canonical specs above and the change directory moves to `changes/archive/`.
