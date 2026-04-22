package com.grarcht.shuttle.framework.integrations.extensions.room

import android.content.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.mock

/**
 * Verifies the functionality of [ShuttleRoomDbConfig]. ShuttleRoomDbConfig is the configuration
 * data class passed to ShuttleRoomDbFactory when creating the Room database, encapsulating the
 * Android context and multi-process flag. If its properties were not correctly set or copied,
 * the database would be built with the wrong context or process-mode settings.
 */
class ShuttleRoomDbConfigTest {

    @Test
    fun verifyDefaultConfigHasMultiprocessFalse() {
        val context = mock<Context>()
        val config = ShuttleRoomDbConfig(context)
        assertAll(
            { assertNotNull(config) },
            { assertEquals(context, config.context) },
            { assertFalse(config.multiprocess) }
        )
    }

    @Test
    fun verifyConfigWithMultiprocessTrue() {
        val context = mock<Context>()
        val config = ShuttleRoomDbConfig(context, multiprocess = true)
        assertAll(
            { assertNotNull(config) },
            { assertEquals(context, config.context) },
            { assertTrue(config.multiprocess) }
        )
    }

    @Test
    fun verifyCopyPreservesContextAndOverridesMultiprocess() {
        val context = mock<Context>()
        val original = ShuttleRoomDbConfig(context, multiprocess = false)
        val copied = original.copy(multiprocess = true)
        assertAll(
            { assertEquals(context, copied.context) },
            { assertTrue(copied.multiprocess) }
        )
    }
}
