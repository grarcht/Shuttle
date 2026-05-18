package com.grarcht.shuttle.framework.integrations.extensions.room

import android.app.Application
import android.content.Context
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Verifies the functionality of [ShuttleRoomDbFactory]. ShuttleRoomDbFactory constructs the Room
 * database instance that backs Shuttle's persistence layer, supporting both single-process and
 * multi-process configurations. Without it, no database could be created and all cargo store and
 * retrieval operations would fail at startup.
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
