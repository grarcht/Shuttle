package com.grarcht.shuttle.framework.integrations.extensions.room

import android.content.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

/**
 * Verifies the [ShuttleRoomDbConfig] data class construction and properties.
 */
class ShuttleRoomDbConfigTest {

    @Test
    fun verifyDefaultConfigHasMultiprocessFalse() {
        val context = mock<Context>()
        val config = ShuttleRoomDbConfig(context)
        assertNotNull(config)
        assertEquals(context, config.context)
        assertFalse(config.multiprocess)
    }

    @Test
    fun verifyConfigWithMultiprocessTrue() {
        val context = mock<Context>()
        val config = ShuttleRoomDbConfig(context, multiprocess = true)
        assertNotNull(config)
        assertEquals(context, config.context)
        assertTrue(config.multiprocess)
    }

    @Test
    fun verifyCopyPreservesContextAndOverridesMultiprocess() {
        val context = mock<Context>()
        val original = ShuttleRoomDbConfig(context, multiprocess = false)
        val copied = original.copy(multiprocess = true)
        assertEquals(context, copied.context)
        assertTrue(copied.multiprocess)
    }
}
