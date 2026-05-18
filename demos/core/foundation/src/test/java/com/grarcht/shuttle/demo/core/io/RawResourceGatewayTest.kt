package com.grarcht.shuttle.demo.core.io

import android.content.res.Resources
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.mock

/**
 * Verifies the functionality of [RawResourceGateway], including the builder factory method,
 * method chaining behavior, and the flow emitted by [RawResourceGateway.create] for both
 * configured and unconfigured gateway instances.
 */
class RawResourceGatewayTest {

    @Test
    fun withReturnsNonNullGatewayInstance() {
        val gateway = RawResourceGateway.with(mock())
        assertNotNull(gateway)
    }

    @Test
    fun logTagReturnsSameInstanceForChaining() {
        val gateway = RawResourceGateway.with(mock())
        assertSame(gateway, gateway.logTag("TAG"))
    }

    @Test
    fun bytesFromRawResourceReturnsSameInstanceForChaining() {
        val gateway = RawResourceGateway.with(mock<Resources>())
        assertSame(gateway, gateway.bytesFromRawResource(1))
    }

    @Test
    fun createBeforeBytesFromRawResourceEmitsError() = runTest {
        val gateway = RawResourceGateway.with(mock())
        val results = gateway.create().toList()

        assertAll(
            { assertEquals(1, results.size) },
            { assertTrue(results[0] is IOResult.Error<*>) }
        )
    }

    @Test
    fun createAfterBytesFromRawResourceReturnsNonNullFlow() {
        val gateway = RawResourceGateway.with(mock<Resources>())
            .bytesFromRawResource(1)
        assertNotNull(gateway.create())
    }
}
