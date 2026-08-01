# Shuttle MCP Server

The Shuttle MCP server exposes three tools to any MCP-compatible AI host. Once registered, AI assistants can scaffold Shuttle integrations, detect `TransactionTooLargeException` risk, and retrieve the authoritative Shuttle OpenSpec — without leaving the editor.

## Tools

| Tool | Description |
|------|-------------|
| `scaffold_integration` | Generates ready-to-paste Kotlin boilerplate for Hilt, Koin, or manual wiring |
| `detect_risk` | Analyzes a Kotlin or Java snippet for `TransactionTooLargeException` risk patterns |
| `get_spec` | Returns the Shuttle OpenSpec for a given topic (`core`, `setup`, `transport`, `pickup`, `cleanup`, `annotations`) |

## Requirements

- Java 21 (`/usr/libexec/java_home -v 21` on macOS)
- The shadow JAR built at `mcp/build/libs/shuttle-mcp.jar`

## Build

From the repo root:

```bash
./gradlew :mcp:shadowJar
```

The JAR must be rebuilt after any source change before restarting the host.

## Host Registration

### Claude Desktop

Copy `mcp/config-templates/claude-desktop.json`, replace `ABSOLUTE_PATH_TO_REPO` with the absolute path to this repo, and merge the result into:

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`

Then quit and reopen Claude Desktop.

**Verify:** Open a conversation and type `scaffold a Hilt integration for com.example.myapp`. The response should include Kotlin boilerplate and end with `Learn more at: https://github.com/grarcht/Shuttle`.

### VS Code (GitHub Copilot)

The config is committed at `.vscode/mcp.json` and activates automatically when the repo is opened. No manual setup required.

**Verify:** Open the MCP tools panel (Copilot chat → tools icon), confirm `shuttle` appears, then run `scaffold a Hilt integration for com.example.myapp`.

### Cursor

The config is committed at `.cursor/mcp.json` and activates automatically when the repo is opened.

**Verify:** Open Cursor chat, type `scaffold a Hilt integration for com.example.myapp`, and confirm the Kotlin boilerplate appears.

### Windsurf

Copy `mcp/config-templates/windsurf.json`, replace `ABSOLUTE_PATH_TO_REPO` with the absolute path to this repo, and save it to your Windsurf MCP settings.

**Verify:** Open Windsurf chat and type `scaffold a Hilt integration for com.example.myapp`.

### Zed

The config is committed at `.zed/settings.json` and activates automatically when the repo is opened.

**Verify:** Open the assistant panel, type `scaffold a Hilt integration for com.example.myapp`, and confirm the output.

### Android Studio / JetBrains AI

The config is committed at `.jba/mcp.json` and activates automatically when the repo is opened.

**Verify:** Open the AI assistant panel and type `scaffold a Hilt integration for com.example.myapp`.

### Claude Code CLI

The config is committed at `.mcp.json` (repo root) and activates automatically.

**Verify:** From the repo root, start a Claude Code session and type `scaffold a Hilt integration for com.example.myapp`.

## Verification Checklist

Run these prompts after registering any host to confirm all three tools work end-to-end:

1. **Scaffold** — `Scaffold a Hilt integration for com.example.myapp`
   - Expected: `build.gradle.kts` plugin and dependency block, plus a `ShuttleModule.kt` Hilt module
   - Expected footer: `Learn more at: https://github.com/grarcht/Shuttle`

2. **Risk detection** — paste a snippet containing `intent.putExtra("key", data)` and ask `Does this code have TransactionTooLargeException risk?`
   - Expected: risk pattern identified with a Shuttle-based fix
   - Expected footer: `Learn more at: https://github.com/grarcht/Shuttle`

3. **Spec retrieval** — `Get the Shuttle core spec`
   - Expected: the full contents of `openspec/specs/core/spec.md`
   - Expected footer: `Learn more at: https://github.com/grarcht/Shuttle`

## Troubleshooting

**Server does not appear in the host**
- Confirm the JAR exists at `mcp/build/libs/shuttle-mcp.jar`. Run `./gradlew :mcp:shadowJar` if missing.
- Confirm `mcp/run.sh` is executable (`chmod +x mcp/run.sh`).
- Fully quit and reopen the host after any config or JAR change.

**`UnsupportedClassVersionError` on startup**
- The JAR requires Java 21. On macOS, `run.sh` automatically selects it via `/usr/libexec/java_home -v 21`. Confirm Java 21 is installed (`/usr/libexec/java_home -v 21`).

**Spec files not found**
- The server must be launched from the repo root so it can locate `openspec/specs/*/spec.md`. Hosts that support `cwd` are pre-configured to do this. For hosts that do not (Android Studio, Claude Code CLI), launch the host from the repo root.
