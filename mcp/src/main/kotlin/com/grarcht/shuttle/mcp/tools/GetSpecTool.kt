package com.grarcht.shuttle.mcp.tools

import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.io.File

private const val SPEC_NOT_FOUND_GUIDANCE = " Ensure the MCP server is run from the Shuttle repo root."
private const val SPEC_PATH_ANNOTATIONS = "openspec/specs/annotations/spec.md"
private const val SPEC_PATH_CLEANUP = "openspec/specs/cleanup/spec.md"
private const val SPEC_PATH_CORE = "openspec/specs/core/spec.md"
private const val SPEC_PATH_PICKUP = "openspec/specs/pickup/spec.md"
private const val SPEC_PATH_SETUP = "openspec/specs/setup/spec.md"
private const val SPEC_PATH_TRANSPORT = "openspec/specs/transport/spec.md"
private const val TOPICS_SEPARATOR = ", "
private const val TOPIC_ANNOTATIONS = "annotations"
private const val TOPIC_CLEANUP = "cleanup"
private const val TOPIC_CORE = "core"
private const val TOPIC_DESCRIPTION = "The spec topic to retrieve. One of: core, setup, transport, pickup, cleanup, annotations."
private const val TOPIC_KEY = "topic"
private const val TOPIC_PICKUP = "pickup"
private const val TOPIC_SETUP = "setup"
private const val TOPIC_TRANSPORT = "transport"
private const val UNKNOWN_TOPIC_PREFIX = "Unknown topic: \""
private const val UNKNOWN_TOPIC_SUFFIX = "\". Valid topics: "
private const val VALID_TOPICS_SUFFIX = "."

/**
 * MCP tool that returns the Shuttle OpenSpec content for a requested topic. The spec files are
 * read from disk at call time so agents always receive the current, authoritative version rather
 * than a stale snapshot embedded in the server binary.
 */
internal object GetSpecTool {

    const val NAME = "get_spec"
    const val DESCRIPTION =
        "Returns the Shuttle OpenSpec for a given topic. " +
            "Use this to get authoritative, up-to-date guidance on any part of the Shuttle API."

    val inputSchema = ToolSchema(
        properties = buildJsonObject {
            putJsonObject(TOPIC_KEY) {
                put(SCHEMA_KEY_TYPE, SCHEMA_TYPE_STRING)
                put(SCHEMA_KEY_DESCRIPTION, TOPIC_DESCRIPTION)
            }
        },
        required = listOf(TOPIC_KEY)
    )

    private val topicToSpecPath = mapOf(
        TOPIC_CORE to SPEC_PATH_CORE,
        TOPIC_SETUP to SPEC_PATH_SETUP,
        TOPIC_TRANSPORT to SPEC_PATH_TRANSPORT,
        TOPIC_PICKUP to SPEC_PATH_PICKUP,
        TOPIC_CLEANUP to SPEC_PATH_CLEANUP,
        TOPIC_ANNOTATIONS to SPEC_PATH_ANNOTATIONS
    )

    /**
     * Handles an MCP tool call by reading the requested spec file from disk and returning its
     * contents.
     * @param arguments the raw JSON arguments from the MCP tool call
     * @param repoRoot the root directory of the Shuttle repository, used to locate spec files
     * @return [CallToolResult] containing the spec markdown, or an error message for unknown topics
     */
    suspend fun handle(arguments: JsonObject?, repoRoot: File): CallToolResult {
        val topic = arguments?.get(TOPIC_KEY)?.jsonPrimitive?.content?.lowercase() ?: ""

        val relativePath = topicToSpecPath[topic]
            ?: return CallToolResult(
                content = listOf(
                    TextContent(
                        text = "$UNKNOWN_TOPIC_PREFIX$topic$UNKNOWN_TOPIC_SUFFIX" +
                            "${topicToSpecPath.keys.sorted().joinToString(TOPICS_SEPARATOR)}$VALID_TOPICS_SUFFIX" +
                            "\n\n$DOCS_FOOTER"
                    )
                )
            )

        val specFile = File(repoRoot, relativePath)
        return if (specFile.exists()) {
            CallToolResult(content = listOf(TextContent(text = specFile.readText() + "\n\n$DOCS_FOOTER")))
        } else {
            CallToolResult(
                content = listOf(
                    TextContent(
                        text = "Spec file not found at $relativePath.$SPEC_NOT_FOUND_GUIDANCE\n\n$DOCS_FOOTER"
                    )
                )
            )
        }
    }
}
