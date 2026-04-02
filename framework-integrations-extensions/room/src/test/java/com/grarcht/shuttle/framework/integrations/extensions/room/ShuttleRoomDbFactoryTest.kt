package com.grarcht.shuttle.framework.integrations.extensions.room

import android.app.Application
import android.content.Context
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Verifies the [ShuttleRoomDbFactory] functionality.
 */
class ShuttleRoomDbFactoryTest {
    private val dbFactory = ShuttleRoomDbFactory()

    @Test
    fun verifyCreationOfDbWithoutMultiprocess() {
        val context = mock<Context>()
        val applicationContext = mock<Application>()
        whenever(context.applicationContext).thenReturn(applicationContext)

        val config = ShuttleRoomDbConfig(context, multiprocess = false)
        val db = dbFactory.createDb(config)

        assertNotNull(db)
        db.close()
    }

    @Test
    fun verifyCreationOfDbWithMultiprocess() {
        val context = mock<Context>()
        val applicationContext = mock<Application>()
        whenever(context.applicationContext).thenReturn(applicationContext)

        val config = ShuttleRoomDbConfig(context, multiprocess = true)
        val db = dbFactory.createDb(config)

        assertNotNull(db)
        db.close()
    }
}
