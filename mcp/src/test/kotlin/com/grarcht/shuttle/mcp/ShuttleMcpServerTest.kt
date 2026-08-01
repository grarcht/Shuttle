package com.grarcht.shuttle.mcp

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private const val DETECT_RISK_REQUEST = """{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"detect_risk","arguments":{"code":"intent.putExtra(\"key\", data)"}}}"""
private const val GET_SPEC_REQUEST = """{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"get_spec","arguments":{"topic":"unknown_test_topic"}}}"""
private const val INITIALIZE_REQUEST =
    """{"jsonrpc":"2.0","id":0,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0.0"}}}"""
private const val INITIALIZED_NOTIFICATION = """{"jsonrpc":"2.0","method":"notifications/initialized"}"""
private const val READER_THREAD_NAME = "mcp-stdout-reader"
private const val REPO_ROOT_STUB_PATH = "."
private val REPO_ROOT_STUB = File(REPO_ROOT_STUB_PATH)
private const val RESPONSE_TIMEOUT_SECONDS = 10L
private const val SCAFFOLD_REQUEST =
    """{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"scaffold_integration","arguments":{"di_framework":"hilt","package_name":"com.example.test"}}}"""
private const val SERVER_EXIT_TIMEOUT_MS = 5_000L
private const val SERVER_THREAD_NAME = "mcp-server"

/**
 * Verifies [buildServer] and the full MCP server lifecycle including [main]. The unit-level test
 * verifies that [buildServer] returns a properly configured server. The integration test exercises
 * all three tool handler lambdas registered inside [buildServer] by running the server over a
 * pair of in-memory pipes and exchanging real MCP JSON-RPC messages — the only way to cover those
 * lambdas without a live agent host.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttleMcpServerTest {

    @Test
    fun verifyBuildServerReturnsNonNullServer() {
        val server = buildServer(REPO_ROOT_STUB)

        assertNotNull(server)
    }

    /**
     * Starts [main] in a daemon thread backed by piped stdin/stdout, exchanges the MCP initialize
     * handshake and one tool call per registered tool, then closes stdin so the server exits
     * cleanly.
     */
    @Test
    fun verifyServerProcessesAllThreeToolCallsViaProtocol() {
        val stdinRead = PipedInputStream()
        val stdinWrite = PipedOutputStream(stdinRead)
        val stdoutRead = PipedInputStream()
        val stdoutWrite = PipedOutputStream(stdoutRead)

        val savedIn = System.`in`
        val savedOut = System.out
        System.setIn(stdinRead)
        System.setOut(PrintStream(stdoutWrite, true))

        val outputQueue = LinkedBlockingQueue<String>()
        val readerThread = thread(isDaemon = true, name = READER_THREAD_NAME) {
            try {
                val reader = BufferedReader(InputStreamReader(stdoutRead))
                var line = reader.readLine()
                while (line != null) {
                    outputQueue.put(line)
                    line = reader.readLine()
                }
            } catch (_: Exception) {
                /* pipe closed: reader exits normally */
            }
        }

        val serverThread = thread(isDaemon = true, name = SERVER_THREAD_NAME) { main() }
        val writer = PrintWriter(stdinWrite, true)

        try {
            writer.println(INITIALIZE_REQUEST)
            val initResponse = outputQueue.poll(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            writer.println(INITIALIZED_NOTIFICATION)

            writer.println(SCAFFOLD_REQUEST)
            val scaffoldResponse = outputQueue.poll(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            writer.println(DETECT_RISK_REQUEST)
            val detectRiskResponse = outputQueue.poll(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            writer.println(GET_SPEC_REQUEST)
            val getSpecResponse = outputQueue.poll(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            assertAll(
                { assertNotNull(initResponse, "Expected initialize response from server") },
                { assertNotNull(scaffoldResponse, "Expected scaffold_integration tool response") },
                { assertNotNull(detectRiskResponse, "Expected detect_risk tool response") },
                { assertNotNull(getSpecResponse, "Expected get_spec tool response") }
            )
        } finally {
            try {
                stdinWrite.close()
            } catch (_: IOException) {
                /* pipe may already be closed */
            }
            serverThread.join(SERVER_EXIT_TIMEOUT_MS)
            readerThread.join(SERVER_EXIT_TIMEOUT_MS)
            System.setIn(savedIn)
            System.setOut(savedOut)
            assertFalse(serverThread.isAlive, "Server thread should exit after stdin is closed")
        }
    }
}
