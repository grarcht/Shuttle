package com.grarcht.shuttle.mcp.tools

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.nio.file.Files

private const val FILE_NOT_FOUND_MARKER = "Spec file not found"
private const val KNOWN_TOPIC_VALUE = "core"
private const val NONEXISTENT_ROOT_PATH = "/nonexistent/path/that/cannot/exist"
private const val REPO_ROOT_STUB_PATH = "."
private val REPO_ROOT_STUB = File(REPO_ROOT_STUB_PATH)
private const val SPEC_CONTENT = "# Test Spec\n\nThis is a test spec."
private const val SPEC_DIR_PATH = "openspec/specs/core"
private const val SPEC_FILE_NAME = "spec.md"
private const val TEMP_DIR_PREFIX = "shuttle-spec-test"
private const val TOPIC_KEY = "topic"
private const val UNKNOWN_TOPIC_MARKER = "Unknown topic"
private const val UNKNOWN_TOPIC_VALUE = "nonexistent"
private const val VALID_TOPICS_MARKER = "Valid topics"

/**
 * Verifies every branch in [GetSpecTool]. The tool must correctly return spec content for known
 * topics whose files exist, and return clear error messages for unknown topics or missing files.
 * Incorrect behavior would give agents stale or absent documentation and cause them to generate
 * wrong integration code.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetSpecToolTest {

    @Test
    fun verifyHandleWithNullArgumentsReturnsUnknownTopicError() = runBlocking {
        val result = GetSpecTool.handle(null, REPO_ROOT_STUB)
        val text = result.content.first().toString()

        assertAll(
            { assertNotNull(result) },
            { assertTrue(text.contains(UNKNOWN_TOPIC_MARKER), "Should report unknown topic") },
            { assertTrue(text.contains(VALID_TOPICS_MARKER), "Should list valid topics") }
        )
    }

    @Test
    fun verifyHandleWithUnknownTopicReturnsErrorWithValidTopics() = runBlocking {
        val args = buildJsonObject { put(TOPIC_KEY, UNKNOWN_TOPIC_VALUE) }

        val result = GetSpecTool.handle(args, REPO_ROOT_STUB)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(UNKNOWN_TOPIC_MARKER), "Should report unknown topic") },
            { assertTrue(text.contains(VALID_TOPICS_MARKER), "Should list valid topics") }
        )
    }

    @Test
    fun verifyHandleWithValidTopicAndMissingFileReturnsFileNotFoundError() = runBlocking {
        val args = buildJsonObject { put(TOPIC_KEY, KNOWN_TOPIC_VALUE) }
        val missingRoot = File(NONEXISTENT_ROOT_PATH)

        val result = GetSpecTool.handle(args, missingRoot)
        val text = result.content.first().toString()

        assertTrue(text.contains(FILE_NOT_FOUND_MARKER), "Should report missing spec file")
    }

    @Test
    fun verifyHandleWithValidTopicAndExistingFileReturnsSpecContent() = runBlocking {
        val repoRoot = Files.createTempDirectory(TEMP_DIR_PREFIX).toFile()
        val specDir = File(repoRoot, SPEC_DIR_PATH).also { it.mkdirs() }
        File(specDir, SPEC_FILE_NAME).writeText(SPEC_CONTENT)

        try {
            val args = buildJsonObject { put(TOPIC_KEY, KNOWN_TOPIC_VALUE) }
            val result = GetSpecTool.handle(args, repoRoot)
            val text = result.content.first().toString()

            assertTrue(text.contains(SPEC_CONTENT), "Should return the spec file content")
        } finally {
            repoRoot.deleteRecursively()
        }
    }
}
