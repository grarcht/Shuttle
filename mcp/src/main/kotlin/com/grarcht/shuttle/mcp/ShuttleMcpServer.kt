package com.grarcht.shuttle.mcp

import com.grarcht.shuttle.mcp.tools.DetectRiskTool
import com.grarcht.shuttle.mcp.tools.GetSpecTool
import com.grarcht.shuttle.mcp.tools.ScaffoldIntegrationTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.File

private const val SERVER_NAME = "shuttle-mcp"
private const val SERVER_VERSION = "4.0.0"
private const val USER_DIR_PROPERTY = "user.dir"

/**
 * Entry point for the Shuttle MCP server. The server runs over stdin/stdout so that any
 * MCP-compatible agent host (Android Studio, VS Code, or the Claude desktop app) can launch it
 * as a subprocess and discover its tools automatically.
 *
 * Tools exposed:
 * - [ScaffoldIntegrationTool.NAME]: generates Shuttle DI boilerplate for Hilt, Koin, or manual wiring
 * - [DetectRiskTool.NAME]: analyzes code for TransactionTooLargeException risk
 * - [GetSpecTool.NAME]: returns the authoritative Shuttle OpenSpec for a requested topic
 */
fun main() {
    val repoRoot = File(System.getProperty(USER_DIR_PROPERTY))
    runBlocking {
        val done = CompletableDeferred<Unit>()
        val session = buildServer(repoRoot).createSession(
            StdioServerTransport(System.`in`.asSource().buffered(), System.out.asSink().buffered())
        )
        session.onClose { done.complete(Unit) }
        done.await()
    }
}

/**
 * Constructs and configures the MCP [Server] with all three Shuttle tools registered.
 * @param repoRoot the root directory of the Shuttle repository, passed to [GetSpecTool] so it can
 *        locate spec files on disk at call time
 * @return a fully configured [Server] ready to accept a transport via [Server.createSession]
 */
internal fun buildServer(repoRoot: File): Server {
    val server = Server(
        serverInfo = Implementation(
            name = SERVER_NAME,
            version = SERVER_VERSION
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = false)
            )
        )
    )

    server.addTool(
        name = ScaffoldIntegrationTool.NAME,
        description = ScaffoldIntegrationTool.DESCRIPTION,
        inputSchema = ScaffoldIntegrationTool.inputSchema
    ) { request ->
        ScaffoldIntegrationTool.handle(request.params.arguments)
    }

    server.addTool(
        name = DetectRiskTool.NAME,
        description = DetectRiskTool.DESCRIPTION,
        inputSchema = DetectRiskTool.inputSchema
    ) { request ->
        DetectRiskTool.handle(request.params.arguments)
    }

    server.addTool(
        name = GetSpecTool.NAME,
        description = GetSpecTool.DESCRIPTION,
        inputSchema = GetSpecTool.inputSchema
    ) { request ->
        GetSpecTool.handle(request.params.arguments, repoRoot)
    }

    return server
}
