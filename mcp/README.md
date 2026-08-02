# Shuttle MCP Server

The Shuttle MCP server gives any AI assistant in any MCP-compatible editor deep, accurate knowledge of Shuttle: how to integrate it, where `TransactionTooLargeException` risk lives in your code, and what the spec says. Your assistant can answer these questions from inside your own project, without you leaving the editor or digging through documentation.

The server is published to the [MCP Registry](https://registry.modelcontextprotocol.io) and runs as a Docker image on GHCR, so you do not need to clone or open this repo to benefit from it. Point your editor at the registry entry and Shuttle expertise travels with you into every Android project you work on. For teams, this means every developer gets the same accurate, consistent guidance on integration patterns and risk detection from day one, regardless of their editor or experience level.

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

## MCP Support

Open this repo in any of the editors below and the Shuttle MCP server activates automatically — no installation, no manual registration, no copy-pasting config. Your AI assistant immediately gains the ability to scaffold integrations, catch `TransactionTooLargeException` risk in your code, and answer questions directly from the Shuttle spec. It just works.

| Editor / Host | Auto-wires? | Config location |
|---|---|---|
| VS Code (GitHub Copilot) | Yes | `.vscode/mcp.json` |
| Cursor | Yes | `.cursor/mcp.json` |
| Zed | Yes | `.zed/settings.json` |
| Android Studio / JetBrains AI | Yes | `.jba/mcp.json` |
| Claude Code CLI | Yes | `.mcp.json` |
| Claude Desktop | One-time setup | see below |
| Windsurf | One-time setup | see below |
| Android Studio (Gemini) | One-time setup today; auto when Google ships project-level MCP | `.gemini/settings.json` |

### VS Code (GitHub Copilot)

The config is committed at `.vscode/mcp.json` and activates automatically when the repo is opened. No manual setup required.

**Verify:** Open the MCP tools panel (Copilot chat → tools icon), confirm `shuttle` appears, then run `scaffold a Hilt integration for com.example.myapp`.

### Cursor

The config is committed at `.cursor/mcp.json` and activates automatically when the repo is opened.

**Verify:** Open Cursor chat, type `scaffold a Hilt integration for com.example.myapp`, and confirm the Kotlin boilerplate appears.

### Zed

The config is committed at `.zed/settings.json` and activates automatically when the repo is opened.

**Verify:** Open the assistant panel, type `scaffold a Hilt integration for com.example.myapp`, and confirm the output.

### Android Studio / JetBrains AI

The config is committed at `.jba/mcp.json` and activates automatically when the repo is opened.

**Verify:** Open the AI assistant panel and type `scaffold a Hilt integration for com.example.myapp`.

### Claude Code CLI

The config is committed at `.mcp.json` (repo root) and activates automatically.

**Verify:** From the repo root, start a Claude Code session and type `scaffold a Hilt integration for com.example.myapp`.

### Claude Desktop

Copy `mcp/config-templates/claude-desktop.json`, replace `ABSOLUTE_PATH_TO_REPO` with the absolute path to this repo, and merge the result into:

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`

Then quit and reopen Claude Desktop.

**Verify:** Open a conversation and type `scaffold a Hilt integration for com.example.myapp`. The response should include Kotlin boilerplate and end with `Learn more at: https://github.com/grarcht/Shuttle`.

### Windsurf

Copy `mcp/config-templates/windsurf.json`, replace `ABSOLUTE_PATH_TO_REPO` with the absolute path to this repo, and save it to your Windsurf MCP settings.

**Verify:** Open Windsurf chat and type `scaffold a Hilt integration for com.example.myapp`.

### Android Studio (Gemini)

The MCP config is already committed at `.gemini/settings.json`, which is the project-level path that Google's tooling is standardising on. Once Android Studio ships project-level MCP support, every contributor who opens this repo will get Shuttle wired into Gemini automatically — same as Cursor and VS Code users get today.

Until that ships, a one-time IDE setup takes about thirty seconds:

1. Open **Settings → Tools → Gemini → Model Context Protocol (MCP)**
2. Click **+** and set:
   - **Command:** `<absolute-path-to-repo>/mcp/run.sh`
   - **Working directory:** `<absolute-path-to-repo>`
3. Click **OK** and restart Android Studio.

**Verify:** Open the Gemini agent panel, paste a snippet containing `intent.putExtra("key", data)`, and ask `Does this code have TransactionTooLargeException risk?` Gemini should identify the risk pattern and recommend Shuttle as the fix.

> **Tip:** Gemini invokes `detect_risk` when you ask explicitly about `TransactionTooLargeException` risk. Phrase your question that way for the best results.

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

## MCP Registry

The Shuttle MCP server is published to the official MCP Registry and discoverable by any MCP-compatible host:

```bash
curl "https://registry.modelcontextprotocol.io/v0.1/servers?search=io.github.grarcht/shuttle"
```

### Republishing

Run `mcp/publish.sh` from the repo root whenever `SERVER_VERSION` in `ShuttleMcpServer.kt` changes:

```bash
./mcp/publish.sh 4.1.0
```

The script will update `server.json`, rebuild the JAR, push a new multi-platform Docker image to GHCR, and publish the updated metadata to the registry.

## Troubleshooting

**Server does not appear in the host**
- Confirm the JAR exists at `mcp/build/libs/shuttle-mcp.jar`. Run `./gradlew :mcp:shadowJar` if missing.
- Confirm `mcp/run.sh` is executable (`chmod +x mcp/run.sh`).
- Fully quit and reopen the host after any config or JAR change.

**`UnsupportedClassVersionError` on startup**
- The JAR requires Java 21. On macOS, `run.sh` automatically selects it via `/usr/libexec/java_home -v 21`. Confirm Java 21 is installed (`/usr/libexec/java_home -v 21`).

**Spec files not found**
- The server must be launched from the repo root so it can locate `openspec/specs/*/spec.md`. Hosts that support `cwd` are pre-configured to do this. For hosts that do not (Android Studio, Claude Code CLI), launch the host from the repo root.
